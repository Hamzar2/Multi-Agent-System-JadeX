package agentsChat;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;

public class Main {

	public static void main(String[] args) {
        PlatformConfiguration   config  = PlatformConfiguration.getDefaultNoGui();

     // Configuration pour utiliser BDIV3
        config.setGui(true);
        
        Starter.createPlatform(config).get();
	}

}