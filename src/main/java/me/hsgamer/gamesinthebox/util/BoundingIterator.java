package me.hsgamer.gamesinthebox.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public class BoundingIterator implements Iterator<Vector> {
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final AtomicReference<Vector> current;

    public BoundingIterator(BoundingBox boundingBox, boolean maxInclusive) {
        this.minX = (int) Math.floor(boundingBox.getMinX());
        this.minY = (int) Math.floor(boundingBox.getMinY());
        this.minZ = (int) Math.floor(boundingBox.getMinZ());
        this.maxX = (int) Math.ceil(boundingBox.getMaxX()) - (maxInclusive ? 0 : 1);
        this.maxY = (int) Math.ceil(boundingBox.getMaxY()) - (maxInclusive ? 0 : 1);
        this.maxZ = (int) Math.ceil(boundingBox.getMaxZ()) - (maxInclusive ? 0 : 1);
        this.current = new AtomicReference<>();
    }

    public BoundingIterator(BoundingBox boundingBox) {
        this(boundingBox, true);
    }

    public void reset() {
        this.current.set(null);
    }

    @Override
    public boolean hasNext() {
        Vector vector = this.current.get();
        return vector == null || vector.getX() < maxX || vector.getY() < maxY || vector.getZ() < maxZ;
    }

    @Override
    public Vector next() {
        Vector vector = this.current.get();
        if (vector == null) {
            vector = new Vector(minX, minY, minZ);
        } else if (vector.getX() < maxX) {
            Vector next = vector.clone();
            next.setX(vector.getX() + 1);
            vector = next;
        } else if (vector.getY() < maxY) {
            Vector next = vector.clone();
            next.setY(vector.getY() + 1);
            next.setX(minX);
            vector = next;
        } else if (vector.getZ() < maxZ) {
            Vector next = vector.clone();
            next.setZ(vector.getZ() + 1);
            next.setX(minX);
            next.setY(minY);
            vector = next;
        } else {
            throw new NoSuchElementException("No more elements");
        }
        this.current.set(vector);
        return vector;
    }

    public Location nextLocation(World world) {
        return this.next().toLocation(world);
    }
}
