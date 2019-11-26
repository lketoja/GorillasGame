package fi.utu.tech.distributed.gorilla.logic;

import java.util.List;

/**
 * TODO: make compatible with network play
 */
public final class GameConfiguration {
    // random seed for constructing the game world
    // should result in identical game worlds on different Java systems
    // affects world creation + gameplay
    public final long seed;

    // world height, affects world creation
    public final double gameWorldHeight;

    // min distance between gorillas, affects world creation
    public final int minGorillaDistance = 600;

    // max distance between gorillas, affects world creation
    public final int maxGorillaDistance = 1100;

    // player names (the number of players can also be deducted from this)
    public final List<String> playerNames;

    // time step for physics simulation (affects gameplay)
    public final double timeStep = 0.15;

    // turn length (see GameCanvas.drawForegroundContent) (affects gameplay)
    public final double turnLength = 30;

    // how many simultaneous objects should the physics engine support (probably ok)
    public final int maxObjects = 10000;

    // how many time step units to wait until the banana becomes lethal (affects gameplay)
    public final int safetyZone = 40;

    // how strongly does the wind affect the banana velocity (affects gameplay)
    public final double windFactor = 40;

    // turn on/off the sun (purely eye candy)
    public final boolean enableSun = true;

    // turn on/off the clouds (purely eye candy)
    public final boolean enableClouds = true;

    public GameConfiguration(long seed, double gameWorldHeight, List<String> playerNames) {
        this.seed = seed;
        this.gameWorldHeight = gameWorldHeight;
        this.playerNames = playerNames;
    }
}