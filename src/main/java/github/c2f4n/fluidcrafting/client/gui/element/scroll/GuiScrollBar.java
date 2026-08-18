package github.c2f4n.fluidcrafting.client.gui.element.scroll;

import net.minecraft.client.gui.GuiGraphics;

/** 1px 宽垂直滚动条，可滚轮、可点击轨道跳转。 */
public class GuiScrollBar {

    public static final int WIDTH = 1;
    private static final int MIN_THUMB_HEIGHT = 6;

    private int scroll;
    private int trackColor = 0x662B5B7D;
    private int thumbColor = 0xFF8FC7FF;

    public void setColors(int trackColor, int thumbColor) {
        this.trackColor = trackColor;
        this.thumbColor = thumbColor;
    }

    public int getMax(int contentHeight, int viewportHeight) {
        return Math.max(0, contentHeight - viewportHeight);
    }

    public int getScroll(int contentHeight, int viewportHeight) {
        scroll = clamp(scroll, getMax(contentHeight, viewportHeight));
        return scroll;
    }

    public int adjust(double delta, int contentHeight, int viewportHeight, int step) {
        int max = getMax(contentHeight, viewportHeight);
        scroll = clamp(scroll - (int) Math.signum(delta) * step, max);
        return scroll;
    }

    public int setFromPosition(double mouseY, int trackTop, int trackHeight,
                               int contentHeight, int viewportHeight) {
        int max = getMax(contentHeight, viewportHeight);
        if (max <= 0) {
            scroll = 0;
            return 0;
        }
        double ratio = Math.max(0, Math.min(1, (mouseY - trackTop) / (double) trackHeight));
        scroll = (int) Math.round(ratio * max);
        return scroll;
    }

    public void render(GuiGraphics guiGraphics, int x, int trackTop, int trackHeight,
                       int contentHeight, int viewportHeight) {
        int max = getMax(contentHeight, viewportHeight);
        if (max <= 0) {
            return;
        }
        guiGraphics.fill(x, trackTop, x + WIDTH, trackTop + trackHeight, trackColor);
        int thumbHeight = Math.max(MIN_THUMB_HEIGHT,
              (int) Math.round(trackHeight * ((double) viewportHeight / contentHeight)));
        int thumbY = trackTop + (int) Math.round((trackHeight - thumbHeight) * (scroll / (double) max));
        guiGraphics.fill(x, thumbY, x + WIDTH, thumbY + thumbHeight, thumbColor);
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(value, max));
    }
}
