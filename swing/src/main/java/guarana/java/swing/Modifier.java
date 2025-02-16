package guarana.java.swing;

import guarana.java.core.Binding;
import guarana.java.core.EmitterDescr;
import guarana.java.core.EventIterator;
import guarana.java.core.VarDescr;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents an action that can be applied to a container either during initialization or amending.
 */
@FunctionalInterface
public interface Modifier<T, Container> {

    void apply(Container container);

    public record VarSetter<T, Wrapper, Container>(VarDescr<T, Container> vd, Function<Wrapper, Container> unwrapper) {

        public Modifier<T, Wrapper> set(T value) {
            return (container) -> {
                vd.valueForInstance(unwrapper.apply(container), new Binding.Const(value));
            };
        }

        public Modifier<T, Wrapper> bind(Supplier<T> f) {
            return (container) -> {
                vd.valueForInstance(unwrapper.apply(container), new Binding.Compute<>(f));
            };
        }

        public Modifier<T, Wrapper> bind(Binding<T> value) {
            return (container) -> {
                vd.valueForInstance(unwrapper.apply(container), value);
            };
        }
    }

    public static <T, Wrapper, Container> VarSetter<T, Wrapper, Container> forVar(VarDescr<T, Container> v, Function<Wrapper, Container> unwrapper) {
        return new VarSetter<>(v, unwrapper);
    }

    public record EventIteratorSetter<T, Wrapper, Container>(EmitterDescr<T, Container> ed, Function<Wrapper, Container> unwrapper) {

        public Modifier<T, Wrapper> connect(EventIterator<T> it) {
            return c -> ed.connect(it, unwrapper.apply(c));
        }
    }

    public static <T, Wrapper, Container> EventIteratorSetter<T, Wrapper, Container> forEmitter(EmitterDescr<T, Container> ed, Function<Wrapper, Container> unwrapper) {
        return new EventIteratorSetter<>(ed, unwrapper);
    }

    public static <Container> Modifier<?, Container> seq(Modifier<?, Container>... mods) {
        return (container) -> {
            for (Modifier<?, Container> mod : mods) {
                mod.apply(container);
            }
        };
    }

    public static <Container> Modifier<?, Container> seq(Iterable<Modifier<?, Container>> mods) {
        return (container) -> {
            for (Modifier<?, Container> mod : mods) {
                mod.apply(container);
            }
        };
    }

    public static <Container> Modifier<?, Container> opt(Optional<Modifier<?, Container>> mod) {
        return (container) -> {
            if (mod.isPresent()) mod.get().apply(container);
        };
    }
}
