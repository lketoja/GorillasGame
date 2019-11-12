package fi.utu.tech.distributed.gorilla.logic;

public final class MoveThrowBanana extends Move {
    public final double angle;
    public final double velocity;

    public MoveThrowBanana(double angle, double velocity) {
        this.angle = angle;
        this.velocity = velocity;
    }
}
