package guarana.java.swing.event;

public sealed interface KeyEvent {

    java.awt.event.KeyEvent awtEvent();

    static record Pressed(java.awt.event.KeyEvent awtEvent) implements KeyEvent {

    }

    static record Released(java.awt.event.KeyEvent awtEvent) implements KeyEvent {

    }

    static record Typed(java.awt.event.KeyEvent awtEvent) implements KeyEvent {

    }
}
