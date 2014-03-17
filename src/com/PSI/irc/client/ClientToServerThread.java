package com.PSI.irc.client;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.PSI.controller.ClientLauncher;
import com.PSI.irc.IfClientServerProtocol;

public class ClientToServerThread extends Thread implements IfSenderModel{
	
    public static final String BOLD_ITALIC = "BoldItalic";
    public static final String GRAY_PLAIN = "Gray";
    public static Boolean launcher = false;
        
	public static DefaultStyledDocument defaultDocumentModel() {
		DefaultStyledDocument res=new DefaultStyledDocument();
	    
	    Style styleDefault = (Style) res.getStyle(StyleContext.DEFAULT_STYLE);
	    
	    res.addStyle(BOLD_ITALIC, styleDefault);
	    Style styleBI = res.getStyle(BOLD_ITALIC);
	    StyleConstants.setBold(styleBI, true);
	    StyleConstants.setItalic(styleBI, true);
	    StyleConstants.setForeground(styleBI, Color.black);	    

	    res.addStyle(GRAY_PLAIN, styleDefault);
        Style styleGP = res.getStyle(GRAY_PLAIN);
        StyleConstants.setBold(styleGP, false);
        StyleConstants.setItalic(styleGP, false);
        StyleConstants.setForeground(styleGP, Color.lightGray);

		return res;
	}

	private Socket socket = null;
	private DataOutputStream streamOut = null;
	private DataInputStream streamIn = null;
	private BufferedReader console = null;
	String login,pwd;
	DefaultListModel<String> clientListModel;
	StyledDocument documentModel;
	
	public ClientToServerThread(StyledDocument documentModel, DefaultListModel<String> clientListModel, Socket socket, String login, String pwd) {
		super();
		this.documentModel=documentModel;
		this.clientListModel=clientListModel;
		this.socket = socket;
		this.login=login;
		this.pwd=pwd;
	}
	
	public void open() throws IOException {
		console = new BufferedReader(new InputStreamReader(System.in));
		streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		streamOut = new DataOutputStream(socket.getOutputStream());
	}
	public void close() throws IOException {
		if (socket != null)
			socket.close();
		if (streamIn != null)
			streamIn.close();
		if (streamOut != null)
			streamOut.close();
	}
	
	public void receiveMessage(String user, String line){
		Style styleBI = ((StyledDocument)documentModel).getStyle(BOLD_ITALIC);
        Style styleGP = ((StyledDocument)documentModel).getStyle(GRAY_PLAIN);
        receiveMessage(user, line, styleBI, styleGP);
	}
	
	public void receiveMessage(String user, String line, Style styleBI,
			Style styleGP) {
        try {        
        	if(ClientLauncher.tabSelected != -1){
        	documentModel = ClientLauncher.listDocuments.get("Salon Principal");
			documentModel.insertString(documentModel.getLength(), user+" : ", styleBI);
			documentModel.insertString(documentModel.getLength(), line+"\n", styleGP);
        	}
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}				        	
	}
    
	void readMsg() throws IOException{
		String line = streamIn.readUTF();
		System.out.println(line);
		System.out.println(line);
		
		if(line.startsWith(IfClientServerProtocol.Whispers))
		{
			
			String newLine =line.substring(IfClientServerProtocol.Whispers.length());
			String[] userMsg=newLine.split(IfClientServerProtocol.SEPARATOR);
			StyledDocument document = null;
			if(ClientLauncher.listDocuments.get(userMsg[0]) != null)
				document = ClientLauncher.listDocuments.get(userMsg[0]);
			else{
				document = new DefaultStyledDocument();
				ClientLauncher.listDocuments.put(userMsg[0],document);
				ClientLauncher.frame.AddPrivateUserTab(userMsg[0], ClientLauncher.listDocuments.get(userMsg[0]));
			}
			
			for (int i = 0; i < ClientLauncher.frame.getTabbedPane().countComponents(); i++) {
				if(ClientLauncher.frame.getTabbedPane().getTitleAt(i).equals(userMsg[0]) && ClientLauncher.tabSelected != i)
					ClientLauncher.frame.getTabbedPane().setBackgroundAt(i, new Color(255,160,0));
			}
			
			Style styleBI = ((StyledDocument)document).getStyle(BOLD_ITALIC);
	        Style styleGP = ((StyledDocument)document).getStyle(GRAY_PLAIN);
			try {
				System.out.println(userMsg.length);
				document.insertString(document.getLength(), userMsg[0]+" : ", styleBI);
				document.insertString(document.getLength(), userMsg[1]+"\n", styleGP);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}	
		}
		else if(line.startsWith(IfClientServerProtocol.ADD)){
			String newUser=line.substring(IfClientServerProtocol.ADD.length());
			if(!clientListModel.contains(newUser)){
				clientListModel.addElement(newUser);
				receiveMessage(newUser, " entre dans le salon...");
			}
		}
		else if(line.startsWith(IfClientServerProtocol.DEL)){
			String delUser=line.substring(IfClientServerProtocol.DEL.length());
			if(clientListModel.contains(delUser)){
				ClientLauncher.frame.getList().setSelectedValue(null, false);
				clientListModel.removeElement(delUser);
				for (int i = 0; i < ClientLauncher.frame.getTabbedPane().getTabCount(); i++) {
					System.out.println("Title : " + ClientLauncher.frame.getTabbedPane().getTitleAt(i) + " - " + delUser);
					
					if(ClientLauncher.frame.getTabbedPane().getTitleAt(i).startsWith(delUser)){
						System.out.println("DelUser on TabbedPane");
						ClientLauncher.frame.getTabbedPane().setSelectedIndex(0);
						ClientLauncher.frame.getTabbedPane().remove(i);
						ClientLauncher.frame.getTabbedPane().repaint();
					}
						
				}
				receiveMessage(delUser, " quitte le salon !");
			}
		}
		else{
			String[] userMsg=line.split(IfClientServerProtocol.SEPARATOR);
			String user=userMsg[1];
			receiveMessage(user, userMsg[2]);
		}
	}
	
