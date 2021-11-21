package rmc.mixins.tick_manager.actual;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkDistanceGraph;
import net.minecraft.world.server.Ticket;
import net.minecraft.world.server.TicketManager;
import net.minecraft.world.server.TicketType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import rmc.mixins.tick_manager.TickManager;
import rmc.mixins.tick_manager.extend.TicketEx;

import java.lang.reflect.Field;
import java.util.Iterator;

/**
 * Разработано командой RMC, 2021
 */
@Mixin(TicketManager.class)
public abstract class TicketManagerMixin {

    @Shadow
    private long currentTime;

    @Shadow @Final
    public Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets;

    @Shadow @Final
    private Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> forcedTickets;

    // @Shadow @Final
    // private TicketManager.ChunkTicketTracker ticketTracker;
    private static Field ticketTrackerField;

    @Shadow
    private static int getLevel(SortedArraySet<Ticket<?>> p_229844_0_) { return 0; }

    @Shadow
    private SortedArraySet<Ticket<?>> getTicketSet(long p_229848_1_) { return null; }

    @Overwrite
    protected void tick() {
        ++this.currentTime;
        ObjectIterator objectiterator = this.tickets.long2ObjectEntrySet().fastIterator();

        while(objectiterator.hasNext()) {
            Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> entry = (Long2ObjectMap.Entry)objectiterator.next();
            // Mohist start - Introduce 'chunk_unload_delay' option
            boolean rem = false, remUnk = false;
            Iterator<Ticket<?>> it = entry.getValue().iterator();
            while (it.hasNext()) {
                Ticket<?> ticket = it.next();
                if (((TicketEx)(Object) ticket).rmc$isExpired(this.currentTime)) {
                    if (ticket.getType() == TicketType.UNKNOWN) {
                        remUnk = true;
                    }
                    if (ticket.getType() == TickManager.UNLOAD_DELAY) {
                        //System.out.println("UNLOAD");
                    }
                    rem = true;
                    it.remove();
                }
            }
            if (rem) {
                if (remUnk && TickManager.UNLOAD_DELAY.getLifespan() > 0) { // Mist - Disable chunk unload delay if <= 0
                    //System.out.println("add1");
                    ((TicketEx)(Object) entry.getValue().func_226175_a_(new Ticket<ChunkPos>(TickManager.UNLOAD_DELAY, 33, new ChunkPos(entry.getLongKey()), false))).rmc$setTimestamp(this.currentTime);
                }
                try {
                    if (ticketTrackerField == null) {
                        for (Field field : TicketManager.class.getDeclaredFields()) {
                            if (field.getType().getName().contains("ChunkTicketTracker")) {
                                ticketTrackerField = field;
                                break;
                            }
                        }
                    }
                    Object ticketTrackerObj = ticketTrackerField.get(this);
                    ((ChunkDistanceGraph) ticketTrackerObj).updateSourceLevel(entry.getLongKey(), getLevel(entry.getValue()), false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            // Mohist end

            if (((SortedArraySet)entry.getValue()).isEmpty()) {
                objectiterator.remove();
            }
        }

    }

    @Overwrite
    public void release(long chunkPosIn, Ticket<?> ticketIn) {
        SortedArraySet<Ticket<?>> sortedarrayset = this.getTicketSet(chunkPosIn);
        if (sortedarrayset.remove(ticketIn)) {
        }

        // Mohist start - Introduce 'chunk_unload_delay' option
        if (ticketIn.getType() == TicketType.PLAYER && TickManager.UNLOAD_DELAY.getLifespan() > 0) { // Mist - Disable chunk unload delay if <= 0
            //System.out.println("add2");
            ((TicketEx)(Object) sortedarrayset.func_226175_a_(new Ticket<ChunkPos>(TickManager.UNLOAD_DELAY, 33, new ChunkPos(chunkPosIn), false))).rmc$setTimestamp(this.currentTime);
        }
        // Mohist end

        if (sortedarrayset.isEmpty()) {
            this.tickets.remove(chunkPosIn);
        }

        try {
            if (ticketTrackerField == null) {
                for (Field field : TicketManager.class.getDeclaredFields()) {
                    if (field.getType().getName().contains("ChunkTicketTracker")) {
                        ticketTrackerField = field;
                        break;
                    }
                }
            }
            Object ticketTrackerObj = ticketTrackerField.get(this);
            ((ChunkDistanceGraph) ticketTrackerObj).updateSourceLevel(chunkPosIn, getLevel(sortedarrayset), false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (ticketIn.isForceTicks()) {
            SortedArraySet<Ticket<?>> tickets = (SortedArraySet)this.forcedTickets.get(chunkPosIn);
            if (tickets != null) {
                tickets.remove(ticketIn);
            }
        }

    }

}
