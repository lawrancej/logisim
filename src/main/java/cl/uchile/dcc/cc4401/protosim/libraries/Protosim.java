package cl.uchile.dcc.cc4401.protosim.libraries;

import java.util.Arrays;
import java.util.List;

import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

import cl.uchile.dcc.cc4401.protosim.components.Breadboard;
import cl.uchile.dcc.cc4401.protosim.components.Led;

/*
 * Protosim components library for Logisim
 * To display it, must be defined in: src/main/resources/logisim/default.templ
 */
public class Protosim extends Library {

	// Available tools
    private static Tool[] TOOLS = {
            new AddTool(Breadboard.FACTORY),
            new AddTool(Led.FACTORY)
    };

    public Protosim() {
    }

    @Override
    public String getName() {
        return "Protosim";
    }

    @Override
    public String getDisplayName() {
        // TODO: l10n this
        // return getFromLocale("protosimLibrary");
        return "Protosim";
    }

    @Override
    public List<Tool> getTools() {
        return Arrays.asList(TOOLS);
    }
}
