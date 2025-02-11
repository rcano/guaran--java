package guarana.java.core;

import guarana.java.core.util.Unique;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author randa
 */
public class InternalsTest {
    
    public InternalsTest() {
    }

    @Test
    public void testKeyed() {
        System.out.println("keyed");
        Unique unique1 = () -> 1;
        Unique unique2 = () -> 2;
        long result = Internals.keyed(unique1, unique2);
        assertEquals(0x0000000100000002L, result);
        assertEquals(1, Internals.keyPart(result));
        assertEquals(2, Internals.instancePart(result));
    }
    
    @Test
    public void testKeyedNegEdgeCase() {
        System.out.println("keyed");
        Unique unique1 = () -> 0xFF000000;
        Unique unique2 = () -> 0xFF000000;
        long result = Internals.keyed(unique1, unique2);
        System.out.println(Long.toHexString(result));
        assertEquals(0xFF000000FF000000L, result);
        assertEquals(0xFF000000, Internals.keyPart(result));
        assertEquals(0xFF000000, Internals.instancePart(result));
    }
    
}
