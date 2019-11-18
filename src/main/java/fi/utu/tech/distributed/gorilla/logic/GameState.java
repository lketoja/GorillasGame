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

public class GameState implements Scheduled {
    private final GameConfiguration configuration;
    private final LinkedBlockingQueue<Move> localMoves;
    private Turn currentTurn;
    private Random randomSource;
    private final List<Player> players = new ArrayList<>();
    private final GameWorld gameWorld;
    private boolean active = true;

    public GameState(GameConfiguration configuration, String localPlayerName, LinkedBlockingQueue<Move> localMoves, List<Player> remotePlayers) {
        this.configuration = configuration;

        this.localMoves = localMoves;

        players.add(new Player(localPlayerName, this.localMoves, true));
        players.addAll(remotePlayers);

        gameWorld = new GameWorld(configuration, players);
        randomSource = new Random(gameWorld.initialStateSeed);
        currentTurn = new Turn(randomSource, 1, 0, configuration.turnLength);
        init();
    }

    private void init() {
        newTurn();
        active = true;
    }

    private void newTurn() {
        currentTurn = currentTurn.next();
        gameWorld.wind.setTarget(currentTurn.wind);
    }

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

    private boolean turnReady() {
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

            if (mtb.angle < -45 || mtb.angle > 225 || mtb.velocity < 0 || mtb.velocity > 150) return;

            gameWorld.addBanana(new Point2D().dir(-mtb.angle, mtb.velocity), player.getLaunchPosition().copy());
        } else if (move instanceof MoveSurrender) {
            // TODO if needed
        }
    }

    public GameConfiguration getConfiguration() {
        return configuration;
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

    public double wind() {
        return currentTurn.wind;
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

        if (turnReady()) {
            newTurn();
            for (Player player : players)
                if (player.alive) handlePlayerMove(player);
        }

        gameWorld.tick();
    }
}