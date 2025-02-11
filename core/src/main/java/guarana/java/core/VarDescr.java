package guarana.java.core;

import java.util.function.Consumer;

public interface VarDescr<T, Container> extends ObsValDescr<T, Container> {

    public default void valueForInstance(Container instance, Binding<T> value) {
        VarContext.getContext().update(this, instance, value);
    }

    public boolean eagerEvaluation();

    public default <C extends Container> Var<T, C> forInstanceVar(C instance) {
        return new Var(this, instance);
    }

    public static <T, Container> VarDescr<T, Container> create(String name, T initialValue, boolean eagerEvaluation) {
        return create(name, initialValue, eagerEvaluation, ObsValDescr.DO_NOTHING);
    }

    public static <T, Container> VarDescr<T, Container> create(String name, T initialValue, boolean eagerEvaluation, Consumer<Object> onFirstAssociation) {
        return new VarDescr<>() {
            final int id = Internals.varsUniqueIdGen.getAndIncrement();

            @Override
            public boolean eagerEvaluation() {
                return eagerEvaluation;
            }

            @Override
            public T initialValueForInstance(Container instance) {
                return initialValue;
            }

            @Override
            public void onFirstAssociation(Container instance) {
                onFirstAssociation.accept(instance);
            }

            @Override
            public int uniqueId() {
                return id;
            }

            @Override
            public String toString() {
                return "Var(" + name + ", " + initialValue + ", " + eagerEvaluation + ")";
            }
            
            
        };
    }
}
