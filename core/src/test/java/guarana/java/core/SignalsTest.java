package guarana.java.core;

import java.util.logging.Level;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SignalsTest {

    class Toolkit extends AbstractToolkit {

        @Override
        protected boolean isToolkitThread() {
            return true;
        }

        @Override
        protected void runOnToolkitThread(Runnable r) {
            r.run();
        }

    }

    final Toolkit tk = new Toolkit();
    final Var<String, SignalsTest> foo = VarDescr.create("foo", "", true).forInstanceVar(this);
    final Var<String, SignalsTest> bar = VarDescr.create("var", "", true).forInstanceVar(this);
    final Var<Integer, SignalsTest> baz = VarDescr.create("baz", 0, true).forInstanceVar(this);

    @Test
    @DisplayName("Cannot use vars outside VarContext")
    public void implicitVarContext() {
        assertThrows(IllegalStateException.class, () -> {
            foo.value("Hello");
            bar.bind(() -> foo.value() + " World!");
        });
    }

    @Test
    public void varAssignment() {
        tk.update(() -> {
            foo.value("first");
        });
    }

    @Test
    public void varSimpleBinding() {
        tk.update(() -> {
            foo.bind(() -> "first");
        });
    }

    @Test
    public void varReactiveBinding() {
        tk.update(() -> {
            foo.bind(() -> "times: " + baz.value());
            baz.value(1);
            assertEquals("times: 1", foo.value());
        });
    }
}
