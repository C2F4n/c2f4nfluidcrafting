package github.C2F4n.common.tile.component;

import github.C2F4n.MachineConfig;
import github.C2F4n.common.inventory.container.ModContainer;
import github.C2F4n.common.inventory.container.sync.SyncableFluidStack;
import github.C2F4n.common.tile.base.TileEntityBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.Arrays;
import java.util.List;

/** 四个机器流体罐：3 个输入 + 1 个输出。负责罐管理、内容回调、同步与 NBT。 */
public class TileComponentFluid implements ITileComponent {

    public static final int INPUT_TANKS = 3;

    private final TileEntityBase tile;
    private final Runnable syncCallback;
    private final FluidTank[] tanks = new FluidTank[INPUT_TANKS + 1];

    public TileComponentFluid(TileEntityBase tile, Runnable syncCallback) {
        this.tile = tile;
        this.syncCallback = syncCallback;
        for (int i = 0; i < tanks.length; i++) {
            tanks[i] = createTank();
        }
        tile.addComponent(this);
    }

    private FluidTank createTank() {
        return new FluidTank(MachineConfig.tankCapacity(), fluid -> true) {
            @Override
            protected void onContentsChanged() {
                tile.onContentsChanged();
                syncCallback.run();
            }
        };
    }

    public FluidTank getInputTank(int index) {
        return tanks[index];
    }

    public FluidTank getOutputTank() {
        return tanks[INPUT_TANKS];
    }

    public FluidTank getTank(int index) {
        return index == INPUT_TANKS ? getOutputTank() : getInputTank(index);
    }

    public List<FluidStack> getFluids() {
        return Arrays.stream(tanks).map(FluidTank::getFluid).toList();
    }

    public List<FluidTank> getTanks() {
        return List.of(tanks);
    }

    public void applyClientData(FluidStack input1, FluidStack input2, FluidStack input3, FluidStack output) {
        getInputTank(0).setFluid(input1);
        getInputTank(1).setFluid(input2);
        getInputTank(2).setFluid(input3);
        getOutputTank().setFluid(output);
    }

    public boolean hasContent() {
        for (FluidTank tank : tanks) {
            if (!tank.getFluid().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addContainerTrackers(ModContainer container) {
        for (FluidTank tank : tanks) {
            container.track(new SyncableFluidStack(tank::getFluid, tank::setFluid));
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.put("input_tank_1", getInputTank(0).writeToNBT(new CompoundTag()));
        tag.put("input_tank_2", getInputTank(1).writeToNBT(new CompoundTag()));
        tag.put("input_tank_3", getInputTank(2).writeToNBT(new CompoundTag()));
        tag.put("output_tank", getOutputTank().writeToNBT(new CompoundTag()));
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        if (tag.contains("input_tank_1")) {
            getInputTank(0).readFromNBT(tag.getCompound("input_tank_1"));
        }
        if (tag.contains("input_tank_2")) {
            getInputTank(1).readFromNBT(tag.getCompound("input_tank_2"));
        }
        if (tag.contains("input_tank_3")) {
            getInputTank(2).readFromNBT(tag.getCompound("input_tank_3"));
        }
        if (tag.contains("output_tank")) {
            getOutputTank().readFromNBT(tag.getCompound("output_tank"));
        }
    }
}
