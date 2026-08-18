package github.c2f4n.fluidcrafting.common.inventory.container.slot;

import github.c2f4n.fluidcrafting.common.inventory.container.ModContainer;
import github.c2f4n.fluidcrafting.common.inventory.slot.BasicInventorySlot;

import java.util.function.Supplier;

/** 仅在对应窗口打开时才“存在”的虚拟槽（升级槽）。 */
public class VirtualMachineSlot extends MachineSlot {

    private final ModContainer container;
    private final Supplier<java.util.UUID> playerUUID;
    private final SelectedWindowData.WindowType windowType;

    public VirtualMachineSlot(ModContainer container, Supplier<java.util.UUID> playerUUID,
                              BasicInventorySlot slot, int x, int y, SelectedWindowData.WindowType windowType) {
        super(slot, x, y, ContainerSlotType.NORMAL);
        this.container = container;
        this.playerUUID = playerUUID;
        this.windowType = windowType;
    }

    private SelectedWindowData getWindow() {
        return container.isRemote() ? container.getSelectedWindow() : container.getSelectedWindow(playerUUID.get());
    }

    @Override
    public boolean exists(SelectedWindowData window) {
        return window != null && window.type == windowType;
    }

    @Override
    public boolean isActive() {
        SelectedWindowData window = getWindow();
        return window != null && window.type == windowType;
    }
}
