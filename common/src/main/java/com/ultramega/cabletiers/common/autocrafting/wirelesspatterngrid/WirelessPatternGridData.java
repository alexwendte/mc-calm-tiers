package com.ultramega.cabletiers.common.autocrafting.wirelesspatterngrid;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record WirelessPatternGridData(PatternGridData patternGridData, PlayerSlotReference playerSlotReference) {
    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessPatternGridData> STREAM_CODEC =
        StreamCodec.composite(
            PatternGridData.STREAM_CODEC, WirelessPatternGridData::patternGridData,
            PlayerSlotReference.STREAM_CODEC, WirelessPatternGridData::playerSlotReference,
            WirelessPatternGridData::new
        );
}
