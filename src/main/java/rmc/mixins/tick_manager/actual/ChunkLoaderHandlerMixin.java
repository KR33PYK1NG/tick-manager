package rmc.mixins.tick_manager.actual;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.collect.Table;

import codechicken.chunkloader.world.ChunkLoaderHandler;
import codechicken.chunkloader.world.Organiser;
import rmc.mixins.tick_manager.extend.ChunkLoaderHandlerEx;

/**
 * Developed by RMC Team, 2021
 */
@Mixin(value = ChunkLoaderHandler.class)
public abstract class ChunkLoaderHandlerMixin
implements ChunkLoaderHandlerEx {

    @Shadow @Final private Table<?, ?, Organiser> playerOrganisers;

    @Override
    public Table<?, ?, Organiser> rmc$getPlayerOrganisers() {
        return this.playerOrganisers;
    }

}