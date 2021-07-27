package rmc.mixins.tick_manager;

import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.Chunk;

import java.util.Iterator;
import java.util.List;

/**
 * Developed by RMC Team, 2021
 * @author KR33PY
 */
public class ZoneIterable implements Iterable<Entity> {

    @Override
    public Iterator<Entity> iterator() {
        return new ZoneIterator(this.chunks);
    }

    private final List<Chunk> chunks;

    ZoneIterable(List<Chunk> chunks) {
        this.chunks = chunks;
    }

}
