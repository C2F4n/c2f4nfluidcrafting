package github.C2F4n.client.gui.element;

import github.C2F4n.client.gui.GuiRenderUtils;
import github.C2F4n.client.gui.IGuiHost;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * 携带一张整幅贴图的元素。默认把贴图按元素矩形完整绘制。
 */
public abstract class GuiTexturedElement extends GuiElement {

    protected final ResourceLocation resource;
    protected final int textureWidth;
    protected final int textureHeight;

    public GuiTexturedElement(IGuiHost gui, int relativeX, int relativeY, int width, int height, ResourceLocation resource) {
        this(gui, relativeX, relativeY, width, height, resource, width, height);
    }

    public GuiTexturedElement(IGuiHost gui, int relativeX, int relativeY, int width, int height,
                              ResourceLocation resource, int textureWidth, int textureHeight) {
        super(gui, relativeX, relativeY, width, height);
        this.resource = resource;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    /**
     * 显式传入纹理宽高。1.20.1 的 7 参数 blit 会把纹理当 256×256，
     * 必须使用带 textureWidth/textureHeight 的重载。
     */
    protected void drawTexture(GuiGraphics guiGraphics) {
        guiGraphics.blit(resource, relativeX, relativeY, 0, 0, width, height, textureWidth, textureHeight);
    }

    /**
     * 九宫格绘制：四角原样、四边拉伸、中心拉伸。
     * 坐标为相对主界面的坐标（renderLabels 坐标系）。
     */
    protected void drawNineSliced(GuiGraphics guiGraphics, ResourceLocation texture,
                                  int x, int y, int targetWidth, int targetHeight, int sideSize,
                                  int sourceWidth, int sourceHeight) {
        GuiRenderUtils.drawNineSliced(guiGraphics, texture, x, y, targetWidth, targetHeight, sideSize, sourceWidth, sourceHeight);
    }
}
