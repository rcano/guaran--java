package guarana.java.core;

/**
 * A tuple of a {@link ObsValDescr} tied to an instance.
 *
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; programmers should treat instances that are {@linkplain #equals(Object) equal} as interchangeable and should not use instances for
 * synchronization, or unpredictable behavior may occur. For example, in a future release, synchronization may fail.
 */
public final record ObsVal<T, Container>(ObsValDescr<T, Container> obsValDescr, Container instance) {

    public T value() {
        return obsValDescr.valueForInstance(instance);
    }
}
