package github.C2F4n.common.capability;

import github.C2F4n.MachineConfig;
import github.C2F4n.common.tile.component.TileComponentConfig;
import github.C2F4n.common.tile.component.TileComponentFluid;
import github.C2F4n.common.tile.component.config.DataType;
import github.C2F4n.common.tile.component.config.TransmissionType;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/** 按面配置暴露单个流体罐的能力。 */
public class SideFluidHandler implements IFluidHandler {

    private final TileComponentConfig config;
    private final TileComponentFluid fluid;
    private final Direction side;

    public SideFluidHandler(TileComponentConfig config, TileComponentFluid fluid, Direction side) {
        this.config = config;
        this.fluid = fluid;
        this.side = side;
    }

    private DataType mode() {
        return config.getDataType(TransmissionType.FLUID, side);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return switch (mode()) {
            case FLUID_IN_1 -> fluid.getInputTank(0).getFluid();
            case FLUID_IN_2 -> fluid.getInputTank(1).getFluid();
            case FLUID_IN_3 -> fluid.getInputTank(2).getFluid();
            case FLUID_OUT -> fluid.getOutputTank().getFluid();
            default -> FluidStack.EMPTY;
        };
    }

    @Override
    public int getTankCapacity(int tank) {
        return MachineConfig.tankCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return mode() != DataType.NONE;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return switch (mode()) {
            case FLUID_IN_1 -> fluid.getInputTank(0).fill(resource, action);
            case FLUID_IN_2 -> fluid.getInputTank(1).fill(resource, action);
            case FLUID_IN_3 -> fluid.getInputTank(2).fill(resource, action);
            default -> 0;
        };
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return mode() == DataType.FLUID_OUT ? fluid.getOutputTank().drain(resource, action) : FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return mode() == DataType.FLUID_OUT ? fluid.getOutputTank().drain(maxDrain, action) : FluidStack.EMPTY;
    }
}
