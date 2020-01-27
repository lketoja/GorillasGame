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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import fi.utu.tech.distributed.gorilla.logic.ChatMessage;

public class Mesh extends Thread{
	
	public final long meshId = new Random().nextLong();
	private List<Handler> handlers= Collections.synchronizedList(new ArrayList<Handler>());
	private ServerSocket ss;
	private int serverPort;
	private List<Long> seenMessages = Collections.synchronizedList(new ArrayList<Long>());
	
	
	/**
     * Luo Mesh-palvelininstanssi
     * @param port Portti, jossa uusien vertaisten liittymispyyntöjä kuunnellaan
	 * @throws IOException 
     */
    public Mesh(int port) throws IOException {
    	serverPort = port;
    	ss = new ServerSocket(port);
    	System.out.println("New Mesh created.");
    }
    
    /**
     *  Käynnistä uusien vertaisten kuuntelusäie
     */
    public void run() {
    	System.out.println("Server started. Running on port " + serverPort);
    	while(true) {
			try {
				Socket cs = ss.accept();
				System.out.println("Connection from " + cs.getInetAddress() + "port " + cs.getPort());
				Handler handler = new Handler(cs);
				handlers.add(handler);
        		handler.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    /**
     * Lähetä hyötykuorma kaikille vastaanottajille
     * @param o Lähetettävä hyötykuorma
     * @throws IOException 
     */
    public void broadcast(Serializable o) throws IOException {
    	synchronized (handlers) {
    		for(Handler handler : handlers) {
        		handler.send(o);
        	}
    	}    	
    }

    /**
     * Lähetä hyötykuorma valitulle vertaiselle
     * @param o Lähetettävä hyötykuorma
     * @param recipient Vastaanottavan vertaisen tunnus
     */
    public void send(Serializable o, long recipient) {}

    /**
     * Sulje mesh-palvelin ja kaikki sen yhteydet 
     */
    public void close() {
    	for(Handler handler : handlers) {
    		try {
				handler.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }

    /**
     * Lisää token, eli "viestitunniste"
     * Käytännössä merkkaa viestin tällä tunnisteella luetuksi
     * Määreenä private, koska tätä käyttävä luokka on sisäluokka (inner class)
     * Jos et käytä sisäluokkaa, pitää olla public
     * @param token Viestitunniste 
     */
    private void addToken(long token) {
    	seenMessages.add(token);
    }

    /**
     * Tarkista, onko viestitunniste jo olemassa
     * Määreenä private, koska tätä käyttävä luokka on sisäluokka (inner class)
     * Jos et käytä sisäluokkaa, pitää olla public
     * @param token Viestitunniste 
     */
    private boolean tokenExists(long token) {
    	if(seenMessages.contains(token)) {
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
    	System.out.println("Metodissa connect: soketti luotu");
		Handler handler = new Handler(s);
    	System.out.println("Metodissa connect: handler luotu");
		handlers.add(handler);
		handler.start();
    }
    
    private class Handler extends Thread {
    	private Socket socket;
    	ObjectOutputStream oOut;
    	ObjectInputStream oIn;
    	
    	public Handler(Socket socket) throws IOException {
    		this.socket = socket;
    		oIn = new ObjectInputStream(socket.getInputStream());
    		oOut = new ObjectOutputStream(socket.getOutputStream());
    		
    	}
    	
    
    	public void run() {
    			try {
    				while(true) {
    					Message message = (Message)oIn.readObject();    					
    					if(!tokenExists(message.token)) {
    						addToken(message.token);
    						long recipient = message.recipient;
        					if(recipient==0L) {
        						System.out.println("Received message for everyone: " + message.contents);
        						broadcast(message);
        					} else if(recipient==meshId){
        						System.out.println("Received message for us: " + message.contents);
        					} else {
        						System.out.println("Received message meant for someone else. "
        								+ "Sending it forward...");
        						broadcast(message);
        					}
    					}
    				}
    			} catch(Exception e) {
    				e.printStackTrace();
    			}	
    		}//run
    	
    	
    	//siirrä tänne broadcast metodin logiikka
    	public void send(Serializable o) {
    		try {
				oOut.writeObject(o);
				oOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	public void close() throws IOException {
    		oOut.close();
    		oIn.close();
    	}
    	
    }//class Handler


}
