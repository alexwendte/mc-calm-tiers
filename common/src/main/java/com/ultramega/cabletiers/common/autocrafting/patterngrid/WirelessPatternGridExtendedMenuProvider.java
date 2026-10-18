package com.ultramega.cabletiers.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridData;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.ProcessingInputData;
import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;

import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

class WirelessPatternGridExtendedMenuProvider implements ExtendedMenuProvider<WirelessPatternGridData> {
    private final Component name;
    // TODO surely this Grid really needs to be a PatternGrid (which doesn't exist today)
    private final WirelessPatternGrid wirelessPatternGrid;
    private final PlayerSlotReference playerSlotReference;

    // TODO surely this Grid really needs to be a PatternGrid (which doesn't exist today)
    WirelessPatternGridExtendedMenuProvider(final Component name, final WirelessPatternGrid wirelessPatternGrid,
                                            final PlayerSlotReference playerSlotReference) {
        this.name = name;
        this.wirelessPatternGrid = wirelessPatternGrid;
        this.playerSlotReference = playerSlotReference;
    }

    @Override
    public WirelessPatternGridData getMenuData() {
        return new WirelessPatternGridData(
            new PatternGridData(
                GridData.of(wirelessPatternGrid),
                wirelessPatternGrid.getPatternType(),
                ProcessingInputData.of(wirelessPatternGrid.getProcessingInput()),
                ResourceContainerData.of(wirelessPatternGrid.getProcessingOutput()),
                wirelessPatternGrid.getStonecutterSelectedRecipe()
            ),
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new WirelessPatternGridContainerMenu(syncId, inventory, wirelessPatternGrid, playerSlotReference);
    }
}
