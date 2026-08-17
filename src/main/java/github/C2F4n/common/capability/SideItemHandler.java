package github.C2F4n.common.capability;

import github.C2F4n.common.inventory.slot.BasicInventorySlot;
import github.C2F4n.common.tile.base.TileEntityBase;
import github.C2F4n.common.tile.component.TileComponentConfig;
import github.C2F4n.common.tile.component.TileComponentInventory;
import github.C2F4n.common.tile.component.config.DataType;
import github.C2F4n.common.tile.component.config.TransmissionType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** 按面配置暴露物品槽的能力；side 为 null 时暴露全部主物品槽。 */
public class SideItemHandler implements IItemHandler {

    private final TileComponentConfig config;
    private final TileComponentInventory inventory;
    private final TileEntityBase tile;
    @Nullable
    private final Direction side;

    public SideItemHandler(TileComponentConfig config, TileComponentInventory inventory,
                           TileEntityBase tile, @Nullable Direction side) {
        this.config = config;
        this.inventory = inventory;
        this.tile = tile;
        this.side = side;
    }

    private DataType mode() {
        return side == null ? DataType.NONE : config.getDataType(TransmissionType.ITEM, side);
    }

    @Override
    public int getSlots() {
        return side == null ? inventory.getMachineSlots().size() : 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (side == null) {
            return inventory.getMachineSlots().get(slot).getStack();
        }
        return switch (mode()) {
            case ITEM_IN -> inventory.getSolidInputSlot().getStack();
            case ITEM_OUT -> inventory.getSolidOutputSlot().getStack();
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (side != null && mode() == DataType.ITEM_IN) {
            return inventory.getSolidInputSlot().insertItem(stack, simulate);
        }
        if (side == null && slot < inventory.getMachineSlots().size()) {
            return inventory.getMachineSlots().get(slot).insertItem(stack, simulate);
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (side != null && mode() == DataType.ITEM_OUT) {
            return inventory.getSolidOutputSlot().extractItem(amount, simulate);
        }
        if (side == null && slot < inventory.getMachineSlots().size()) {
            return inventory.getMachineSlots().get(slot).extractItem(amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (side == null && slot < inventory.getMachineSlots().size()) {
            return inventory.getMachineSlots().get(slot).getLimit();
        }
        return mode() == DataType.ITEM_IN ? inventory.getSolidInputSlot().getLimit() : 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (side != null && mode() == DataType.ITEM_IN) {
            return inventory.getSolidInputSlot().isItemValid(stack);
        }
        return side == null && slot < inventory.getMachineSlots().size()
              && inventory.getMachineSlots().get(slot).isItemValid(stack);
    }
}
