package fi.utu.tech.distributed.gorilla.logic;

import java.util.Random;

public final class Turn {
    public final int id;
    public final double wind;
    public final double startTimeStamp;
    public final double turnLength;
    private transient Random builder;

    public Turn(Random builder, int id, double startTimeStamp, double turnLength) {
        this.builder = builder;
        wind = (builder.nextInt(100) - 50) / 10.0;
        this.id = id;
        this.startTimeStamp = startTimeStamp;
        this.turnLength = turnLength;
    }

    public Turn next() {
        return new Turn(builder, id + 1, startTimeStamp + turnLength, turnLength);
    }
}