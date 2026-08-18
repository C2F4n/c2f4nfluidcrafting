package github.c2f4n.fluidcrafting.common.inventory.container;

import github.c2f4n.fluidcrafting.common.inventory.container.slot.IInsertableSlot;
import github.c2f4n.fluidcrafting.common.inventory.container.slot.MachineSlot;
import github.c2f4n.fluidcrafting.common.inventory.container.slot.SelectedWindowData;
import github.c2f4n.fluidcrafting.common.inventory.container.sync.ISyncableData;
import github.c2f4n.fluidcrafting.common.inventory.container.sync.SyncableBoolean;
import github.c2f4n.fluidcrafting.common.inventory.container.sync.SyncableFluidStack;
import github.c2f4n.fluidcrafting.common.inventory.container.sync.SyncableInt;
import github.c2f4n.fluidcrafting.common.inventory.container.sync.SyncableItemStack;
import github.c2f4n.fluidcrafting.common.network.PacketHandler;
import github.c2f4n.fluidcrafting.common.network.to_client.PacketUpdateContainer;
import github.c2f4n.fluidcrafting.common.network.to_server.PacketWindowSelect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** 容器基类：类型化同步 + 窗口选择 + 窗口感知的快速移动。 */
public abstract class ModContainer extends AbstractContainerMenu {

    private final List<ISyncableData> trackedData = new ArrayList<>();
    protected final Inventory inv;
    private final List<Slot> machineSlots = new ArrayList<>();
    private final List<Slot> hotBarSlots = new ArrayList<>();
    private final List<Slot> mainSlots = new ArrayList<>();
    private final Map<Object, List<ISyncableData>> dynamicTrackers = new HashMap<>();
    @Nullable
    private SelectedWindowData selectedWindow;
    private Map<UUID, SelectedWindowData> selectedWindows;

    protected ModContainer(@Nullable MenuType<?> type, int id, Inventory inv) {
        super(type, id);
        this.inv = inv;
        if (!isRemote()) {
            selectedWindows = new HashMap<>(1);
        }
    }

    public boolean isRemote() {
        return inv.player.level().isClientSide;
    }

    public UUID getPlayerUUID() {
        return inv.player.getUUID();
    }

    public Player getPlayerOrNull() {
        return inv.player;
    }

    public void track(ISyncableData data) {
        trackedData.add(data);
    }

    /** 追加一组动态同步数据；同一个 key 重复 start 会被忽略。 */
    public void startTracking(Object key, List<ISyncableData> data) {
        if (!dynamicTrackers.containsKey(key)) {
            dynamicTrackers.put(key, data);
            trackedData.addAll(data);
        }
    }

    /** 停止任意一组动态同步数据；客户端与服务端会按相同顺序移除。 */
    public void stopTracking(Object key) {
        List<ISyncableData> removed = dynamicTrackers.remove(key);
        if (removed != null) {
            trackedData.removeAll(removed);
        }
    }

    protected Slot addMachineSlot(Slot slot) {
        machineSlots.add(slot);
        return addSlot(slot);
    }

    protected Slot addHotBarSlot(Slot slot) {
        hotBarSlots.add(slot);
        return addSlot(slot);
    }

    protected Slot addMainSlot(Slot slot) {
        mainSlots.add(slot);
        return addSlot(slot);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (inv.player instanceof ServerPlayer player) {
            List<PacketUpdateContainer.PropertyData> dirty = new ArrayList<>();
            for (short i = 0; i < trackedData.size(); i++) {
                ISyncableData data = trackedData.get(i);
                ISyncableData.DirtyType dirtyType = data.isDirty();
                if (dirtyType != ISyncableData.DirtyType.CLEAN) {
                    dirty.add(data.getPropertyData(i, dirtyType));
                }
            }
            if (!dirty.isEmpty()) {
                PacketHandler.sendTo(new PacketUpdateContainer((short) containerId, dirty), player);
            }
        }
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        if (inv.player instanceof ServerPlayer player) {
            List<PacketUpdateContainer.PropertyData> all = new ArrayList<>();
            for (short i = 0; i < trackedData.size(); i++) {
                ISyncableData data = trackedData.get(i);
                data.isDirty();
                all.add(data.getPropertyData(i, ISyncableData.DirtyType.DIRTY));
            }
            if (!all.isEmpty()) {
                PacketHandler.sendTo(new PacketUpdateContainer((short) containerId, all), player);
            }
        }
    }

