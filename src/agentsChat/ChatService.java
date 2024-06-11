package agentsChat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.bridge.service.annotation.ServiceShutdown;
import jadex.bridge.service.annotation.ServiceStart;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.gui.future.SwingExceptionDelegationResultListener;

@Service
public class ChatService implements IChatService {

    @ServiceComponent
    IInternalAccess agent;
    @ServiceComponent
    IRequiredServicesFeature requiredServicesFeature;

    IClockService clock;
    DateFormat format;
    ChatGui gui;
    Timer timer;


    private String generateMessage() {
        String[] messages = {"Hello!", "How are you?", "What's up?", "Nice to meet you!"};
        int randomIndex = new Random().nextInt(messages.length);
        return messages[randomIndex];
    }

    @ServiceStart
    public IFuture<Void> startService() {
        final IExternalAccess exta = agent.getExternalAccess();
        format = new SimpleDateFormat("hh:mm:ss");
        final Future<Void> ret = new Future<Void>();
        IFuture<IClockService> clockservice = requiredServicesFeature.getRequiredService("clockservice");
        clockservice.addResultListener(new SwingExceptionDelegationResultListener<IClockService, Void>(ret) {
            public void customResultAvailable(IClockService result) {
                clock = result;
                gui = createGui(exta);

                // Schedule message sending using Timer
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String message = generateMessage();
                        IFuture<Collection<IChatService>> chatservices = 
                                agent.getComponentFeature(IRequiredServicesFeature.class).getRequiredServices("chatservices");
                        chatservices.addResultListener(new DefaultResultListener<Collection<IChatService>>() {
                            public void resultAvailable(Collection<IChatService> result) {
                                for (IChatService service : result) {
                                    service.message(agent.getComponentIdentifier().getLocalName(), message);
                                }
                            }
                        });
                    }
                }, 0, 5000); // Start immediately, repeat every 3 seconds
                
                ret.setResult(null);
            }
        });
        return ret;
    }

    protected ChatGui createGui(IExternalAccess agent) {
        return new ChatGui(agent);
    }

    @ServiceShutdown
    public void shutdownService () {
        // Stop the timer when shutting down
        timer.cancel();
        
        Runnable doWorkRunnable = new Runnable() {
            public void run() {
                gui.dispose();
            }
        };
        SwingUtilities.invokeLater(doWorkRunnable);
    }
    
    public void message(String sender, String text) {
        boolean sameAgent = sender.equals(agent.getComponentIdentifier().getLocalName());
        String formattedMessage;
        if (sameAgent) {
            // Message sent by this agent
            formattedMessage = agent.getComponentIdentifier().getLocalName() + " <sent> at \"" + 
                               new Date(clock.getTime()) + "\" <message>: " + text;
        } else {
            // Message received from another agent 
            formattedMessage = agent.getComponentIdentifier().getLocalName() + " <received> at \"" + 
                               new Date(clock.getTime()) + "\" <from>: " + sender + " <message>: " + text; 
        }
        // Update the GUI (ensure this happens on the Swing event dispatch thread)
        SwingUtilities.invokeLater(() -> gui.addMessage(formattedMessage, sameAgent)); 
    }
    
}



