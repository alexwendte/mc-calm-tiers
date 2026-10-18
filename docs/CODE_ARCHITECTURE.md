Here is the complete step-by-step breakdown of what to extend, what to implement, and how to structure all the files in
your `patterngrid` package.

Because Refined Storage does not have a native Wireless Pattern Grid, you can reuse RS's base grid interfaces for
network storage access, but you will build the **pattern encoding engine** and **network listener properties** directly
into your wireless class.

---

## 🛠️ File List Architecture Breakdown

### 1. `WirelessPatternGridData.java`

* **Type:** Record
* **Extends/Implements:** `Record`
* **Description:** The network payload passed between server and client when opening the menu. It wraps Refined
  Storage's `PatternGridData` alongside the `PlayerSlotReference` so the client knows which inventory slot holds the
  item.
* **Key Components:**
* Record components: `(PatternGridData patternGridData, PlayerSlotReference playerSlotReference)`
* `public static final StreamCodec<RegistryFriendlyByteBuf, WirelessPatternGridData> STREAM_CODEC`

---

### 2. `WirelessPatternGrid.java`

* **Type:** Class
* **Extends/Implements:** `implements Grid` *(from Refined Storage)*
* **Description:** The server-side backing logic object that connects the handheld item to the Refined Storage network.
  Since there is no physical `PatternGridBlockEntity` in the world, this class delegates standard grid operations (like
  item storage/crafting previews) to the network, while managing pattern encoding states (Pattern Type, Processing
  Inputs/Outputs, Stonecutter recipes, and Fuzzy Mode) in memory or via item data components.
* **Key Responsibilities:**
* Implement `Grid` interface methods (delegating storage and network checks to `autocraftingNetworkComponent` /
  `storageNetworkComponent`).
* Store and manage active `PatternType`, `ProcessingInputData`, `ProcessingOutputData`, and property listeners for
  pattern creation.
* Provide getter methods for the menu (e.g., `getPatternInput()`, `getPatternOutput()`, `getCraftingMatrix()`).

---

### 3. `WirelessPatternGridOperations.java`

* **Type:** Class
* **Extends/Implements:** `implements GridOperations` *(from Refined Storage)*
* **Description:** Handles grid interaction actions such as extracting items, inserting items, or shift-clicking
  resources directly from the pattern grid into the network or player inventory.
* **Key Responsibilities:**
* Wraps standard `GridOperations` created by the network.
* Ensures energy is drained from the `WirelessPatternGridItem` for each network operation executed.

---

### 4. `WirelessPatternGridContainerMenu.java`

* **Type:** Class
* **Extends/Implements:** `extends PatternGridContainerMenu`
* **Description:** The container menu bridging server and client UI state.
* **Key Responsibilities:**
* **Client Constructor:** Takes `(int syncId, Inventory playerInventory, WirelessPatternGridData data)` and calls
  `super(syncId, playerInventory, data.patternGridData())`.
* **Server Constructor:** Takes
  `(int syncId, Inventory playerInventory, WirelessPatternGrid wirelessPatternGrid, PlayerSlotReference slotReference)`.
* Binds `disabledSlot` to the `PlayerSlotReference` so players cannot drop or move the open Wireless Pattern Grid item
  in their inventory.
* Registers network/pattern properties (Redstone Mode, Pattern Type, Fuzzy Mode, Stonecutter Selection) to sync client
  GUI button state with the server `WirelessPatternGrid`.

---

### 5. `WirelessPatternGridExtendedMenuProvider.java`

* **Type:** Class
* **Extends/Implements:** `implements ExtendedMenuProvider<WirelessPatternGridData>`
* **Description:** Server-side factory responsible for validating the player's connection/energy before opening the
  container, building the `WirelessPatternGridData` payload, and creating the `WirelessPatternGridContainerMenu`.
* **Key Responsibilities:**
* Implements `getDisplayName()` returning the translated item name.
* Implements `createMenu(...)` returning `new WirelessPatternGridContainerMenu(...)`.
* Implements `getCustomData(...)` creating the `WirelessPatternGridData` stream packet from the active
  `WirelessPatternGrid`.

---

### 6. `WirelessPatternGridItem.java`

* **Type:** Class
* **Extends/Implements:** `extends NetworkBoundItem` *(or Refined Storage's base `AbstractWirelessGridItem` /
  `AbstractNetworkBoundItem`)*
* **Description:** The actual inventory item class.
* **Key Responsibilities:**
* Overrides `use(Level level, Player player, InteractionHand hand)` to validate energy and range against the Refined
  Storage network.
* Constructs the server-side `WirelessPatternGrid` backing instance when used.
* Calls `player.openMenu(...)` with `WirelessPatternGridExtendedMenuProvider`.
* Handles energy consumption logic (draining energy per action unless it's the Creative variant).

---

## 📋 Step-by-Step Implementation Strategy

### Step 1: Clean up `WirelessPatternGridData`

Ensure it cleanly wraps Refined Storage's `PatternGridData`:

```java
public record WirelessPatternGridData(
    PatternGridData patternGridData,
    PlayerSlotReference playerSlotReference
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessPatternGridData> STREAM_CODEC = StreamCodec.composite(
        PatternGridData.STREAM_CODEC,
        WirelessPatternGridData::patternGridData,
        PlayerSlotReference.STREAM_CODEC,
        WirelessPatternGridData::playerSlotReference,
        WirelessPatternGridData::new
    );
}

```

### Step 2: Implement `WirelessPatternGrid` (The Core Engine)

Because `PatternGridContainerMenu` expects container slots for pattern inputs/outputs and recipe matrices, instantiate
these container fields inside `WirelessPatternGrid`:

* `FilteredContainer patternInput` (for blank patterns)
* `PatternOutputContainer patternOutput` (for encoded patterns)
* `RecipeMatrixContainer craftingMatrix` (3x3 crafting grid)
* `ResultContainer craftingResult`
* `RecipeMatrixContainer processingInput` / `processingOutput`

### Step 3: Wire up `WirelessPatternGridContainerMenu`

Extend `PatternGridContainerMenu` and use `WirelessPatternGridData` for client-side menu creation:

```java
public class WirelessPatternGridContainerMenu extends PatternGridContainerMenu {
    private final PlayerSlotReference playerSlotReference;

    // Client/Packet Constructor
    public WirelessPatternGridContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final WirelessPatternGridData data) {
        super(syncId, playerInventory, data.patternGridData());
        this.playerSlotReference = data.playerSlotReference();
        this.disabledSlot = data.playerSlotReference();
    }

    // Server Constructor
    public WirelessPatternGridContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final WirelessPatternGrid wirelessPatternGrid,
                                            final PlayerSlotReference playerSlotReference) {
        // Pass the server backing grid's PatternGridData payload to super
        super(syncId, playerInventory, wirelessPatternGrid.createPatternGridData());
        this.playerSlotReference = playerSlotReference;
        this.disabledSlot = playerSlotReference;
    }
}

```

### Step 4: Hook up `WirelessPatternGridItem` & ExtendedMenuProvider

In `use()`, retrieve the `Network` using Refined Storage's network binding helper, construct `WirelessPatternGrid`, and
open the menu using `WirelessPatternGridExtendedMenuProvider`.

---

## 💡 What about `WirelessPatternGridScreen` on the Client?

You don't need a custom screen class! In your client setup, simply map `Menus.INSTANCE.getWirelessPatternGrid()`
directly to Refined Storage's `PatternGridScreen::new`:

```java
event.register(
    Menus.INSTANCE.getWirelessPatternGrid(),

PatternGridScreen::new
    );

```

Which class in the list above would you like to build out first?