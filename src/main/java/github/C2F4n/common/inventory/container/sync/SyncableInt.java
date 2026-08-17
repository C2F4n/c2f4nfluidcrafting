package github.C2F4n.common.inventory.container.sync;

import github.C2F4n.common.network.to_client.PacketUpdateContainer;

import java.util.function.IntSupplier;
import java.util.function.IntConsumer;

public class SyncableInt implements ISyncableData {

    private final IntSupplier getter;
    private final IntConsumer setter;
    private int lastKnown;

    public SyncableInt(IntSupplier getter, IntConsumer setter) {
        this.getter = getter;
        this.setter = setter;
        lastKnown = getter.getAsInt();
    }

    public int get() {
        return getter.getAsInt();
    }

    public void set(int value) {
        setter.accept(value);
        lastKnown = value;
    }

    @Override
    public DirtyType isDirty() {
        int value = getter.getAsInt();
        if (value != lastKnown) {
            lastKnown = value;
            return DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }

    @Override
    public PacketUpdateContainer.PropertyData getPropertyData(short index, DirtyType dirtyType) {
        return new PacketUpdateContainer.IntPropertyData(index, get());
    }
}
