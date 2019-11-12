package fi.utu.tech.distributed.gorilla.engine.rtree;

import fi.utu.tech.distributed.gorilla.engine.Region;
import fi.utu.tech.oomkit.canvas.Point2D;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Node<X extends Region> implements Region {
    private final Point2D topLeft, bottomRight;

    protected Node(Point2D topLeft, Point2D bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    protected Node() {
        this(new Point2D(), new Point2D());
    }

    @Override
    public Point2D topLeft() {
        return topLeft;
    }

    @Override
    public Point2D bottomRight() {
        return bottomRight;
    }

    protected void setRegion(Region r) {
        topLeft.set(r.topLeft());
        bottomRight.set(r.bottomRight());
    }

    public abstract Collection<X> findIntersections(Region region);

    public abstract void findIntersections(Region region, Consumer<X> handler);

    public abstract Node<X> add(X obj);

    public abstract Node<X> remove(X obj);

    public abstract Collection<X> contents();

    public abstract int size();

    public abstract int depth();

    public String toString() {
        return "" + topLeft().toString() + "-" + bottomRight().toString() + " d: " + depth() + ", s: " + size();
    }

    static final class NullNode<X extends Region> extends Node<X> {
        NullNode() {
            super(null, null);
        }

        @Override
        public Collection<X> findIntersections(Region region) {
            return Set.of();
        }

        @Override
        public void findIntersections(Region region, Consumer<X> handler) {
        }

        @Override
        public Node<X> add(X obj) {
            return new MultiLeaf<>(obj);
        }

        @Override
        public Node<X> remove(X obj) {
            return this;
        }

        @Override
        public Collection<X> contents() {
            return Set.of();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int depth() {
            return 0;
        }
    }

    protected NullNode<X> nullNode() {
        if (nullNode == null) nullNode = new NullNode<>();
        return nullNode;
    }

    private NullNode<X> nullNode;
}
