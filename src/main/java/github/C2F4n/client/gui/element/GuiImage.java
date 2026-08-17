package github.C2F4n.client.gui.element;

import github.C2F4n.client.gui.IGuiHost;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** 纯装饰性贴图，不参与交互。 */
public class GuiImage extends GuiTexturedElement {

    public GuiImage(IGuiHost gui, int relativeX, int relativeY, int width, int height, ResourceLocation resource) {
        super(gui, relativeX, relativeY, width, height, resource);
        active = false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        drawTexture(guiGraphics);
    }
}
