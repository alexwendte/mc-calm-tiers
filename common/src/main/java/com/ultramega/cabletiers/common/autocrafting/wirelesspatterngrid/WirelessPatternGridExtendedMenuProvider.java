package com.ultramega.cabletiers.common.autocrafting.wirelesspatterngrid;

import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class WirelessPatternGridExtendedMenuProvider implements ExtendedMenuProvider<WirelessPatternGridData> {
    private final Component name;
    private final Grid grid;
    private final PlayerSlotReference playerSlotReference;

    WirelessPatternGridExtendedMenuProvider(final Component name,
                                            final Grid grid,
                                            final PlayerSlotReference playerSlotReference) {
        this.name = name;
        this.grid = grid;
        this.playerSlotReference = playerSlotReference;
    }

    @Override
    public WirelessPatternGridData getMenuData() {
        return new WirelessPatternGridData(
            GridData.of(grid),
            playerSlotReference
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, WirelessPatternGridData> getMenuCodec() {
        return WirelessPatternGridData.STREAM_CODEC;
    }


    @Override
    public Component getDisplayName() {
        return name;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new WirelessPatternGridContainerMenu(syncId, inventory, grid, playerSlotReference);
    }
}
