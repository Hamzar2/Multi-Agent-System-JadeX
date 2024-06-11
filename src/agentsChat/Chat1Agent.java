package agentsChat;

import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.types.clock.IClockService;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

@Description("This agent declares a required clock service.")
@Agent
@RequiredServices
({
		@RequiredService(name="clockservice", type=IClockService.class,
				binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)),
		@RequiredService(name="chatservices", type=IChatService.class, 
		multiple=true, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM,
		dynamic=true))
})
@ProvidedServices(@ProvidedService(type=IChatService.class,
implementation=@Implementation(ChatService.class)))
public class Chat1Agent {
	
}