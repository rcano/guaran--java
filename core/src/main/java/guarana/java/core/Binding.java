package guarana.java.core;

import java.util.function.Supplier;

public sealed interface Binding<T> {

    public static record Const<T>(T t) implements Binding<T> {

    }

    public static record Compute<T>(Supplier<T> f) implements Binding<T> {

    }
}
