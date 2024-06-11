package agentsChat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;

public class ChatGui extends JFrame{

	private static final long serialVersionUID = 1L;
	
	JTextPane received;
	
	public ChatGui(final IExternalAccess agent) throws HeadlessException {
		super(agent.getComponentIdentifier().getName());
		
		final JTextField message = new JTextField();
		JButton send = new JButton("Send");
		received = new JTextPane();
		received.setEditable(false);
	    setVisible(false);
	    JScrollPane scrollPane = new JScrollPane(received);
	    JPanel southPanel = new JPanel(new BorderLayout());
	    southPanel.add(BorderLayout.CENTER, message);
	    southPanel.add(BorderLayout.EAST, send);
	      
	    add(BorderLayout.CENTER, scrollPane);
	    add(BorderLayout.SOUTH, southPanel);
	    setSize(600, 400);
	    setVisible(true);
		
	    this.getRootPane().setDefaultButton(send);
	    
		send.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        final String text = message.getText(); 
		        agent.scheduleStep(new IComponentStep<Void>()
		        {
		            public IFuture<Void> execute(IInternalAccess ia)
		            {
		                IFuture<Collection<IChatService>> chatservices = ia.getComponentFeature(IRequiredServicesFeature.class).getRequiredServices("chatservices");
		                chatservices.addResultListener(new DefaultResultListener<Collection<IChatService>>()
		                {
		                    public void resultAvailable(Collection<IChatService> result)
		                    {
		                    	for(IChatService iChatService : result)
		                        {
		                    		if(!text.isEmpty())
		                    		{
			                            iChatService.message(agent.getComponentIdentifier().getLocalName(), text);
			                            message.setText("");
		                    		}
		                        }
		                    }
		                });
		                return IFuture.DONE;
		            }
		        });
		    }
		});
		
		addWindowListener(new WindowAdapter()
		{
		  public void windowClosing(WindowEvent e)
		  {
		    agent.killComponent();
		  }
		});
		
	}

	public void addMessage(String messageAppend, boolean sameAgentWriting) {
	    if(sameAgentWriting) {
	        append(messageAppend+"\n", Color.BLUE);
	    } else {
	        append(messageAppend+"\n", Color.BLACK);
	    }
	}
	public void append(String messageAppend, Color color) {
		Style agentStyle = received.addStyle("agentStyle", null);
        StyleConstants.setForeground(agentStyle, color);
	
		try {
			Document doc = received.getDocument();
			doc.insertString(doc.getLength(), messageAppend, agentStyle);
		} catch(BadLocationException exc) {
			exc.printStackTrace();
		}
	}
}