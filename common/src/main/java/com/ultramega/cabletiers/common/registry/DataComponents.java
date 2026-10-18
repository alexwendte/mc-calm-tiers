package com.ultramega.cabletiers.common.registry;

import com.ultramega.cabletiers.common.autocrafting.sidedinput.SidedInputPatternState;

import java.util.function.Supplier;

import com.ultramega.cabletiers.common.autocrafting.wirelesscraftinggrid.WirelessCraftingGridState;
import net.minecraft.core.component.DataComponentType;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class DataComponents {
    public static final DataComponents INSTANCE = new DataComponents();

    @Nullable
    private Supplier<DataComponentType<SidedInputPatternState>> sidedInputPatternState;

    @Nullable
    private Supplier<DataComponentType<WirelessCraftingGridState>> wirelessCraftingGridState;

    private DataComponents() {
    }

    public DataComponentType<SidedInputPatternState> getSidedInputPatternState() {
        return requireNonNull(this.sidedInputPatternState).get();
    }

    public void setSidedInputPatternState(@Nullable final Supplier<DataComponentType<SidedInputPatternState>> supplier) {
        this.sidedInputPatternState = supplier;
    }

    public DataComponentType<WirelessCraftingGridState> getWirelessCraftingGridState() {
        return requireNonNull(wirelessCraftingGridState).get();
    }

    public void setWirelessCraftingGridState(
        @Nullable final Supplier<DataComponentType<WirelessCraftingGridState>> supplier
    ) {
        this.wirelessCraftingGridState = supplier;
    }
}
