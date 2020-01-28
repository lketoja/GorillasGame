package fi.utu.tech.distributed.gorilla.logic;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import fi.utu.tech.distributed.gorilla.mesh.Mesh;
import fi.utu.tech.distributed.gorilla.mesh.Message;

public class GorillaMultiplayerLogic extends GorillaLogic{
	
	private Mesh mesh;
	
	
	
	/**
     * Start the mesh server on the specified port
     * @param port The port the mesh should listen to for new nodes
     */
	@Override
    protected void startServer(String port) {
        //System.out.println("Starting server at port " + port);
        // ...or at least somebody should be
		try {
			mesh = new Mesh(Integer.parseInt(port));
			mesh.start();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }

    /**
     * Connect the Mesh into an existing mesh
     * @param address The IP address of the mesh node to connect to
     * @param port The listening port of the mesh node to connect to
     */
	@Override
    protected void connectToServer(String address, String port) {
        //System.out.printf("Connecting to server at %s%n", address, port);
        // ...or at least somebody should be
        try {
			InetAddress inetAddress = InetAddress.getByName(address);
			
			mesh.connect(inetAddress, Integer.parseInt(port));

		} catch (NumberFormatException | ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	
	/**
    * Handles message sending. Usually fired by "say" command
    * @param msg Chat message object containing the message and other information
    */
	@Override
	protected void handleChatMessage(ChatMessage chatMessage) {
       System.out.printf("Sinä sanot: %s%n", chatMessage.contents);
       Message message = new Message(mesh.meshId, 0L, chatMessage);
       try {
			mesh.broadcast(message);
       } catch (IOException e) {
			e.printStackTrace();
       }
	}
	
	/**
     * Handles starting a multiplayer game. This event is usually fired by selecting
     * Palvelinyhteys in game menu
     */
	@Override
    protected void handleMultiplayer() {
        System.out.println("Not implemented on this logic");
        
      //kutsuu metodia initGame(), joten tämä pitäisi tehdä vasta kun kaikki pelaajat mukana?
        //Odotetaan tietty aika?
        //Kaikki lähettävät viestin "liityn <nimi> <id>"? Ja lisätää pelaajat listaan "otherPlayers"?
        setMode(GameMode.Game); 
    }
	
	@Override
	private void initGame() {
        double h = getCanvas().getHeight();

        //Tässä pitäisi tallentaa pelaajat
        // Create maxPlayers-1 AI players
        for (int i=1; i<maxPlayers; i++) {
            joinGame("Kingkong " + i);
        }
        
        //Nimilista lisätään configurationiin. Miksi?
        List<String> names = new LinkedList<>();
        names.add(myName);
        for (Player player : otherPlayers) names.add(player.name);
        
        //tässä pitäisi kutsua toista konstruktoria
        GameConfiguration configuration = new GameConfiguration(gameSeed, h, names);

        //tässä pitäisi ilmeisesti kutsua toista konstruktoria?
        gameState = new GameState(configuration, myName, new LinkedBlockingQueue<>(), otherPlayers);
        views.setGameState(gameState);
    }
	
	

}
