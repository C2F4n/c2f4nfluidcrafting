package github.c2f4n.fluidcrafting.common.inventory.container.slot;

import github.c2f4n.fluidcrafting.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/** 直接与机器侧 BasicInventorySlot 交互的容器槽。 */
public class MachineSlot extends Slot implements IInsertableSlot {

    private static final Container EMPTY_INVENTORY = new SimpleContainer(0);

    protected final BasicInventorySlot slot;
    private final ContainerSlotType slotType;

    public MachineSlot(BasicInventorySlot slot, int x, int y, ContainerSlotType slotType) {
        super(EMPTY_INVENTORY, 0, x, y);
        this.slot = slot;
        this.slotType = slotType;
    }

    public ContainerSlotType getSlotType() {
        return slotType;
    }

    public BasicInventorySlot getInventorySlot() {
        return slot;
    }

    @Override
    public boolean exists(SelectedWindowData window) {
        return true;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        return slot.getStack();
    }

    @Override
    public boolean hasItem() {
        return !slot.isEmpty();
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        slot.setStackUnchecked(stack);
        setChanged();
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if (stack.isEmpty() || !slot.isItemValid(stack)) {
            return false;
        }
        return slot.insertItem(stack, true).getCount() < stack.getCount();
    }

    @NotNull
    @Override
    public ItemStack remove(int amount) {
        ItemStack extracted = slot.extractItem(amount, false);
        setChanged();
        return extracted;
    }

    @Override
    public int getMaxStackSize() {
        return slot.getLimit();
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return Math.min(slot.getLimit(), stack.getMaxStackSize());
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        return slot.insertItem(stack, simulate);
    }

    @Override
    public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
        super.onTake(player, stack);
        setChanged();
    }
}
