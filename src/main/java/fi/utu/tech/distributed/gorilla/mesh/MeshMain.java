package fi.utu.tech.distributed.gorilla.mesh;

import java.io.IOException;
import java.net.InetAddress;


public class MeshMain {
	
	/**
	 * Jos käyttäjä antaa vain yhden komentoriviparametrin (portin, jota hän kuuntelee), 
	 * hän on Mesh-verkon alkupiste.
	 * Mikäli käyttä haluaa liittyä olemassa olevaan Mesh-verkkoon, hänen tulee antaa myös
	 * IP-osoite ja portti koneeseen, johon hän haluaa liittyä
	 * @param args
	 */
	public static void main(String[] args) {
		int portForClients = Integer.parseInt(args[0]);
		Mesh mesh = null;
		try {
			mesh = new Mesh(portForClients);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if(args.length==3) {
			try {
				InetAddress address = InetAddress.getByName(args[1]);
				int portForConnection = Integer.parseInt(args[2]);
				mesh.connect(address, portForConnection);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

}
