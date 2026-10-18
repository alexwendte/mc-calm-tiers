package com.ultramega.cabletiers.common.autocrafting.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.common.grid.ExtractTransaction;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.world.entity.player.Player;

class WirelessCraftingGridExtractTransaction implements ExtractTransaction {
    /// @param resource
    /// @param player
    /// @return
    @Override
    public boolean extract(ItemResource resource, Player player) {
        return false;
    }

    ///
    @Override
    public void close() {

    }
}
