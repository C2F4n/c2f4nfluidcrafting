package github.C2F4n.client.gui.element;

import github.C2F4n.client.gui.GuiRenderUtils;
import github.C2F4n.client.gui.GuiTextures;
import github.C2F4n.client.gui.IGuiHost;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.DoubleSupplier;

/** 垂直的安装/拆卸进度条。贴图未定之前用 holder + 色块占位。 */
public class GuiUpgradeProgress extends GuiElement {

    private final DoubleSupplier progress;
    private final int color;

    public GuiUpgradeProgress(IGuiHost gui, int relativeX, int relativeY, int width, int height,
                              DoubleSupplier progress, int color) {
        super(gui, relativeX, relativeY, width, height);
        this.progress = progress;
        this.color = color;
        active = false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        GuiRenderUtils.drawNineSliced(guiGraphics, GuiTextures.ELEMENT_HOLDER,
              relativeX, relativeY, width, height, 1, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        double value = Math.max(0, Math.min(1, progress.getAsDouble()));
        int fillHeight = (int) Math.round(value * (height - 2));
        if (fillHeight > 0) {
            guiGraphics.fill(relativeX + 1, relativeY + height - 1 - fillHeight,
                  relativeX + width - 1, relativeY + height - 1, color);
        }
    }
}
