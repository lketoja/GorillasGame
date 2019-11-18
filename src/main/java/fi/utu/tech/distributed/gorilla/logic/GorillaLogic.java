package fi.utu.tech.distributed.gorilla.logic;

import fi.utu.tech.distributed.gorilla.views.MainCanvas;
import fi.utu.tech.distributed.gorilla.views.Views;
import fi.utu.tech.oomkit.app.AppConfiguration;
import fi.utu.tech.oomkit.app.GraphicalAppLogic;
import fi.utu.tech.oomkit.canvas.Canvas;
import fi.utu.tech.oomkit.util.Console;
import fi.utu.tech.oomkit.windows.Window;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class GorillaLogic implements GraphicalAppLogic {
    private Console console;
    private final MainCanvas mainCanvas = new MainCanvas();
    public Views views;

    private GameState gameState;
    private GameMode gameMode;

    private String myName = "Mää";
    private final int gameSeed = 1;
    private final boolean lowendMachine = true; // piirretäänkö 1 vai 3 kaupunkitasoa + feidaukset
    private final int tickDuration = 20; // päivitys 20 ms välein
    private final boolean synkistely = false;

    private final LinkedBlockingQueue<Move> moves = new LinkedBlockingQueue<>();
    private final List<LinkedBlockingQueue<Move>> otherMoves = new ArrayList<>();
    private final List<Player> otherPlayers = new ArrayList<>();

    // we should return the one we actually use for drawing
    // the others are just proxies that end to drawing here
    @Override
    public Canvas getCanvas() {
        return mainCanvas;
    }

    // alustaa pelin logiikan
    @Override
    public AppConfiguration configuration() {
        return new AppConfiguration(tickDuration, "Gorilla", false, false, true, true, true);
    }

    @Override
    public void handleKey(Key k) {
        if (gameMode == GameMode.Intro)
            setMode(GameMode.Game);
    }

    @Override
    public void initialize(Window window) {
        views = new Views(mainCanvas, lowendMachine, synkistely, configuration().tickDuration, new Random().nextLong());
        this.console = window.console();

        setMode(GameMode.Intro);

        moves.clear();
        otherMoves.clear();
        otherPlayers.clear();

        for (int i = 0; i < 5; i++)
            otherMoves.add(new LinkedBlockingQueue<>());

        {
            int idx = 1;
            for (LinkedBlockingQueue<Move> moveQueue : otherMoves)
                otherPlayers.add(new Player("Kingkong " + (idx++), moveQueue, false));
        }
    }

    public void setMode(GameMode mode) {
        switch (mode) {
            case Intro:
                gameState = null;
                break;

            case Game:
                // restart when coming from the intro
                if (gameState == null) initGame();
                break;
        }

        gameMode = mode;
        views.setMode(mode);
    }

    private void initGame() {
        double h = getCanvas().getHeight();

        List<String> names = new LinkedList<>();
        names.add(myName);
        for(Player player: otherPlayers) names.add(player.name);

        GameConfiguration configuration =
                new GameConfiguration(gameSeed, h, names);

        gameState = new GameState(configuration, myName, moves, otherPlayers);
        views.setGameState(gameState);
    }

    public void addPlayerMove(String player, Move move) {
        for (Player p : otherPlayers)
            if (p.name.equals(player))
                p.moves.add(move);
    }

    private void handleChatMessage(ChatMessage msg) {
        System.out.println(msg.sender + " sanoi: " + msg.contents);
    }

    private void parseCommandLine(String cmd) {
        if (cmd.contains(" ")) {
            String rest = cmd.substring(cmd.split(" ")[0].length() + 1);
            switch (cmd.split(" ")[0]) {
                case "q":
                case "quit":
                case "exit":
                    break;
                case "s":
                case "chat":
                case "say":
                    handleChatMessage(new ChatMessage("you", "all", rest));
                    break;
                case "a":
                case "k":
                case "angle":
                case "kulma":
                    try {
                        double angle = Double.parseDouble(rest);
                        if (angle >= -45 && angle <= 225) {
                            System.out.println("Asetettu kulma: " + angle);
                            gameState.addPlayerMove(new MoveThrowBanana(angle, -1));
                        } else {
                            System.out.println("Virheellinen kulman arvo, sallittu väli -45 .. 225 astetta.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Virheellinen komento, oikea on: angle <liukuluku -45..225>");
                    }
                    break;
                case "v":
                case "n":
                case "velocity":
                case "nopeus":
                    try {
                        double velocity = Double.parseDouble(rest);
                        if (velocity >= 0 && velocity <= 150) {
                            System.out.println("Asetettu nopeus: " + velocity);
                            gameState.addPlayerMove(new MoveThrowBanana(-1, velocity));
                        } else {
                            System.out.println("Virheellinen nopeuden arvo, sallittu väli 0 .. 150 voimayksikköä.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Virheellinen komento, oikea on: velocity <liukuluku 0..150>");
                    }
                    break;
            }
        }
    }

    private void handleConsoleInput() {
        if (console != null && console.inputQueue().peek() != null) {
            parseCommandLine(console.inputQueue().poll());
        }
    }

    private int c = 0;
    private int s = 0;

    private void toggleGameMode() {
        switch (gameMode) {
            case Intro:
                if (views.introDone())
                    setMode(GameMode.Game);
                break;
            case Menu:
                c++;
                if (c > 50) {
                    c = 0;
                    s++;
                    s = s % 3;
                    views.setSelectedMenuItem(s);
                }
                break;
            case Game:
                // currently a rather primitive random AI
                if (new Random().nextInt(50) < 4 && !otherPlayers.isEmpty()) {
                    Move move = new MoveThrowBanana(
                            new Random().nextDouble() * 180,
                            35 + new Random().nextDouble() * 35);

                    addPlayerMove("Kingkong " + (new Random().nextInt(otherPlayers.size()) + 1), move);
                }

                gameState.tick();
                break;
        }
    }

    @Override
    public void tick() {
        handleConsoleInput();
        toggleGameMode();
        views.redraw();
    }
}