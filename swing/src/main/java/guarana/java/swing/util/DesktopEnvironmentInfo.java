package guarana.java.swing.util;

import io.vavr.Lazy;
import io.vavr.control.Option;
import java.awt.Font;
import java.util.function.Supplier;
import javax.swing.plaf.FontUIResource;

public class DesktopEnvironmentInfo {

    public enum DesktopEnvironment {
        Kde, Gtk, OSX, Windows;
    }

    public static final DesktopEnvironment DE = ((Supplier<DesktopEnvironment>) () -> {
        var osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            return Option.of(System.getenv("KDE_FULL_SESSION")).map(_ -> DesktopEnvironment.Kde).getOrElse(DesktopEnvironment.Gtk);
        } else if (osName.startsWith("Windows")) {
            return DesktopEnvironment.Windows;
        } else {
            return DesktopEnvironment.OSX;
        }
    }).get();

    // The following logic was taken from FlatLafUI
    public static Lazy<Option<FontUIResource>> fontDeterminedByOs = Lazy.of(() -> {
        var tk = java.awt.Toolkit.getDefaultToolkit();
        return switch (DE) {
            case Windows ->
                Option.of(tk.getDesktopProperty("win.messagebox.font")).map(f -> new FontUIResource((Font) f));
            case OSX ->
                Option.some(new FontUIResource("Lucida Grande", Font.PLAIN, 13));
            case Kde ->
                Option.some(new FontUIResource(LinuxFontPolicy.getKDEFont()));
            case Gtk ->
                Option.some(new FontUIResource(LinuxFontPolicy.getGnomeFont()));
        };
    });
}
