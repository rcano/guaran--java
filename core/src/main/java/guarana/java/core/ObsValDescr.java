package guarana.java.core;

import guarana.java.core.util.Unique;
import java.util.function.Consumer;

public interface ObsValDescr<T, Container> extends Unique {

    default public T valueForInstance(Container instance) {
        return VarContext.getContext().get(this, instance);
    }

    public T initialValueForInstance(Container instance);

    public void onFirstAssociation(Container instance);

    public default <C extends Container> ObsVal<T, C> forInstance(C instance) {
        return new ObsVal(this, instance);
    }

    static Consumer<Object> DO_NOTHING = obj -> {
    };
}
