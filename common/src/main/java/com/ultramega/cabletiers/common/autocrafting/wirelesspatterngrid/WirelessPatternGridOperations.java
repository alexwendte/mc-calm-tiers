package com.ultramega.cabletiers.common.autocrafting.wirelesspatterngrid;

import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridWatcherManager;
import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridInsertMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;

class WirelessPatternGridOperations implements GridOperations {
    private final GridOperations delegate;
    private final NetworkItemContext context;
    private final GridWatcherManager watchers;

    WirelessPatternGridOperations(final GridOperations delegate,
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

    /// Inserts the given resource into the given storage (I think it is the network storage itself - I don't think it is an external machine for example)
    /// I don't think an amount is configured but the insertMode
    /// I wonder if the GridInsertMode is left click (all) or right click (one)
    ///
    /// @param resource   the resource
    /// @param insertMode the insertion mode
    /// @param source     the source
    /// @return
    @Override
    public boolean insert(final ResourceKey resource,
                          final GridInsertMode insertMode,
                          final ExtractableStorage source) {
        final boolean success = delegate.insert(resource, insertMode, source);
        if (success) {
            drain(Platform.INSTANCE.getConfig().getWirelessGrid().getInsertEnergyUsage());
        }
        return success;
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
