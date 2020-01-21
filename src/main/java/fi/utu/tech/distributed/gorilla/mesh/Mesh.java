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

public class Mesh {
	
	public long identifier;
	private ArrayList<ObjectOutputStream> clientOutStreams;
	private ObjectOutputStream serverOutStream;
	private ArrayList<Long> seenMessages;
	
	
	/**
     * Luo Mesh-palvelininstanssi
     * @param port Portti, jossa uusien vertaisten liittymispyyntöjä kuunnellaan
	 * @throws IOException 
     */
    public Mesh(int port) throws IOException {
    	clientOutStreams = new ArrayList<>();
    	ServerSocket ss = new ServerSocket(port);
    	while(true) {
    		Socket cs = ss.accept();
    		System.out.println("Connection from " + cs.getInetAddress() + "port " + cs.getPort());
    		new Handler(cs).start();
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
    			addStreamToMesh(oOut);
    			ObjectInputStream oIn = new ObjectInputStream(is);
    			try {
    				while(true) {
    					Message message = (Message)oIn.readObject();
    					System.out.println(message.getText());
    					if(!seenMessages.contains(message.token)) {
    						seenMessages.add(message.token);
    						long recipient = message.getRecipient();
        					if(recipient == 0L) {
        						broadcast(message);
        					} else {
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
    
    public void addStreamToMesh(ObjectOutputStream oOut) {
    	clientOutStreams.add(oOut);    	
    }

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
    private void addToken(long token);

    /**
     * Tarkista, onko viestitunniste jo olemassa
     * Määreenä private, koska tätä käyttävä luokka on sisäluokka (inner class)
     * Jos et käytä sisäluokkaa, pitää olla public
     * @param token Viestitunniste 
     */
    private boolean tokenExists(long token);

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
    		System.out.println(message.getText());
    	}
 
    }


}
