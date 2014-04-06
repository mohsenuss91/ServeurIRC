package com.PSI.irc.client;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.PSI.controller.ClientLauncher;
import com.PSI.irc.IfClientServerProtocol;
import com.PSI.irc.server.ServerToClientThread;

public class ClientToServerThread extends Thread implements IfSenderModel{
	
    public static final String BOLD_ITALIC = "BoldItalic";
    public static final String GRAY_PLAIN = "Gray";
    public static final String LABEL_STYLE = "labelstyle";
    public static Boolean launcher = false;
    public static Integer nbUsers = 1;
        
	public static DefaultStyledDocument defaultDocumentModel() {
		DefaultStyledDocument res=new DefaultStyledDocument();
	    
	    Style styleDefault = (Style) res.getStyle(StyleContext.DEFAULT_STYLE);
	    
	    res.addStyle(BOLD_ITALIC, styleDefault);
	    Style styleBI = res.getStyle(BOLD_ITALIC);
	    StyleConstants.setBold(styleBI, true);
	    StyleConstants.setItalic(styleBI, true);
	    StyleConstants.setForeground(styleBI, Color.black);	 
	    StyleConstants.setFontSize(styleBI, 13);
	    

	    res.addStyle(GRAY_PLAIN, styleDefault);
        Style styleGP = res.getStyle(GRAY_PLAIN);
        StyleConstants.setBold(styleGP, false);
        StyleConstants.setItalic(styleGP, false);
        StyleConstants.setForeground(styleGP, Color.DARK_GRAY);
        StyleConstants.setFontSize(styleGP, 11);
        
        res.addStyle(LABEL_STYLE, styleDefault);
		Style labelStyle = res.getStyle(LABEL_STYLE);
	    Icon icon = new ImageIcon(ClientToServerThread.class.getResource("/user_min.png"));
	    JLabel label = new JLabel(icon);
	    StyleConstants.setComponent(labelStyle, label);

		return res;
	}

	private Socket socket = null;
	private DataOutputStream streamOut = null;
	private DataInputStream streamIn = null;
	private BufferedReader console = null;
	public String login,pwd;
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
	
