package rmc.mixins.tick_manager.extend;

import com.google.common.collect.Table;

import codechicken.chunkloader.world.Organiser;

/**
 * Developed by RMC Team, 2021
 */
public interface ChunkLoaderHandlerEx {

    public Table<?, ?, Organiser> rmc$getPlayerOrganisers();

}