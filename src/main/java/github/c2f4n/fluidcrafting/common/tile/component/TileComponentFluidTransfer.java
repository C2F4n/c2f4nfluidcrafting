package github.c2f4n.fluidcrafting.common.tile.component;

import github.c2f4n.fluidcrafting.MachineConfig;
import github.c2f4n.fluidcrafting.common.inventory.slot.BasicInventorySlot;
import github.c2f4n.fluidcrafting.common.tile.base.TileEntityBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.Optional;

/** 机器与桶/储罐之间的流体传输：容器槽每 10 tick 处理一次，右键取液最多 1 桶。 */
public class TileComponentFluidTransfer implements ITileComponent {

    private final TileEntityBase tile;
    private final TileComponentFluid fluid;
    private final TileComponentInventory inventory;
    private int tickCounter;

    public TileComponentFluidTransfer(TileEntityBase tile, TileComponentFluid fluid,
                                      TileComponentInventory inventory) {
        this.tile = tile;
        this.fluid = fluid;
        this.inventory = inventory;
        tile.addComponent(this);
    }

    @Override
    public void tickServer() {
        tickCounter++;
        if (tickCounter % MachineConfig.containerInterval() != 0) {
            return;
        }
        for (int i = 0; i < TileComponentFluid.INPUT_TANKS; i++) {
            BasicInventorySlot slot = inventory.getContainerInputSlot(i);
            if (!slot.isEmpty()) {
                emptyContainerInto(slot, fluid.getInputTank(i));
            }
        }
        BasicInventorySlot outputSlot = inventory.getContainerOutputSlot();
        if (!outputSlot.isEmpty() && !fluid.getOutputTank().getFluid().isEmpty()) {
            fillContainerFrom(outputSlot, fluid.getOutputTank());
        }
    }

    public void interactWithTank(Player player, int tankIndex, boolean extract, boolean shift) {
        if (tankIndex < 0 || tankIndex > TileComponentFluid.INPUT_TANKS) {
            return;
        }
        ItemStack carried = player.containerMenu.getCarried();
        Optional<IFluidHandlerItem> optional = carried.isEmpty() ? Optional.empty()
              : FluidUtil.getFluidHandler(carried).resolve();
        if (optional.isEmpty()) {
            return;
        }
        IFluidHandlerItem handler = optional.get();
        if (extract) {
            extractFromTank(player, handler, fluid.getTank(tankIndex), shift);
        } else if (tankIndex < TileComponentFluid.INPUT_TANKS) {
            insertIntoInputTank(player, handler, fluid.getInputTank(tankIndex), shift);
        }
    }

    private void insertIntoInputTank(Player player, IFluidHandlerItem handler, FluidTank tank, boolean shift) {
        int amount = shift ? MachineConfig.tankCapacity() : MachineConfig.bucketAmount();
        FluidStack drainable = handler.drain(amount, IFluidHandler.FluidAction.SIMULATE);
        int accepted = tank.fill(drainable, IFluidHandler.FluidAction.SIMULATE);
        if (!drainable.isEmpty() && accepted > 0) {
            FluidStack drained = handler.drain(new FluidStack(drainable.getFluid(), accepted),
                  IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                player.containerMenu.setCarried(handler.getContainer());
            }
        }
    }

    private void extractFromTank(Player player, IFluidHandlerItem handler, FluidTank tank, boolean shift) {
        if (tank.getFluidAmount() <= 0) {
            return;
        }
        int accepted = handler.fill(tank.getFluid(), IFluidHandler.FluidAction.SIMULATE);
        if (accepted > 0) {
            int take = shift
                  ? Math.min(accepted, tank.getFluidAmount())
                  : Math.min(MachineConfig.bucketAmount(), Math.min(accepted, tank.getFluidAmount()));
            FluidStack toFill = new FluidStack(tank.getFluid().getFluid(), take);
            int filled = handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                player.containerMenu.setCarried(handler.getContainer());
            }
        }
    }

    private void emptyContainerInto(BasicInventorySlot slot, FluidTank tank) {
        Optional<IFluidHandlerItem> optional = FluidUtil.getFluidHandler(slot.getStack()).resolve();
        if (optional.isEmpty()) {
            return;
        }
        IFluidHandlerItem handler = optional.get();
        FluidStack drainable = handler.drain(MachineConfig.tankCapacity(), IFluidHandler.FluidAction.SIMULATE);
        int accepted = tank.fill(drainable, IFluidHandler.FluidAction.SIMULATE);
        if (!drainable.isEmpty() && accepted > 0) {
            FluidStack drained = handler.drain(new FluidStack(drainable.getFluid(), accepted),
                  IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                slot.setStack(handler.getContainer());
            }
        }
    }

    private void fillContainerFrom(BasicInventorySlot slot, FluidTank tank) {
        Optional<IFluidHandlerItem> optional = FluidUtil.getFluidHandler(slot.getStack()).resolve();
        if (optional.isEmpty()) {
            return;
        }
        IFluidHandlerItem handler = optional.get();
        int accepted = handler.fill(tank.getFluid(), IFluidHandler.FluidAction.SIMULATE);
        if (accepted > 0) {
            FluidStack toFill = new FluidStack(tank.getFluid().getFluid(),
                  Math.min(accepted, tank.getFluidAmount()));
            int filled = handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                slot.setStack(handler.getContainer());
            }
        }
    }
}
