package com.ultramega.cabletiers.common.autocrafting.wirelesspatterngrid;

import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.ultramega.cabletiers.common.registry.Menus;
import net.minecraft.world.entity.player.Inventory;

public class WirelessPatternGridContainerMenu extends PatternGridContainerMenu {
    public WirelessPatternGridContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final WirelessPatternGridData wirelessPatternGridData) {
        super(Menus.INSTANCE.getWirelessPatternGrid(), syncId, playerInventory, wirelessPatternGridData.patternGridData());
        this.disabledSlot = wirelessPatternGridData.playerSlotReference();
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
