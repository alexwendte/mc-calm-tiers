package com.ultramega.cabletiers.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridData; // Adjust import to your package

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternType;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.ProcessingInputData;
import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WirelessPatternGridData(PatternGridData patternGridData,
                                      PatternType patternType,
                                      ProcessingInputData processingInputData,
                                      ResourceContainerData processingOutputData,
                                      int stonecutterSelectedRecipe,
                                      PlayerSlotReference playerSlotReference
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessPatternGridData> STREAM_CODEC = StreamCodec.composite(
        PatternGridData.STREAM_CODEC, WirelessPatternGridData::patternGridData,
        PlatformUtil.enumStreamCodec(PatternType.values()), WirelessPatternGridData::patternType,
        ProcessingInputData.STREAM_CODEC, WirelessPatternGridData::processingInputData,
        ResourceContainerData.STREAM_CODEC, WirelessPatternGridData::processingOutputData,
        ByteBufCodecs.INT, WirelessPatternGridData::stonecutterSelectedRecipe,
        WirelessPatternGridData::new
    );
}