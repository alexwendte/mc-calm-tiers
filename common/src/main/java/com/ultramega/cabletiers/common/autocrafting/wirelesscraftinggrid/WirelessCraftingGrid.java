package com.ultramega.cabletiers.common.autocrafting.wirelesscraftinggrid;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridWatcherManager;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridWatcherManagerImpl;
import com.refinedmods.refinedstorage.api.network.node.grid.EmptyGridOperations;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.NoopStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.PlatformSecurityNetworkComponent;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.grid.*;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.ultramega.cabletiers.common.Platform;
import com.ultramega.cabletiers.common.registry.DataComponents;
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
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

class WirelessCraftingGrid implements CraftingGrid {
    private final NetworkItemContext context;
    private final Level level;
    private final GridWatcherManager watchers = new GridWatcherManagerImpl();
    @Nullable
    private final RecipeMatrix<CraftingRecipe, CraftingInput> recipeMatrix;
    /// playerSlotReference is the reference to the slot in the player's inventory where the wireless crafting grid item is located.
    private final PlayerSlotReference playerSlotReference;

    WirelessCraftingGrid(final Player player, final NetworkItemContext context,
                         final PlayerSlotReference playerSlotReference) {
        this.level = player.level();
        this.context = context;
        this.playerSlotReference = playerSlotReference;
        // we add storeCurrentRecipeMatrix as a listener to the recipeMatrix so that whenever the matrix is updated, it will call storeCurrentRecipeMatrix to update the wireless crafting grid's state accordingly.
        this.recipeMatrix = createAndRestoreRecipeMatrix(player, () -> storeCurrentRecipeMatrix(player), playerSlotReference);
    }


    /// This method creates a new RecipeMatrix for the wireless crafting grid (I ASSUME it happens each time the grid is opened).
    /// It initializes the matrix with the items stored in the wireless crafting grid's state
    ///
    /// @param player
    /// @param listener            called whenever the matrix is updated, allowing us to update the wireless crafting grid's state accordingly.
    /// @param playerSlotReference
    private static RecipeMatrix<CraftingRecipe, CraftingInput> createAndRestoreRecipeMatrix(
        final Player player,
        final Runnable listener,
        final PlayerSlotReference playerSlotReference
    ) {
        // create a brand-new empty crafting grid matrix (3x3)
        final RecipeMatrix<CraftingRecipe, CraftingInput> recipeMatrix = RecipeMatrix.crafting(listener, player::level);
        // get the wireless crafting grid item from the player's inventory
        final ItemStack wirelessGridItem = playerSlotReference.get(player);
        // update the crafting grid matrix with the items stored in the wireless crafting grid's state
        setMatrixSlots(wirelessGridItem, recipeMatrix);
        return recipeMatrix;
    }


    /// This method updates the recipeMatrix with the items stored in the wireless crafting grid's state.
    ///
    /// @param wirelessGridItem this represents the wireless crafting grid itself. any items in the crafting grid are stored in the wireless crafting grid's state, which is stored in the wirelessGridItem's data component
    /// @param recipeMatrix     this represents the actual recipeMatrix matrix (3x3 grid) shown to the user. We need to populate it with the items stored in the wireless crafting grid's state, which is stored in the wirelessGridItem's data component
    private static void setMatrixSlots(final ItemStack wirelessGridItem,
                                       final RecipeMatrix<CraftingRecipe, CraftingInput> recipeMatrix) {
        final WirelessCraftingGridState state = wirelessGridItem.get(DataComponents.INSTANCE.getWirelessCraftingGridState());
        if (state == null) {
            return;
        }
        final CraftingInput.Positioned positioned = state.input();
        final int left = positioned.left();
        final int top = positioned.top();
        final CraftingInput input = positioned.input();
        for (int y = 0; y < input.height(); ++y) {
            for (int x = 0; x < input.width(); ++x) {
                final int matrixIndex = x + left + (y + top) * recipeMatrix.getMatrix().getWidth();
                recipeMatrix.getMatrix().setItem(matrixIndex, input.getItem(x, y).copy());
            }
        }
    }

