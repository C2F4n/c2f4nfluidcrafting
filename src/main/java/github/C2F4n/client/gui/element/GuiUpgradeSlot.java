package github.C2F4n.client.gui.element;

import github.C2F4n.client.gui.GuiTextures;
import github.C2F4n.client.gui.IGuiHost;
import github.C2F4n.common.inventory.slot.BasicInventorySlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * 升级窗口里的输入/输出虚拟槽：只负责显示与点击转发。
 * 容器里的 VirtualMachineSlot 才是真正参与原版点击逻辑的槽。
 */
public class GuiUpgradeSlot extends GuiElement {

    private final int containerSlotIndex;
    private final Supplier<BasicInventorySlot> inventorySlot;

    public GuiUpgradeSlot(IGuiHost gui, int relativeX, int relativeY, int containerSlotIndex,
                          Supplier<BasicInventorySlot> inventorySlot) {
        super(gui, relativeX, relativeY, 18, 18);
        this.containerSlotIndex = containerSlotIndex;
        this.inventorySlot = inventorySlot;
    }

    private ItemStack getStack() {
        return inventorySlot.get().getStack();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blit(GuiTextures.SLOT_NORMAL, relativeX, relativeY, 0, 0, 18, 18, 18, 18);
        guiGraphics.blit(GuiTextures.OVERLAY_UPGRADE, relativeX, relativeY, 0, 0, 18, 18, 18, 18);
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            guiGraphics.renderItem(stack, relativeX + 1, relativeY + 1);
            if (stack.getCount() > 1) {
                guiGraphics.renderItemDecorations(gui.getFont(), stack, relativeX + 1, relativeY + 1);
            }
        }
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY)) {
            guiGraphics.fill(relativeX, relativeY, relativeX + width, relativeY + height, 0x2AFFFFFF);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        handleClick(button);
    }

    /** 供窗口点击与屏幕拖放共用：把点击转发给容器里的对应虚拟槽。 */
    public void handleClick(int button) {
        Player player = gui.getMenu().getPlayerOrNull();
        if (player == null || !gui.getMenu().getSlot(containerSlotIndex).isActive()) {
            return;
        }
        ClickType clickType = Screen.hasShiftDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP;
        int clickButton = Screen.hasShiftDown() ? 0 : button;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode != null) {
            // 必须走 gameMode：它同时更新本地容器并发送 ServerboundContainerClickPacket。
            // 只调 menu.clicked() 只会改客户端，服务端输入槽永远不会收到物品。
            minecraft.gameMode.handleInventoryMouseClick(gui.getMenu().containerId,
                  containerSlotIndex, clickButton, clickType, player);
        }
    }

    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isHovered(mouseX, mouseY)) {
            return;
        }
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            guiGraphics.renderTooltip(gui.getFont(), stack, mouseX, mouseY);
        } else {
            gui.displayTooltip(guiGraphics, mouseX, mouseY,
                  Component.translatable("gui.c2f4nfluidcrafting.upgrade"));
        }
    }
}
