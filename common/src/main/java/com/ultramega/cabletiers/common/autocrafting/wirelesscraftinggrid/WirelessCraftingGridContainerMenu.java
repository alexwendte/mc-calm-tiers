package com.ultramega.cabletiers.common.autocrafting.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.grid.AbstractCraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.CraftingGrid;
import com.ultramega.cabletiers.common.registry.Menus;
import net.minecraft.world.entity.player.Inventory;

public class WirelessCraftingGridContainerMenu extends AbstractCraftingGridContainerMenu {
    public WirelessCraftingGridContainerMenu(final int syncId,
                                             final Inventory playerInventory,
                                             final WirelessCraftingGridData wirelessCraftingGridData) {
        super(Menus.INSTANCE.getWirelessCraftingGrid(), syncId, playerInventory, wirelessCraftingGridData.gridData());
        this.disabledSlot = wirelessCraftingGridData.playerSlotReference();
        resized(0, 0, 0);
    }
    
    WirelessCraftingGridContainerMenu(final int syncId,
                                      final Inventory playerInventory,
                                      final CraftingGrid craftingGrid,
                                      final PlayerSlotReference playerSlotReference) {
        super(Menus.INSTANCE.getWirelessCraftingGrid(), syncId, playerInventory, craftingGrid);
        this.disabledSlot = playerSlotReference;
        resized(0, 0, 0);
    }
}
