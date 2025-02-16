package guarana.java.swing.event;

/**
 *
 * @author randa
 */
public sealed interface MouseEvent {

    java.awt.event.MouseEvent awtEvent();

    static record Moved(java.awt.event.MouseEvent awtEvent) implements MouseEvent {

    }

    static record Dragged(java.awt.event.MouseEvent awtEvent) implements MouseEvent {

    }

    static record Entered(java.awt.event.MouseEvent awtEvent) implements MouseEvent {

    }

    static record Exited(java.awt.event.MouseEvent awtEvent) implements MouseEvent {

    }

    static record Pressed(java.awt.event.MouseEvent awtEvent) implements MouseEvent {

    }

    static record Released(java.awt.event.MouseEvent awtEvent) implements MouseEvent {

    }

    static record Clicked(java.awt.event.MouseEvent awtEvent) implements MouseEvent {

    }

    static record WheelMoved(java.awt.event.MouseWheelEvent awtEvent) implements MouseEvent {

    }
}
