package github.C2F4n.common.tile.component;

import github.C2F4n.common.inventory.container.ModContainer;
import github.C2F4n.common.inventory.container.sync.SyncableItemStack;
import github.C2F4n.common.inventory.slot.BasicInventorySlot;
import github.C2F4n.common.tile.base.TileEntityBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** 机器主物品栏：固体输入/输出 + 3 个容器输入 + 1 个容器输出。 */
public class TileComponentInventory implements ITileComponent {

    private final List<BasicInventorySlot> slots = new ArrayList<>(6);

    public TileComponentInventory(TileEntityBase tile) {
        slots.add(BasicInventorySlot.at(tile, 64));
        slots.add(BasicInventorySlot.at(tile, 64));
        slots.add(BasicInventorySlot.at(tile, 1));
        slots.add(BasicInventorySlot.at(tile, 1));
        slots.add(BasicInventorySlot.at(tile, 1));
        slots.add(BasicInventorySlot.at(tile, 1));
        tile.addComponent(this);
    }

    public BasicInventorySlot getSolidInputSlot() {
        return slots.get(0);
    }

    public BasicInventorySlot getSolidOutputSlot() {
        return slots.get(1);
    }

    public BasicInventorySlot getContainerInputSlot(int index) {
        return slots.get(2 + index);
    }

    public BasicInventorySlot getContainerOutputSlot() {
        return slots.get(5);
    }

    public List<BasicInventorySlot> getMachineSlots() {
        return List.copyOf(slots);
    }

    public boolean hasContent() {
        return slots.stream().anyMatch(slot -> !slot.isEmpty());
    }

    @Override
    public void addContainerTrackers(ModContainer container) {
        for (BasicInventorySlot slot : slots) {
            container.track(new SyncableItemStack(slot::getStack, slot::setStackUnchecked));
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        CompoundTag inventory = new CompoundTag();
        inventory.putInt("Size", slots.size());
        ListTag items = new ListTag();
        for (int i = 0; i < slots.size(); i++) {
            BasicInventorySlot slot = slots.get(i);
            if (!slot.isEmpty()) {
                CompoundTag entry = slot.getStack().save(new CompoundTag());
                entry.putInt("Slot", i);
                items.add(entry);
            }
        }
        inventory.put("Items", items);
        tag.put("inventory", inventory);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        if (!tag.contains("inventory")) {
            return;
        }
        CompoundTag inventory = tag.getCompound("inventory");
        ListTag items = inventory.getList("Items", 10);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag entry = items.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot >= 0 && slot < slots.size()) {
                slots.get(slot).setStackUnchecked(ItemStack.of(entry));
            }
        }
    }
}
