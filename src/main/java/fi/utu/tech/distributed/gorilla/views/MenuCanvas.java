package fi.utu.tech.distributed.gorilla.views;

import fi.utu.tech.distributed.gorilla.engine.SimpleEngine;
import fi.utu.tech.distributed.gorilla.logic.Move;
import fi.utu.tech.distributed.gorilla.logic.Player;
import fi.utu.tech.distributed.gorilla.objects.Gorilla;
import fi.utu.tech.distributed.gorilla.views.layers.Parallax;
import fi.utu.tech.distributed.gorilla.views.layers.ScrollingTextView;
import fi.utu.tech.oomkit.canvas.Canvas;
import fi.utu.tech.oomkit.canvas.Point2D;
import fi.utu.tech.oomkit.colors.CoreColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MenuCanvas extends ProxyCanvas {
    private final Parallax layer3;
    private final Point2D topLeft = new Point2D(0, 0);
    private boolean lowendMachine;
    private List<String> menu;
    private ScrollingTextView rows;
    private int selectedItem = 0;

    private final Gorilla menuGorilla = new Gorilla(new SimpleEngine(1,2,1), new Point2D(), new Player("foo", null, false)) {
        public Move playTurn() {
            return null;
        }
    };

    public MenuCanvas(Canvas backend, boolean lowendMachine, long seed, String title, String[] menuItems) {
        super(backend);
        this.lowendMachine = lowendMachine;
        setMenu(title, menuItems);
        layer3 = new Parallax(backend, 0.7, false, new Random(seed).nextLong());
    }

    public void setMenu(String title, String[] menuItems) {
        menu = new ArrayList<>();
        menu.add(title);
        menu.add("");
        menu.addAll(Arrays.asList(menuItems));

        rows = new ScrollingTextView(backend, menu.toArray(new String[]{}), 48) {
            @Override
            protected Point2D place(Point2D p) {
                double f = (int) fontSize();
                return p.set(160 + p.x * f, 128 + p.y * 100);
            }
        };

    }

    @Override
    public void updateContent() {
        layer3.update(0.5);
        rows.tick();
    }

    @Override
    public void drawBackgroundContent() {
        drawRectangle(topLeft, dimensions, CoreColor.Blue, true);
        if (!lowendMachine) {
            layer3.redraw();
        }
    }

    public void setSelected(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    public int menuItemCount() {
        return menu.size() - 2;
    }

    @Override
    public void drawForegroundContent() {
        rows.drawForegroundContent();
        menuGorilla.getPosition().set(32, selectedItem * 100 + 338 - menuGorilla.getForm().y);
        menuGorilla.draw(this, topLeft);
    }
}
