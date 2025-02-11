package guarana.java.core;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.agrona.collections.Long2ObjectHashMap;
import org.agrona.collections.LongHashSet;
import org.jspecify.annotations.Nullable;

interface SignalSwitchboard {

    public default <T, Container> T get(ObsValDescr<T, Container> var, Container instance) {
        return getOpt(var, instance).orElseThrow();
    }

    public <T, Container> Optional<T> getOpt(ObsValDescr<T, Container> var, Container instance);

    public default <T, Container> T getOrElseUpdate(VarDescr<T, Container> var, Container instance, Supplier<T> initialVlaue) {
        var prev = getOpt(var, instance);
        if (prev.isPresent()) return prev.get();

        var res = initialVlaue.get();
        update(var, instance, res);
        return res;
    }

    public <T, Container> void update(VarDescr<T, Container> var, Container instance, T res);

    public <T, Container> void externalPropertyChanged(ObsValDescr<T, Container> var, Container instance, @Nullable T oldValue);

    public <T, Container> void bind(VarDescr<T, Container> var, Container instance, Function<SignalSwitchboard, T> f);

    public <T, Container> void remove(long keyed);

    public static interface Reporter {

        public <T, Container> void signalRemoved(SignalSwitchboard sb, long keyed);

        public <T, Container> void signalUpdated(SignalSwitchboard sb, ObsValDescr<T, Container> var, Container instance, @Nullable T oldValue, T newValue, LongHashSet dependencies, LongHashSet dependents);

        public <T, Container> void signalInvalidated(SignalSwitchboard sb, ObsValDescr<T, Container> var, Container instance);
    }

    public static interface VarLookup {

        public Map.@Nullable Entry<ObsValDescr<Object, Object>, Object> lookupVar(long key);
    }
}

class SignalSwitchboardImpl implements SignalSwitchboard {

    private final System.Logger logger = System.getLogger(SignalSwitchboardImpl.class.getName());

    private final Reporter reporter;
    private final Long2ObjectHashMap<State> signalStates = new Long2ObjectHashMap<>(1024, 0.67f);
    private final Long2ObjectHashMap<Function<SignalSwitchboard, Object>> signalEvaluator = new Long2ObjectHashMap<>();
    private final Long2ObjectHashMap<LongHashSet> signalDeps = new Long2ObjectHashMap<>();
    private final Long2ObjectHashMap<Relationships> signalRels = new Long2ObjectHashMap<>();
    private final LongHashSet reentrancyDetector = new LongHashSet();

    private static final LongHashSet EmptyUnmodifiableLongHashSet = new LongHashSet();
    private final VarLookup varLookup;
    private final boolean useReentrancyDetection;

    public SignalSwitchboardImpl(Reporter reporter, VarLookup varLookup, boolean useReentrancyDetection) {
        this.reporter = reporter;
        this.varLookup = varLookup;
        this.useReentrancyDetection = useReentrancyDetection;
    }

    @Override
    public <T, Container> Optional<T> getOpt(ObsValDescr<T, Container> var, Container instance) {
        return switch (signalStates.get(Internals.keyed(var, instance))) {
            case null ->
                Optional.empty();
            case State.Value(Object value) ->
                Optional.of((T) value);
            case State.Recompute(Object oldValue) ->
                Optional.of(recompute(var, instance, (T) oldValue));
            case State.External.INSTANCE ->
                Optional.of(((ExternalVarDescr<T, Container>) var).get(instance));
        };
    }

