package com.ultramega.cabletiers.common.autocrafting.wirelesspatterngrid;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.grid.AbstractCraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.CraftingGrid;
import com.ultramega.cabletiers.common.registry.Menus;
import net.minecraft.world.entity.player.Inventory;

public class WirelessPatternGridContainerMenu extends AbstractCraftingGridContainerMenu {
    public WirelessPatternGridContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final WirelessPatternGridData wirelessPatternGridData) {
        super(Menus.INSTANCE.getWirelessCraftingGrid(), syncId, playerInventory, wirelessPatternGridData.gridData());
        this.disabledSlot = wirelessPatternGridData.playerSlotReference();
        resized(0, 0, 0);
    }
    
    WirelessPatternGridContainerMenu(final int syncId,
                                     final Inventory playerInventory,
                                     final CraftingGrid craftingGrid,
                                     final PlayerSlotReference playerSlotReference) {
        super(Menus.INSTANCE.getWirelessCraftingGrid(), syncId, playerInventory, craftingGrid);
        this.disabledSlot = playerSlotReference;
        resized(0, 0, 0);
    }
}
