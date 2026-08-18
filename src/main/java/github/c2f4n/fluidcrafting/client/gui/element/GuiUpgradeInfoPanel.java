package github.c2f4n.fluidcrafting.client.gui.element;

import github.c2f4n.fluidcrafting.client.gui.GuiRenderUtils;
import github.c2f4n.fluidcrafting.client.gui.GuiTextures;
import github.c2f4n.fluidcrafting.client.gui.IGuiHost;
import github.c2f4n.fluidcrafting.client.gui.element.scroll.GuiScrollBar;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentUpgrade;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 升级窗口右侧的说明框：内容超高时可手动滚轮滚动，
 * 右侧绘制 1px 宽的滚动条，滚轮方向与普通滚动条一致。
 */
public class GuiUpgradeInfoPanel extends GuiElement {

    private static final float TEXT_SCALE = 0.7f;
    private static final int LINE_HEIGHT = 8;
    private static final int TITLE_COUNT_HEIGHT = 10;
    /** 卸载按钮在窗口内的 y 起点，说明区内容必须截到这个位置之前。 */
    private static final int CONTENT_BOTTOM_OFFSET = 36;
    private static final int SCROLLBAR_WIDTH = 1;

    private final Supplier<TileComponentUpgrade> component;
    private final Supplier<UpgradeDefinition> selected;
    private final GuiScrollBar scrollBar = new GuiScrollBar();
    private boolean draggingScrollbar;

    public GuiUpgradeInfoPanel(IGuiHost gui, int relativeX, int relativeY, int width, int height,
                               Supplier<TileComponentUpgrade> component, Supplier<UpgradeDefinition> selected) {
        super(gui, relativeX, relativeY, width, height);
        this.component = component;
        this.selected = selected;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        GuiRenderUtils.drawNineSliced(guiGraphics, GuiTextures.ELEMENT_HOLDER,
              relativeX, relativeY, width, height, 1, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<ScrollLine> lines = buildLines();
        int contentHeight = contentHeight(lines);
        int scrollOffset = scrollBar.getScroll(contentHeight, viewportHeight());

        int clipMinX = gui.getGuiLeft() + relativeX + 2;
        int clipMaxX = gui.getGuiLeft() + relativeX + width - 1 - SCROLLBAR_WIDTH;
        int clipMinY = gui.getGuiTop() + relativeY + 2;
        int clipMaxY = gui.getGuiTop() + relativeY + CONTENT_BOTTOM_OFFSET - 1;
        guiGraphics.enableScissor(clipMinX, clipMinY, clipMaxX, clipMaxY);
        int textY = relativeY + 3 - scrollOffset;
        for (ScrollLine line : lines) {
            if (textY + line.height() >= relativeY + 2
                  && textY < relativeY + CONTENT_BOTTOM_OFFSET - 1) {
                drawScaledText(guiGraphics, line.text(),
                      relativeX + 3, textY, line.color(), TEXT_SCALE);
            }
            textY += line.height();
        }
        guiGraphics.disableScissor();
        int scrollX = relativeX + width - 1 - SCROLLBAR_WIDTH;
        scrollBar.render(guiGraphics, scrollX, relativeY + 2, viewportHeight(),
              contentHeight, viewportHeight());
    }

    private List<ScrollLine> buildLines() {
        List<ScrollLine> lines = new ArrayList<>();
        UpgradeDefinition type = selected.get();
        int wrapWidth = (int) ((width - 6 - SCROLLBAR_WIDTH) / TEXT_SCALE);
        if (type == null) {
            String text = Component.translatable("gui.c2f4nfluidcrafting.upgrade.no_selection").getString();
            for (String line : wrapText(gui, text, wrapWidth)) {
                lines.add(new ScrollLine(Component.literal(line), LINE_HEIGHT, 0xFFD2D2D2));
            }
            return lines;
        }
        lines.add(new ScrollLine(type.getDisplayName(), TITLE_COUNT_HEIGHT, 0xFFFFF27A));
        lines.add(new ScrollLine(Component.translatable("gui.c2f4nfluidcrafting.upgrade.count",
              component.get().getUpgrades(type.id()), type.maxCount()), TITLE_COUNT_HEIGHT, 0xFFFFFFFF));
        String description = type.getDescription().getString();
        for (String line : wrapText(gui, description, wrapWidth)) {
            lines.add(new ScrollLine(Component.literal(line), LINE_HEIGHT, 0xFFE8FFE8));
        }
        return lines;
    }

    private int contentHeight(List<ScrollLine> lines) {
        int total = 0;
        for (ScrollLine line : lines) {
            total += line.height();
        }
        return total;
    }

    private int getContentHeight() {
        return contentHeight(buildLines());
    }

    private int viewportHeight() {
        return CONTENT_BOTTOM_OFFSET - 3;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isMouseOverSelf(mouseX, mouseY)) {
            return false;
        }
        int contentHeight = getContentHeight();
        scrollBar.adjust(delta, contentHeight, viewportHeight(), 4);
        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (isScrollbarArea(mouseX)) {
            draggingScrollbar = true;
            updateScrollFromMouse(mouseY);
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            updateScrollFromMouse(mouseY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        draggingScrollbar = false;
    }

    private boolean isScrollbarArea(double mouseX) {
        return mouseX >= getX() + width - 2 && mouseX < getX() + width;
    }

    private void updateScrollFromMouse(double mouseY) {
        int contentHeight = getContentHeight();
        scrollBar.setFromPosition(mouseY, getY() + 2, viewportHeight(),
              contentHeight, viewportHeight());
    }

    /** 按字符实测宽度折行，中文没有空格也能正确换行。 */
    private static List<String> wrapText(IGuiHost gui, String text, int wrapWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            String character = new String(Character.toChars(codePoint));
            offset += Character.charCount(codePoint);
            if (current.length() > 0 && gui.getFont().width(current.toString() + character) > wrapWidth) {
                lines.add(current.toString());
                current = new StringBuilder(character);
            } else {
                current.append(character);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private void drawScaledText(GuiGraphics guiGraphics, Component text, int x, int y, int color, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.drawString(gui.getFont(), text, 0, 0, color, true);
        guiGraphics.pose().popPose();
    }

    private record ScrollLine(Component text, int height, int color) {
    }
}
