package me.hsgamer.gamesinthebox.util.iterator;

import me.hsgamer.gamesinthebox.util.BoundingIterator;
import me.hsgamer.hscore.common.CollectionUtils;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class RandomTypeBoundingIterator implements BoundingIterator {
    private final BoundingBox boundingBox;
    private final boolean maxInclusive;
    private final AtomicReference<BoundingIterator> current;

    public RandomTypeBoundingIterator(BoundingBox boundingBox, boolean maxInclusive) {
        this.boundingBox = boundingBox;
        this.maxInclusive = maxInclusive;
        current = new AtomicReference<>(getRandom());
    }

    private BoundingIterator getRandom() {
        return Objects.requireNonNull(
                CollectionUtils.pickRandom(
                        BoundingIterator.Enums.values(),
                        e -> e != BoundingIterator.Enums.RANDOM_TYPE)
                )
                .get(boundingBox, maxInclusive);
    }

    @Override
    public void reset() {
        current.set(getRandom());
    }

    @Override
    public boolean hasNext() {
        return current.get().hasNext();
    }

    @Override
    public Vector next() {
        return current.get().next();
    }
}
