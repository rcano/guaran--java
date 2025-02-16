package guarana.java.core;

import guarana.java.core.util.Throwables;
import guarana.java.core.util.Unique;
import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.agrona.collections.Int2NullableObjectHashMap;
import org.agrona.collections.Int2ObjectHashMap;
import org.agrona.collections.IntHashSet;
import org.agrona.collections.LongHashSet;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class AbstractToolkit {

    private final System.Logger logger = System.getLogger(AbstractToolkit.class.getName());

    static AbstractToolkit INSTANCE;
    private SignalSwitchboard signalSwitchboard;
    private EmittersStation emittersStation;

    private record InstanceData(WeakReference<Object> instance, IntHashSet vars, IntHashSet emitters) {

    }
    private final Int2ObjectHashMap<InstanceData> instancesData = new Int2NullableObjectHashMap<>();
    private final IntHashSet externalVars = new IntHashSet(1024);
    private final Int2ObjectHashMap<ObsValDescr<?, ?>> seenVars = new Int2NullableObjectHashMap<>();
    private final Cleaner cleaner = Cleaner.create();
    private final LongHashSet reactingExtVars = new LongHashSet(16);

    public Stylist stylist = Stylist.NoOp.INSTANCE;

    public AbstractToolkit() {

        INSTANCE = null;
        if (INSTANCE != null) {
            throw new IllegalStateException("A toolkit was already instantiated!");
        }
        // We don't need to make this threadsafe. Instantiating the toolkit from multiple threads is unsupported
        INSTANCE = this;

        final var reporter = new SignalSwitchboard.Reporter() {
            @Override
            public <T, Container> void signalRemoved(SignalSwitchboard sb, long keyed) {
            }

            @Override
            public <T, Container> void signalUpdated(SignalSwitchboard sb, ObsValDescr<T, Container> var, Container instance, T oldValue, T newValue, LongHashSet dependencies, LongHashSet dependents) {
                // TODO do emitters
            }

            @Override
            public <T, Container> void signalInvalidated(SignalSwitchboard sb, ObsValDescr<T, Container> var, Container instance) {
                logger.log(System.Logger.Level.DEBUG, () -> "%s(%s) invalidated".formatted(var, instance));
                if (var instanceof VarDescr vd && vd.eagerEvaluation()) {
                    logger.log(System.Logger.Level.DEBUG, () -> "%s(%s) eagerly evaluating".formatted(var, instance));
                    final long keyed = Internals.keyed(var, instance);
                    /**
                     * Executes the given thunk preventing external property changes from being propagated to the signals. Reason: When
                     * computing the values for external properties, they'll notify that they were change, and we want to avoid resetting
                     * the value and discarding the user provided Binding.
                     */
                    reactingExtVars.add(keyed);
                    ((VarContextImpl) VarContext.getContext()).signalSwitchboard.get(var, instance);
                    reactingExtVars.remove(keyed);
                }
            }
        };

        final var varLookup = new SignalSwitchboard.VarLookup() {
            @Override
            public Map.Entry<ObsValDescr<Object, Object>, Object> lookupVar(long key) {
                final ObsValDescr<Object, Object> varPart = (ObsValDescr<Object, Object>) seenVars.get(Internals.keyPart(key));
                if (varPart == null) throw new IllegalStateException("We've never observed the var " + Internals.keyPart(key));
                final var instanceData = instancesData.get(Internals.instancePart(key));
                if (instanceData == null)
                    throw new IllegalStateException("We've never observed the instance " + Internals.instancePart(key));

                return new AbstractMap.SimpleImmutableEntry<>(varPart, instanceData.instance.get());
            }
        ;
        };

        signalSwitchboard = new SignalSwitchboardImpl(reporter, varLookup, true);
        emittersStation = new EmittersStationImpl();
    }

    protected abstract boolean isToolkitThread();

    protected abstract void runOnToolkitThread(Runnable r);
    
    public abstract Stylist.Metrics getMetrics();

    public void update(Runnable thunk) {
        update(() -> {
            thunk.run();
            return null;
        });
    }

    public <R> R update(Supplier<R> thunk) {
        return updateAsync(thunk).join();
    }

    public <R> CompletableFuture<R> updateAsync(Supplier<R> thunk) {
        final var res = new CompletableFuture<R>();
        final Runnable action = () -> {
            try {
                var r = VarContext.CONTEXT.isBound() ? thunk.get() : ScopedValue.where(VarContext.CONTEXT, new VarContextImpl(signalSwitchboard)).call(() ->
                        thunk.get());
                res.complete(r);
            } catch (Throwable e) {
                Throwables.rethrowIfFatal(e);
                res.completeExceptionally(e);
            }
        };

        if (isToolkitThread()) {
            action.run();
        } else {
            runOnToolkitThread(action);
        }

        return res;
    }

    private class VarContextImpl implements VarContext {

        private final SignalSwitchboard signalSwitchboard;

        public VarContextImpl(SignalSwitchboard signalSwitchboard) {
            this.signalSwitchboard = signalSwitchboard;
        }

        private InstanceData recordInstance(Object instance) {
            final var id = switch (instance) {
                case Unique u ->
                    u.uniqueId();
                default ->
                    System.identityHashCode(instance);
            };
            switch (instancesData.get(id)) {
                case null:
                    var vars = new IntHashSet(8);
                    var emitters = new IntHashSet(4);
                    var data = new InstanceData(new WeakReference<Object>(instance), vars, emitters);
                    instancesData.put(id, data);
                    cleaner.register(instance, () -> {
                        var removed = instancesData.remove(id);
                        if (removed != null) {
                            logger.log(System.Logger.Level.DEBUG, () -> "Instance data 0x%H removed".formatted(id));
                            data.vars().forEachInt(vid -> {
                                signalSwitchboard.remove(vid);
                            });
                            data.emitters().forEachInt(vid -> {
                                // TODO: signals
                            });

                        }
                    });
                    return data;

                case InstanceData data2:
                    return data2;
            }
        }

        private <T, Container> void recordVardUsage(ObsValDescr<T, Container> var, Container instance) {
            var varForInstanceAdded = recordInstance(instance).vars().add(var.uniqueId());
            seenVars.put(var.uniqueId(), var);
            if (var instanceof ExternalObsValDescr) {
                externalVars.add(var.uniqueId());
            }
            if (varForInstanceAdded) {
                logger.log(System.Logger.Level.DEBUG, () ->
                        "Var(%s) recorded. Key = %s".formatted(instance, Internals.keyed(var, instance)));
                var.onFirstAssociation(instance);
            }
        }

        @Override
        public <T, Container> T get(ObsValDescr<T, Container> val, Container instance) {
            recordVardUsage(val, instance);
            return signalSwitchboard.getOpt(val, instance).orElseGet(() -> stylist.apply(getMetrics(), val, instance).orElseGet(() ->
                    val.initialValueForInstance(instance)));
        }

        @Override
        public <T, Container> void update(VarDescr<T, Container> var, Container instance, Binding<T> binding) {
            recordVardUsage(var, instance);
            switch (binding) {
                case Binding.Const<T>(T value) ->
                    signalSwitchboard.update(var, instance, value);
                case Binding.Compute<T>(Supplier<T> c) ->
                    signalSwitchboard.bind(var, instance, sb -> ScopedValue.where(VarContext.CONTEXT, new VarContextImpl(sb)).call(() ->
                            c.get()));
            };
        }

        @Override
        public <T, Container> void externalPropertyUpdated(ObsValDescr<T, Container> val, Container instance, T oldValue) {
            recordVardUsage(val, instance);
            if (!reactingExtVars.contains(Internals.keyed(val, instance))) {
                signalSwitchboard.externalPropertyChanged(val, instance, oldValue);
            }
        }

        @Override
        public <T, Container> void connectEmitter(EmitterDescr<T, Container> emitterDescr, EventIterator<T> it, Container instance) {
            recordInstance(instance).emitters().add(emitterDescr.uniqueId());
            emittersStation.listen(emitterDescr, instance, it);
        }

        @Override
        public <T, Container> void emit(EmitterDescr<T, Container> emitterDescr, T elem, Container instance) {
            recordInstance(instance).emitters().add(emitterDescr.uniqueId());
            emittersStation.emit(emitterDescr, instance, elem);
        }

    }

    public stateReader stateReader()  { return new stateReader(); }

    public class stateReader {

        protected stateReader() {

        }

        /**
         * Reads the current value of an ObsVal as it is read by doing `descr.forInstance(instance).value()` (that is using the VarContext).
         */
        public <T, Container> T computed(ObsValDescr<T, Container> val, Container instance) {
            return signalSwitchboard.getOpt(val, instance).orElseGet(() -> stylist.apply(getMetrics(), val, instance).orElseGet(() ->
                    val.initialValueForInstance(instance)));
        }

        /**
         * Reads the user-defined value for a variable, or the ObsValDescr default.
         */
        public <T, Container> Optional<T> get(ObsValDescr<T, Container> descr, Container instance) {
            return signalSwitchboard.getOpt(descr, instance);
        }

        /**
         * Reads the user-defined value for a variable, if any.
         */
        public <T, Container> T getOrDefaul(ObsValDescr<T, Container> descr, Container instance) {
            return signalSwitchboard.getOpt(descr, instance).orElseGet(() -> descr.initialValueForInstance(instance));
        }
    }
}
