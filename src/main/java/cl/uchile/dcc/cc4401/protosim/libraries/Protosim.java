package cl.uchile.dcc.cc4401.protosim.libraries;

import java.util.List;

import com.cburch.logisim.std.wiring.Wiring;
import com.cburch.logisim.tools.FactoryDescription;
import com.cburch.logisim.tools.Library;
import com.cburch.logisim.tools.Tool;

public class Protosim extends Library {
	
    private static FactoryDescription[] DESCRIPTIONS = {
    		// TODO: l10n this
            // new FactoryDescription("Power", getFromLocale("powerComponent"), "power.svg", "Power"),
            new FactoryDescription("Power", "Voltage generator", "power.svg", "Power"),
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
    	return FactoryDescription.getTools(Wiring.class, DESCRIPTIONS);
    }
}
