package guarana.java.core;

import guarana.java.core.util.Unique;
import java.util.function.Supplier;

/**
 * A tuple of a {@link ValDescr} tied to an instance.
 *
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; programmers should treat instances that are {@linkplain #equals(Object) equal} as interchangeable and should not use instances for
 * synchronization, or unpredictable behavior may occur. For example, in a future release, synchronization may fail.
 */
public record Var<T, Container>(VarDescr<T, Container> varDescr, Container instance) {

    public T value() {
        return varDescr.valueForInstance(instance);
    }

    public void value(T t) {
        varDescr.valueForInstance(instance, new Binding.Const(t));
    }

    public void bind(Supplier<T> f) {
        varDescr.valueForInstance(instance, new Binding.Compute<>(f));
    }

    /**
     * Creates a singleton VarDescr wrapped in this var.
     *
     * The instance of this Var is the very VarDescr, which means this Var is itself (hence, singleton).
     *
     * @param <T>
     * @param name
     * @param initialValue
     * @param eagerEvaluation
     * @return
     */
    public static <T> Var<T, ?> singleton(String name, T initialValue, boolean eagerEvaluation) {
        final VarDescr<T, Object> varDescr = VarDescr.create(name, initialValue, eagerEvaluation);
        return new Var(varDescr, varDescr);
    }
}
