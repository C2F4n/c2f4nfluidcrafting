package github.c2f4n.fluidcrafting.common.inventory.container.sync;

import github.c2f4n.fluidcrafting.common.network.to_client.PacketUpdateContainer;

public interface ISyncableData {

    DirtyType isDirty();

    PacketUpdateContainer.PropertyData getPropertyData(short index, DirtyType dirtyType);

    enum DirtyType {
        CLEAN,
        SIZE,
        DIRTY
    }
}