    public void handleWindowProperty(short index, boolean value) {
        if (index >= 0 && index < trackedData.size() && trackedData.get(index) instanceof SyncableBoolean syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short index, int value) {
        if (index >= 0 && index < trackedData.size() && trackedData.get(index) instanceof SyncableInt syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short index, FluidStack value) {
        if (index >= 0 && index < trackedData.size() && trackedData.get(index) instanceof SyncableFluidStack syncable) {
            syncable.set(value);
        }
    }

    public void handleWindowProperty(short index, ItemStack value) {
        if (index >= 0 && index < trackedData.size() && trackedData.get(index) instanceof SyncableItemStack syncable) {
            syncable.set(value);
        }
    }

    @Nullable
    public SelectedWindowData getSelectedWindow() {
        return selectedWindow;
    }

    @Nullable
    public SelectedWindowData getSelectedWindow(UUID player) {
        return selectedWindows == null ? null : selectedWindows.get(player);
    }

    public void setSelectedWindow(@Nullable SelectedWindowData window) {
        if (!Objects.equals(selectedWindow, window)) {
            this.selectedWindow = window;
            PacketHandler.sendToServer(new PacketWindowSelect(window));
        }
    }

    public void setSelectedWindow(UUID player, @Nullable SelectedWindowData window) {
        if (selectedWindows == null) {
            selectedWindows = new HashMap<>(1);
        }
        if (window == null || window.type == SelectedWindowData.WindowType.UNSPECIFIED) {
            selectedWindows.remove(player);
        } else {
            selectedWindows.put(player, window);
        }
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            setSelectedWindow(player.getUUID(), null);
        }
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int slotID) {
        Slot current = slots.get(slotID);
        if (current == null || !current.hasItem()) {
            return ItemStack.EMPTY;
        }
        SelectedWindowData window = isRemote() ? getSelectedWindow() : getSelectedWindow(player.getUUID());
        if (current instanceof IInsertableSlot insertable && !insertable.exists(window)) {
            return ItemStack.EMPTY;
        }
        ItemStack slotStack = current.getItem();
        ItemStack stackToInsert = slotStack.copy();
        if (current instanceof MachineSlot) {
            // 照 MEK 顺序：先热栏后主背包，先合并再填空
            stackToInsert = insertItem(hotBarSlots, stackToInsert, true, window);
            stackToInsert = insertItem(mainSlots, stackToInsert, true, window);
            stackToInsert = insertItem(hotBarSlots, stackToInsert, false, window);
            stackToInsert = insertItem(mainSlots, stackToInsert, false, window);
        } else {
            stackToInsert = insertItem(machineSlots, stackToInsert, true, window);
            if (stackToInsert.getCount() == slotStack.getCount()) {
                stackToInsert = insertItem(machineSlots, stackToInsert, false, window);
            }
            if (stackToInsert.getCount() == slotStack.getCount()) {
                if (hotBarSlots.contains(current)) {
                    stackToInsert = insertItem(mainSlots, stackToInsert, true, window);
                    stackToInsert = insertItem(mainSlots, stackToInsert, false, window);
                } else {
                    stackToInsert = insertItem(hotBarSlots, stackToInsert, true, window);
                    stackToInsert = insertItem(hotBarSlots, stackToInsert, false, window);
                }
            }
        }
        if (stackToInsert.getCount() == slotStack.getCount()) {
            return ItemStack.EMPTY;
        }
        int difference = slotStack.getCount() - stackToInsert.getCount();
        ItemStack removed = current.remove(difference);
        current.onTake(player, removed);
        return removed;
    }

    public static ItemStack insertItem(List<? extends Slot> slots, ItemStack stack, boolean ignoreEmpty,
                                       @Nullable SelectedWindowData window) {
        for (Slot slot : slots) {
            if (slot instanceof IInsertableSlot insertable && !insertable.exists(window)) {
                continue;
            }
            if (ignoreEmpty != slot.hasItem()) {
                continue;
            }
            stack = insertInto(slot, stack);
            if (stack.isEmpty()) {
                break;
            }
        }
        return stack;
    }

    private static ItemStack insertInto(Slot slot, ItemStack stack) {
        if (stack.isEmpty() || !slot.mayPlace(stack)) {
            return stack;
        }
        if (slot instanceof MachineSlot machineSlot) {
            return machineSlot.insertItem(stack, false);
        }
        ItemStack existing = slot.getItem();
        int limit = slot.getMaxStackSize(stack);
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameTags(existing, stack)) {
                return stack;
            }
            int space = Math.min(limit, existing.getMaxStackSize()) - existing.getCount();
            int move = Math.min(space, stack.getCount());
            if (move > 0) {
                existing.grow(move);
                slot.setChanged();
                stack = stack.copyWithCount(stack.getCount() - move);
            }
            return stack;
        }
        int move = Math.min(limit, stack.getCount());
        slot.set(stack.copyWithCount(move));
        stack = stack.copyWithCount(stack.getCount() - move);
        return stack;
    }
}
