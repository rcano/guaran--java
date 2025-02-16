package guarana.java.core;

import io.vavr.CheckedConsumer;
import java.lang.ref.WeakReference;

/**
 *
 * @author randa
 */
public record Emitter<T, Container>(EmitterDescr<T, Container> emitterDescr, Container instance) {

    public void connect(EventIterator<T> it) {
        emitterDescr.connect(it, instance);
    }

    public ObsVal<T, Container> toVar(T initialValue, EventIterator<T> iterator) {
        VarDescr<T, Container> descr = VarDescr.create("emitter-var", initialValue, false);
        var res = descr.forInstance(instance);

        // in order to not leak an eventIterator and the var, we'll create a weak reference to it so that when users stop using it, we stop updating it.
        var weakRef = new WeakReference<>(res);
        
        connect(EventIterator.all.takeWhile(_ -> !weakRef.refersTo(null)).foreach(v ->
                descr.valueForInstance(instance, new Binding.Const<>(v))));
        return res;
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
