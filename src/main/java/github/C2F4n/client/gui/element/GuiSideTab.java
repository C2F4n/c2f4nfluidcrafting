package github.C2F4n.client.gui.element;

import github.C2F4n.client.gui.GuiTextures;
import github.C2F4n.client.gui.IGuiHost;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * MEK 风格的边缘 tab：holder 底板 + 18×18 图标。
 * 开关类 tab 用 active 控制“亮/暗”，窗口类 tab 用 active 表示“窗口已打开”。
 */
public class GuiSideTab extends GuiTexturedElement {

    private static final int WIDTH = 26;
    private static final int ICON_SIZE = 18;
    private static final int HOLDER_SIDE = 4;
    private static final int HOLDER_WIDTH = 26;
    private static final int HOLDER_HEIGHT = 9;
    private static final int BUTTON_SIZE = 18;
    private static final int BUTTON_SIDE = 4;
    private static final int BUTTON_TEXTURE_WIDTH = 20;
    private static final int BUTTON_TEXTURE_HEIGHT = 60;
    private static final int BUTTON_STATE_HEIGHT = 20;

    private final boolean left;
    private final ResourceLocation icon;
    private final Supplier<Component> tooltip;
    private final Runnable action;
    private final BooleanSupplier active;

    public GuiSideTab(IGuiHost gui, int relativeX, int relativeY, int height, boolean left, ResourceLocation icon,
                      Supplier<Component> tooltip, Runnable action, BooleanSupplier active) {
        super(gui, relativeX, relativeY, WIDTH, height, left ? GuiTextures.HOLDER_LEFT : GuiTextures.HOLDER_RIGHT);
        this.left = left;
        this.icon = icon;
        this.tooltip = tooltip;
        this.action = action;
        this.active = active;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 外层背景：竖条九宫格，保持原亮度
        drawNineSliced(guiGraphics, resource,
              relativeX, relativeY, WIDTH, height, HOLDER_SIDE, HOLDER_WIDTH, HOLDER_HEIGHT);
        // 内部按钮：18×18，按下/悬停/普通三态
        int buttonX = relativeX + 4;
        int buttonY = relativeY + (height - BUTTON_SIZE) / 2;
        // active 表示“窗口已打开”或“该选项已选中”，必须持续保持按下态；
        // 只有鼠标短暂按住时 isPressed 才为 true，所以不能作为持久状态的依据。
        int stateRow = active.getAsBoolean() ? 0 : (isHovered(mouseX, mouseY) ? 2 : 1);
        drawButtonState(guiGraphics, buttonX, buttonY, stateRow);
        // 图标始终保持原亮度，状态只由下方按钮的按下/弹起纹理表达。
        guiGraphics.blit(icon, buttonX, buttonY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }

    private void drawButtonState(GuiGraphics guiGraphics, int x, int y, int stateRow) {
        int right = BUTTON_SIZE - BUTTON_SIDE;
        int bottom = BUTTON_SIZE - BUTTON_SIDE;
        int mid = BUTTON_SIZE - BUTTON_SIDE * 2;
        int v = stateRow * BUTTON_STATE_HEIGHT;
        guiGraphics.blit(GuiTextures.BUTTON, x, y, 0, v, BUTTON_SIDE, BUTTON_SIDE, BUTTON_TEXTURE_WIDTH, BUTTON_TEXTURE_HEIGHT);
        guiGraphics.blit(GuiTextures.BUTTON, x + right, y, 16, v, BUTTON_SIDE, BUTTON_SIDE, BUTTON_TEXTURE_WIDTH, BUTTON_TEXTURE_HEIGHT);
        guiGraphics.blit(GuiTextures.BUTTON, x, y + bottom, 0, v + 16, BUTTON_SIDE, BUTTON_SIDE, BUTTON_TEXTURE_WIDTH, BUTTON_TEXTURE_HEIGHT);
        guiGraphics.blit(GuiTextures.BUTTON, x + right, y + bottom, 16, v + 16, BUTTON_SIDE, BUTTON_SIDE, BUTTON_TEXTURE_WIDTH, BUTTON_TEXTURE_HEIGHT);
        guiGraphics.blit(GuiTextures.BUTTON, x + BUTTON_SIDE, y, 4, v, mid, BUTTON_SIDE, BUTTON_TEXTURE_WIDTH, BUTTON_TEXTURE_HEIGHT);
        guiGraphics.blit(GuiTextures.BUTTON, x + BUTTON_SIDE, y + bottom, 4, v + 16, mid, BUTTON_SIDE, BUTTON_TEXTURE_WIDTH, BUTTON_TEXTURE_HEIGHT);
        guiGraphics.blit(GuiTextures.BUTTON, x, y + BUTTON_SIDE, 0, v + 4, BUTTON_SIDE, mid, BUTTON_TEXTURE_WIDTH, BUTTON_TEXTURE_HEIGHT);
        guiGraphics.blit(GuiTextures.BUTTON, x + right, y + BUTTON_SIDE, 16, v + 4, BUTTON_SIDE, mid, BUTTON_TEXTURE_WIDTH, BUTTON_TEXTURE_HEIGHT);
        guiGraphics.blit(GuiTextures.BUTTON, x + BUTTON_SIDE, y + BUTTON_SIDE, 4, v + 4, mid, mid, BUTTON_TEXTURE_WIDTH, BUTTON_TEXTURE_HEIGHT);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        action.run();
    }

    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY)) {
            gui.displayTooltip(guiGraphics, mouseX, mouseY, tooltip.get());
        }
    }
}
