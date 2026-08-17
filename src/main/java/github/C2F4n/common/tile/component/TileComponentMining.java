package github.C2F4n.common.tile.component;

import github.C2F4n.block.entity.MiningMode;
import github.C2F4n.common.inventory.container.ModContainer;
import github.C2F4n.common.inventory.container.sync.SyncableInt;
import github.C2F4n.common.tile.base.TileEntityBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** 挖掘行为组件：保留 NBT 或掉落物品/升级并清空液体。 */
public class TileComponentMining implements ITileComponent {

    private final TileEntityBase tile;
    private MiningMode mode = MiningMode.KEEP_NBT;

    public TileComponentMining(TileEntityBase tile) {
        this.tile = tile;
        tile.addComponent(this);
    }

    public MiningMode getMode() {
        return mode;
    }

    public void setMode(MiningMode mode) {
        this.mode = mode;
        tile.onContentsChanged();
    }

    public boolean hasCustomData() {
        return mode != MiningMode.KEEP_NBT;
    }

    /** 根据挖掘模式生成完整掉落列表；保留 NBT 时只返回带数据的机器物品。 */
    public List<ItemStack> createDrops(ItemStack blockStack, ItemStack blockWithData,
                                       List<ItemStack> machineItems, List<ItemStack> upgradeItems) {
        if (mode == MiningMode.KEEP_NBT) {
            return List.of(blockWithData.isEmpty() ? blockStack.copy() : blockWithData.copy());
        }
        List<ItemStack> drops = new ArrayList<>(machineItems.size() + upgradeItems.size() + 1);
        for (ItemStack stack : machineItems) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        for (ItemStack stack : upgradeItems) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        ItemStack modeStack = blockStack.copy();
        CompoundTag modeTag = new CompoundTag();
        modeTag.putInt("mining_mode", MiningMode.DROP_ITEMS.ordinal());
        modeStack.addTagElement("BlockEntityTag", modeTag);
        drops.add(modeStack);
        return drops;
    }

    @Override
    public void addContainerTrackers(ModContainer container) {
        container.track(new SyncableInt(() -> mode.ordinal(), value -> mode = MiningMode.byIndex(value)));
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putInt("mining_mode", mode.ordinal());
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        if (tag.contains("mining_mode")) {
            mode = MiningMode.byIndex(tag.getInt("mining_mode"));
        }
    }
}
