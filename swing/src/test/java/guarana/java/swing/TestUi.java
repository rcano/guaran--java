package guarana.java.swing;

import guarana.java.core.EventIterator;
import static guarana.java.swing.Modifiers.*;
import io.vavr.collection.Stream;
import java.awt.Cursor;
import java.awt.Font;

/**
 *
 * @author randa
 */
public class TestUi {

    public static void main(String[] args) {

        Toolkit.INSTANCE.update(() -> {
            // creating a single node
            Node n = Node.create(
                    background.set(Colors.AntiqueWhite),
                    foreground.bind(() -> Colors.theme.TextInactiveText.value())
            );

            // defining a bunch of settings
            var bunchOfCommonSettings = Modifier.seq(
                    background.set(Colors.DarkCyan),
                    foreground.bind(() -> Colors.theme.TextInactiveText.value()),
                    cursor.set(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
            );

            //using the bunch of esttings
            var someNodes = Stream.fill(10, () -> Node.create(
                    bunchOfCommonSettings,
                    // some extra settings
                    keyEvents.connect(EventIterator.all.take(10).foreach(evt -> System.out.println("Key event: " + evt)))
            )).toList();

            // we can modify properties directly
            n.font().bind(() -> Fonts.Base.value().deriveFont(
                    n.focused().value() ? Font.BOLD : Font.PLAIN,
                    Toolkit.INSTANCE.emSize.value()));
        });
    }

}
