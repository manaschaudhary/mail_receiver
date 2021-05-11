package com.rd.mailreceiver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

public class EmailReceiver extends JFrame {
	
	static Logger logger = Logger.getLogger(EmailReceiver.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3513329135328903166L;

	private JLabel 	lbl_protocol, 
					lbl_host, 
					lbl_port, 
					lbl_userName, 
					lbl_password;
	
	private JTextField 	txt_protocol, 
						txt_host, 
						txt_port, 
						txt_userName, 
						txt_password;
	
	private static JTextArea textArea;
	private JScrollPane jsp;
	
	private JButton button;

	public EmailReceiver() {
		
	    lbl_protocol = new JLabel("Protocol*");
	    lbl_host = new JLabel("Host*");
	    lbl_port = new JLabel("Port*");
	    lbl_userName = new JLabel("Mail Id*");
	    lbl_password = new JLabel("Mail Password*");
	    
	    txt_protocol = new JTextField("imap", 50); 
		txt_host = new JTextField("outlook.office365.com", 50);
		txt_port = new JTextField("993", 50);
		txt_userName = new JTextField(50);
		txt_password = new JPasswordField(50);
	    
		textArea = new JTextArea("output", 10, 50);
		jsp = new JScrollPane(textArea);

		button = new JButton("Fetch Mails");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String protocol = txt_protocol.getText().trim();
				String host = txt_host.getText().trim();
				String port = txt_port.getText().trim();
				String userName = txt_userName.getText().trim();
				String password = txt_password.getText().trim();
				
				if(protocol.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Protocol cannot be blank");
					return;
				}
				
				if(host.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Host cannot be blank");
					return;
				}
				
				if(port.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Port cannot be blank");
					return;
				}
				
				if(userName.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Mail Id cannot be blank");
					return;
				}
				
				if(password.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Mail Password cannot be blank");
					return;
				}
				
				textArea.setText("");
				downloadEmails(protocol, host, port, userName, password);
			}
		});
		
		
		lbl_protocol.setBounds(50,50,100,30);
		txt_protocol.setBounds(50,80,400,30);
		
		lbl_host.setBounds(50,120,100,30);
		txt_host.setBounds(50,150,400,30);
		
		lbl_port.setBounds(50,190,100,30);
		txt_port.setBounds(50,220,400,30);
		
		lbl_userName.setBounds(50,260,100,30);
		txt_userName.setBounds(50,290,400,30);
		
		lbl_password.setBounds(50,330,100,30);
		txt_password.setBounds(50,360,400,30);
		
		jsp.setBounds(50,400,400,100);
		button.setBounds(50,520,100,30);
		
		add(lbl_protocol); add(txt_protocol);
		add(lbl_host); add(txt_host);
		add(lbl_port); add(txt_port);
		add(lbl_userName); add(txt_userName);
		add(lbl_password); add(txt_password);
		add(button); add(jsp);
		
		setLayout(null);  
		setSize(500, 600);
		setVisible(true);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	 


	public String getExecutableNameFromProcessId(String output, String processId) {
		
		logger.info("getExecutableNameFromProcessId:: Output:  " + output);
		
		int startIndex = output.indexOf(processId) + processId.length();
		String tempOutput = output.substring(startIndex);
		int endIndex = tempOutput.indexOf(".jar");
		
		logger.info("Start Index: " + startIndex);
		logger.info("End Index: " + endIndex);
		
		String processName = tempOutput.substring(0, endIndex).trim();
		
		logger.info("Process Name: " + processName);
		
		return processName;
	}


	/**
     * Returns a Properties object which is configured for a POP3/IMAP server
     *
     * @param protocol either "imap" or "pop3"
     * @param host
     * @param port
     * @return a Properties object
     */
    private Properties getServerProperties(String protocol, String host,
            String port) {
        Properties properties = new Properties();
        
        // server setting
        //properties.put(String.format("mail.%s.host", protocol), host);
        //properties.put(String.format("mail.%s.port", protocol), port);
 
        // SSL setting
        properties.setProperty(
                String.format("mail.%s.socketFactory.class", protocol),
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty(
                String.format("mail.%s.socketFactory.fallback", protocol),
                "false");
        properties.setProperty(
                String.format("mail.%s.socketFactory.port", protocol),
                String.valueOf(port));
        
//        properties.setProperty(
//        		String.format("mail.%s.auth.plain.disable",protocol), 
//        		"true");
        
        properties.setProperty("mail.debug", "true");
        
        //TLS Setting
//        properties.put("mail.smtp.auth", "true"); 
//        properties.put("mail.smtp.starttls.enable", "true"); 
        
        
        return properties;
    }
 
    /**
     * Downloads new messages and fetches details for each message.
     * @param protocol
     * @param host
     * @param port
     * @param userName
     * @param password
     */
    public void downloadEmails(String protocol, String host, String port,
            String userName, String password) {
    	
    	StringBuffer sb = null;
        Properties properties = getServerProperties(protocol, host, port);
        Session session = Session.getDefaultInstance(properties);
 
        try {
            // connects to the message store
            Store store = session.getStore(protocol);
            store.connect(host, Integer.parseInt(port), userName, password);
            
            textArea.setText("Connection successful");
 
            // opens the inbox folder
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
            
            // fetches new messages from server
            ReceivedDateTerm term = new ReceivedDateTerm(ComparisonTerm.EQ,new Date() );
            
            Message[] messages = folderInbox.search(term);
            
 
            for (int i = 0; i < 3; i++) {
                Message msg = messages[i];
                
                MimeMessage mimeMsg = (MimeMessage)msg;
                String mimeMsgId = mimeMsg.getMessageID();
                
                Address[] fromAddress = msg.getFrom();
                String from = fromAddress[0].toString();
                String subject = msg.getSubject();
                String toList = parseAddresses(msg
                        .getRecipients(RecipientType.TO));
                String ccList = parseAddresses(msg
                        .getRecipients(RecipientType.CC));
                String sentDate = msg.getSentDate().toString();
 
                String contentType = msg.getContentType();
                String messageContent = "";
 
               /* if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    try {
                        Object content = msg.getContent();
                        if (content != null) {
                            messageContent = content.toString();
                        }
                    } catch (Exception ex) {
                        messageContent = "[Error downloading content]";
                        ex.printStackTrace();
                    }
                } */
 
                // print out details of each message
                sb = new StringBuffer();
                sb = sb.append("Message #" + (i + 1) + ":");
                sb = sb.append("\n From: " + from);
                sb = sb.append("\n To: " + toList);
                sb = sb.append("\n CC: " + ccList);
                sb = sb.append("\n Subject: " + subject);
                sb = sb.append("\n Sent Date: " + sentDate);
                //System.out.println("\t Message: " + messageContent);
                
                textArea.setText(textArea.getText() + "\n\n\n" + sb.toString());
                
                
                ///***************************
                
                ///***************************
                
            }
 
            // disconnect
            folderInbox.close(false);
            store.close();
        } catch (MessagingException ex) {
        	textArea.setText("Connection failure: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        } 
    }
 
    /**
     * Returns a list of addresses in String format separated by comma
     *
     * @param address an array of Address objects
     * @return a string represents a list of addresses
     */
    private String parseAddresses(Address[] address) {
        String listAddress = "";
 
        if (address != null) {
            for (int i = 0; i < address.length; i++) {
                listAddress += address[i].toString() + ", ";
            }
        }
        if (listAddress.length() > 1) {
            listAddress = listAddress.substring(0, listAddress.length() - 2);
        }
 
        return listAddress;
    }
 
    /**
     * Test downloading e-mail messages
     */
    public static void main(String[] args) {
    	System.out.println("Version 1");
        EmailReceiver receiver = new EmailReceiver();
    }
	
}

