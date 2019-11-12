package fi.utu.tech.distributed.gorilla.logic;

import fi.utu.tech.oomkit.canvas.Point2D;

import java.util.concurrent.LinkedBlockingQueue;

public class Player {
    public final String name;
    public final boolean local;
    private final Point2D launchPos = new Point2D();
    public final LinkedBlockingQueue<Move> moves;

    public double angle = -1;
    public double velocity = -1;
    public boolean alive = true;

    public Player(String name, LinkedBlockingQueue<Move> moves, boolean local) {
        this.name = name;
        this.moves = moves;
        this.local = local;
    }

    public void setLaunchPosition(Point2D s) {
        launchPos.set(s);
    }

    public Point2D getLaunchPosition() {
        return launchPos;
    }

    public void readMoves() {
        while (moves.peek() != null) {
            Move suggestion = moves.poll();
            if (suggestion instanceof MoveThrowBanana) {
                MoveThrowBanana mtb = (MoveThrowBanana) suggestion;
                if (mtb.angle >= 0) angle = mtb.angle;
                if (mtb.velocity >= 0) velocity = mtb.velocity;
            }
        }
    }

    public boolean readyToMove() {
        return angle != -1 && velocity != -1;
    }

    // return a Move or null if haven't decided yet.
    // called by the game main thread. must not block
    public Move playTurn() {
        readMoves();
        if (angle >= 0 && velocity >= 0) {
            Move move = new MoveThrowBanana(angle, velocity);
            angle = velocity = -1;
            return move;
        }
        return null;
    }
}