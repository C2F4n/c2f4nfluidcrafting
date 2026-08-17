package github.C2F4n.client.gui.element;

import github.C2F4n.client.gui.GuiRenderUtils;
import github.C2F4n.client.gui.GuiTextures;
import github.C2F4n.client.gui.IGuiHost;
import github.C2F4n.common.upgrade.UpgradeDefinition;
import github.C2F4n.common.upgrade.UpgradeRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

/** 升级窗口底部的“支持的升级”区域：一页显示 4 个，滚轮翻页。 */
public class GuiSupportedUpgrades extends GuiElement {

    private static final int ELEMENT_SIZE = 14;
    private static final int TITLE_WIDTH = 58;
    private static final int ICONS_PER_PAGE = 4;

    private int pageIndex;

    public GuiSupportedUpgrades(IGuiHost gui, int relativeX, int relativeY, int width, int height) {
        super(gui, relativeX, relativeY, width, height);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        GuiRenderUtils.drawNineSliced(guiGraphics, GuiTextures.ELEMENT_HOLDER,
              relativeX, relativeY, width, height, 1, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawScaledText(guiGraphics, Component.translatable("gui.c2f4nfluidcrafting.upgrade.supported"),
              relativeX + 2, relativeY + 2, 0x202020, 0.7f);

        List<UpgradeDefinition> types = UpgradeRegistry.client().getDefinitions();
        int pageCount = pageCount(types.size());
        pageIndex = Math.min(pageIndex, Math.max(0, pageCount - 1));
        int start = pageIndex * ICONS_PER_PAGE;
        int iconY = relativeY + (height - 12) / 2;
        for (int i = start; i < Math.min(types.size(), start + ICONS_PER_PAGE); i++) {
            int slot = i - start;
            int x = relativeX + 1 + TITLE_WIDTH + slot * ELEMENT_SIZE;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x, iconY, 0);
            guiGraphics.pose().scale(0.75f, 0.75f, 1f);
            guiGraphics.renderItem(types.get(i).createStack(1), 0, 0);
            guiGraphics.pose().popPose();
        }

        if (pageCount > 1) {
            String pageText = (pageIndex + 1) + "/" + pageCount;
            int textWidth = gui.getFont().width(pageText);
            guiGraphics.drawString(gui.getFont(), pageText,
                  relativeX + width - textWidth - 2, relativeY + height - 9, 0x404040, false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isHovered(mouseX, mouseY)) {
            return false;
        }
        int pageCount = pageCount(UpgradeRegistry.client().getDefinitions().size());
        if (pageCount <= 1) {
            return false;
        }
        pageIndex = Math.floorMod(pageIndex + (delta > 0 ? -1 : 1), pageCount);
        return true;
    }

    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int iconStartX = getX() + 1 + TITLE_WIDTH;
        int iconEndX = iconStartX + ICONS_PER_PAGE * ELEMENT_SIZE;
        if (mouseX < iconStartX || mouseX >= iconEndX || mouseY < getY() || mouseY >= getY() + height) {
            return;
        }
        List<UpgradeDefinition> types = UpgradeRegistry.client().getDefinitions();
        int start = pageIndex * ICONS_PER_PAGE;
        int index = start + (int) ((mouseX - iconStartX) / ELEMENT_SIZE);
        if (index >= start && index < Math.min(types.size(), start + ICONS_PER_PAGE)) {
            UpgradeDefinition type = types.get(index);
            gui.displayTooltip(guiGraphics, mouseX, mouseY,
                  type.getDisplayName(), type.getDescription());
        }
    }

    private static int pageCount(int typeCount) {
        return Math.max(1, (typeCount + ICONS_PER_PAGE - 1) / ICONS_PER_PAGE);
    }

    private void drawScaledText(GuiGraphics guiGraphics, Component text, int x, int y, int color, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.drawString(gui.getFont(), text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }
}
