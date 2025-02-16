package guarana.java.swing;

import java.awt.Color;

/**
 *
 * @author randa
 */
public class Modifiers {

    public static final Modifier.VarSetter<Color, Node> background = Modifier.forVar(Node.Background);

    {
//        Modifier.VarSetter<Color, Node> forVar = Modifier.forVar(Node.Background);
    }
}
