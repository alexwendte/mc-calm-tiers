package com.ultramega.cabletiers.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
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
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.security.PlatformSecurityNetworkComponent;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.storage.root.FuzzyRootStorage;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.*;
import com.refinedmods.refinedstorage.common.grid.FuzzyGridOperations;
import com.refinedmods.refinedstorage.common.grid.GridData;
import com.refinedmods.refinedstorage.common.grid.SecuredGridOperations;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.common.support.RecipeMatrix;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class WirelessPatternGrid implements Grid {
    /*
     * BEGIN Grid interface implementation
     */
    private final NetworkItemContext context;
    private final GridWatcherManager watchers = new GridWatcherManagerImpl();

    private final Level level;
    private final PlayerSlotReference playerSlotReference;

    WirelessPatternGrid(final Player player, final NetworkItemContext context,
                        final PlayerSlotReference playerSlotReference) {
        this.level = player.level();
        this.playerSlotReference = playerSlotReference;
        this.context = context;
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

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        context.drainEnergy(Platform.INSTANCE.getConfig().getWirelessGrid().getOpenEnergyUsage());
        final Optional<Network> optionalNetwork = context.resolveNetwork();
        final StorageNetworkComponent storage = optionalNetwork
            .map(network -> network.getComponent(StorageNetworkComponent.class))
            .orElse(null);
        final AutocraftingNetworkComponent autocrafting = optionalNetwork
            .map(network -> network.getComponent(AutocraftingNetworkComponent.class))
            .orElse(null);
        watchers.addWatcher(watcher, actorType, storage, autocrafting);
    }

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

    @Override
    public Storage getItemStorage() {
        return getStorage().map(Storage.class::cast).orElseGet(NoopStorage::new);
    }

    @Override
    public boolean isGridActive() {
        final boolean networkActive = context.resolveNetwork()
            .map(network -> !RefinedStorageApi.INSTANCE.isEnergyRequired()
                || network.getComponent(EnergyNetworkComponent.class).getStored() > 0)
            .orElse(false);
        return networkActive && context.isActive();
    }

    @Override
    public List<TrackedResourceAmount> getResources(final Class<? extends Actor> actorType) {
        return getStorage().map(storage -> storage.getResources(actorType)).orElse(Collections.emptyList());
    }

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

    @Override
    public Map<PlatformResourceKey, Set<TaskId>> getCurrentlyAutocrafting() {
        return getAutocrafting().map(autocrafting -> autocrafting.getStatuses()
            .stream()
            .filter(status -> status.info().resource() instanceof PlatformResourceKey)
            .collect(Collectors.groupingBy(
                status -> (PlatformResourceKey) status.info().resource(),
                Collectors.mapping(status -> status.info().id(), Collectors.toSet())
            ))).orElse(Collections.emptyMap());
    }

    @Override
    public List<TaskStatus> getAutocraftingTaskStatuses(final Set<TaskId> taskIds) {
        return getAutocrafting().map(autocrafting -> autocrafting
            .getStatuses()
            .stream()
            .filter(status -> taskIds.contains(status.info().id()))
            .toList()).orElse(Collections.emptyList());
    }

    @Override
    public GridOperations createOperations(final ResourceType resourceType, final ServerPlayer player) {
        return getStorage()
            .flatMap(rootStorage -> getSecurity()
                .map(security -> createGridOperations(resourceType, player, rootStorage, security)))
            .map(operations -> (GridOperations) new WirelessPatternGridOperations(operations, context, watchers))
            .orElse(EmptyGridOperations.INSTANCE);
    }

    @Override
    public boolean canMenuStayOpen(final Player player) {
        return true;
    }

    private GridOperations createGridOperations(final ResourceType resourceType,
                                                final ServerPlayer player,
                                                final RootStorage rootStorage,
                                                final PlatformSecurityNetworkComponent securityNetworkComponent) {
        final PlayerActor playerActor = new PlayerActor(player);
        final GridOperations operations = resourceType.createGridOperations(rootStorage, playerActor);
        final SecuredGridOperations secured = new SecuredGridOperations(player, securityNetworkComponent, operations);
        if (rootStorage instanceof FuzzyRootStorage fuzzyRootStorage) {
            return new FuzzyGridOperations(player, fuzzyRootStorage, secured);
        }
        return secured;
    }

    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource, final long amount,
                                                           final CancellationToken cancellationToken) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.getPreview(resource, amount, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    @Override
    public CompletableFuture<Optional<TreePreview>> getTreePreview(final ResourceKey resource, final long amount,
                                                                   final CancellationToken cancellationToken) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.getTreePreview(resource, amount, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource, final CancellationToken cancellationToken) {
        return getAutocrafting()
            .map(autocrafting -> autocrafting.getMaxAmount(resource, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(0L));
    }

    @Override
    public Optional<TaskId> startTask(final ResourceKey resource,
                                      final long amount,
                                      final Actor actor,
                                      final boolean notify,
                                      final CancellationToken cancellationToken) {
        return getAutocrafting().flatMap(
            autocrafting -> autocrafting.startTask(resource, amount, actor, notify, cancellationToken));
    }
    /*
      END Grid interface implementation
     */

    /*
      BEGIN PatternGrid implementation
     */
    private static final String TAG_CRAFTING_INPUT = "crafting_input";
    private static final String TAG_PATTERN_INPUT = "pattern_input";
    private static final String TAG_PATTERN_OUTPUT = "pattern_output";
    private static final String TAG_PROCESSING_INPUT = "processing_input";
    private static final String TAG_PROCESSING_INPUT_ALLOWED_TAG_IDS = "processing_input_allowed_tag_ids";
    private static final String TAG_PROCESSING_OUTPUT = "processing_output";
    private static final String TAG_FUZZY_MODE = "fuzzy_mode";
    private static final String TAG_PATTERN_TYPE = "processing";
    private static final String TAG_STONECUTTER_INPUT = "stonecutter_input";
    private static final String TAG_STONECUTTER_SELECTED_RECIPE = "stonecutter_selected_recipe";
    private static final String TAG_SMITHING_INPUT = "smithing_input";

    private final RecipeMatrix<CraftingRecipe, CraftingInput> craftingRecipe = RecipeMatrix.crafting(
        this::setChanged,
        this::getLevel
    );
    private final ProcessingMatrixInputResourceContainer processingInput = createProcessingMatrixInputContainer();
    private final ResourceContainer processingOutput = createProcessingMatrixOutputContainer();
    // TODO don't want this because of the BlockEntity
    private final FilteredContainer patternInput = new FilteredContainer(1, PatternGridBlockEntity::isValidPattern) {
        @Override
        public void setChanged() {
            super.setChanged();
            // TODO not sure wha to replace this with
            // PatternGridBlockEntity.this.setChanged();
        }
    };
    private final FilteredContainer patternOutput = new PatternOutputContainer() {
        @Override
        public void setChanged() {
            super.setChanged();
            // TODO not sure wha to replace this with
            // PatternGridBlockEntity.this.setChanged();
        }
    };
    private final StonecutterInputContainer stonecutterInput = new StonecutterInputContainer(this::getLevel) {
        @Override
        public void setChanged() {
            super.setChanged();
            // TODO not sure what to replace this with
            // PatternGridBlockEntity.this.setChanged();
        }
    };
    private final RecipeMatrix<SmithingRecipe, SmithingRecipeInput> smithingTableRecipe = RecipeMatrix.smithingTable(
        this::setChanged,
        this::getLevel
    );

    private boolean fuzzyMode;
    private PatternType patternType = PatternType.CRAFTING;

    RecipeMatrixContainer getCraftingMatrix() {
        return craftingRecipe.getMatrix();
    }

    ResultContainer getCraftingResult() {
        return craftingRecipe.getResult();
    }

    ProcessingMatrixInputResourceContainer getProcessingInput() {
        return processingInput;
    }

    ResourceContainer getProcessingOutput() {
        return processingOutput;
    }

    public FilteredContainer getPatternInput() {
        return patternInput;
    }

    FilteredContainer getPatternOutput() {
        return patternOutput;
    }

    StonecutterInputContainer getStonecutterInput() {
        return stonecutterInput;
    }

    int getStonecutterSelectedRecipe() {
        return stonecutterInput.getSelectedRecipe();
    }

    void setStonecutterSelectedRecipe(final int index) {
        stonecutterInput.setSelectedRecipe(index);
        setChanged();
    }

    RecipeMatrixContainer getSmithingTableMatrix() {
        return smithingTableRecipe.getMatrix();
    }

    ResultContainer getSmithingTableResult() {
        return smithingTableRecipe.getResult();
    }

    boolean isFuzzyMode() {
        return fuzzyMode;
    }

    PatternType getPatternType() {
        return patternType;
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        this.fuzzyMode = fuzzyMode;
        setChanged();
    }

    void setPatternType(final PatternType patternType) {
        this.patternType = patternType;
        setChanged();
    }

    /*
      END PatternGrid implementation
     */



    /*
      BEGIN PatternGrid adapter from BlockEntity
     */

    // TODO not sure what to replace this with - this comes from AbstractBlockEntity so we may need to maintain this some other way
    public void setChanged() {
        // Override to bypass BlockEntity#setChanged: we don't need to call Level#updateNeighbourForOutputSignal
        // as we do not use any redstone related block state properties and do not need the performance hit
        // of looking them up.
        //if (level != null) {
        //    level.blockEntityChanged(worldPosition);
        //}
    }

    Level getLevel() {
        return level;
    }

    /*
      END PatternGrid adapter from BlockEntity
     */
}
