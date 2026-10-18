package com.ultramega.cabletiers.common.autocrafting.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.grid.GridData;

public record WirelessCraftingGridData(GridData gridData, PlayerSlotReference playerSlotReference) {
}
