package guarana.java.core;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ExternalObsValDescr<T, Container> extends ObsValDescr<T, Container> {

    public T get(Container owner);

    @Override
    public default T initialValueForInstance(Container instance) {
        return get(instance);
    }

    public static <T, Container> ExternalObsValDescr<T, Container> create(
            String name,
            Function<Container, T> getter,
            Consumer<Object> onFirstAssociation
    ) {
        return new ExternalObsValDescr<T, Container>() {
            private int id = Internals.varsUniqueIdGen.getAndIncrement();
            @Override
            public T get(Container owner) {
                return getter.apply(owner);
            }

            @Override
            public void onFirstAssociation(Container instance) {
                onFirstAssociation.accept(instance);
            }

            @Override
            public int uniqueId() {
                return id;
            }
        };
    }
}
