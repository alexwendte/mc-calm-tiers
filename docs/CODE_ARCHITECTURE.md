<!-- TOC -->

* [🛠️ File List Architecture Breakdown](#-file-list-architecture-breakdown)
    * [1. `WirelessPatternGridData.java`](#1-wirelesspatterngriddatajava)
    * [2. `WirelessPatternGrid.java`](#2-wirelesspatterngridjava)
    * [3. `WirelessPatternGridOperations.java`](#3-wirelesspatterngridoperationsjava)
    * [4. `WirelessPatternGridContainerMenu.java`](#4-wirelesspatterngridcontainermenujava)
    * [5. `WirelessPatternGridExtendedMenuProvider.java`](#5-wirelesspatterngridextendedmenuproviderjava)
    * [6. `WirelessPatternGridItem.java`](#6-wirelesspatterngriditemjava)
* [Wireless Crafting Grid](#wireless-crafting-grid)
    * [1. When is the
      `NetworkItemContext` Created, and Is It Recreated?](#1-when-is-the-networkitemcontext-created-and-is-it-recreated)
    * [2. Under-the-Hood Wiring & Execution Flow](#2-under-the-hood-wiring--execution-flow)
        * [Detailed Component Breakdown:](#detailed-component-breakdown)
* [How the Screens, Containers, Menus, and ExtendedMenuProvider Work Together](#how-the-screens-containers-menus-and-extendedmenuprovider-work-together)
    * [Component Breakdown & Primary Roles](#component-breakdown--primary-roles)
        * [1. `WirelessCraftingGridExtendedMenuProvider`](#1-wirelesscraftinggridextendedmenuprovider)
        * [2. `WirelessCraftingGridData`](#2-wirelesscraftinggriddata)
        * [3. `AbstractCraftingGridContainerMenu`](#3-abstractcraftinggridcontainermenu)
        * [4. `CraftingGridScreen`](#4-craftinggridscreen)
    * [The End-to-End Architectural Flow](#the-end-to-end-architectural-flow)

<!-- TOC -->


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

## Wireless Crafting Grid

### 1. When is the `NetworkItemContext` Created, and Is It Recreated?

**Yes, it gets recreated every single time.**

A new instance of `NetworkItemContext` is instantiated **fresh on demand** every time the player right-clicks and
triggers the item.

The lifecycle works as follows:

1. **The Trigger:** When a player right-clicks the `WirelessCraftingGridItem`, NeoForge invokes
   `AbstractNetworkEnergyItem#use(Level, Player, InteractionHand)` on the server thread.
2. **The Factory Call:** The abstract base class creates a `PlayerSlotReference` pointing to the exact inventory slot or
   hand holding the item, and immediately calls
   `RefinedStorageApi.INSTANCE.getNetworkItemHelper().createContext(stack, player, playerSlotReference)`.
3. **The Lifecycle Scope:** This `NetworkItemContext` is a **transient, request-scoped object**. It lives only for the
   duration of that specific interaction tick—used to validate permissions, resolve the bound network, and instantiate
   the menu provider—and then it is discarded. It is *not* a persistent singleton or stored directly on the item stack.
   Instead, it reads its backing data (such as the bound network UUID) dynamically from the `ItemStack`'s data
   components whenever methods like `resolveNetwork()` are called.

---

### 2. Under-the-Hood Wiring & Execution Flow

To trace how everything connects from a right-click to an open GUI menu, here is the architectural data flow:

```
[Player Right-Clicks Item]
       │
       ▼
[AbstractNetworkEnergyItem#use(...)] (Server Side)
       │
       ├──> 1. Creates PlayerSlotReference (tracks slot/hand index)
       ├──> 2. Creates NetworkItemContext (reads bound network ID from ItemStack components)
       │
       ▼
[WirelessCraftingGridItem#use(...)] (Subclass Override)
       │
       ├──> 3. Resolves Network via context.resolveNetwork()
       ├──> 4. Validates Security via SecurityHelper.isAllowed(player, OPEN, network)
       ├──> 5. Instantiates WirelessCraftingGrid (implements CraftingGrid)
       ├──> 6. Instantiates WirelessCraftingGridExtendedMenuProvider
       │
       ▼
[Platform.getMenuOpener().openMenu(...)]
       │
       ├──> Serializes ExtendedMenuProvider data payload (PlayerSlotReference, etc.)
       └──> Opens container menu on Server & syncs payload to Client

```

#### Detailed Component Breakdown:

* **`PlayerSlotReference`**: Encapsulates *where* the item lives (e.g., main inventory slot vs. curios slot vs.
  offhand). This allows the network item and its underlying energy storage to interact with the exact `ItemStack`
  instance in the player's inventory even if it moves or updates while the menu is open.
* **`NetworkItemContext`**: Acts as a bridge between the physical `ItemStack` and the server-side `NetworkManager`. It
  encapsulates network resolution logic (checking if the bound network exists, is chunk-loaded, is in the correct
  dimension, and is within range via `NetworkItemPlayerValidator`).
* **`CraftingGrid` (`WirelessCraftingGrid`)**: Manages the grid inventory state, recipe matrix, and synchronization back
  to the Refined Storage network grid crafting inventory handlers. It takes the `context` and `playerSlotReference` so
  that operations (like extracting items or crafting) can drain energy from the correct item stack via
  `EnergyItemContext`.
* **`ExtendedMenuProvider` & Menu Opener**: NeoForge’s extended menu system allows passing custom synchronization data
  (the `PlayerSlotReference` payload) from the server to the client-side screen (`AbstractContainerScreen`) when the
  container opens. This ensures that the client-side GUI knows exactly which item instance it is interacting with.

## How the Screens, Containers, Menus, and ExtendedMenuProvider Work Together

Here is how all 7 components fit together in a complete, end-to-end architectural stack—ordered from the physical item
in the player's hands, through network serialization and backend data handling, up to the rendering screen on the
client.

---

### End-to-End Component Map

#### 1. `WirelessCraftingGridItem` *(The Item & Interceptor)*

* **Layer:** Physical Item / Entry Point
* **Role:** The actual `ItemStack` in the player's inventory. It manages internal energy storage and network node links.
  When right-clicked, it validates battery power and triggers **`WirelessCraftingGridExtendedMenuProvider`** to begin
  opening the interface.

---

#### 2. `WirelessCraftingGridExtendedMenuProvider` *(The Server Gatekeeper)*

* **Layer:** Network / Server Initialization
* **Role:** Implements NeoForge's menu provider interface on the server. When invoked by the **
  `WirelessCraftingGridItem`**, it reads the item's NBT/components, instantiates the server-side **
  `WirelessCraftingGrid`**, and packages initial setup data into a **`WirelessCraftingGridData`** payload to send to the
  client.

---

#### 3. `WirelessCraftingGridData` *(The Sync Payload)*

* **Layer:** Network Data Packet
* **Role:** An immutable record sent over the network from server to client during menu creation. It transports general
  network settings (`GridData`) along with a `PlayerSlotReference`, which tells **`AbstractCraftingGridContainerMenu`**
  which exact inventory slot holding the **`WirelessCraftingGridItem`** must be locked while the GUI is open.

---

#### 4. `WirelessCraftingGrid` *(The Backend Data Model)*

* **Layer:** Business Logic / State Holder
* **Role:** Implements the core `CraftingGrid` interface on the server. Because the wireless terminal lacks a physical
  block entity in the world, this object acts as the backing container for the 3x3 matrix and output slot, managing
  recipe lookups and updates via **`WirelessCraftingGridState`**.

---

#### 5. `WirelessCraftingGridState` *(The Matrix State Snapshot)*

* **Layer:** State Serialization / Recipe Logic
* **Role:** A lightweight record holding a snapshot of the grid's matrix configuration (e.g.,
  `CraftingInput.Positioned`). **`WirelessCraftingGrid`** uses this to track item arrangements in the 3x3 crafting
  matrix, evaluate matching recipes, and synchronize ghost recipes or crafting updates back to the client.

---

#### 6. `AbstractCraftingGridContainerMenu` *(The Controller / Slot Manager)*

* **Layer:** Controller (MVC)
* **Role:** The common base container menu instantiated on both server and client. It links the inventory slots,
  standard crafting matrix slots, and result slot, handling shift-clicking logic, container sync, and slot locks passed
  down from **`WirelessCraftingGridData`**.

---

#### 7. `CraftingGridScreen` *(The Client Interface)*

* **Layer:** View (MVC / Renderer)
* **Role:** Runs exclusively on the client to render the graphical UI. It connects directly to **
  `AbstractCraftingGridContainerMenu`** to draw textures, item grid overlays, search bars, and crafting slots, capturing
  user input (clicks, drags, keystrokes) and forwarding those interactions back through the container menu.

---

### Visual Data Flow Summary

$$\text{Player Right-Click} \xrightarrow{} \mathbf{1.\;Item} \xrightarrow{} \mathbf{2.\;Provider} \xrightarrow{\mathbf{3.\;Data\;Packet}} \mathbf{6.\;Container\;Menu} \xrightarrow{} \mathbf{7.\;Screen}$$

$$\qquad\qquad\qquad\qquad\quad\downarrow$$

$$\qquad\qquad\qquad\quad\mathbf{4.\;WirelessCraftingGrid} \longleftrightarrow \mathbf{5.\;WirelessCraftingGridState}$$