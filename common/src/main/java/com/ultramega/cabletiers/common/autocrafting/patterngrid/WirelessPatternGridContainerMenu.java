package com.ultramega.cabletiers.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridContainerMenu;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridData;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.ProcessingInputData;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.ultramega.cabletiers.common.registry.Menus;
import net.minecraft.world.entity.player.Inventory;

public class WirelessPatternGridContainerMenu extends PatternGridContainerMenu {
    // 1. Client / Data packet constructor (used by ExtendedMenuTypeFactory)
    public WirelessPatternGridContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final WirelessPatternGridData wirelessPatternGridData) {
        super(syncId, playerInventory, wirelessPatternGridData.patternGridData());
        this.disabledSlot = wirelessPatternGridData.playerSlotReference();
        resized(0, 0, 0);
    }

    // 2. Optional server-side helper constructor (if opening directly from server item logic)
    WirelessPatternGridContainerMenu(final int syncId,
                                     final Inventory playerInventory,
                                     // TODO surely this Grid really needs to be a PatternGrid (which doesn't exist today)
                                     final WirelessPatternGrid wirelessPatternGrid,
                                     final PlayerSlotReference playerSlotReference) {
        // the 2nd constructor method for PatternGridContainerMenu requires PatternGridBlockEntity, so we will just provide PatternGridData
        super(syncId, playerInventory, new PatternGridData(
            GridData.of(wirelessPatternGrid),
            wirelessPatternGrid.getPatternType(),
            ProcessingInputData.of(wirelessPatternGrid.getProcessingInput()),
            ResourceContainerData.of(wirelessPatternGrid.getProcessingOutput()),
            wirelessPatternGrid.getStonecutterSelectedRecipe()
        ));
        this.disabledSlot = playerSlotReference;
        resized(0, 0, 0);
    }
}