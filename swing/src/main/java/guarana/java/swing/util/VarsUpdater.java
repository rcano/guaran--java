package guarana.java.swing.util;

import guarana.java.core.ExternalVarDescr;
import guarana.java.core.VarContext;
import guarana.java.swing.Node;
import guarana.java.swing.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.AccessFlag;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VarsUpdater {

    /**
     * properties that should be ignored are registered here
     */
    public final Set<String> ignoreProperties = new HashSet<>();
    private final Map<String, ExternalVarDescr> varsMap;

    public VarsUpdater(Class<? extends Node> c) {
        varsMap = Stream.of(c.getDeclaredFields())
                .filter(f -> f.accessFlags().contains(AccessFlag.STATIC) && ExternalVarDescr.class.isAssignableFrom(f.getType()))
                .map(f -> {
                    try {
                        return (ExternalVarDescr) f.get(null);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        throw new IllegalStateException(ex);
                    }
                }).collect(Collectors.toMap(v -> v.name().toLowerCase(), v -> v));
    }

    public PropertyChangeListener createListenerFor(Node node) {
        return (PropertyChangeEvent evt) -> {
            if (ignoreProperties.contains(evt.getPropertyName())) return;

            var extVar = varsMap.get(evt.getPropertyName().toLowerCase());
            if (extVar != null) {
                Toolkit.INSTANCE.update(() -> {
                    VarContext.getContext().externalPropertyUpdated(extVar, node, evt.getOldValue());
                });
            }
        };
    }

}
