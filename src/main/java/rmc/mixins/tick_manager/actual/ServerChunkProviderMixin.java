package rmc.mixins.tick_manager.actual;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.mohistmc.api.mc.ChunkMcAPI;

import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketManager;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraft.world.spawner.WorldEntitySpawner.EntityDensityManager;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rmc.mixins.tick_manager.TickManager;
import rmc.mixins.tick_manager.extend.ChunkManagerEx;

/**
 * Developed by RMC Team, 2021
 */
@Mixin(value = ServerChunkProvider.class)
public abstract class ServerChunkProviderMixin {

    @Shadow @Final private TicketManager ticketManager;
    @Shadow @Final public ServerWorld world;
    @Shadow @Final public ChunkManager chunkManager;
    @Shadow public boolean spawnHostiles;
    @Shadow public boolean spawnPassives;
    @Shadow private EntityDensityManager field_241097_p_;

    @Shadow private void func_241098_a_(long p_241098_1_, Consumer<Chunk> p_241098_3_) {}

    @Inject(method = "Lnet/minecraft/world/server/ServerChunkProvider;tickChunks()V",
            cancellable = true,
            at = @At(value = "HEAD"))
    private void tickChunks(CallbackInfo mixin) {
        boolean doMobSpawning = this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
        if (doMobSpawning) {
            //this.field_241097_p_ = WorldEntitySpawner.func_234964_a_(this.ticketManager.getSpawningChunksCount(), this.world.func_241136_z_(), this::func_241098_a_);
        }
        TickManager.PENDING_CHUNKS.forEach((chunk, pci) -> {
            ChunkMcAPI.getChunkHolder(this.world, chunk.getPos().asLong()).ifPresent((holder) -> {
                holder.sendChanges(chunk);
            });
            if (pci.isAlsoEntityTicking) {
                WorldEntitySpawner.EntityDensityManager density;
                if (doMobSpawning && pci.spawnMobs && (density = TickManager.CHUNK_DENSITY.get(chunk)) != null) {
                    WorldEntitySpawner.func_234979_a_(this.world, chunk, density, this.spawnHostiles, this.spawnPassives, this.spawnPassives);
                }
                this.world.tickEnvironment(chunk, this.world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED));
            }
        });
        if (doMobSpawning) {
            this.world.func_241123_a_(this.spawnHostiles, this.spawnPassives);
        }
        ((ChunkManagerEx) this.chunkManager).rmc$tickEntityTracker();
        mixin.cancel();
    }

}
