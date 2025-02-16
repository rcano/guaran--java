package guarana.java.swing;

import guarana.java.core.ExternalVarDescr;
import guarana.java.core.ObsVal;
import guarana.java.core.Var;
import guarana.java.core.VarContext;
import guarana.java.core.VarDescr;
import guarana.java.swing.event.MouseDrag;
import guarana.java.swing.util.VarsUpdater;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Optional;

/**
 * Top level of the component hierarchy. Corresponds to java.awt.Component
 */
public interface Node {

    java.awt.Component peer();

    public static final ExternalVarDescr<Color, Node> Background = ExternalVarDescr.create(
            "background",
            n -> n.peer().getBackground(),
            (n, v) -> n.peer().setBackground(v),
            true
    );
    public static final ExternalVarDescr<Rectangle, Node> Bounds = ExternalVarDescr.create(
            "bounds",
            n -> n.peer().getBounds(),
            (n, v) -> n.peer().setBounds(v),
            true
    );
    public static ExternalVarDescr<java.awt.ComponentOrientation, Node> ComponentOrientation = ExternalVarDescr.create(
            "componentOrientation",
            n -> n.peer().getComponentOrientation(),
            (n, v) -> n.peer().setComponentOrientation(v),
            true
    );
    public static ExternalVarDescr<java.awt.Cursor, Node> Cursor = ExternalVarDescr.create(
            "cursor",
            n -> n.peer().getCursor(),
            (n, v) -> n.peer().setCursor(v),
            true
    );
    public static ExternalVarDescr<Boolean, Node> Enabled = ExternalVarDescr.create(
            "enabled",
            (n) -> n.peer().isEnabled(),
            (n, v) -> n.peer().setEnabled(v),
            true
    );
    public static ExternalVarDescr<Boolean, Node> Focusable = ExternalVarDescr.create(
            "focusable",
            n -> n.peer().isFocusable(),
            (n, v) -> n.peer().setFocusable(v),
            true
    );
    public static ExternalVarDescr<java.awt.Font, Node> Font = ExternalVarDescr.create(
            "font",
            n -> n.peer().getFont(),
            (n, v) -> n.peer().setFont(v),
            true
    );
    public static ExternalVarDescr<java.awt.Color, Node> Foreground = ExternalVarDescr.create(
            "foreground",
            n -> n.peer().getForeground(),
            (n, v) -> n.peer().setForeground(v),
            true
    );
    public static ExternalVarDescr<Dimension, Node> MaxSize = ExternalVarDescr.create(
            "maxSize",
            n -> n.peer().getMaximumSize(),
            (n, v) -> n.peer().setMaximumSize(v),
            true
    );
    public static ExternalVarDescr<Dimension, Node> MinSize = ExternalVarDescr.create(
            "minSize",
            n -> n.peer().getMinimumSize(),
            (n, v) -> n.peer().setMinimumSize(v),
            true
    );
    public static ExternalVarDescr<String, Node> Name = ExternalVarDescr.create("name", n -> n.peer().getName(), (n, v) ->
            n.peer().setName(v), true);
    public static ExternalVarDescr<Dimension, Node> PrefSize = ExternalVarDescr.create(
            "prefSize",
            n -> n.peer().getPreferredSize(),
            (n, v) -> n.peer().setPreferredSize(v),
            true
    );
    public static ExternalVarDescr<Boolean, Node> Visible = ExternalVarDescr.create("visible", n -> n.peer().isVisible(), (n, v) ->
            n.peer().setVisible(v), true);

    public default Var<Color, Node> background() {
        return Background.forInstanceVar(this);
    }

    public default Var<Rectangle, Node> bounds() {
        return Bounds.forInstanceVar(this);
    }

    public default Var<java.awt.ComponentOrientation, Node> componentOrientation() {
        return ComponentOrientation.forInstanceVar(this);
    }

    public default Var<java.awt.Cursor, Node> cursor() {
        return Cursor.forInstanceVar(this);
    }

    public default Var<Boolean, Node> enabled() {
        return Enabled.forInstanceVar(this);
    }

    public default Var<Boolean, Node> focusable() {
        return Focusable.forInstanceVar(this);
    }

    public default ObsVal<Boolean, Node> focused() {
        return NodeInternals.FocusedMut.forInstance(this);
    }

    public default Var<java.awt.Font, Node> font() {
        return Font.forInstanceVar(this);
    }

    public default Var<java.awt.Color, Node> foreground() {
        return Foreground.forInstanceVar(this);
    }

    public default ObsVal<Boolean, Node> hovered() {
        return NodeInternals.HoveredMut.forInstance(this);
    }

    public default Var<Dimension, Node> maxSize() {
        return MaxSize.forInstanceVar(this);
    }

    public default Var<Dimension, Node> minSize() {
        return MinSize.forInstanceVar(this);
    }

    public default ObsVal<Optional<MouseDrag>, Node> mouseDrag() {
        return NodeInternals.MouseDragMut.forInstance(this);
    }

    public default ObsVal<Point, Node> mouseLocation() {
        return NodeInternals.MouseLocationMut.forInstance(this);
    }

    public default Var<String, Node> name() {
        return Name.forInstanceVar(this);
    }

    public default Var<Dimension, Node> prefSize() {
        return PrefSize.forInstanceVar(this);
    }

    public default Var<Boolean, Node> visible() {
        return Visible.forInstanceVar(this);
    }

    public default void onFirstTimeVisible(Runnable r) {
        var hl = new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) > 0 && peer().isDisplayable()) {
                    peer().removeHierarchyListener(this);
                    Toolkit.INSTANCE.update(r);
                }
            }
        };
        peer().addHierarchyListener(hl);
    }

    public static Node wrap(java.awt.Container c) {
        return new Node() {
            @Override
            public Component peer() {
                return c;
            }
        };
    }

    public static Node uninitialized() {
        return wrap(new java.awt.Container());
    }

    public static void init(Node n) {
        final var v = n.peer();
        v.addPropertyChangeListener(NodeInternals.VarUpdated.createListenerFor(n));
        v.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Toolkit.INSTANCE.update(() -> {
                    NodeInternals.MouseLocationMut.forInstanceVar(n).value(e.getPoint());
                });
            }
        });
        v.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                Toolkit.INSTANCE.update(() -> {
                    NodeInternals.FocusedMut.forInstanceVar(n).value(true);
                });
            }

            @Override
            public void focusLost(FocusEvent e) {
                NodeInternals.FocusedMut.forInstanceVar(n).value(false);
            }
        });
        v.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                updateBounds();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                updateBounds();
            }

            private void updateBounds() {
                Toolkit.INSTANCE.update(() -> { 
                    VarContext.getContext().externalPropertyUpdated(Bounds, n, null);
                });
            }
        });

    }

    public static Node create(Modifier<?, ? super Node>... mods) {
        var peer = new java.awt.Container();
        var res = new Node() {
            @Override
            public Component peer() {
                return peer;
            }
        };
        for (Modifier<?, ? super Node> mod : mods) {
            mod.apply(res);
        }
        return res;
    }
}

class NodeInternals {

    static final VarsUpdater VarUpdated = new VarsUpdater(Node.class);

    static VarDescr<Boolean, Node> FocusedMut = VarDescr.create("focusedMut", false, false);
    static VarDescr<Boolean, Node> HoveredMut = VarDescr.create("hoveredMut", false, false);
    static VarDescr<Optional<MouseDrag>, Node> MouseDragMut = VarDescr.create("mouseDragMut", Optional.empty(), false);
    static VarDescr<Point, Node> MouseLocationMut = VarDescr.create("mouseLocationMut", new Point(0, 0), false);
}
