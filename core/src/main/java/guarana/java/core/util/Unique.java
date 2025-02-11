package guarana.java.core.util;

/**
 * Marks a type as having a unique integer ID within this JVM instance.
 * 
 * The ID doesn't have to consistent between JVM instances, but for a given type T that implements Unique,
 * no two different instances can share an ID.
 */
public interface Unique {

    public int uniqueId();
}
