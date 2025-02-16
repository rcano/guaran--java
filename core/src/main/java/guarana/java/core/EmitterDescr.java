package guarana.java.core;

import guarana.java.core.util.Unique;
import io.vavr.CheckedConsumer;

public interface EmitterDescr<T, Container> extends Unique {

    String name();

    default void emit(T t, Container c) {
        VarContext.getContext().emit(this, t, c);
    }
    
    default void connect(EventIterator<T> it, Container c) {
        VarContext.getContext().connectEmitter(this, it, c);
    }

    /**
     * Given an instance of ForInstance, perform a side-effect every time a this emitter receives a listener on a specific instance when it
     * had previously none.
     *
     * @param c
     */
    default void onFirstListener(Container c) {
    }

    /**
     * Given an instance of ForInstance, perform a side-effect whenever this emitter has no listeners (in a specific instance).
     *
     * @param c
     */
    default void onNoListener(Container c) {
    }

    static <T, Container> EmitterDescr<T, Container> create(String name) {
        return create(name, (CheckedConsumer) EmitterInternals.NO_OP, (CheckedConsumer) EmitterInternals.NO_OP);
    }

    static <T, Container> EmitterDescr<T, Container> create(String name, CheckedConsumer<Container> onFirstListener, CheckedConsumer<Container> onNoListener) {
        return new EmitterDescr<T, Container>() {
            final int id = Internals.varsUniqueIdGen.getAndIncrement();

            @Override
            public String name() {
                return name;
            }

            @Override
            public int uniqueId() {
                return id;
            }

            @Override
            public void onFirstListener(Container c) {
                onFirstListener.unchecked().accept(c);
            }

            @Override
            public void onNoListener(Container c) {
                onNoListener.unchecked().accept(c);
            }
        };
    }
}

class EmitterInternals {

    public static final CheckedConsumer<Object> NO_OP = c -> {
    };
}
