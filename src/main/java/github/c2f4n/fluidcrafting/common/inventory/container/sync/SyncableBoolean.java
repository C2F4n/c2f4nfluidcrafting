package github.c2f4n.fluidcrafting.common.inventory.container.sync;

import github.c2f4n.fluidcrafting.common.network.to_client.PacketUpdateContainer;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SyncableBoolean implements ISyncableData {

    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private boolean lastKnown;

    public SyncableBoolean(BooleanSupplier getter, Consumer<Boolean> setter) {
        this.getter = getter;
        this.setter = setter;
        lastKnown = getter.getAsBoolean();
    }

    public boolean get() {
        return getter.getAsBoolean();
    }

    public void set(boolean value) {
        setter.accept(value);
        lastKnown = value;
    }

    @Override
    public DirtyType isDirty() {
        boolean value = getter.getAsBoolean();
        if (value != lastKnown) {
            lastKnown = value;
            return DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }

    @Override
    public PacketUpdateContainer.PropertyData getPropertyData(short index, DirtyType dirtyType) {
        return new PacketUpdateContainer.BooleanPropertyData(index, get());
    }
}