    private <T, Container> T recompute(ObsValDescr<T, Container> var, Container instance, T oldValue) {
        final long keyed = Internals.keyed(var, instance);
        //when recomputing the value, we gotta undo all the dependents
        final var existingDeps = signalRels.get(keyed);
        if (existingDeps != null) existingDeps.dependents.forEachLong(this::remove);

        if (reentrancyDetector.contains(keyed))
            throw new IllegalStateException("Detected evaluation loop in %s(%s)".formatted(var, instance));

        if (useReentrancyDetection) reentrancyDetector.add(keyed);
        final var tracker = new TrackingContext<T, Container>(var, instance);
        // before computing the value, we set the signalState to the oldValue in case the compute lambda has a self reference
        if (oldValue != null) signalStates.put(keyed, new State.Value(oldValue));
        else signalStates.remove(keyed);

        T result;
        try {
            result = (T) signalEvaluator.get(keyed).apply(tracker);
            signalStates.put(keyed, new State.Value(result));
        } finally {
            if (useReentrancyDetection) reentrancyDetector.remove(keyed);
        }

        final var computedRels = new Relationships(tracker.dependencies, tracker.dependents);
        tracker.dependents.forEachLong(l -> {
            if (computedRels.dependencies.contains(l)) {
                var found = varLookup.lookupVar(l);
                throw new IllegalStateException("Var %s(s) depends on %s(%s) but it also has it as dependent, this will always lead to stack overflow.".formatted(var, instance, found.getKey(), found.getValue()));
            }
        });
        logger.log(System.Logger.Level.DEBUG, () ->
                "Recomputed signal %s to %s, new relationships %s".formatted(var.forInstance(instance), result, computedRels));
        signalRels.put(keyed, computedRels);

        tracker.dependencies.forEachLong(dep -> {
            var deps = signalDeps.get(dep);
            if (deps == null) {
                deps = new LongHashSet();
                signalDeps.put(dep, deps);
            }
            deps.add(keyed);
        });

        if (!Objects.equals(result, oldValue)) {
            reporter.signalUpdated(this, var, instance, oldValue, result, tracker.dependencies, tracker.dependents);
        }

        return result;
    }

    @Override
    public <T, Container> void update(VarDescr<T, Container> var, Container instance, T value) {
        final long keyed = Internals.keyed(var, instance);
        var oldState = signalStates.get(keyed);
        var mustUpdate = switch (oldState) {
            case null ->
                true;
            case State.External.INSTANCE ->
                /* external properties must always fire, since we have no way of tracking for a value change because we get notified of update after the underlying
           value was modified. There's no way of weaving this. In the future we might investigate how to do it, but we have to be mindful of allocations as well.
           ExternalVar implementors are encouraged to make this check on their own before notifying the switchboard.
                 */
                true;
            case State.Value(Object v) when v == value || v.equals(value) ->
                false;
            default ->
                true;
        };
        if (mustUpdate) {
            unbindPrev(keyed);
            signalStates.put(keyed, var instanceof ExternalObsValDescr ? State.External.INSTANCE : new State.Value(value));
            propagateSignal(var, instance);
            var oldValue = switch (oldState) {
                case null ->
                    null;
                case State.External.INSTANCE ->
                    null;
                case State.Recompute r ->
                    null;
                case State.Value(Object v) ->
                    (T) v;
            };
            reporter.signalUpdated(this, var, instance, oldValue, value, EmptyUnmodifiableLongHashSet, EmptyUnmodifiableLongHashSet);
        }
    }

    @Override
    public <T, Container> void externalPropertyChanged(ObsValDescr<T, Container> var, Container instance, T oldValue) {
        // an external change doesn't mean a voluntary (by the user) change on the behavior of the signal (whether compute or set)
        // so we don't unbind its current state and instead just propagate the signal invalidation, unless there is no current state, in
        // which case it means the external signal was never observed, and we need to setup an initial state so signal propagation works
        var keyed = Internals.keyed(var, instance);
        if (!signalStates.containsKey(keyed)) signalStates.put(keyed, State.External.INSTANCE);
        propagateSignal(var, instance);
        var extVar = (ExternalVarDescr<T, Container>) var;
        reporter.signalUpdated(this, var, instance, oldValue, extVar.get(instance), EmptyUnmodifiableLongHashSet, EmptyUnmodifiableLongHashSet);
    }

    @Override
    public <T, Container> void bind(VarDescr<T, Container> var, Container instance, Function<SignalSwitchboard, T> f) {
        var keyed = Internals.keyed(var, instance);
        unbindPrev(keyed);
        signalStates.put(keyed, new State.Recompute(getOpt(var, instance).orElse(null)));
        signalEvaluator.put(keyed, (Function) f);
        propagateSignal(var, instance);
    }