	String msgToSend=null;
	
	/* (non-Javadoc)
	 * @see com.cfranc.irc.client.IfSenderModel#setMsgToSend(java.lang.String)
	 */
	@Override
	public void setMsgToSend(String msgToSend) throws IOException {
		this.msgToSend = msgToSend;
		sendMsg();
	}

	private boolean sendMsg() throws IOException{
		boolean res=false;
		if(msgToSend!=null){
			if(ClientLauncher.frame.getTabbedPane().getTitleAt(ClientLauncher.tabSelected).startsWith("Salon"))
			streamOut.writeUTF("#"+login+"#"+msgToSend);
			else{
			streamOut.writeUTF(IfClientServerProtocol.Whispers+login+"#"+ClientLauncher.frame.getTabbedPane().getTitleAt(ClientLauncher.tabSelected)+"#"+msgToSend);
			StyledDocument doc = ClientLauncher.listDocuments.get(ClientLauncher.frame.getTabbedPane().getTitleAt(ClientLauncher.tabSelected));
			Style styleBI = ((StyledDocument)doc).getStyle(BOLD_ITALIC);
	        Style styleGP = ((StyledDocument)doc).getStyle(GRAY_PLAIN);
			try {
				doc.insertString(doc.getLength(), login+" : ", styleBI);
				doc.insertString(doc.getLength(), msgToSend+"\n", styleGP);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}	
			//receiveMessage(login, msgToSend);
			}msgToSend=null;
		    streamOut.flush();
		    res=true;
		}
		return res;
	}
	
	public void quitServer() throws IOException{
		streamOut.writeUTF(IfClientServerProtocol.DEL+login);
		System.out.println("QuitServer : " + IfClientServerProtocol.DEL+login);
		streamOut.flush();
		done=true;
	}
	
	boolean done;
	@Override
	public void run() {
		try {
			open();
			done = !authentification();
			System.out.println(done);
			if(!done && !launcher)
			{
				ClientLauncher.launchClient(this);
				launcher = true;
			}
			else
			{
				ClientLauncher.error = true;
				ClientLauncher.main(null);
			}
			
			while (!done) {
				try {
					
					if(streamIn.available()>0){
						readMsg();
					}


					if(!sendMsg()){
						Thread.sleep(100);
					}
				} 
				catch (IOException | InterruptedException ioe) {
					ioe.printStackTrace();
					done = true;
				}
			}
			close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean authentification() {
		boolean res=false;
		String loginPwdQ;
		try {
			while(streamIn.available()<=0){
				Thread.sleep(100);
			}
			loginPwdQ = streamIn.readUTF();
			if(loginPwdQ.equals(IfClientServerProtocol.LOGIN_PWD)){
				streamOut.writeUTF(IfClientServerProtocol.SEPARATOR+this.login+IfClientServerProtocol.SEPARATOR+this.pwd);
			}
			while(streamIn.available()<=0){
				Thread.sleep(100);
			}
			String acq=streamIn.readUTF();
			if(acq.equals(IfClientServerProtocol.OK)){
				res=true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res=false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;		
	}
	
}

