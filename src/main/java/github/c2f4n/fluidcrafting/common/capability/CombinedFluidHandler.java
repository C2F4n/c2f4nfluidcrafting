package github.c2f4n.fluidcrafting.common.capability;

import github.c2f4n.fluidcrafting.common.tile.component.TileComponentFluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/** 四个罐的组合流体能力：前三个是输入，最后一个输出。 */
public class CombinedFluidHandler implements IFluidHandler {

    private final TileComponentFluid fluid;

    public CombinedFluidHandler(TileComponentFluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public int getTanks() {
        return TileComponentFluid.INPUT_TANKS + 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return fluid.getTank(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluid.getTank(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return fluid.getTank(tank).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return fluid.getInputTank(0).fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return fluid.getOutputTank().drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return fluid.getOutputTank().drain(maxDrain, action);
    }
}