    @Override
    public <T, Container> void remove(long keyed) {
        signalStates.remove(keyed);
        unbindPrev(keyed);
        reporter.signalRemoved(this, keyed);
    }

    private <T, Container> void unbindPrev(long keyed) {
        switch (signalEvaluator.remove(keyed)) {
            case null -> {
            }
            default -> {
                switch (signalRels.remove(keyed)) {
                    case null -> {
                    }
                    case Relationships(LongHashSet deps, LongHashSet depnts) -> {
                        depnts.forEachLong(this::remove);
                        deps.forEachLong(dep -> {
                            var d = signalDeps.get(dep);
                            if (d != null) d.remove(dep);
                        });
                    }
                }
            }

        }
    }

    private <T, Container> void propagateSignal(ObsValDescr<T, Container> var, Container instance) {
        // due to how propagating signal works, where the set of dependencies is iterated, it is entirely possible to find during
        // the iteration a signal that was removed, hence why we check here if that's the case by checking the state
        final var keyed = Internals.keyed(var, instance);
        if (!signalStates.containsKey(keyed)) return;

        if (signalEvaluator.get(keyed) instanceof Function<SignalSwitchboard, Object> compute) {
            switch (signalStates.get(keyed)) {
                case null -> signalStates.put(keyed, new State.Recompute(null));
                case State.Recompute rec -> { // if already recompute, do nothing
                }
                case State.Value(Object oldv) -> signalStates.put(keyed, new State.Recompute(oldv));
                case State.External.INSTANCE -> {
                    var extVar = (ExternalVarDescr<T, Container>) var;
                    signalStates.put(keyed, new State.Recompute(extVar.get(instance)));
                }
            }
        }

        if (signalDeps.get(keyed) instanceof LongHashSet deps) deps.forEachLong(dep -> {
                if (varLookup.lookupVar(dep) instanceof Map.Entry<ObsValDescr<Object, Object>, Object> e) {
                    propagateSignal(e.getKey(), e.getValue());
                }
            });

        reporter.signalInvalidated(this, var, instance);
    }

    private record Relationships(LongHashSet dependencies, LongHashSet dependents) {

    }

    private sealed interface State {

        record Value(Object value) implements State {

        }

        record Recompute(Object oldValue) implements State {

        }

        enum External implements State {
            INSTANCE
        }
    }

    private class TrackingContext<T, Container> implements SignalSwitchboard {

        final ObsValDescr<T, Container> forSignal;
        final Container signalInstance;
        final LongHashSet dependencies = new LongHashSet(4);
        final LongHashSet dependents = new LongHashSet(4);

        public TrackingContext(ObsValDescr<T, Container> forSignal, Container signalInstance) {
            this.forSignal = forSignal;
            this.signalInstance = signalInstance;
        }

        @Override
        public <T, Container> Optional<T> getOpt(ObsValDescr<T, Container> var, Container instance) {
            if (var != forSignal || instance != signalInstance) dependencies.add(Internals.keyed(var, instance));
            return SignalSwitchboardImpl.this.getOpt(var, instance);
        }

        @Override
        public <T, Container> void update(VarDescr<T, Container> var, Container instance, T res) {
            if (var != forSignal || instance != signalInstance) dependents.add(Internals.keyed(var, instance));
            SignalSwitchboardImpl.this.update(var, instance, res);
        }

        @Override
        public <T, Container> void bind(VarDescr<T, Container> var, Container instance, Function<SignalSwitchboard, T> f) {
            if (var != forSignal || instance != signalInstance) dependents.add(Internals.keyed(var, instance));
            SignalSwitchboardImpl.this.bind(var, instance, f);
        }

        @Override
        public <T, Container> void externalPropertyChanged(ObsValDescr<T, Container> var, Container instance, T oldValue) {
            SignalSwitchboardImpl.this.externalPropertyChanged(var, instance, oldValue);
        }

        @Override
        public <T, Container> void remove(long keyed) {
            SignalSwitchboardImpl.this.remove(keyed);
        }

    }
}
