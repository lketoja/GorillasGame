package fi.utu.tech.distributed.gorilla.logic;

import fi.utu.tech.distributed.gorilla.views.MainCanvas;
import fi.utu.tech.distributed.gorilla.views.Views;
import fi.utu.tech.oomkit.app.AppConfiguration;
import fi.utu.tech.oomkit.app.GraphicalAppLogic;
import fi.utu.tech.oomkit.canvas.Canvas;
import fi.utu.tech.oomkit.util.Console;
import fi.utu.tech.oomkit.windows.Window;
import javafx.application.Application;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TODO: make compatible with network play
 */
public class GorillaLogic implements GraphicalAppLogic {
    private Console console;
    private final MainCanvas mainCanvas = new MainCanvas();
    public Views views;

    private GameState gameState;
    private GameMode gameMode;

    private String myName = "Mää";
    private final int gameSeed = 1;
    private final int maxPlayers = 6;

    // in case the game runs too slow:

    // on Linux/Mac, first try to add the Java VM parameter -Dprism.order=sw
    // JavaFX may have some memory leaks that can crash the whole system

    // true = turns off background levels and fade in/out = faster, but not as pretty
    private final boolean lowendMachine = true;

    // duration between game ticks (in ms). larger number = computationally less demanding game
    private final int tickDuration = 20;

    // no comment
    private final boolean synkistely = false;

    // true = you can check from the text console if the computer is too slow to render all frames
    // the system will display 'Frame skipped!' if the tick() loop takes too long.
    private final boolean verboseMessages = false;

    private final LinkedBlockingQueue<Move> moves = new LinkedBlockingQueue<>();
    private final List<Player> otherPlayers = new ArrayList<>();

    // we should return the one we actually use for drawing
    // the others are just proxies that end to drawing here
    @Override
    public Canvas getCanvas() {
        return mainCanvas;
    }

    // initializes the game logic
    @Override
    public AppConfiguration configuration() {
        return new AppConfiguration(tickDuration, "Gorilla", false, verboseMessages, true, true, true);
    }

    // in order to make the menu work, click the text area on the right
    // then, to enter game moves, click the area again
    @Override
    public void handleKey(Key k) {
        switch (gameMode) {
            case Intro:
                setMode(GameMode.Menu);
                break;
            case Menu:
                if (k == Key.Up) {
                    if (selectedMenuItem > 0) selectedMenuItem--;
                    else selectedMenuItem = 2;
                    views.setSelectedMenuItem(selectedMenuItem);
                    return;
                }
                if (k == Key.Down) {
                    if (selectedMenuItem < 2) selectedMenuItem++;
                    else selectedMenuItem = 0;
                    views.setSelectedMenuItem(selectedMenuItem);
                    return;
                }
                if (k == Key.Enter) {
                    switch (selectedMenuItem) {
                        case 0:
                            // quit active game
                            if (gameState != null) {
                                resetGame();
                                setMode(GameMode.Menu);
                            } else {
                                setMode(GameMode.Game);
                            }
                            break;
                        case 1:
                            // TODO
                            break;
                        case 2:
                            Platform.exit();
                    }
                }
                break;
            case Game:
                // instead we read with 'handleConsoleInput'
                break;
        }
    }

    private void handleConsoleInput() {
        if (console != null && console.inputQueue().peek() != null) {
            parseCommandLine(console.inputQueue().poll());
        }
    }

    @Override
    public void initialize(Window window, Application.Parameters parameters) {
        // reconfigure using the app command line parameter: --port=1234
        // IDEA: Run -> Edit configurations -> Program arguments
        // Eclipse: TODO
        startServer(parameters.getNamed().getOrDefault("port", "1234"));

        connectToServer(parameters.getNamed().getOrDefault("server", "localhost"));

        views = new Views(mainCanvas, lowendMachine, synkistely, configuration().tickDuration, new Random().nextLong());
        this.console = window.console();

        setMode(GameMode.Intro);

        resetGame();

        views.setMenu("Gorillasota 2029", new String[]{
                "Aloita / lopeta peli",
                "Palvelinyhteys",
                "Lopeta"
        });

        updateMenuInfo();
    }

    public void resetGame() {
        moves.clear();
        otherPlayers.clear();
        playerIdx = 1;

        gameState = null;
    }

    public void joinGame(String name, LinkedBlockingQueue<Move> moveQueue) {
        if (otherPlayers.size() + 1 < maxPlayers) {
            otherPlayers.add(new Player(name, moveQueue, false));
        }
    }

    @Override
    public void tick() {
        handleConsoleInput();
        toggleGameMode();
        views.redraw();
    }

    public void setMode(GameMode mode) {
        switch (mode) {
            case Game:
                // restart if not running
                if (gameState == null) initGame();
                break;
        }

        gameMode = mode;
        views.setMode(mode);
        updateMenuInfo();
    }

    private void startServer(String port) {
        System.out.println("Starting server at port " + port);
    }

    private void connectToServer(String address) {
        System.out.println("Connecting to server at " + address);
    }

    private void initGame() {
        double h = getCanvas().getHeight();

        List<String> names = new LinkedList<>();
        names.add(myName);
        for (Player player : otherPlayers) names.add(player.name);

        GameConfiguration configuration =
                new GameConfiguration(gameSeed, h, names);

        gameState = new GameState(configuration, myName, moves, otherPlayers);
        views.setGameState(gameState);
    }

    private void addPlayerMove(String player, Move move) {
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
                    if (gameMode != GameMode.Game) return;
                    try {
                        double angle = Double.parseDouble(rest);
                        gameState.addPlayerMove(new MoveThrowBanana(angle, Double.NaN));
                        System.out.println("Asetettu kulma: " + angle);
                    } catch (NumberFormatException e) {
                        System.out.println("Virheellinen komento, oikea on: angle <liukuluku -45..225>");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "v":
                case "n":
                case "velocity":
                case "nopeus":
                    if (gameMode != GameMode.Game) return;
                    try {
                        double velocity = Double.parseDouble(rest);
                        gameState.addPlayerMove(new MoveThrowBanana(Double.NaN, velocity));
                        System.out.println("Asetettu nopeus: " + velocity);
                    } catch (NumberFormatException e) {
                        System.out.println("Virheellinen komento, oikea on: velocity <liukuluku 0..150>");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
            }
        }
    }


    private int c = 0;
    private int selectedMenuItem = 0;
    private int playerIdx = 1;

    private void moveAIplayers() {
        // currently a rather primitive random AI
        if (new Random().nextInt(50) < 4 && !otherPlayers.isEmpty()) {
            Move move = new MoveThrowBanana(
                    new Random().nextDouble() * 180,
                    35 + new Random().nextDouble() * 35);

            addPlayerMove("Kingkong " + (new Random().nextInt(otherPlayers.size()) + 1), move);
        }
    }

    private void updateMenuInfo() {
        views.setMenuInfo(new String[]{"Pelaajia: " + (otherPlayers.size() + 1), "Yhdistetty koneeseen <->", "Peli aktiivinen: " + (gameState != null)});
    }

    private void toggleGameMode() {
        switch (gameMode) {
            case Intro:
                // when the intro is done, jump to menu
                if (views.introDone())
                    setMode(GameMode.Menu);
                break;
            case Menu:
                c++;
                if (c > 50) {
                    c = 0;
                }
                if (selectedMenuItem == 1 && c == 0) {
                    joinGame("Kingkong " + (playerIdx++), new LinkedBlockingQueue<>());
                    updateMenuInfo();
                }
                break;
            case Game:
                moveAIplayers();
                gameState.tick();
                break;
        }
    }
}