package github.c2f4n.fluidcrafting.client.gui.element;

import github.c2f4n.fluidcrafting.client.gui.IGuiHost;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 图片按钮。悬停叠加半透明白色变亮，按下叠加暗色下移 1px 的视觉，
 * 可选“选中态”覆盖层（单选按钮 / 已打开窗口的入口按钮）。
 */
public class GuiButton extends GuiTexturedElement {

    public static final int HOVER_COLOR = 0x2AFFFFFF;
    public static final int SELECTED_COLOR = 0x66000000;
    public static final int PRESSED_COLOR = 0x66000000;

    @Nullable
    protected final Supplier<Component> tooltip;
    @Nullable
    protected final Runnable action;
    @Nullable
    protected final Supplier<Boolean> selected;

    public GuiButton(IGuiHost gui, int relativeX, int relativeY, int width, int height, ResourceLocation texture,
                     @Nullable Supplier<Component> tooltip, @Nullable Runnable action) {
        this(gui, relativeX, relativeY, width, height, texture, tooltip, action, null);
    }

    public GuiButton(IGuiHost gui, int relativeX, int relativeY, int width, int height, ResourceLocation texture,
                     @Nullable Supplier<Component> tooltip, @Nullable Runnable action,
                     @Nullable Supplier<Boolean> selected) {
        super(gui, relativeX, relativeY, width, height, texture);
        this.tooltip = tooltip;
        this.action = action;
        this.selected = selected;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        drawTexture(guiGraphics);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isSelected()) {
            guiGraphics.fill(relativeX, relativeY, relativeX + width, relativeY + height, SELECTED_COLOR);
        }
        if (isHovered(mouseX, mouseY)) {
            guiGraphics.fill(relativeX, relativeY, relativeX + width, relativeY + height, HOVER_COLOR);
        }
        if (isPressed()) {
            guiGraphics.fill(relativeX + 1, relativeY + 1, relativeX + 1 + width, relativeY + 1 + height, PRESSED_COLOR);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (action != null) {
            action.run();
        }
    }

    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        if (isHovered(mouseX, mouseY) && tooltip != null) {
            gui.displayTooltip(guiGraphics, mouseX, mouseY, tooltip.get());
        }
    }

    protected boolean isSelected() {
        return selected != null && selected.get();
    }
}
