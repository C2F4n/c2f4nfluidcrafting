package github.C2F4n.common.inventory.container.sync;

import github.C2F4n.common.network.to_client.PacketUpdateContainer;

public interface ISyncableData {

    DirtyType isDirty();

    PacketUpdateContainer.PropertyData getPropertyData(short index, DirtyType dirtyType);

    enum DirtyType {
        CLEAN,
        SIZE,
        DIRTY
    }
}
