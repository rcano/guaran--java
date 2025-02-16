package guarana.java.core;

import io.vavr.CheckedConsumer;

/**
 *
 * @author randa
 */
public record Emitter<T, Container>(EmitterDescr<T, Container> emitterDescr, Container instance) {

    public void connect(EventIterator<T> it) {
        emitterDescr.connect(it, instance);
    }

    /**
     * Creates a singleton EmitterDescr wrapped in this emitter.
     *
     * The instance of this Emitter is the very EmitterDescr, which means this Emitter is itself (hence, singleton).
     *
     * @param <T>
     * @param name
     * @return
     */
    public static <T> Emitter<T, ?> singleton(String name) {
        final var r = EmitterDescr.<T, Object>create(name);
        return new Emitter(r, r);
    }

    /**
     * Creates a singleton EmitterDescr wrapped in this emitter.
     *
     * The instance of this Emitter is the very EmitterDescr, which means this Emitter is itself (hence, singleton).
     *
     * @param <T>
     * @param name
     * @return
     */
    public static <T> Emitter<T, ?> singleton(String name, CheckedConsumer<Object> onFirstListener, CheckedConsumer<Object> onNoListener) {
        final var r = EmitterDescr.<T, Object>create(name, onFirstListener, onNoListener);
        return new Emitter(r, r);
    }
}
