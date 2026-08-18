package github.c2f4n.fluidcrafting.common.tile.component;

import github.c2f4n.fluidcrafting.MachineConfig;
import github.c2f4n.fluidcrafting.common.inventory.slot.BasicInventorySlot;
import github.c2f4n.fluidcrafting.common.tile.base.TileEntityBase;
import github.c2f4n.fluidcrafting.common.tile.component.config.DataType;
import github.c2f4n.fluidcrafting.common.tile.component.config.TransmissionType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** 自动弹出组件：按面配置把产物和空容器推给邻居，不参与配方合成。 */
public class TileComponentEjector implements ITileComponent {

    private final TileEntityBase tile;
    private final TileComponentConfig config;
    private final TileComponentFluid fluid;
    private final TileComponentInventory inventory;

    public TileComponentEjector(TileEntityBase tile, TileComponentConfig config,
                                TileComponentFluid fluid, TileComponentInventory inventory) {
        this.tile = tile;
        this.config = config;
        this.fluid = fluid;
        this.inventory = inventory;
        tile.addComponent(this);
    }

    @Override
    public void tickServer() {
        ejectFluid();
        ejectItem();
        ejectEmptyContainers();
    }

    private void ejectFluid() {
        if (!config.isEjecting(TransmissionType.FLUID) || fluid.getOutputTank().getFluid().isEmpty()) {
            return;
        }
        for (Direction direction : config.getConfig(TransmissionType.FLUID).getSidesFor(DataType.FLUID_OUT)) {
            IFluidHandler neighbor = getNeighborFluidHandler(direction);
            if (neighbor != null) {
                FluidUtil.tryFluidTransfer(neighbor, fluid.getOutputTank(),
                      Math.min(MachineConfig.fluidEjectPerTick(), fluid.getOutputTank().getFluidAmount()), true);
            }
        }
    }

    private void ejectItem() {
        if (!config.isEjecting(TransmissionType.ITEM) || inventory.getSolidOutputSlot().isEmpty()) {
            return;
        }
        for (Direction direction : config.getConfig(TransmissionType.ITEM).getSidesFor(DataType.ITEM_OUT)) {
            IItemHandler neighbor = getNeighborItemHandler(direction);
            if (neighbor != null) {
                ItemStack leftover = insertIntoHandler(neighbor, inventory.getSolidOutputSlot().getStack());
                inventory.getSolidOutputSlot().setStack(leftover);
                if (leftover.isEmpty()) {
                    break;
                }
            }
        }
    }

    private void ejectEmptyContainers() {
        for (Direction direction : config.getConfig(TransmissionType.ITEM).getSidesFor(DataType.EMPTY_OUT)) {
            IItemHandler neighbor = getNeighborItemHandler(direction);
            if (neighbor == null) {
                continue;
            }
            for (int i = 0; i < TileComponentFluid.INPUT_TANKS; i++) {
                BasicInventorySlot slot = inventory.getContainerInputSlot(i);
                if (slot.isEmpty() || !isEmptyContainer(slot.getStack())) {
                    continue;
                }
                slot.setStack(insertIntoHandler(neighbor, slot.getStack()));
            }
        }
    }

    private static boolean isEmptyContainer(ItemStack stack) {
        Optional<IFluidHandlerItem> optional = FluidUtil.getFluidHandler(stack).resolve();
        if (optional.isEmpty()) {
            return false;
        }
        IFluidHandlerItem handler = optional.get();
        for (int i = 0; i < handler.getTanks(); i++) {
            if (!handler.getFluidInTank(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
            remaining = handler.insertItem(i, remaining, false);
        }
        return remaining;
    }

    @Nullable
    private IFluidHandler getNeighborFluidHandler(Direction direction) {
        BlockEntity neighbor = tile.getLevel() == null ? null
              : tile.getLevel().getBlockEntity(tile.getBlockPos().relative(direction));
        return neighbor == null ? null
              : neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).orElse(null);
    }

    @Nullable
    private IItemHandler getNeighborItemHandler(Direction direction) {
        BlockEntity neighbor = tile.getLevel() == null ? null
              : tile.getLevel().getBlockEntity(tile.getBlockPos().relative(direction));
        return neighbor == null ? null
              : neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).orElse(null);
    }
}
