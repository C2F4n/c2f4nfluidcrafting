package github.C2F4n.common.tile.component;

import github.C2F4n.common.inventory.container.ModContainer;
import net.minecraft.nbt.CompoundTag;

/** 方块实体组件：配置、升级、红石等，各自负责 tick、容器同步与 NBT。 */
public interface ITileComponent {

    default void tickServer() {
    }

    default void addContainerTrackers(ModContainer container) {
    }

    default void writeToNBT(CompoundTag tag) {
    }

    default void readFromNBT(CompoundTag tag) {
    }
}
