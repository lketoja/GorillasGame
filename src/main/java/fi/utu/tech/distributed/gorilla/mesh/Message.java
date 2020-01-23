package fi.utu.tech.distributed.gorilla.mesh;

import java.io.Serializable;
import java.util.Random;

public class Message implements Serializable{
	
	//vain yksi token
	private long token = new Random().nextLong();
	private String text;
	private long recipient=0L; //default arvo 0 tarkoittaa kaikille suunnattua viestiä
	private long sender;
	
	//viesti tietylle vastaanottajalle
	public Message(String text, long recipient, long sender) {
		this.text=text;
		this.recipient=recipient;
		this.sender=sender;
	}
	
	//viesti kaikille
	public Message(String text, long sender) {
		this.text=text;
		this.sender=sender;
	}
	
	public String getText() {
		return text;
	}
	
	public long getRecipient() {
		return recipient;
	}
	
	public long getToken(){
		return token;
	}

}
