package guarana.java.swing;

import guarana.java.swing.event.KeyEvent;
import guarana.java.swing.event.MouseEvent;
import io.vavr.Tuple2;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;

/**
 *
 * @author randa
 */
public class Modifiers {

    public static final Modifier.VarSetter<Color, Node, ?> background = Modifier.forVar(Node.Background, Node::peer);
    public static final Modifier.VarSetter<Boolean, Node, Component> visible = Modifier.forVar(Node.Visible, Node::peer);
    public static final Modifier.VarSetter<Dimension, Node, Component> prefSize = Modifier.forVar(Node.PrefSize, Node::peer);
    public static final Modifier.VarSetter<String, Node, Component> name = Modifier.forVar(Node.Name, Node::peer);
    public static final Modifier.VarSetter<Dimension, Node, Component> minSize = Modifier.forVar(Node.MinSize, Node::peer);
    public static final Modifier.VarSetter<Dimension, Node, Component> maxSize = Modifier.forVar(Node.MaxSize, Node::peer);
    public static final Modifier.VarSetter<Color, Node, Component> foreground = Modifier.forVar(Node.Foreground, Node::peer);
    public static final Modifier.VarSetter<Font, Node, Component> font = Modifier.forVar(Node.Font, Node::peer);
    public static final Modifier.VarSetter<Boolean, Node, Component> focusable = Modifier.forVar(Node.Focusable, Node::peer);
    public static final Modifier.VarSetter<Boolean, Node, Component> enabled = Modifier.forVar(Node.Enabled, Node::peer);
    public static final Modifier.VarSetter<Cursor, Node, Component> cursor = Modifier.forVar(Node.Cursor, Node::peer);
    public static final Modifier.VarSetter<ComponentOrientation, Node, Component> componentOrientation = Modifier.forVar(Node.ComponentOrientation, Node::peer);
    public static final Modifier.VarSetter<Rectangle, Node, Component> bounds = Modifier.forVar(Node.Bounds, Node::peer);
    
    public static final Modifier.EventIteratorSetter<MouseEvent, Node, Component> mouseEvents = Modifier.forEmitter(Node.MouseEvents, Node::peer);
    public static final Modifier.EventIteratorSetter<KeyEvent, Node, Component> keyEvents = Modifier.forEmitter(Node.KeyEvents, Node::peer);
    public static final Modifier.EventIteratorSetter<Tuple2<FocusEvent, Boolean>, Node, Component> focusEvents = Modifier.forEmitter(Node.FocusEvents, Node::peer);

    {
//        Modifier.VarSetter<Color, Node> forVar = Modifier.forVar(Node.Background);

    }
}
