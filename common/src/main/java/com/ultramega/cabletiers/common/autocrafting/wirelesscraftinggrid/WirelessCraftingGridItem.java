package com.ultramega.cabletiers.common.autocrafting.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.common.api.support.energy.AbstractNetworkEnergyItem;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemHelper;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public class WirelessCraftingGridItem extends AbstractNetworkEnergyItem {
    protected WirelessCraftingGridItem(Properties properties, EnergyItemHelper energyItemHelper, NetworkItemHelper helper) {
        super(properties, energyItemHelper, helper);
    }

    /// @param name
    /// @param player
    /// @param playerSlotReference
    /// @param context
    @Override
    protected void use(@Nullable Component name, ServerPlayer player, PlayerSlotReference playerSlotReference, NetworkItemContext context) {

    }
}
