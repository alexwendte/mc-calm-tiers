package com.ultramega.cabletiers.common.autocrafting.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

public class WirelessCraftingGridExtendedMenuProvider implements ExtendedMenuProvider<WirelessCraftingGridData> {
    /// @return
    @Override
    public WirelessCraftingGridData getMenuData() {
        return null;
    }

    /// @return
    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, WirelessCraftingGridData> getMenuCodec() {
        return null;
    }

    /// @return
    @Override
    public Component getDisplayName() {
        return null;
    }

    /// @param i
    /// @param inventory
    /// @param player
    /// @return
    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }
}
