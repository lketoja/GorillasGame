package fi.utu.tech.distributed.gorilla.mesh;

import java.io.Serializable;

public class Message implements Serializable{
	
	public long token;
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

}
