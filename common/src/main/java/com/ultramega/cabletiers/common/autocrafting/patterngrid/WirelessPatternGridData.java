package com.ultramega.cabletiers.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import com.refinedmods.refinedstorage.common.grid.GridData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record WirelessPatternGridData(GridData gridData, PlayerSlotReference playerSlotReference) {
    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessPatternGridData> STREAM_CODEC = StreamCodec.composite(
        GridData.STREAM_CODEC, WirelessPatternGridData::gridData,
        PlayerSlotReference.STREAM_CODEC, WirelessPatternGridData::playerSlotReference,
        WirelessPatternGridData::new
    );
}
