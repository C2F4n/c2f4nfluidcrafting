package github.c2f4n.fluidcrafting.client.gui.window;

import github.c2f4n.fluidcrafting.client.gui.IGuiHost;
import github.c2f4n.fluidcrafting.client.gui.element.GuiSupportedUpgrades;
import github.c2f4n.fluidcrafting.client.gui.element.GuiTextButton;
import github.c2f4n.fluidcrafting.client.gui.element.GuiUpgradeInfoPanel;
import github.c2f4n.fluidcrafting.client.gui.element.GuiUpgradeList;
import github.c2f4n.fluidcrafting.client.gui.element.GuiUpgradeProgress;
import github.c2f4n.fluidcrafting.client.gui.element.GuiUpgradeSlot;
import github.c2f4n.fluidcrafting.common.inventory.container.BasicFluidMixerContainer;
import github.c2f4n.fluidcrafting.common.network.PacketHandler;
import github.c2f4n.fluidcrafting.common.network.to_server.PacketGuiInteract;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentUpgrade;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** 升级窗口，布局与交互照 Mekanism 的 GuiUpgradeWindow。 */
public class UpgradeWindow extends GuiWindow {

    private final BasicFluidMixerContainer menu;
    private final TileComponentUpgrade upgradeComponent;
    private UpgradeDefinition selectedType;

    public UpgradeWindow(IGuiHost gui) {
        super(gui, WindowType.UPGRADE);
        this.menu = gui.getMenu();
        this.upgradeComponent = gui.getBlockEntity().getUpgradeComponent();

        addChild(new GuiUpgradeList(gui, 6, 18, 66, 50,
              () -> upgradeComponent, () -> selectedType, this::selectType));
        addChild(new GuiUpgradeInfoPanel(gui, 72, 18, 59, 50,
              () -> upgradeComponent, () -> selectedType));
        addChild(new GuiUpgradeProgress(gui, 134, 37, 10, 14,
              upgradeComponent::getScaledInstallProgress, 0xFF5CB1FF));
        addChild(new GuiTextButton(gui, 73, 54, 56, 12,
              () -> Component.translatable("gui.c2f4nfluidcrafting.upgrade.uninstall"),
              () -> Component.translatable("gui.c2f4nfluidcrafting.upgrade.uninstall.tooltip"),
              this::uninstall, () -> selectedType != null));
        addChild(new GuiSupportedUpgrades(gui, 6, 66, 125, 20));
        // 两个虚拟槽最后加入，保证点击命中优先于列表和按钮。
        addChild(new GuiUpgradeSlot(gui, 133, 18,
              BasicFluidMixerContainer.UPGRADE_SLOT_START, gui.getBlockEntity()::getUpgradeInputSlot));
        addChild(new GuiUpgradeSlot(gui, 133, 73,
              BasicFluidMixerContainer.UPGRADE_SLOT_START + 1, gui.getBlockEntity()::getUpgradeOutputSlot));

        selectFirstInstalledIfNeeded();
        menu.startTracking(BasicFluidMixerContainer.UPGRADE_TRACKING,
              upgradeComponent.getContainerTrackers());
        PacketHandler.sendToServer(new PacketGuiInteract(
              PacketGuiInteract.GuiInteraction.CONTAINER_TRACK_UPGRADES,
              gui.getBlockEntity().getBlockPos()));
    }

    private void selectType(UpgradeDefinition type) {
        this.selectedType = type;
    }

    private void selectFirstInstalledIfNeeded() {
        if (selectedType == null) {
            var installed = upgradeComponent.getInstalledDefinitions();
            if (!installed.isEmpty()) {
                selectedType = installed.get(0);
            }
        }
    }

    private void uninstall() {
        if (selectedType == null) {
            return;
        }
        int extra = Screen.hasShiftDown() ? 1 : 0;
        PacketHandler.sendToServer(new PacketGuiInteract(
              PacketGuiInteract.GuiInteraction.UPGRADE_REMOVE,
              gui.getBlockEntity().getBlockPos(), extra, selectedType.id().toString()));
    }

    @Override
    public void tick() {
        super.tick();
        if (selectedType != null && upgradeComponent.getUpgrades(selectedType.id()) == 0) {
            selectedType = null;
        }
        selectFirstInstalledIfNeeded();
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component title = Component.translatable("gui.c2f4nfluidcrafting.upgrade");
        int titleWidth = gui.getFont().width(title);
        int titleX = relativeX + 12 + Math.max(0, (width - 24 - titleWidth) / 2);
        guiGraphics.drawString(gui.getFont(), title, titleX, relativeY + 5, 0x404040, false);
    }

    @Override
    public void close() {
        menu.stopTracking(BasicFluidMixerContainer.UPGRADE_TRACKING);
        PacketHandler.sendToServer(new PacketGuiInteract(
              PacketGuiInteract.GuiInteraction.CONTAINER_STOP_TRACKING,
              gui.getBlockEntity().getBlockPos()));
        super.close();
    }
}
