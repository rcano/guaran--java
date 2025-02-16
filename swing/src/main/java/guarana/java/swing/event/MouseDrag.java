package guarana.java.swing.event;

import guarana.java.swing.util.MousePosition;
import org.w3c.dom.events.MouseEvent;

/**
 *
 * @author randa
 */
public record MouseDrag(MousePosition dragStart, MousePosition dragStop, boolean isReleased, MouseEvent awtEvent, MouseEvent dragTriggetEvent) {

}
