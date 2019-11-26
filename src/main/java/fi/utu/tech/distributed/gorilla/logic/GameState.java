package fi.utu.tech.distributed.gorilla.logic;

import fi.utu.tech.distributed.gorilla.engine.Engine;
import fi.utu.tech.distributed.gorilla.engine.ProxyGameObject;
import fi.utu.tech.distributed.gorilla.engine.Region;
import fi.utu.tech.oomkit.app.Scheduled;
import fi.utu.tech.oomkit.canvas.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * TODO: make compatible with network play
 */
public class GameState implements Scheduled {
    public final GameConfiguration configuration;
    private final LinkedBlockingQueue<Move> localMoves;
    private final List<Player> players = new ArrayList<>();
    private final Player me;
    private final GameWorld gameWorld;
    private Turn currentTurn;
    private boolean active = true;

    public GameState(GameConfiguration configuration, String localPlayerName, LinkedBlockingQueue<Move> localMoves, List<Player> remotePlayers) {
        this.configuration = configuration;
        this.localMoves = localMoves;

        me = new Player(localPlayerName, this.localMoves, true);
        players.add(me);
        players.addAll(remotePlayers);

        gameWorld = new GameWorld(configuration, players);

        // note that the randomSource is constructed from the gameWorld.initialStateSeed
        // and not used by anything else -> deterministic sequence of turn events
        {
            Random randomSource = new Random(gameWorld.initialStateSeed);
            currentTurn = new Turn(randomSource, 1, 0, configuration.turnLength);
        }
        init();
    }

    private void init() {
        newTurn();
        active = true;
    }

    private void newTurn() {
        currentTurn = currentTurn.next();
        gameWorld.newTurn(currentTurn);
    }

    /**
     * If the game is active, sets active = false if only 0 or 1 players are alive.
     * Also prints the result of the game to the console.
     */
    private void handleEndGameLogic() {
        int aliveCount = 0;
        for (Player player : players) {
            if (player.alive) aliveCount++;
        }

        if (aliveCount < 2 && active) {
            active = false;
            System.out.println("Peli päättyi.");
            if (aliveCount == 0) {
                System.out.println("Kukaan ei voittanut!");
                return;
            }
            for (Player player : players)
                if (player.alive)
                    System.out.println(player.name + " voitti!");
        }
    }

    private boolean isTurnReady() {
        boolean allReady = true;
        for (Player player : players) {
            if (player.alive && !player.readyToMove()) allReady = false;
        }

        return this.turnTimeLeft() < 0 || allReady;
    }

    private void handlePlayerMove(Player player) {
        Move move = player.playTurn();

        if (move instanceof MoveThrowBanana) {
            MoveThrowBanana mtb = (MoveThrowBanana) move;

            if (Double.isNaN(mtb.angle) || Double.isNaN(mtb.velocity) || mtb.angle < -45 || mtb.angle > 225 || mtb.velocity < 0 || mtb.velocity > 150)
                return;

            gameWorld.addBanana(new Point2D().dir(-mtb.angle, mtb.velocity), player.getLaunchPosition().copy());
        } else if (move instanceof MoveSurrender) {
            // TODO: if needed
        }
    }

    public Player getLocalPlayer() {
        return me;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Engine getEngine() {
        return gameWorld.engine;
    }

    public void addPlayerMove(Move move) {
        localMoves.add(move);
    }

    public double turnTimeLeft() {
        return currentTurn.startTimeStamp == -1 ? -1 : currentTurn.turnLength - (getEngine().currentTimeStamp() - currentTurn.startTimeStamp);
    }

    public double getWindSpeed() {
        return currentTurn.windSpeed;
    }
/*
    public Collection<ProxyGameObject> objectsInRegion(Region region) {
        return getEngine().objectsInRegion(region);
    }*/

    public void forObjectsInRegion(Region region, Consumer<ProxyGameObject> handler) {
        getEngine().handleObjectsInRegion(region, handler);
    }

    @Override
    public void tick() {
        getEngine().run();

        for (Player player : players) player.readMoves();

        handleEndGameLogic();

        if (isTurnReady()) {
            newTurn();
            for (Player player : players)
                if (player.alive) handlePlayerMove(player);
        }

        gameWorld.tick();
    }
}