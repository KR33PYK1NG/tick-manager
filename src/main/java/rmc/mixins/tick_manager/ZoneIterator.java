package rmc.mixins.tick_manager;

import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import rmc.mixins.tick_manager.extend.ClassInheritanceMultiMapEx;

import java.util.Iterator;
import java.util.List;

/**
 * Developed by RMC Team, 2021
 * @author KR33PY
 */
public class ZoneIterator implements Iterator<Entity> {

    @Override
    public boolean hasNext() {
        Chunk chunk = this.chunks.get(this.cPos);
        ClassInheritanceMultiMap<Entity> slice = chunk.getEntityLists()[this.sPos];
        while (this.ePos == slice.size()) {
            if (++this.sPos == chunk.getEntityLists().length) {
                if (++this.cPos == this.chunks.size()) {
                    return false;
                }
                this.sPos = 0;
                chunk = this.chunks.get(this.cPos);
            }
            this.ePos = 0;
            slice = chunk.getEntityLists()[this.sPos];
        }
        this.prepared = ((ClassInheritanceMultiMapEx) slice).rmc$getEntity(this.ePos);
        if (!this.prepared.isAlive())
            return this.hasNext();
        return true;
    }

    @Override
    public Entity next() {
        this.ePos++;
        return this.prepared;
    }

    private Entity prepared;
    private int cPos, sPos, ePos;
    private final List<Chunk> chunks;

    ZoneIterator(final List<Chunk> chunks) {
        this.chunks = chunks;
    }

}
