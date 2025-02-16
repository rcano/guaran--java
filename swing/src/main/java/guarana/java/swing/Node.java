package guarana.java.swing;

import guarana.java.core.EmitterDescr;
import guarana.java.core.ExternalVarDescr;
import guarana.java.core.ObsVal;
import guarana.java.core.Var;
import guarana.java.core.VarContext;
import guarana.java.core.VarDescr;
import guarana.java.swing.event.KeyEvent;
import guarana.java.swing.event.MouseDrag;
import guarana.java.swing.event.MouseEvent;
import guarana.java.swing.util.VarsUpdater;
import io.vavr.Tuple;
import io.vavr.Tuple2;
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
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Optional;

/**
 * Top level of the component hierarchy. Corresponds to java.awt.Component
 */
public interface Node {

    java.awt.Component peer();

    public static final ExternalVarDescr<Color, java.awt.Component> Background = ExternalVarDescr.create(
            "background",
            n -> n.getBackground(),
            (n, v) -> n.setBackground(v),
            true
    );
    public static final ExternalVarDescr<Rectangle, java.awt.Component> Bounds = ExternalVarDescr.create(
            "bounds",
            n -> n.getBounds(),
            (n, v) -> n.setBounds(v),
            true
    );
    public static ExternalVarDescr<java.awt.ComponentOrientation, java.awt.Component> ComponentOrientation = ExternalVarDescr.create(
            "componentOrientation",
            n -> n.getComponentOrientation(),
            (n, v) -> n.setComponentOrientation(v),
            true
    );
    public static ExternalVarDescr<java.awt.Cursor, java.awt.Component> Cursor = ExternalVarDescr.create(
            "cursor",
            n -> n.getCursor(),
            (n, v) -> n.setCursor(v),
            true
    );
    public static ExternalVarDescr<Boolean, java.awt.Component> Enabled = ExternalVarDescr.create(
            "enabled",
            (n) -> n.isEnabled(),
            (n, v) -> n.setEnabled(v),
            true
    );
    public static ExternalVarDescr<Boolean, java.awt.Component> Focusable = ExternalVarDescr.create(
            "focusable",
            n -> n.isFocusable(),
            (n, v) -> n.setFocusable(v),
            true
    );
    public static ExternalVarDescr<java.awt.Font, java.awt.Component> Font = ExternalVarDescr.create(
            "font",
            n -> n.getFont(),
            (n, v) -> n.setFont(v),
            true
    );
    public static ExternalVarDescr<java.awt.Color, java.awt.Component> Foreground = ExternalVarDescr.create(
            "foreground",
            n -> n.getForeground(),
            (n, v) -> n.setForeground(v),
            true
    );
    public static ExternalVarDescr<Dimension, java.awt.Component> MaxSize = ExternalVarDescr.create(
            "maxSize",
            n -> n.getMaximumSize(),
            (n, v) -> n.setMaximumSize(v),
            true
    );
    public static ExternalVarDescr<Dimension, java.awt.Component> MinSize = ExternalVarDescr.create(
            "minSize",
            n -> n.getMinimumSize(),
            (n, v) -> n.setMinimumSize(v),
            true
    );
    public static ExternalVarDescr<String, java.awt.Component> Name = ExternalVarDescr.create("name", n -> n.getName(), (n, v) ->
            n.setName(v), true);
    public static ExternalVarDescr<Dimension, java.awt.Component> PrefSize = ExternalVarDescr.create(
            "prefSize",
            n -> n.getPreferredSize(),
            (n, v) -> n.setPreferredSize(v),
            true
    );
    public static ExternalVarDescr<Boolean, java.awt.Component> Visible = ExternalVarDescr.create("visible", n -> n.isVisible(), (n, v) ->
            n.setVisible(v), true);

    public static EmitterDescr<Tuple2<FocusEvent, Boolean>, java.awt.Component> FocusEvents = EmitterDescr.create(
            "FocusEvents",
            container -> container.addFocusListener(NodeInternals.FocusListenerToEmitter),
            container -> container.removeFocusListener(NodeInternals.FocusListenerToEmitter)
    );

    public static EmitterDescr<KeyEvent, java.awt.Component> KeyEvents = EmitterDescr.create(
            "FocusEvents",
            container -> container.addKeyListener(NodeInternals.KeyEventsToEmitter),
            container -> container.addKeyListener(NodeInternals.KeyEventsToEmitter)
    );

    public static EmitterDescr<MouseEvent, java.awt.Component> MouseEvents = EmitterDescr.create(
            "FocusEvents",
            container -> {
        container.addMouseListener(NodeInternals.MouseEventsToEmitter);
        container.addMouseMotionListener(NodeInternals.MouseEventsToEmitter);
        container.addMouseWheelListener(NodeInternals.MouseEventsToEmitter);
    },
            container -> {
        container.removeMouseListener(NodeInternals.MouseEventsToEmitter);
        container.removeMouseMotionListener(NodeInternals.MouseEventsToEmitter);
        container.removeMouseWheelListener(NodeInternals.MouseEventsToEmitter);
    }
    );

    public default Var<Color, java.awt.Component> background() {
        return Background.forInstanceVar(peer());
    }

