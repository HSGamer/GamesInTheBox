package me.hsgamer.gamesinthebox.util.iterator;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class RandomBoundingIterator extends BaseBoundingIterator {
    private final LinkedList<Vector> queue;

    public RandomBoundingIterator(BoundingBox boundingBox, boolean maxInclusive) {
        super(boundingBox, maxInclusive);
        queue = new LinkedList<>();
    }

    @Override
    public void reset() {
        super.reset();
        queue.clear();
    }

    @Override
    public Vector initial() {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    queue.add(new Vector(x, y, z));
                }
            }
        }
        Collections.shuffle(queue);
        return queue.poll();
    }

    @Override
    public Vector getContinue(Vector current) throws NoSuchElementException {
        Vector vector = queue.poll();
        if (vector == null) {
            throw new NoSuchElementException();
        }
        return vector;
    }

    @Override
    public boolean hasContinue(Vector current) {
        return !queue.isEmpty();
    }
}
