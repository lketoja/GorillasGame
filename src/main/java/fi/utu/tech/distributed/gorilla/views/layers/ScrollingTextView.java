package fi.utu.tech.distributed.gorilla.views.layers;

import fi.utu.tech.oomkit.app.Scheduled;
import fi.utu.tech.oomkit.canvas.Canvas;
import fi.utu.tech.oomkit.canvas.Point;
import fi.utu.tech.oomkit.canvas.Point2D;

import java.util.LinkedList;

public class ScrollingTextView extends TextView implements Scheduled {
    protected final LinkedList<Point> hiddenLetters = new LinkedList<>();
    private int currentLine;

    public boolean done() {
        return hiddenLetters.isEmpty();
    }

    public ScrollingTextView(Canvas backend, String[] rows, double size) {
        super(backend, rows, size);
        init();
    }

    public void init() {
        currentLine = -1;
        hiddenLetters.clear();
        for (int y = 0; y < rows.length; y++)
            for (int x = 0; x < rows[y].length(); x++)
                if (super.charAt(x, y) != ' ') hiddenLetters.add(new Point(x, y));
    }

    @Override
    protected char charAt(int x, int y) {
        return hiddenLetters.contains(new Point(x, y)) ? ' ' : super.charAt(x, y);
    }

    @Override
    protected Point2D place(Point2D p) {
        return super.place(p.add(0, -Math.max(0, currentLine / 2)));
    }

    @Override
    public void tick() {
        if (!hiddenLetters.isEmpty()) {
            currentLine = hiddenLetters.remove().y;
        }
    }
}