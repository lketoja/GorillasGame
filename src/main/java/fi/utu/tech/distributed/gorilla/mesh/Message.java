package fi.utu.tech.distributed.gorilla.mesh;

import java.io.Serializable;
import java.util.Random;

/**
 * TODO: make compatible with network play
 */
public final class Message implements Serializable{
    public final long sender;
    public final long recipient;
    public final Serializable contents;
    public final long token = new Random().nextLong();

    public Message(long sender, long recipient, Serializable contents) {
        this.sender = sender;
        this.recipient = recipient;
        this.contents = contents;
    }
    
 
}