	public static synchronized void playSound(final String url) {
		  Thread t = new Thread(new Runnable() {
		  // The wrapper thread is unnecessary, unless it blocks on the
		  // Clip finishing; see comments.
		    public void run() {
		      try {
		    	Clip clip = AudioSystem.getClip();
		        AudioInputStream inputStream = AudioSystem.getAudioInputStream(
		        ServerToClientThread.class.getResourceAsStream("/" +url + "_modif.wav"));
		        
		        clip.open(inputStream);
		        clip.start(); 
		      } catch (Exception e) {
		    	  e.printStackTrace();
		      }
		    }
		  });
		  t.setDaemon(true);
		  t.start();
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
	
	public void receiveMessage(String user, String line, Document doc){

		Style styleBI = ((StyledDocument)this.defaultDocumentModel()).getStyle(BOLD_ITALIC);
        Style styleGP = ((StyledDocument)this.defaultDocumentModel()).getStyle(GRAY_PLAIN);
        Style labelStyle = ((StyledDocument)this.defaultDocumentModel()).getStyle(LABEL_STYLE);
        receiveMessage(user, line, styleBI, styleGP, labelStyle,doc);
	}
	
	public void receiveMessage(String user, String line, Style styleBI,
			Style styleGP, Style iconStyle, Document doc) {
        try {        
        	if(ClientLauncher.tabSelected != -1){
        	StyledDocument writeDocumentModel = ClientLauncher.documentModel;
        	if(ClientLauncher.tabSelected > 0) writeDocumentModel = (StyledDocument) doc;
        	
        	writeDocumentModel.insertString(writeDocumentModel.getLength(), " ", iconStyle);
        	writeDocumentModel.insertString(writeDocumentModel.getLength(), "  "+user+" : ", styleBI);
        	writeDocumentModel.insertString(writeDocumentModel.getLength(), line+"\n\n", styleGP);
        	}
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}				        	
	}
    
	void readMsg() throws IOException{
		String line = streamIn.readUTF();
		System.out.println("ReadMsg : " + line);
		
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
				{
					playSound("BOP");
					ClientLauncher.frame.getTabbedPane().setBackgroundAt(i, new Color(255,160,0));
				}
			}
			receiveMessage(userMsg[0], userMsg[1], document);
		}
		else if(line.startsWith(IfClientServerProtocol.ADD)){
			
			String[] userPlace = line.split(IfClientServerProtocol.SEPARATOR);
			if(userPlace.length > 2){
			if(!clientListModel.contains(userPlace[2]) && ClientLauncher.SalonName.startsWith(userPlace[3]) && !login.equals(userPlace[2]) ){
				playSound("lawardine");
				nbUsers++;
				ClientLauncher.frame.getNbusers().setText(nbUsers.toString());
				clientListModel.addElement(userPlace[2]);
				receiveMessage(userPlace[2], " entre dans le salon...",documentModel);
			}
			}
		}
		else if(line.startsWith(IfClientServerProtocol.DEL)){
			String delUser=line.substring(IfClientServerProtocol.DEL.length());
			String[] delUsers = delUser.split(IfClientServerProtocol.SEPARATOR);
			String user = delUsers[0];
			String salon = delUsers[1];
			System.out.println("del user : " + delUsers[0]);
			if(clientListModel.contains(user)){
				ClientLauncher.frame.getList().setSelectedValue(null, false);
				nbUsers--;
				ClientLauncher.frame.getNbusers().setText(nbUsers.toString());
				clientListModel.removeElement(user);
				ClientLauncher.listDocuments.remove(user);
				for (int i = 0; i < ClientLauncher.frame.getTabbedPane().getTabCount(); i++) {
					System.out.println("Title : " + ClientLauncher.frame.getTabbedPane().getTitleAt(i) + " - " + user);
					
					if(ClientLauncher.frame.getTabbedPane().getTitleAt(i).startsWith(delUsers[0])){
						System.out.println("DelUser on TabbedPane");
						ClientLauncher.frame.getTabbedPane().setSelectedIndex(0);
						ClientLauncher.frame.getTabbedPane().remove(i);
						ClientLauncher.frame.getTabbedPane().repaint();
					}
						
				}
				
				receiveMessage(user, " quitte le salon !", documentModel);
			}
		}
		else{
			
			String newLine = line.substring(IfClientServerProtocol.Salon.length());
			String[] userMsg=newLine.split(IfClientServerProtocol.SEPARATOR);
			String user=userMsg[0];
			if(userMsg[1].startsWith(ClientLauncher.SalonName))
				receiveMessage(user, userMsg[2],documentModel);
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
			if(ClientLauncher.frame.getTabbedPane().getTitleAt(ClientLauncher.tabSelected).startsWith("Salon")){
			System.out.println("SendMsg : #S#"+login+"#"+ClientLauncher.SalonName+"#"+msgToSend);
			streamOut.writeUTF("#S#"+login+"#"+ClientLauncher.SalonName+"#"+msgToSend);
			}
			else{
			streamOut.writeUTF(IfClientServerProtocol.Whispers+login+"#"+ClientLauncher.frame.getTabbedPane().getTitleAt(ClientLauncher.tabSelected)+"#"+msgToSend);
			StyledDocument doc = ClientLauncher.listDocuments.get(ClientLauncher.frame.getTabbedPane().getTitleAt(ClientLauncher.tabSelected));
			receiveMessage(login, msgToSend, doc);
			//receiveMessage(login, msgToSend);
			}msgToSend=null;
		    streamOut.flush();
		    res=true;
		}
		return res;
	}
	
	public void ChangeSalon(String BeforeSalon, String NextSalon){
		try {
			streamOut.writeUTF(IfClientServerProtocol.RemoveFromSalon+login+"#"+BeforeSalon);
			Thread.sleep(100);
			streamOut.writeUTF(IfClientServerProtocol.AddToSalon+login+"#"+NextSalon);
			nbUsers = 1;
			ClientLauncher.frame.getNbusers().setText(nbUsers.toString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void quitServer() throws IOException{

		try {
			playSound("adishatz");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		streamOut.writeUTF(IfClientServerProtocol.DEL+login+"#"+ClientLauncher.SalonName);
		System.out.println("QuitServer : " + IfClientServerProtocol.DEL+login+"#"+ClientLauncher.SalonName);
		streamOut.flush();
		done=true;
	}
	
	boolean done;
	@Override
	public void run() {
		try {
			open();
			done = !authentification();
			System.out.println("Authentification : " + done);
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
			e.printStackTrace();
			res=false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return res;		
	}
	
}

