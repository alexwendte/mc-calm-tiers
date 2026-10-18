package com.ultramega.cabletiers.common.autocrafting.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridWatcherManager;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridWatcherManagerImpl;
import com.refinedmods.refinedstorage.api.network.node.grid.EmptyGridOperations;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.common.api.security.PlatformSecurityNetworkComponent;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.grid.CraftingGrid;
import com.refinedmods.refinedstorage.common.grid.ExtractTransaction;
import com.refinedmods.refinedstorage.common.grid.SecuredGridOperations;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.ultramega.cabletiers.common.Platform;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

class WirelessCraftingGrid implements CraftingGrid {
    private final NetworkItemContext context;
    private final Level level;
    private final GridWatcherManager watchers = new GridWatcherManagerImpl();
    @Nullable
    private final RecipeMatrix<CraftingRecipe, CraftingInput> craftingRecipe;
    private final PlayerSlotReference playerSlotReference;

    WirelessCraftingGrid(final Player player, final NetworkItemContext context,
                         final PlayerSlotReference playerSlotReference) {
        this.level = player.level();
        this.context = context;
        this.playerSlotReference = playerSlotReference;
        this.craftingRecipe = createMatrix(player, () -> updateCraftingRecipe(player), playerSlotReference);
    }


    private static RecipeMatrix<CraftingRecipe, CraftingInput> createMatrix(
        final Player player,
        final Runnable listener,
        final PlayerSlotReference playerSlotReference
    ) {
        return null;
    }

    private static void setMatrixSlots(final ItemStack stack,
                                       final RecipeMatrix<CraftingRecipe, CraftingInput> recipe) {
        return null;
    }

    private void updateCraftingRecipe(final Player player) {
        return null;
    }

    private Optional<StorageNetworkComponent> getStorage() {
        return context.resolveNetwork().map(network -> network.getComponent(StorageNetworkComponent.class));
    }

    private Optional<PlatformSecurityNetworkComponent> getSecurity() {
        return context.resolveNetwork().map(network -> network.getComponent(PlatformSecurityNetworkComponent.class));
    }

    private Optional<AutocraftingNetworkComponent> getAutocrafting() {
        return context.resolveNetwork().map(network -> network.getComponent(AutocraftingNetworkComponent.class));
    }

    private Optional<Network> getNetworkForActiveGrid() {
        return null;
    }

    /// @return
    @Override
    public RecipeMatrixContainer getCraftingMatrix() {
        return null;
    }

    /// @return
    @Override
    public ResultContainer getCraftingResult() {
        return null;
    }

    /// @param player
    /// @param input
    /// @return
    @Override
    public NonNullList<ItemStack> getRemainingItems(Player player, CraftingInput input) {
        return null;
    }

    /// @param player
    /// @param directCommit
    /// @return
    @Override
    public ExtractTransaction startExtractTransaction(Player player, boolean directCommit) {
        return null;
    }

    /// @param player
    /// @param toPlayerInventory
    /// @return
    @Override
    public boolean clearMatrix(Player player, boolean toPlayerInventory) {
        return false;
    }

    /// @param player
    /// @param recipe
    @Override
    public void transferRecipe(Player player, List<List<ItemResource>> recipe) {

    }

    /// @param player
    /// @param craftedStack
    @Override
    public void acceptQuickCraft(Player player, ItemStack craftedStack) {

    }

    /// @param watcher
    /// @param actorType
    @Override
    public void addWatcher(GridWatcher watcher, Class<? extends Actor> actorType) {

    }

    /// @param watcher
    @Override
    public void removeWatcher(GridWatcher watcher) {

    }

    /// @return
    @Override
    public Storage getItemStorage() {
        return null;
    }

    /// @return
    @Override
    public boolean isGridActive() {
        return false;
    }

    /// @param actorType
    /// @return
    @Override
    public List<TrackedResourceAmount> getResources(Class<? extends Actor> actorType) {
        return List.of();
    }

    /// @return
    @Override
    public Set<PlatformResourceKey> getAutocraftableResources() {
        return Set.of();
    }

    /// @return
    @Override
    public Map<PlatformResourceKey, Set<TaskId>> getCurrentlyAutocrafting() {
        return Map.of();
    }

    // TODO stopped here

    /// @param taskIds
    /// @return
    @Override
    public List<TaskStatus> getAutocraftingTaskStatuses(final Set<TaskId> taskIds) {
        return getAutocrafting().map(autocrafting -> autocrafting
            .getStatuses()
            .stream()
            .filter(status -> taskIds.contains(status.info().id()))
            .toList()).orElse(Collections.emptyList());
    }

    /// @param resourceType
    /// @param player
    /// @return
    @Override
    public GridOperations createOperations(final ResourceType resourceType, final ServerPlayer player) {
        return getStorage()
            .flatMap(rootStorage -> getSecurity()
                .map(security -> createGridOperations(resourceType, player, rootStorage, security)))
            .map(operations -> (GridOperations) new WirelessCraftingGridOperations(operations, context, watchers))
            .orElse(EmptyGridOperations.INSTANCE);
    }

    /// @param player
    /// @return
    @Override
    public boolean canMenuStayOpen(Player player) {
        return true;
    }

    private GridOperations createGridOperations(final ResourceType resourceType,
                                                final ServerPlayer player,
                                                final StorageNetworkComponent rootStorage,
                                                final PlatformSecurityNetworkComponent securityNetworkComponent) {
        final PlayerActor playerActor = new PlayerActor(player);
        final GridOperations operations = resourceType.createGridOperations(rootStorage, playerActor);
        return new SecuredGridOperations(player, securityNetworkComponent, operations);
    }

    /// @param resource
    /// @param amount
    /// @param cancellationToken
    /// @return
    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource, final long amount, final CancellationToken cancellationToken) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.getPreview(resource, amount, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    /// @param resource
    /// @param amount
    /// @param cancellationToken
    /// @return
    @Override
    public CompletableFuture<Optional<TreePreview>> getTreePreview(final ResourceKey resource, final long amount,
                                                                   final CancellationToken cancellationToken) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.getTreePreview(resource, amount, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    /// @param resource
    /// @param cancellationToken
    /// @return
    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource, final CancellationToken cancellationToken) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.getMaxAmount(resource, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(0L));
    }

    /// @param resourceKey
    /// @param amount
    /// @param actor
    /// @param notify
    /// @param cancellationToken
    /// @return
    @Override
    public Optional<TaskId> startTask(final ResourceKey resourceKey,
                                      final long amount,
                                      final Actor actor,
                                      final boolean notify,
                                      final CancellationToken cancellationToken) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.startTask(resourceKey, amount, actor, notify, cancellationToken))
            .flatMap(taskId -> {
                context.drainEnergy(Platform.getConfig().getWirelessCraftingGrid().getAutocraftingEnergyUsage());
                return taskId;
            });

    }
}
