package guarana.java.core.util;

import java.util.Map;
import java.util.function.Function;

public final class Throwables {

    public static boolean isFatal(Throwable t) {
        return switch (t) {
            case VirtualMachineError e ->
                true;
            case InterruptedException e ->
                true;
            case LinkageError e ->
                true;
            default ->
                false;
        };
    }

    public static <T extends Throwable> void rethrowIfFatal(Throwable t) throws T {
        var recFunc = new Object() {
            Map<Long, Long> rec(Map<Long, Long> state, int iteration) {
              if (iteration == 75) return state;
              //... perform this step and return the updated state:
              return rec(state, iteration + 1); // imgaine it's updated
          }  
        };
        
        recFunc.rec(Map.of(), 0);
        
        throw (T) t;
    }
}
