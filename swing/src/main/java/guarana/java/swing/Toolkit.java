package guarana.java.swing;

import guarana.java.core.AbstractToolkit;
import guarana.java.core.ExternalObsValDescr;
import guarana.java.core.ObsVal;
import guarana.java.core.ObsValDescr;
import guarana.java.core.Stylist;
import guarana.java.core.Var;
import guarana.java.core.VarContext;
import guarana.java.swing.util.DesktopEnvironmentInfo;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Toolkit extends AbstractToolkit {

    public static Toolkit INSTANCE = new Toolkit();
    public final Var<Double, ?> emSize = Var.singleton("emSize", (double) DesktopEnvironmentInfo.fontDeterminedByOs.get().map(f ->
            (double) f.getSize2D()).getOrElse(14.0), false);
    public final Var<Stylist.Metrics, ?> sysMetrics = Var.singleton("sysMetrics", Stylist.Metrics.Empty, false);

    private final Map<String, ExternalObsValDescr<?, ?>> uidefaultsObsVals = new HashMap<>();

    private Toolkit() {
        UIManager.getDefaults().addPropertyChangeListener(uiDefaultsChangeListener.INSTANCE);

        var fontMetrics = new Stylist.FontMetrics() {
            private JPanel peer = new JPanel();

            @Override
            public double ascent(Object font) {
                return peer.getFontMetrics((Font) font).getAscent();
            }

            @Override
            public double descent(Object font) {
                return peer.getFontMetrics((Font) font).getDescent();
            }

            @Override
            public double stringWidth(Object font, String s) {
                return peer.getFontMetrics((Font) font).charsWidth(s.toCharArray(), 0, s.length());
            }
        };

        update(() -> {
            sysMetrics.bind(() -> new Stylist.Metrics(emSize.value(), -1, -1, fontMetrics));
        });
    }

    @Override
    protected boolean isToolkitThread() {
        return SwingUtilities.isEventDispatchThread();
    }

    @Override
    protected void runOnToolkitThread(Runnable r) {
        SwingUtilities.invokeLater(r);
    }

    @Override
    public Stylist.Metrics getMetrics() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    <T> ObsVal<T, ?> registerUiDefaultObsVal(String name, String property, Class<T> tpe) {
        var descr = ExternalObsValDescr.create(name, _ -> {
            var value = UIManager.get(property);
            if (tpe.isInstance(value)) return (T) value;
            else return null;
        });
        uidefaultsObsVals.put(property, descr);
        return descr.forInstance(this);
    }

    private static enum uiDefaultsChangeListener implements PropertyChangeListener {
        INSTANCE;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final var extObsVal = Toolkit.INSTANCE.uidefaultsObsVals.get(evt.getPropertyName());

            Toolkit.INSTANCE.update(() -> {
                VarContext.getContext().externalPropertyUpdated((ExternalObsValDescr) extObsVal, Toolkit.INSTANCE, evt.getOldValue());
            });
        }
    }

    class swingStateReader extends stateReader {

        private swingStateReader() {

        }

        public double emSize() {
            return getOrDefaul((ObsValDescr<Double, Object>) emSize.varDescr(), emSize.instance());
        }
    }
}
