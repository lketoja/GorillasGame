package fi.utu.tech.distributed.gorilla.logic;

import java.io.IOException;
import java.io.Serializable;

import fi.utu.tech.distributed.gorilla.logic.GorillaLogic;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import fi.utu.tech.distributed.gorilla.mesh.Mesh;
import fi.utu.tech.distributed.gorilla.mesh.Message;
import javafx.application.Platform;

public class GorillaMultiplayerLogic extends GorillaLogic{
	
	private Mesh mesh;
	//Tämä hashmap tarvitaan GameConfigurationin konstruktoria varten. Ei käytetä muualla.
	private Map<Long, String> playerId_Name = new HashMap<Long, String>();
	private List<Long> playerIds = new ArrayList<>();
	private GameConfiguration configuration;
	public final int NUMBER_OF_PLAYERS = 2;
		
	/**
     * Start the mesh server on the specified port
     * @param port The port the mesh should listen to for new nodes
     */
	@Override
    protected void startServer(String port) {
        //System.out.println("Starting server at port " + port);
        // ...or at least somebody should be
		try {
			mesh = new Mesh(Integer.parseInt(port), this);
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
       mesh.getSeenMessages().add(message.token);
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
        System.out.println("Metodi handleMultiplayer()");
        
	        try {
	        	//Lähettää muille pelaajille tiedon siitä, että tämä node toimii hostina. Muut pelaajat
	        	//reagoivat tähän lähettämällä viestin MeshCoummunication.Join, johon reagoidaan 
	        	//tallentamalla pelaajan id
	        	sendMessage(MeshCommunication.Host);
	        	
	        	//Lähetetään muille pelajille tämän noden id.
	        	sendMessage(MeshCommunication.Join);
	        	
	        	playerIds.add(mesh.meshId);
	        	
	        	sendChatMessage("I'm hosting the game!");

			} catch (Exception e) {
				e.printStackTrace();
			}
    }
	
	
	protected void initGame() {
		System.out.println("Metodia initGame() kutsuttu");
		Collections.sort(playerIds);
		System.out.println(playerIds);
		
        List<Player> players = new ArrayList<>();
        
        Player me = null;
        
        //public Player(String name, LinkedBlockingQueue<Move> moves, boolean local)
        for(Long playerId : playerIds) {
        	if(playerId == mesh.meshId) {
        		me = new Player(myName, new LinkedBlockingQueue<>(), true);
        		players.add(me);
        	}else {
        		players.add(new Player(null, new LinkedBlockingQueue<>(), false));
        	}
        }

        //public GameState(GameConfiguration configuration, List<Player> players, Player me)
        gameState = new GameState(configuration, players, me);
        
        views.setGameState(gameState);
    }
	
	private void sendChatMessage(String text) throws IOException {
		ChatMessage chatMessage = new ChatMessage(myName, "all", text);
        Message message = new Message(mesh.meshId, 0L, chatMessage);
        mesh.getSeenMessages().add(message.token);
        mesh.broadcast(message);
	}
	
	private void sendMessage(Serializable contents) throws IOException {
        Message message = new Message(mesh.meshId, 0L, contents);
        mesh.getSeenMessages().add(message.token);
        mesh.broadcast(message);
	}
	
	/**
     * Handles banana throwing. This event is usually fired by angle and velocity commands
     * @param mtb
     */
	@Override
    protected void handleThrowBanana(MoveThrowBanana mtb) {
        gameState.addLocalPlayerMove(mtb);
        try {
			sendMessage(mtb);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public void parseServer(Message message){
		Platform.runLater(new Runnable() {
			@Override public void run() {
		
				if(message.contents instanceof MeshCommunication) {
					MeshCommunication meshComm = (MeshCommunication)message.contents;
					switch(meshComm) {
						case Host:
							playerIds.add(mesh.meshId);
							try {
								sendMessage(MeshCommunication.Join);
								System.out.println("Received message 'host', sent message 'join'");
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case Join:
							System.out.println("Received message 'join' from " + message.sender);
							playerId_Name.put(message.sender, null);
							playerIds.add(message.sender);
							if(playerIds.size()==NUMBER_OF_PLAYERS) {
								double h = getCanvas().getHeight();
						        
						        //public GameConfiguration(long seed, double gameWorldHeight, Map<Long, String> playerIdNames)
						        GameConfiguration configuration = new GameConfiguration(gameSeed, h, playerId_Name);
						        
						        try {
									sendMessage(configuration);
								} catch (IOException e) {
									e.printStackTrace();
								}
						        
						        //setMode kutsuu metodia initGame()
						        setMode(GameMode.Game); 
							}
							break;
					}
				}
				
				if(message.contents instanceof ChatMessage) {
					ChatMessage chatMessage = (ChatMessage)message.contents;
					String text = chatMessage.contents;
					System.out.println("received chat message: " + text);
				}
				
				if(message.contents instanceof GameConfiguration) {
					GameConfiguration config = (GameConfiguration)message.contents;
					configuration = config;
					//setMode kutsuu metodia initGame()
			        setMode(GameMode.Game); 
				}
				
				if(message.contents instanceof Move) {
					Move move = (Move)message.contents;
					Long sender = message.sender;
					int index = playerIds.indexOf(sender);
					Player player = gameState.getPlayers().get(index);
					player.moves.add(move);
					System.out.println("lisätty move pelaajalle " + sender);
				}
			}
		});
	}
	
	


}
