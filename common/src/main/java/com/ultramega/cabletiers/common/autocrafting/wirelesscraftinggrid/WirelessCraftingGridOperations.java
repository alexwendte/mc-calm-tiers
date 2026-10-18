package com.ultramega.cabletiers.common.autocrafting.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridWatcherManager;
import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridInsertMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;

class WirelessCraftingGridOperations implements GridOperations {
    private final GridOperations delegate;
    private final NetworkItemContext context;
    private final GridWatcherManager watchers;

    WirelessCraftingGridOperations(final GridOperations delegate,
                                   final NetworkItemContext context,
                                   final GridWatcherManager watchers) {
        this.delegate = delegate;
        this.context = context;
        this.watchers = watchers;
    }

    /**
     * Tries to move a resource from the network storage to the destination.
     * The amount being extracted depends on the extraction mode.
     *
     * @param resource    the resource
     * @param extractMode the extract mode
     * @param destination the destination
     */
    @Override
    public boolean extract(final ResourceKey resource, final GridExtractMode extractMode, final InsertableStorage destination) {
        final boolean success = delegate.extract(resource, extractMode, destination);
        if (success) {
            drain(Platform.INSTANCE.getConfig().getWirelessGrid().getExtractEnergyUsage());
        }
        return success;
    }

    /**
     * Tries to move a resource from the source to the network storage.
     * The amount being inserted depends on the insert mode.
     *
     * @param resource   the resource
     * @param insertMode the insertion mode
     * @param source     the source
     */
    @Override
    public boolean insert(ResourceKey resource, GridInsertMode insertMode, ExtractableStorage source) {
        return false;
    }
    
    private void drain(final long amount) {
        final boolean wasActive = context.isActive();
        context.drainEnergy(amount);
        final boolean isActive = context.isActive();
        if (wasActive != isActive) {
            watchers.activeChanged(isActive);
        }
    }
}
