package fi.utu.tech.distributed.gorilla.mesh;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Message implements Serializable{
	
	private ArrayList<Long> tokens = new ArrayList<>();
	private String text;
	private long recipient=0L; //default arvo 0 tarkoittaa kaikille suunnattua viestiä
	
	//viesti tietylle vastaanottajalle
	public Message(String text, long recipient) {
		this.text=text;
		this.recipient=recipient;
	}
	
	//viesti kaikille
	public Message(String text) {
		this.text=text;
	}
	
	public String getText() {
		return text;
	}
	
	public long getRecipient() {
		return recipient;
	}
	
	public ArrayList<Long> getTokens(){
		return tokens;
	}

}
