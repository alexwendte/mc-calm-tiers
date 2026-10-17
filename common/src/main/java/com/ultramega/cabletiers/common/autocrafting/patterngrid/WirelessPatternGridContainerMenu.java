package com.ultramega.cabletiers.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.ultramega.cabletiers.common.registry.Menus;
import net.minecraft.world.entity.player.Inventory;

public class WirelessPatternGridContainerMenu extends AbstractGridContainerMenu {
    public WirelessPatternGridContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final WirelessPatternGridData wirelessGridData) {
        super(Menus.INSTANCE.getWirelessPatternGrid(), syncId, playerInventory, wirelessGridData.gridData());
        this.disabledSlot = wirelessGridData.playerSlotReference();
        resized(0, 0, 0);
    }

    WirelessPatternGridContainerMenu(final int syncId,
                                     final Inventory playerInventory,
                                     final Grid grid,
                                     final PlayerSlotReference playerSlotReference) {
        super(Menus.INSTANCE.getWirelessPatternGrid(), syncId, playerInventory, grid);
        this.disabledSlot = playerSlotReference;
        resized(0, 0, 0);
    }
}
