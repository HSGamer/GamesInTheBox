package me.hsgamer.gamesinthebox.util.iterator;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.NoSuchElementException;

public class LinearBoundingIterator extends BaseBoundingIterator {
    public static final LinearCoordinate X_COORDINATE = new LinearCoordinate() {
        @Override
        public boolean hasNext(Vector current, BaseBoundingIterator iterator) {
            return current.getX() < iterator.maxX;
        }

        @Override
        public void next(Vector next) {
            next.setX(next.getX() + 1);
        }

        @Override
        public void reset(Vector next, BaseBoundingIterator iterator) {
            next.setX(iterator.minX);
        }
    };
    public static final LinearCoordinate Y_COORDINATE = new LinearCoordinate() {
        @Override
        public boolean hasNext(Vector current, BaseBoundingIterator iterator) {
            return current.getY() < iterator.maxY;
        }

        @Override
        public void next(Vector next) {
            next.setY(next.getY() + 1);
        }

        @Override
        public void reset(Vector next, BaseBoundingIterator iterator) {
            next.setY(iterator.minY);
        }
    };
    public static final LinearCoordinate Z_COORDINATE = new LinearCoordinate() {
        @Override
        public boolean hasNext(Vector current, BaseBoundingIterator iterator) {
            return current.getZ() < iterator.maxZ;
        }

        @Override
        public void next(Vector next) {
            next.setZ(next.getZ() + 1);
        }

        @Override
        public void reset(Vector next, BaseBoundingIterator iterator) {
            next.setZ(iterator.minZ);
        }
    };

    private final LinearCoordinate[] coordinates;

    public LinearBoundingIterator(BoundingBox boundingBox, boolean maxInclusive, LinearCoordinate... coordinates) {
        super(boundingBox, maxInclusive);
        this.coordinates = coordinates;
    }

    public LinearBoundingIterator(BoundingBox boundingBox, boolean maxInclusive) {
        this(boundingBox, maxInclusive, X_COORDINATE, Y_COORDINATE, Z_COORDINATE);
    }

    @Override
    public Vector initial() {
        return new Vector(minX, minY, minZ);
    }

    @Override
    public Vector getContinue(Vector current) throws NoSuchElementException {
        Vector next = current.clone();
        for (int i = 0; i < coordinates.length; i++) {
            LinearCoordinate coordinate = coordinates[i];
            if (coordinate.hasNext(next, this)) {
                coordinate.next(next);
                break;
            } else if (i == coordinates.length - 1) {
                throw new NoSuchElementException("No more elements");
            } else {
                coordinate.reset(next, this);
            }
        }
        return next;
    }

    @Override
    public boolean hasContinue(Vector current) {
        return current.getX() < maxX || current.getY() < maxY || current.getZ() < maxZ;
    }

    public interface LinearCoordinate {
        boolean hasNext(Vector current, BaseBoundingIterator iterator);

        void next(Vector next);

        void reset(Vector next, BaseBoundingIterator iterator);
    }
}
