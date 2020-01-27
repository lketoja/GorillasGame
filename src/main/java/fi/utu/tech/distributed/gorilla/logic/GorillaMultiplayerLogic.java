package fi.utu.tech.distributed.gorilla.logic;

import java.io.IOException;
import java.net.InetAddress;

import fi.utu.tech.distributed.gorilla.mesh.Mesh;

public class GorillaMultiplayerLogic extends GorillaLogic{
	
	private Mesh mesh;
	
	
	
	/**
     * Start the mesh server on the specified port
     * @param port The port the mesh should listen to for new nodes
     */
	@Override
    protected void startServer(String port) {
        System.out.println("Starting server at port " + port);
        // ...or at least somebody should be
		try {
			mesh = new Mesh(Integer.parseInt(port));
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
        System.out.printf("Connecting to server at %s%n", address, port);
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
	protected void handleChatMessage(ChatMessage msg) {
       System.out.printf("Sinä sanot: %s%n", msg.contents);
       try {
			mesh.broadcast(msg);
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
    }
	
	

}
