package github.C2F4n.common.inventory.container.sync;

import github.C2F4n.common.network.to_client.PacketUpdateContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SyncableFluidStack implements ISyncableData {

    private final Supplier<FluidStack> getter;
    private final Consumer<FluidStack> setter;
    private FluidStack lastKnown = FluidStack.EMPTY;

    public SyncableFluidStack(Supplier<FluidStack> getter, Consumer<FluidStack> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public FluidStack get() {
        return getter.get();
    }

    public void set(FluidStack value) {
        setter.accept(value);
        lastKnown = value.copy();
    }

    @Override
    public DirtyType isDirty() {
        FluidStack value = getter.get();
        if (!value.isFluidStackIdentical(lastKnown)) {
            boolean sizeChanged = value.getAmount() != lastKnown.getAmount();
            lastKnown = value.copy();
            return sizeChanged ? DirtyType.SIZE : DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }

    @Override
    public PacketUpdateContainer.PropertyData getPropertyData(short index, DirtyType dirtyType) {
        return new PacketUpdateContainer.FluidStackPropertyData(index, get());
    }
}
