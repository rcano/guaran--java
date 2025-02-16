package guarana.java.core;

import org.jspecify.annotations.Nullable;

public interface VarContext {

    public <T, Container> void update(VarDescr<T, Container> var, Container instance, Binding<T> binding);
    
    public <T, Container> T get(ObsValDescr<T, Container> val, Container instance);

    public <T, Container> void externalPropertyUpdated(ObsValDescr<T, Container> val, Container instance, @Nullable T oldValue);
    
    public <T, Container> void connectEmitter(EmitterDescr<T, Container> emitterDescr, EventIterator<T> it, Container instance);
    
    public <T, Container> void emit(EmitterDescr<T, Container> emitterDescr, T elem, Container instance);
    
    static ScopedValue<VarContext> CONTEXT = ScopedValue.newInstance();
    
    static VarContext getContext() {
        if (CONTEXT.isBound()) return CONTEXT.get();
        else throw new IllegalStateException("Trying to read or update a Var outside toolkit control. Make sure to run your code that makes changes to vars within Toolkit.update");
    }
}
