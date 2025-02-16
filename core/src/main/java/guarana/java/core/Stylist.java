package guarana.java.core;

import java.util.Optional;

public interface Stylist {

    /**
     * Return the style defined value for a given property, if defined.
     */
    <T, Container> Optional<T> apply(Metrics metrics, ObsValDescr<T, Container> property, Container instance);

    /**
     * Notifies the stylist that the cache for the given node should be invalidated, typically called when the node is removed from the
     * toolkit.
     *
     * @param node The node whose cache should be invalidated
     */
    void invalidateCache(Object node);

    /**
     * This method is called by LaF instances when setting up defaults for a node, giving the stylist a chance to set specific settings.
     *
     * Typically, opacity or some spacings are configured here.
     */
    void installDefaults(Object node);

    /**
     * Counterpart method to [[installDefaults]].
     *
     * Stylists are expected to undo settings they set here, so as to avoid conflicts with other LaFs.
     */
    void uninstallDefaults(Object node);

    static record Metrics(double emSize, int screenWidth, int screenHeight, FontMetrics fontMetrics) {

        public static Metrics Empty = new Metrics(0, 0, 0, new FontMetrics() {
            @Override
            public double ascent(Object font) {
                    return 0;
            }

            @Override
            public double descent(Object font) {
                return 0;
            }

            @Override
            public double stringWidth(Object font, String s) {
                return 0;
            }
        });
    }

    interface FontMetrics {

        double ascent(Object font);

        double descent(Object font);

        double stringWidth(Object font, String s);
    }

    enum NoOp implements Stylist {
        INSTANCE;

        @Override
        public <T, Container> Optional<T> apply(Metrics metrics, ObsValDescr<T, Container> property, Container instance) {
            return Optional.empty();
        }

        @Override
        public void invalidateCache(Object node) {
        }

        @Override
        public void installDefaults(Object node) {
        }

        @Override
        public void uninstallDefaults(Object node) {
        }
    }
}
