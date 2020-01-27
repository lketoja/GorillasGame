package fi.utu.tech.distributed.gorilla;

/**
 * This is the main class. In order to launch JavaFX from an IDE, needs to call a different
 * "Application" class (here App). Note, App.launch will block until the GUI application is closed.
 */
public class Main {
    public static void main(String[] args) {
    	App.launch(App.class, args);
//		int portForClients = Integer.parseInt(args[0]);
//		System.out.println("portti asiakkaille on " + portForClients);
//		Mesh mesh = null;
//		try {
//			mesh = new Mesh(portForClients);
//			mesh.start();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
//		if(args.length==3) {
//			try {
//				InetAddress address = InetAddress.getByName(args[1]);
//				int portForConnection = Integer.parseInt(args[2]);
//				System.out.println("Yhdistetään osoitteeseen " + address + " ja siellä porttiin " + portForConnection);
//				mesh.connect(address, portForConnection);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			try {
//				mesh.broadcast(new ChatMessage("sender", "all", "Hello World!"));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			
//		}
		
    }
}