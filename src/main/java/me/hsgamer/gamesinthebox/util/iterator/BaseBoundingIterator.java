package me.hsgamer.gamesinthebox.util.iterator;

import me.hsgamer.gamesinthebox.util.BoundingIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BaseBoundingIterator implements BoundingIterator {
    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int maxZ;
    private final AtomicReference<Vector> current;

    protected BaseBoundingIterator(BoundingBox boundingBox, boolean maxInclusive) {
        this.minX = (int) Math.floor(boundingBox.getMinX());
        this.minY = (int) Math.floor(boundingBox.getMinY());
        this.minZ = (int) Math.floor(boundingBox.getMinZ());
        this.maxX = (int) Math.ceil(boundingBox.getMaxX()) - (maxInclusive ? 0 : 1);
        this.maxY = (int) Math.ceil(boundingBox.getMaxY()) - (maxInclusive ? 0 : 1);
        this.maxZ = (int) Math.ceil(boundingBox.getMaxZ()) - (maxInclusive ? 0 : 1);
        this.current = new AtomicReference<>();
    }

    public void reset() {
        this.current.set(null);
    }

    public Vector getCurrent() {
        return this.current.get();
    }

    public abstract Vector initial();

    public abstract Vector getContinue(Vector current) throws NoSuchElementException;

    public abstract boolean hasContinue(Vector current);

    @Override
    public boolean hasNext() {
        Vector vector = this.current.get();
        return vector == null || hasContinue(vector);
    }

    @Override
    public Vector next() {
        Vector vector = getCurrent();
        if (vector == null) {
            vector = initial();
        } else {
            vector = getContinue(vector);
        }
        this.current.set(vector);
        return vector;
    }
}