    /// This method updates the wireless crafting grid's state with the current items in the recipeMatrix.
    /// It is called RecipeMatrix itself whenever the recipeMatrix is updated
    ///
    /// @param player
    private void storeCurrentRecipeMatrix(final Player player) {
        // if the matrix hasn't been created then we can't update the crafting recipe
        if (recipeMatrix == null) {
            return;
        }
        final CraftingInput.Positioned input = recipeMatrix.getMatrix().asPositionedCraftInput();
        final ItemStack wirelessGridItem = playerSlotReference.get(player);
        wirelessGridItem.set(DataComponents.INSTANCE.getWirelessCraftingGridState(), new WirelessCraftingGridState(input));
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

    /// This method returns the network as long as the following are true:
    ///     1. The grid is active (I THINK has energy and is bound)
    ///     2. The network is reachable (I THINK has energy and is bound)
    ///    3. The network is in range -> I IMAGINE this is part of `isGridActive` but I can't confirm
    private Optional<Network> getNetworkForActiveGrid() {
        if (!isGridActive()) {
            return Optional.empty();
        }
        return context.resolveNetwork();
    }

    /// @return
    @Override
    public RecipeMatrixContainer getCraftingMatrix() {
        return requireNonNull(recipeMatrix).getMatrix();
    }

    /// Simply returns the crafting result which is maintained in RecipeMatrix
    ///
    /// @return
    @Override
    public ResultContainer getCraftingResult() {
        return requireNonNull(recipeMatrix).getResult();
    }

    /// Not sure what this does at all, but it internally calls Platform.INSTANCE.getRemainingCraftingItems(player, craftingRecipe, input)
    ///
    /// @param player
    /// @param craftingInput
    /// @return
    @Override
    public NonNullList<ItemStack> getRemainingItems(final Player player, final CraftingInput craftingInput) {
        return requireNonNull(recipeMatrix).getRemainingItems(level, player, craftingInput);
    }

    /// I THINK performs the actual removal of the item(s) from the network (if direct)
    /// I THINK just updates available/used  (if snapshot).
    ///
    /// @param player
    /// @param directCommit
    /// @return
    @Override
    public ExtractTransaction startExtractTransaction(final Player player, final boolean directCommit) {
        return getNetworkForActiveGrid()
            .map(network -> network.getComponent(StorageNetworkComponent.class))
            .map(storage -> (ExtractTransaction) new WirelessCraftingGridExtractTransaction(
                context,
                createExtractTransaction(player, directCommit, storage)
            ))
            .orElse(ExtractTransaction.NOOP);
    }

    /// I THINK performs the actual removal of the item(s) from the network (if direct)
    /// I THINK just updates available/used  (if snapshot).
    private ExtractTransaction createExtractTransaction(final Player player,
                                                        final boolean directCommit,
                                                        final StorageNetworkComponent storage) {
        return directCommit
            ? new DirectCommitExtractTransaction(storage)
            : new SnapshotExtractTransaction(player, storage, getCraftingMatrix());
    }

    /// clears the matrix in to the player inventory or storage depending on the toPlayerInventory flag.
    /// If the matrix is cleared successfully, it drains energy from the wireless crafting grid item.
    ///
    /// @param player
    /// @param toPlayerInventory
    /// @return
    @Override
    public boolean clearMatrix(final Player player, final boolean toPlayerInventory) {
        final boolean success = toPlayerInventory
            ? getCraftingMatrix().clearToPlayerInventory(player)
            : clearMatrixIntoStorage(player);
        if (success) {
            context.drainEnergy(Platform.getConfig().getWirelessCraftingGrid().getClearMatrixEnergyUsage());
        }
        return success;
    }

    /// clears the matrix into storage - broken out because clearing into storage needs access to the network storage
    private boolean clearMatrixIntoStorage(final Player player) {
        return getNetworkForActiveGrid()
            .map(network -> network.getComponent(StorageNetworkComponent.class))
            .map(storage -> getCraftingMatrix().clearIntoStorage(storage, player))
            .orElse(false);
    }

    /// The + button being pressed from JEI
    /// Clears the board first (whatever is currently sitting in the crafting grid)
    /// It sorts the recipe options based on what is currently available in the network
    /// Once a recipe is selected, it populates the grid with items based on
    ///     1. What is available in the network
    ///     2. What is available in the player's inventory
    ///
    /// @param player
    /// @param recipe
    @Override
    public void transferRecipe(final Player player, final List<List<ItemResource>> recipe) {
        context.drainEnergy(Platform.getConfig().getWirelessCraftingGrid().getRecipeTransferEnergyUsage());
        getCraftingMatrix().transferRecipe(
            player,
            getNetworkForActiveGrid().map(network -> network.getComponent(StorageNetworkComponent.class))
                .orElse(null),
            recipe
        );
    }

    // this runs when the player is using the crafting grid to craft an item and shift-clicks the item to craft it quickly

    /// @param player
    /// @param craftedStack
    @Override
    public void acceptQuickCraft(final Player player, final ItemStack craftedStack) {
        // I think this means if the player has space in their inventory then it just gets added and that's all
        if (player.getInventory().add(craftedStack)) {

            return;
        }
        // if the player doesn't have space in their inventory then it tries to insert it into the network
        // however, we may not be able to insert all items into the network if it runs out of space or energy
        final long inserted = getNetworkForActiveGrid()
            .map(network -> network.getComponent(StorageNetworkComponent.class))
            .map(rootStorage -> rootStorage.insert(
                ItemResource.ofItemStack(craftedStack),
                craftedStack.getCount(),
                Action.EXECUTE,
                new PlayerActor(player)
            ))
            .orElse(0L);
        // if there isn't enough space in the network then the remainder is dropped on the ground
        if (inserted != craftedStack.getCount()) {
            final long remainder = craftedStack.getCount() - inserted;
            final ItemStack remainderStack = craftedStack.copyWithCount((int) remainder);
            player.drop(remainderStack, false);
        }
    }

    /// @param watcher
    /// @param actorType
    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        context.drainEnergy(Platform.getConfig().getWirelessCraftingGrid().getOpenEnergyUsage());
        final Optional<Network> optionalNetwork = context.resolveNetwork();
        final StorageNetworkComponent storage = optionalNetwork
            .map(network -> network.getComponent(StorageNetworkComponent.class))
            .orElse(null);
        final AutocraftingNetworkComponent autocrafting = optionalNetwork
            .map(network -> network.getComponent(AutocraftingNetworkComponent.class))
            .orElse(null);
        watchers.addWatcher(watcher, actorType, storage, autocrafting);
    }

