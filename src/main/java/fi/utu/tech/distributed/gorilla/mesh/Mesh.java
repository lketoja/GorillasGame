package fi.utu.tech.distributed.gorilla.mesh;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Mesh {
	
	final long identifier =new Random().nextInt(1000000);
	private ArrayList<ObjectOutputStream> clientOutStreams;
	private ObjectOutputStream serverOutStream;
	
	
	/**
     * Luo Mesh-palvelininstanssi
     * @param port Portti, jossa uusien vertaisten liittymispyyntöjä kuunnellaan
	 * @throws IOException 
     */
    public Mesh(int port) throws IOException {
    	clientOutStreams = new ArrayList<>();
    	ServerSocket ss = new ServerSocket(port);
    	new ServerThread(ss).start();
    	System.out.println("Server started.");	
    }
    
    private class ServerThread extends Thread {
    	private ServerSocket ss;
    	
    	public ServerThread(ServerSocket ss) {
    		this.ss = ss;
    	}
    	
    	public void run() {
    		while(true) {
				try {
					Socket cs = ss.accept();
					System.out.println("Connection from " + cs.getInetAddress() + "port " + cs.getPort());
	        		new Handler(cs).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
        		
        	}
    	}
    	
    }
    
    private class Handler extends Thread {
    	private Socket client;
    	
    	public Handler(Socket clientSocket) {
    		client = clientSocket;
    	}
    	
    	/**
         *  Käynnistä uusien vertaisten kuuntelusäie
         */
    	public void run() {
    		try {
    			InputStream is = client.getInputStream();
    			OutputStream os = client.getOutputStream();
    			ObjectOutputStream oOut = new ObjectOutputStream(os);
    			clientOutStreams.add(oOut);
    			ObjectInputStream oIn = new ObjectInputStream(is);
    			try {
    				while(true) {
    					Message message = (Message)oIn.readObject();
    					
    					
    					if(!tokenExists(message)) {
    						addToken(message);
    						long recipient = message.getRecipient();
        					if(recipient == 0L) {
        						System.out.println("Received message for everyone (from a client): " + message.getText());
        						broadcast(message);
        					} else if(recipient == identifier){
        						System.out.println("Received message for us (from a client): " + message.getText());
        					} else {
        						System.out.println("Received message meant for someone else (from a client). "
        								+ "Sending it forward...");
        						send(message, recipient);
        					}
    						
    					}
    				}
    			} catch(IOException e) {
    				client.close();
    			}
    			
    		}catch(Exception e){
    			throw new Error(e.toString());
    			
    		}
    	}//run
    }//class Handler
    

    /**
     * Lähetä hyötykuorma kaikille vastaanottajille
     * @param o Lähetettävä hyötykuorma
     * @throws IOException 
     */
    public void broadcast(Serializable o) throws IOException {
    	for(ObjectOutputStream oOut : clientOutStreams) {
    		oOut.writeObject(o);
    		oOut.flush();
    	}
    	serverOutStream.writeObject(o);
    	System.out.println("Broadcast: " + o.toString());
    }

    /**
     * Lähetä hyötykuorma valitulle vertaiselle
     * @param o Lähetettävä hyötykuorma
     * @param recipient Vastaanottavan vertaisen tunnus
     */
    public void send(Serializable o, long recipient);

    /**
     * Sulje mesh-palvelin ja kaikki sen yhteydet 
     */
    public void close();

    /**
     * Lisää token, eli "viestitunniste"
     * Käytännössä merkkaa viestin tällä tunnisteella luetuksi
     * Määreenä private, koska tätä käyttävä luokka on sisäluokka (inner class)
     * Jos et käytä sisäluokkaa, pitää olla public
     * @param token Viestitunniste 
     */
    private void addToken(Message message) {
    	message.getTokens().add(this.identifier);
    }

    /**
     * Tarkista, onko viestitunniste jo olemassa
     * Määreenä private, koska tätä käyttävä luokka on sisäluokka (inner class)
     * Jos et käytä sisäluokkaa, pitää olla public
     * @param token Viestitunniste 
     */
    private boolean tokenExists(Message message) {
    	if(message.getTokens().contains(this.identifier)) {
    		return true;
    	}
    	return false;
    }

    /**
     * Yhdistä tämä vertainen olemassaolevaan Mesh-verkkoon
     * @param addr Solmun ip-osoite, johon yhdistetään
     * @param port Portti, jota vastapuolinen solmu kuuntelee
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public void connect(InetAddress addr, int port) throws IOException, ClassNotFoundException {
    	Socket s = new Socket(addr, port);
    	System.out.println("Connection made");
    	InputStream is = s.getInputStream();
    	OutputStream os = s.getOutputStream();
    	serverOutStream = new ObjectOutputStream(os);
    	ObjectInputStream oIn = new ObjectInputStream(is);
    	while(true) {
    		Message message = (Message)oIn.readObject();
			if(!tokenExists(message)) {
				addToken(message);
				long recipient = message.getRecipient();
				if(recipient == 0L) {
					System.out.println("Received message for everyone (from a server): " + message.getText());
					broadcast(message);
				} else if(recipient == identifier){
					System.out.println("Received message for us (from a server): " + message.getText());
				} else {
					System.out.println("Received message meant for someone else (from a server). "
							+ "Sending it forward...");
					send(message, recipient);
				}
				
			}
    	}
 
    }


}
