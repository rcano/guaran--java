package guarana.java.core;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ExternalVarDescr<T, Container> extends VarDescr<T, Container>, ExternalObsValDescr<T, Container> {

    public void set(Container owner, T value);

    @Override
    public default void valueForInstance(Container instance, Binding<T> value) {
        VarContext.getContext().update(this, instance, value);
    }

    public static <T, Container> ExternalVarDescr<T, Container> create(
            String name,
            Function<Container, T> getter,
            BiConsumer<Container, T> setter,
            boolean eagerEvaluation
    ) {
        return create(name, getter, setter, eagerEvaluation, ObsValDescr.DO_NOTHING);
    }

    public static <T, Container> ExternalVarDescr<T, Container> create(
            String name,
            Function<Container, T> getter,
            BiConsumer<Container, T> setter,
            boolean eagerEvaluation,
            Consumer<Object> onFirstAssociation
    ) {
        return new ExternalVarDescr<T, Container>() {
            final int id = Internals.varsUniqueIdGen.getAndIncrement();

            @Override
            public T get(Container owner) {
                return getter.apply(owner);
            }

            @Override
            public void set(Container owner, T value) {
                setter.accept(owner, value);
            }

            @Override
            public void onFirstAssociation(Object instance) {
                onFirstAssociation.accept(instance);
            }

            @Override
            public int uniqueId() {
                return id;
            }

            @Override
            public boolean eagerEvaluation() {
                return eagerEvaluation;
            }
        };
    }
}
