package me.hsgamer.gamesinthebox.util;

import me.hsgamer.gamesinthebox.util.iterator.LinearBoundingIterator;
import me.hsgamer.gamesinthebox.util.iterator.RandomBoundingIterator;
import me.hsgamer.gamesinthebox.util.iterator.RandomTypeBoundingIterator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.function.BiFunction;

import static me.hsgamer.gamesinthebox.util.iterator.LinearBoundingIterator.*;

public interface BoundingIterator extends Iterator<Vector> {
    void reset();

    default Location nextLocation(World world) {
        return this.next().toLocation(world);
    }

    enum Enums {
        X_LEAD((boundingBox, maxInclusive) -> new LinearBoundingIterator(
                boundingBox, maxInclusive,
                Y_COORDINATE,
                Z_COORDINATE,
                X_COORDINATE
                )),
        Y_LEAD((boundingBox, maxInclusive) -> new LinearBoundingIterator(
                boundingBox, maxInclusive,
                X_COORDINATE,
                Z_COORDINATE,
                Y_COORDINATE
                )),
        Z_LEAD((boundingBox, maxInclusive) -> new LinearBoundingIterator(
                boundingBox, maxInclusive,
                X_COORDINATE,
                Y_COORDINATE,
                Z_COORDINATE
                )),
        RANDOM_TYPE(RandomTypeBoundingIterator::new),
        RANDOM(RandomBoundingIterator::new);

        private final BiFunction<BoundingBox, Boolean, BoundingIterator> function;

        Enums(BiFunction<BoundingBox, Boolean, BoundingIterator> function) {
            this.function = function;
        }

        public static BoundingIterator.Enums get(String name) {
            if (name != null) {
                for (BoundingIterator.Enums enums : values()) {
                    if (enums.name().equalsIgnoreCase(name)) {
                        return enums;
                    }
                }
            }
            return Z_LEAD;
        }

        public BoundingIterator get(BoundingBox boundingBox, boolean maxInclusive) {
            return function.apply(boundingBox, maxInclusive);
        }
    }
}
