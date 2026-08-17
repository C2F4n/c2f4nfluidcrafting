package github.C2F4n.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** GUI 渲染工具：九宫格绘制，照 Mekanism GuiUtils.blitNineSlicedSized 的 UV 语义实现。 */
public final class GuiRenderUtils {

    private GuiRenderUtils() {
    }

    /**
     * 九宫格：四角原样、四边与中心用“源中间带”拉伸，支持非方形源贴图。
     *
     * @param sideSize 源贴图四角的边长（水平/垂直相同）
     */
    public static void drawNineSliced(GuiGraphics guiGraphics, ResourceLocation texture,
                                      int x, int y, int targetWidth, int targetHeight, int sideSize,
                                      int sourceWidth, int sourceHeight) {
        int right = targetWidth - sideSize;
        int bottom = targetHeight - sideSize;
        int midWidth = targetWidth - sideSize * 2;
        int midHeight = targetHeight - sideSize * 2;
        int sourceRight = sourceWidth - sideSize;
        int sourceBottom = sourceHeight - sideSize;
        int sourceMidWidth = sourceWidth - sideSize * 2;
        int sourceMidHeight = sourceHeight - sideSize * 2;
        // 四角
        guiGraphics.blit(texture, x, y, sideSize, sideSize, (float) 0, (float) 0, sideSize, sideSize, sourceWidth, sourceHeight);
        guiGraphics.blit(texture, x + right, y, sideSize, sideSize, (float) sourceRight, (float) 0, sideSize, sideSize, sourceWidth, sourceHeight);
        guiGraphics.blit(texture, x, y + bottom, sideSize, sideSize, (float) 0, (float) sourceBottom, sideSize, sideSize, sourceWidth, sourceHeight);
        guiGraphics.blit(texture, x + right, y + bottom, sideSize, sideSize, (float) sourceRight, (float) sourceBottom, sideSize, sideSize, sourceWidth, sourceHeight);
        // 四条边
        guiGraphics.blit(texture, x + sideSize, y, midWidth, sideSize, (float) sideSize, (float) 0, sourceMidWidth, sideSize, sourceWidth, sourceHeight);
        guiGraphics.blit(texture, x + sideSize, y + bottom, midWidth, sideSize, (float) sideSize, (float) sourceBottom, sourceMidWidth, sideSize, sourceWidth, sourceHeight);
        guiGraphics.blit(texture, x, y + sideSize, sideSize, midHeight, (float) 0, (float) sideSize, sideSize, sourceMidHeight, sourceWidth, sourceHeight);
        guiGraphics.blit(texture, x + right, y + sideSize, sideSize, midHeight, (float) sourceRight, (float) sideSize, sideSize, sourceMidHeight, sourceWidth, sourceHeight);
        // 中心
        guiGraphics.blit(texture, x + sideSize, y + sideSize, midWidth, midHeight, (float) sideSize, (float) sideSize, sourceMidWidth, sourceMidHeight, sourceWidth, sourceHeight);
    }
}
