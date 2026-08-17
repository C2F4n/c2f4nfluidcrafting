package github.C2F4n.common.tile.base;

import github.C2F4n.api.IContentsListener;
import github.C2F4n.common.inventory.container.ModContainer;
import github.C2F4n.common.tile.component.ITileComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 方块实体基类：组件注册、容器追踪、NBT、玩家打开/关闭统计。 */
public abstract class TileEntityBase extends BlockEntity implements IContentsListener {

    protected final List<ITileComponent> components = new ArrayList<>();
    private final Set<Player> playersUsing = new HashSet<>();

    protected TileEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void addComponent(ITileComponent component) {
        components.add(component);
    }

    public void tickServer() {
        for (ITileComponent component : components) {
            component.tickServer();
        }
    }

    public void addContainerTrackers(ModContainer container) {
        for (ITileComponent component : components) {
            component.addContainerTrackers(container);
        }
    }

    public void open(Player player) {
        playersUsing.add(player);
    }

    public void close(Player player) {
        playersUsing.remove(player);
    }

    public boolean isUsedByPlayer() {
        return !playersUsing.isEmpty();
    }

    @Override
    public void onContentsChanged() {
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (ITileComponent component : components) {
            component.writeToNBT(tag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (ITileComponent component : components) {
            component.readFromNBT(tag);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }
}
