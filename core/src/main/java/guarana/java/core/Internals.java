package guarana.java.core;

import guarana.java.core.util.Unique;
import java.util.concurrent.atomic.AtomicInteger;

class Internals {

    static final AtomicInteger varsUniqueIdGen = new AtomicInteger();

    public static long keyed(Unique unique, Object instance) {
        return (((long) unique.uniqueId() << 32L) & 0xFFFFFFFF00000000L) | (getId(instance) & 0x00000000FFFFFFFFL);
    }

    public static int getId(Object instance) {
        return switch (instance) {
            case Unique u ->
                u.uniqueId();
            default ->
                System.identityHashCode(instance);
        };
    }

    public static int keyPart(long keyed) {
        return (int) (keyed >>> 32);
    }

    public static int instancePart(long keyed) {
        return (int) keyed & 0xFFFFFFFF;
    }
}
