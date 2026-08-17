package github.C2F4n.client.gui.element;

import github.C2F4n.client.gui.IGuiHost;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** 内嵌文本框：背景贴图 + 缩放文本（自动弹出状态）。 */
public class GuiTextPanel extends GuiTexturedElement {

    private final Supplier<Component> text;
    private final IntSupplier color;

    public GuiTextPanel(IGuiHost gui, int relativeX, int relativeY, int width, int height, ResourceLocation texture,
                        Supplier<Component> text, IntSupplier color) {
        super(gui, relativeX, relativeY, width, height, texture);
        this.text = text;
        this.color = color;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        drawTexture(guiGraphics);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(relativeX + 3, relativeY + 4, 0);
        guiGraphics.pose().scale(0.8f, 0.8f, 1f);
        guiGraphics.drawString(gui.getFont(), text.get(), 0, 0, color.getAsInt(), false);
        guiGraphics.pose().popPose();
    }
}
