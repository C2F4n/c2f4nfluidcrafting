package github.C2F4n.common.inventory.container;

import github.C2F4n.block.entity.BasicFluidMixerBlockEntity;
import github.C2F4n.common.inventory.container.slot.ContainerSlotType;
import github.C2F4n.common.inventory.container.slot.MachineSlot;
import github.C2F4n.common.inventory.container.slot.SelectedWindowData;
import github.C2F4n.common.inventory.container.slot.VirtualMachineSlot;
import github.C2F4n.registry.ModBlocks;
import github.C2F4n.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;

/** 基础流体混合器容器：机器槽 + 升级虚拟槽 + 玩家背包，全部走类型化同步。 */
public class BasicFluidMixerContainer extends ModContainer {

    public static final int MACHINE_SLOTS = 6;
    public static final int UPGRADE_SLOT_START = 6;
    public static final int UPGRADE_SLOT_END = 8;
    public static final Object UPGRADE_TRACKING = new Object();

    private final BasicFluidMixerBlockEntity tile;

    public BasicFluidMixerContainer(int id, Inventory inventory, BasicFluidMixerBlockEntity tile) {
        super(ModMenuTypes.BASIC_FLUID_MIXER.get(), id, inventory);
        this.tile = tile;
        tile.addContainerTrackers(this);
        addSlots(inventory);
        tile.open(inventory.player);
    }

    public BasicFluidMixerContainer(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory,
              (BasicFluidMixerBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    private void addSlots(Inventory inventory) {
        addMachineSlot(new MachineSlot(tile.getSolidInputSlot(), 112, 32, ContainerSlotType.INPUT));
        addMachineSlot(new MachineSlot(tile.getSolidOutputSlot(), 144, 80, ContainerSlotType.OUTPUT));
        addMachineSlot(new MachineSlot(tile.getContainerInputSlot(0), 16, 112, ContainerSlotType.NORMAL));
        addMachineSlot(new MachineSlot(tile.getContainerInputSlot(1), 48, 112, ContainerSlotType.NORMAL));
        addMachineSlot(new MachineSlot(tile.getContainerInputSlot(2), 80, 112, ContainerSlotType.NORMAL));
        addMachineSlot(new MachineSlot(tile.getContainerOutputSlot(), 176, 112, ContainerSlotType.NORMAL));
        addMachineSlot(new VirtualMachineSlot(this, inventory.player::getUUID, tile.getUpgradeInputSlot(),
              -1000, -1000, SelectedWindowData.WindowType.UPGRADE));
        addMachineSlot(new VirtualMachineSlot(this, inventory.player::getUUID, tile.getUpgradeOutputSlot(),
              -1001, -1000, SelectedWindowData.WindowType.UPGRADE));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addMainSlot(new Slot(inventory, col + row * 9 + 9, 32 + col * 18, 144 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addHotBarSlot(new Slot(inventory, col, 32 + col * 18, 202));
        }
    }

    public BasicFluidMixerBlockEntity getBlockEntity() {
        return tile;
    }

    @Override
    public void removed(Player player) {
        tile.close(player);
        super.removed(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(tile.getLevel(), tile.getBlockPos()),
              player, ModBlocks.BASIC_FLUID_MIXER.get());
    }
}
