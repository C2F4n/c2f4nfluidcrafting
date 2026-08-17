package github.C2F4n.common.inventory.container.sync;

import github.C2F4n.common.network.to_client.PacketUpdateContainer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SyncableItemStack implements ISyncableData {

    private final Supplier<ItemStack> getter;
    private final Consumer<ItemStack> setter;
    private ItemStack lastKnown = ItemStack.EMPTY;

    public SyncableItemStack(Supplier<ItemStack> getter, Consumer<ItemStack> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public ItemStack get() {
        return getter.get();
    }

    public void set(ItemStack value) {
        setter.accept(value);
        lastKnown = value.copy();
    }

    @Override
    public DirtyType isDirty() {
        ItemStack value = getter.get();
        if (!ItemStack.matches(lastKnown, value)) {
            ItemStack copy = value.copy();
            boolean sizeChanged = lastKnown.getCount() != value.getCount();
            lastKnown = copy;
            return sizeChanged ? DirtyType.SIZE : DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }

    @Override
    public PacketUpdateContainer.PropertyData getPropertyData(short index, DirtyType dirtyType) {
        return new PacketUpdateContainer.ItemStackPropertyData(index, get());
    }
}
