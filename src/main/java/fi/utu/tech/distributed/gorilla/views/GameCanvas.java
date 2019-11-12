package fi.utu.tech.distributed.gorilla.views;

import fi.utu.tech.distributed.gorilla.logic.GameState;
import fi.utu.tech.distributed.gorilla.engine.GameObject;
import fi.utu.tech.distributed.gorilla.engine.Rect;
import fi.utu.tech.distributed.gorilla.logic.Player;
import fi.utu.tech.distributed.gorilla.views.layers.Parallax;
import fi.utu.tech.distributed.gorilla.views.objects.ObjectView;
import fi.utu.tech.oomkit.app.Scheduled;
import fi.utu.tech.oomkit.canvas.Canvas;
import fi.utu.tech.oomkit.canvas.Point;
import fi.utu.tech.oomkit.canvas.Point2D;
import fi.utu.tech.oomkit.colors.CoreColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class GameCanvas extends ProxyCanvas implements Scheduled {
    private final boolean lowendMachine;
    private final Parallax layer2;
    private final Parallax layer3;
    private final Point2D topLeft = new Point2D(0, 0);
    private final double gameTickDuration;

    private GameState gameState;
    private final Rect view = new Rect(new Point2D(), new Point2D());
    private int viewVelocity = 0;

    public GameCanvas(double tickDuration, Canvas main, GameState gameState, boolean lowendMachine, long seed) {
        super(main);
        this.lowendMachine = lowendMachine;
        this.gameTickDuration = tickDuration;
        Random generator = new Random(seed);
        layer2 = new Parallax(main, 0.6, false, generator.nextLong());
        layer3 = new Parallax(main, 0.8, false, generator.nextLong());
        setGameState(gameState);
    }

    public void setVelocity(int v) {
        viewVelocity = v;
    }

    public void addVelocity(int v) {
        viewVelocity += v;
    }

    @Override
    protected void resized() {
        super.resized();
        setGameState(gameState);
    }

    public void setGameState(GameState gameState) {
        if (gameState == null) return;

        this.gameState = gameState;
        updateContent();
        double sceneHeight = gameState.getConfiguration().gameWorldHeight;
        view.topLeft.set(0, sceneHeight - getHeight());
        view.bottomRight.set(getWidth(), sceneHeight);
        viewVelocity = 0;
    }

    @Override
    public void updateContent() {
        if (viewVelocity != 0) {
            view.topLeft.add(viewVelocity, 0);
            view.bottomRight.add(viewVelocity, 0);
        }
        layer2.update(viewVelocity / 2.0);
        layer3.update(viewVelocity / 4.0);
    }

    private final Point tmp = new Point(10, 30);

    private String renderTime(double seconds) {
        return (int) (seconds) + " millisekuntia";
    }

    private String renderGameStatus() {
        int aliveCount = 0;
        for (Player p : gameState.getPlayers())
            if (p.alive) aliveCount++;

        return aliveCount + " / " + gameState.getPlayers().size() + " gorillaa elossa.";
    }

    private String renderWindStatus() {
        return "Tuuli: " + gameState.wind() + (gameState.wind()>0 ? " yks. oikealle" : " yks. vasemmalle");
    }

    @Override
    public void drawBackgroundContent() {
        drawRectangle(topLeft, dimensions, CoreColor.Blue, true);
    }

    public void drawForegroundContent() {
        if (!lowendMachine) {
            layer3.redraw();
            layer2.redraw();
        }
        if (gameState != null) {
            ArrayList<ObjectView> objs = new ArrayList<>();
            for (GameObject g : gameState.objectsInRegion(view)) {
                if (g instanceof ObjectView)
                    objs.add((ObjectView) g);
            }

            Collections.sort(objs);

            for (ObjectView obj : objs) obj.draw(this, view.topLeft);

            drawText(tmp, CoreColor.Yellow, "Vuoroa jäljellä: " + renderTime(gameTickDuration * (gameState.turnTimeLeft() / gameState.getConfiguration().timeStep)), 16, true, false);
            drawText(tmp.add(0, 20), CoreColor.Yellow, renderGameStatus(), 16, true, false);
            drawText(tmp.add(0, 40), CoreColor.Yellow, renderWindStatus(), 16, true, false);
        }
    }
}

