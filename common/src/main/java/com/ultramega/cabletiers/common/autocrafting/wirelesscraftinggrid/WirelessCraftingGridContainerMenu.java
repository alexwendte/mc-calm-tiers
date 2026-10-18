package com.ultramega.cabletiers.common.autocrafting.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.common.grid.AbstractCraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.GridData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class WirelessCraftingGridContainerMenu extends AbstractCraftingGridContainerMenu {
    protected WirelessCraftingGridContainerMenu(MenuType<? extends AbstractGridContainerMenu> menuType, int syncId, Inventory playerInventory, GridData gridData) {
        super(menuType, syncId, playerInventory, gridData);
    }
}