    /// @param watcher
    @Override
    public void removeWatcher(final GridWatcher watcher) {
        final Optional<Network> optionalNetwork = context.resolveNetwork(true);
        final StorageNetworkComponent storage = optionalNetwork
            .map(network -> network.getComponent(StorageNetworkComponent.class))
            .orElse(null);
        final AutocraftingNetworkComponent autocrafting = optionalNetwork
            .map(network -> network.getComponent(AutocraftingNetworkComponent.class))
            .orElse(null);
        watchers.removeWatcher(watcher, storage, autocrafting);
    }

    /// @return
    @Override
    public Storage getItemStorage() {
        return getStorage().map(Storage.class::cast).orElseGet(NoopStorage::new);
    }

    // networkActive is checking whether the connected network has energy or
    // whether energy is not required in the config
    // if the network couldn't be resolve then we are out of range and the grid is not active
    // context.isActive() potentially checks if the item has energy & is bound to a network/in range (but can't be for sure)

    /// @return
    @Override
    public boolean isGridActive() {
        final boolean networkActive = context.resolveNetwork()
            .map(network -> !RefinedStorageApi.INSTANCE.isEnergyRequired()
                || network.getComponent(EnergyNetworkComponent.class).getStored() > 0)
            .orElse(false);
        return networkActive && context.isActive();
    }

    /// @param actorType
    /// @return
    @Override
    public List<TrackedResourceAmount> getResources(final Class<? extends Actor> actorType) {
        return getStorage().map(storage -> storage.getResources(actorType)).orElse(Collections.emptyList());
    }

    /// @return
    @Override
    public Set<PlatformResourceKey> getAutocraftableResources() {
        return getAutocrafting()
            .map(AutocraftingNetworkComponent::getOutputs)
            .map(outputs -> outputs.stream()
                .filter(PlatformResourceKey.class::isInstance)
                .map(PlatformResourceKey.class::cast)
                .collect(Collectors.toSet()))
            .orElse(Collections.emptySet());
    }

    /// @return
    @Override
    public Map<PlatformResourceKey, Set<TaskId>> getCurrentlyAutocrafting() {
        return getAutocrafting().map(autocrafting -> autocrafting
            .getStatuses()
            .stream()
            .filter(status -> status.info().resource() instanceof PlatformResourceKey)
            .collect(Collectors.groupingBy(
                status -> (PlatformResourceKey) status.info().resource(),
                Collectors.mapping(status -> status.info().id(), Collectors.toSet())
            ))).orElse(Collections.emptyMap());
    }

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


    /// Creates grid operations for the given resource type and player.
    /// Ensures they have connection to the network
    /// Ensures they have security access
    /// Drains energy on success
    ///
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

    /// Creates grid operations for the given resource and amount.
    /// Runs them through the security network layer
    private GridOperations createGridOperations(final ResourceType resourceType,
                                                final ServerPlayer player,
                                                final StorageNetworkComponent rootStorage,
                                                final PlatformSecurityNetworkComponent securityNetworkComponent) {
        final PlayerActor playerActor = new PlayerActor(player);
        final GridOperations operations = resourceType.createGridOperations(rootStorage, playerActor);
        return new SecuredGridOperations(player, securityNetworkComponent, operations);
    }


    /// Resolves the preview for the given resource and amount
    ///
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

    /// Resolves the tree preview for the given resource and amount
    ///
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

    /// Resolves the maximum amount of the given resource that can be autocrafted
    ///
    /// @param resource
    /// @param cancellationToken
    /// @return
    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource, final CancellationToken cancellationToken) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.getMaxAmount(resource, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(0L));
    }


    /// Kicks off an autocrafting task for the given resource and amount
    /// If the task is successfully started, it drains energy from the wireless crafting grid item

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
