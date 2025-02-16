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
    public final Var<Float, ?> emSize = Var.singleton("emSize", DesktopEnvironmentInfo.fontDeterminedByOs.get().map(f ->
            f.getSize2D()).getOrElse(14.0f), false);
    public final Var<Stylist.Metrics, ?> sysMetrics = Var.singleton("sysMetrics", Stylist.Metrics.Empty, false);

    private final Map<String, ExternalObsValDescr<?, ?>> uidefaultsObsVals = new HashMap<>();

    private Toolkit() {
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
            
            // we only want to register the uiDefaultsChangeListener after the Toolkit.INSTANCE is fully instantiated. so we place this in an update call, which happens in the next swing tick.
            UIManager.getDefaults().addPropertyChangeListener(uiDefaultsChangeListener.INSTANCE);
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

    @Override
    public swingStateReader stateReader() {
        return new swingStateReader();
    }
    
    

    class swingStateReader extends stateReader {

        private swingStateReader() {

        }

        public float emSize() {
            return getOrDefaul((ObsValDescr<Float, Object>) emSize.varDescr(), emSize.instance());
        }
    }
}
