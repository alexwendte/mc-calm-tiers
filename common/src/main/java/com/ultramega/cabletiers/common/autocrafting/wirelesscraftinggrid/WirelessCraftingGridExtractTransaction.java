package com.ultramega.cabletiers.common.autocrafting.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.grid.ExtractTransaction;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.ultramega.cabletiers.common.Platform;
import net.minecraft.world.entity.player.Player;

class WirelessCraftingGridExtractTransaction implements ExtractTransaction {
    private final NetworkItemContext context;
    private final ExtractTransaction delegate;

    WirelessCraftingGridExtractTransaction(final NetworkItemContext context, final ExtractTransaction delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    /// I THINK performs the actual removal of the item(s) from the network (if direct)
    /// I THINK just updates available/used  (if snapshot).
    @Override
    public boolean extract(final ItemResource itemResource, final Player player) {
        return delegate.extract(itemResource, player);
    }

    /// I THINK For snapshot this is the "accept" method so the resources actually get removed
    @Override
    public void close() {
        delegate.close();
        context.drainEnergy(Platform.getConfig().getWirelessCraftingGrid().getCraftingEnergyUsage());
    }
}
