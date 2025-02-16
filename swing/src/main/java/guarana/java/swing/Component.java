package guarana.java.swing;

import guarana.java.swing.util.VarsUpdater;
import javax.swing.JComponent;

/**
 * Top level of the component hierarchy. Corresponds to java.awt.Component
 */
public interface Component extends Node {

    @Override
    JComponent peer();

    public static Component wrap(JComponent c) {
        return new Component() {
            @Override
            public JComponent peer() {
                return c;
            }
        };
    }

    public static void init(Component c) {
        Node.init(c);
        c.peer().addPropertyChangeListener(ComponentVarUpdater.VarUpdated.createListenerFor(c));
    }
    
    public default void amend(Modifier<?, ? super Component>... mods) {
        
    }
}

class ComponentVarUpdater {

    static final VarsUpdater VarUpdated = new VarsUpdater(Component.class);
}
