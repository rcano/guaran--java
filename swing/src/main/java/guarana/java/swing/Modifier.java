package guarana.java.swing;

import guarana.java.core.Binding;
import guarana.java.core.VarDescr;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents an action that can be applied to a container either during initialization or amending.
 */
@FunctionalInterface
public interface Modifier<T, Container> {

    void apply(Container container);

    public record VarSetter<T, Container>(VarDescr<T, Container> vd) {

        public Modifier<T, Container> set(T value) {
            return (container) -> {
                vd.valueForInstance(container, new Binding.Const(value));
            };
        }

        public Modifier<T, Container> bind(Supplier<T> f) {
            return (container) -> {
                vd.valueForInstance(container, new Binding.Compute<>(f));
            };
        }

        public Modifier<T, Container> bind(Binding<T> value) {
            return (container) -> {
                vd.valueForInstance(container, value);
            };
        }
    }

    public static <T, Container> VarSetter<T, Container> forVar(VarDescr<T, Container> v) {
        return new VarSetter<>(v);
    }

    public static <T, Container> Modifier<T, Container> seq(Modifier<T, Container>... mods) {
        return (container) -> {
            for (Modifier<T, Container> mod : mods) {
                mod.apply(container);
            }
        };
    }

    public static <T, Container> Modifier<T, Container> seq(Iterable<Modifier<T, Container>> mods) {
        return (container) -> {
            for (Modifier<T, Container> mod : mods) {
                mod.apply(container);
            }
        };
    }

    public static <T, Container> Modifier<T, Container> opt(Optional<Modifier<T, Container>> mod) {
        return (container) -> {
            if (mod.isPresent()) mod.get().apply(container);
        };
    }
}
