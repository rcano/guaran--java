package guarana.java.swing;

import guarana.java.core.ObsVal;
import java.awt.Font;

public class Fonts {

    public static final ObsVal<Font, ?> Base = Toolkit.INSTANCE.registerUiDefaultObsVal("Fonts.Base", "Panel.font", Font.class);

    public static final ObsVal<Font, ?> Input = Toolkit.INSTANCE.registerUiDefaultObsVal("Fonts.InputBase", "TextField.font", Font.class);

    public static final ObsVal<Font, ?> Label = Toolkit.INSTANCE.registerUiDefaultObsVal("Fonts.LabelBase", "Label.font", Font.class);
}