    public default Var<Rectangle, java.awt.Component> bounds() {
        return Bounds.forInstanceVar(peer());
    }

    public default Var<java.awt.ComponentOrientation, java.awt.Component> componentOrientation() {
        return ComponentOrientation.forInstanceVar(peer());
    }

    public default Var<java.awt.Cursor, java.awt.Component> cursor() {
        return Cursor.forInstanceVar(peer());
    }

    public default Var<Boolean, java.awt.Component> enabled() {
        return Enabled.forInstanceVar(peer());
    }

    public default Var<Boolean, java.awt.Component> focusable() {
        return Focusable.forInstanceVar(peer());
    }

    public default ObsVal<Boolean, java.awt.Component> focused() {
        return NodeInternals.FocusedMut.forInstance(peer());
    }

    public default Var<java.awt.Font, java.awt.Component> font() {
        return Font.forInstanceVar(peer());
    }

    public default Var<java.awt.Color, java.awt.Component> foreground() {
        return Foreground.forInstanceVar(peer());
    }

    public default ObsVal<Boolean, java.awt.Component> hovered() {
        return NodeInternals.HoveredMut.forInstance(peer());
    }

    public default Var<Dimension, java.awt.Component> maxSize() {
        return MaxSize.forInstanceVar(peer());
    }

    public default Var<Dimension, java.awt.Component> minSize() {
        return MinSize.forInstanceVar(peer());
    }

    public default ObsVal<Optional<MouseDrag>, java.awt.Component> mouseDrag() {
        return NodeInternals.MouseDragMut.forInstance(peer());
    }

    public default ObsVal<Point, java.awt.Component> mouseLocation() {
        return NodeInternals.MouseLocationMut.forInstance(peer());
    }

    public default Var<String, java.awt.Component> name() {
        return Name.forInstanceVar(peer());
    }

    public default Var<Dimension, java.awt.Component> prefSize() {
        return PrefSize.forInstanceVar(peer());
    }

    public default Var<Boolean, java.awt.Component> visible() {
        return Visible.forInstanceVar(peer());
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
            public void mouseDragged(java.awt.event.MouseEvent e) {
            }

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                Toolkit.INSTANCE.update(() -> {
                    NodeInternals.MouseLocationMut.forInstanceVar(v).value(e.getPoint());
                });
            }
        });
        v.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                Toolkit.INSTANCE.update(() -> {
                    NodeInternals.FocusedMut.forInstanceVar(v).value(true);
                });
            }

            @Override
            public void focusLost(FocusEvent e) {
                NodeInternals.FocusedMut.forInstanceVar(v).value(false);
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
                    VarContext.getContext().externalPropertyUpdated(Bounds, v, null);
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

    static VarDescr<Boolean, java.awt.Component> FocusedMut = VarDescr.create("focusedMut", false, false);
    static VarDescr<Boolean, java.awt.Component> HoveredMut = VarDescr.create("hoveredMut", false, false);
    static VarDescr<Optional<MouseDrag>, java.awt.Component> MouseDragMut = VarDescr.create("mouseDragMut", Optional.empty(), false);
    static VarDescr<Point, java.awt.Component> MouseLocationMut = VarDescr.create("mouseLocationMut", new Point(0, 0), false);

    static FocusListener FocusListenerToEmitter = new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.FocusEvents, Tuple.of(e, true), e.getComponent()));
        }

        @Override
        public void focusLost(FocusEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.FocusEvents, Tuple.of(e, false), e.getComponent()));
        }
    };
    static KeyListener KeyEventsToEmitter = new KeyListener() {
        @Override
        public void keyTyped(java.awt.event.KeyEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.KeyEvents, new KeyEvent.Typed(e), e.getComponent()));
        }

        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.KeyEvents, new KeyEvent.Pressed(e), e.getComponent()));
        }

        @Override
        public void keyReleased(java.awt.event.KeyEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.KeyEvents, new KeyEvent.Released(e), e.getComponent()));
        }
    };

    static MouseEventsToEmitter MouseEventsToEmitter = new MouseEventsToEmitter();

    static class MouseEventsToEmitter implements MouseListener, MouseMotionListener, MouseWheelListener {

        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.MouseEvents, new MouseEvent.Clicked(e), e.getComponent()));
        }

        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.MouseEvents, new MouseEvent.Pressed(e), e.getComponent()));
        }

        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.MouseEvents, new MouseEvent.Released(e), e.getComponent()));
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.MouseEvents, new MouseEvent.Entered(e), e.getComponent()));
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.MouseEvents, new MouseEvent.Exited(e), e.getComponent()));
        }

        @Override
        public void mouseDragged(java.awt.event.MouseEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.MouseEvents, new MouseEvent.Dragged(e), e.getComponent()));
        }

        @Override
        public void mouseMoved(java.awt.event.MouseEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.MouseEvents, new MouseEvent.Moved(e), e.getComponent()));
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            Toolkit.INSTANCE.update(() -> VarContext.getContext().emit(Node.MouseEvents, new MouseEvent.WheelMoved(e), e.getComponent()));
        }

    }
}
