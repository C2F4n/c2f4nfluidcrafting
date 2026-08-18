package github.c2f4n.fluidcrafting.common.tile.component;

import github.c2f4n.fluidcrafting.common.inventory.container.ModContainer;
import github.c2f4n.fluidcrafting.common.inventory.container.sync.SyncableInt;
import github.c2f4n.fluidcrafting.common.tile.base.TileEntityBase;
import net.minecraft.nbt.CompoundTag;

/** 红石组件：关闭 / 有信号时工作。 */
public class TileComponentRedstone implements ITileComponent {

    private final TileEntityBase tile;
    private RedstoneMode mode = RedstoneMode.DISABLED;

    public TileComponentRedstone(TileEntityBase tile) {
        this.tile = tile;
        tile.addComponent(this);
    }

    public RedstoneMode getMode() {
        return mode;
    }

    public void setMode(RedstoneMode mode) {
        this.mode = mode;
        tile.onContentsChanged();
    }

    public boolean shouldWork(boolean hasSignal) {
        return mode.shouldWork(hasSignal);
    }

    public boolean isEnabled() {
        return mode == RedstoneMode.ON_SIGNAL;
    }

    @Override
    public void addContainerTrackers(ModContainer container) {
        container.track(new SyncableInt(() -> mode.ordinal(), value -> mode = RedstoneMode.byIndex(value)));
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putInt("redstone_mode", mode.ordinal());
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        if (tag.contains("redstone_mode")) {
            mode = RedstoneMode.byIndex(tag.getInt("redstone_mode"));
        }
    }

    public enum RedstoneMode {
        DISABLED,
        ON_SIGNAL;

        public boolean shouldWork(boolean hasSignal) {
            return switch (this) {
                // “已关闭红石控制”表示忽略红石信号，始终允许工作；
                // “有红石信号时工作”才要求信号。
                case DISABLED -> true;
                case ON_SIGNAL -> hasSignal;
            };
        }

        public static RedstoneMode byIndex(int index) {
            RedstoneMode[] values = values();
            return index >= 0 && index < values.length ? values[index] : DISABLED;
        }
    }
}
