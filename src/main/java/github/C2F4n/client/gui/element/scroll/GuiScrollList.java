package github.C2F4n.client.gui.element.scroll;

import github.C2F4n.client.gui.GuiRenderUtils;
import github.C2F4n.client.gui.GuiTextures;
import github.C2F4n.client.gui.IGuiHost;
import github.C2F4n.client.gui.element.GuiElement;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 通用垂直列表：行布局、滚轮、滚动条、选择与 tooltip 都由这里处理。
 * 子类实现 {@link #renderRow} 与 {@link #renderRowTooltip}。
 */
public abstract class GuiScrollList<T> extends GuiElement {

    private static final int HOVER_COLOR = 0x2AFFFFFF;
    private static final int SELECTED_COLOR = 0x66FFFFFF;
    private static final int SCROLLBAR_GAP = 2;

    private final Supplier<List<T>> elementsSupplier;
    private final Consumer<T> onSelect;
    private final int rowHeight;
    private final GuiScrollBar scrollBar = new GuiScrollBar();
    private T selected;
    private boolean draggingScrollbar;

    protected GuiScrollList(IGuiHost gui, int relativeX, int relativeY, int width, int height,
                            int rowHeight, Supplier<List<T>> elementsSupplier, Consumer<T> onSelect) {
        super(gui, relativeX, relativeY, width, height);
        this.rowHeight = rowHeight;
        this.elementsSupplier = elementsSupplier;
        this.onSelect = onSelect;
    }

    protected T getSelected() {
        return selected;
    }

    protected void setSelected(T selected) {
        this.selected = selected;
    }

    protected List<T> getElements() {
        return elementsSupplier.get();
    }

    protected int getRowHeight() {
        return rowHeight;
    }

    private int viewportHeight() {
        return Math.max(1, height - 2);
    }

    private int rowAt(double mouseY) {
        if (mouseY < getY() + 1 || mouseY >= getY() + height - 1) {
            return -1;
        }
        int scroll = scrollBar.getScroll(getElements().size() * rowHeight, viewportHeight());
        int firstRow = Math.max(0, scroll / rowHeight);
        int visibleRow = (int) ((mouseY - getY() - 1) / rowHeight);
        int row = firstRow + visibleRow;
        return row >= 0 && row < getElements().size() ? row : -1;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        GuiRenderUtils.drawNineSliced(guiGraphics, GuiTextures.ELEMENT_HOLDER,
              relativeX, relativeY, width, height, 1, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<T> elements = getElements();
        int contentHeight = elements.size() * rowHeight;
        int scroll = scrollBar.getScroll(contentHeight, viewportHeight());
        int firstRow = Math.max(0, scroll / rowHeight);
        int visibleRows = Math.max(1, viewportHeight() / rowHeight);
        int clipMinX = gui.getGuiLeft() + relativeX + 1;
        int clipMaxX = gui.getGuiLeft() + relativeX + width - 1 - GuiScrollBar.WIDTH;
        int clipMinY = gui.getGuiTop() + relativeY + 1;
        int clipMaxY = gui.getGuiTop() + relativeY + height - 1;
        guiGraphics.enableScissor(clipMinX, clipMinY, clipMaxX, clipMaxY);
        for (int row = 0; row < visibleRows; row++) {
            int index = firstRow + row;
            if (index >= elements.size()) {
                break;
            }
            T element = elements.get(index);
            int rowY = relativeY + 1 + row * rowHeight;
            if (element == selected) {
                guiGraphics.fill(relativeX + 1, rowY, relativeX + width - 2, rowY + rowHeight - 1, SELECTED_COLOR);
            } else if (rowAt(mouseY) == index) {
                guiGraphics.fill(relativeX + 1, rowY, relativeX + width - 2, rowY + rowHeight - 1, HOVER_COLOR);
            }
            renderRow(guiGraphics, element, index, rowY, mouseX, mouseY);
        }
        guiGraphics.disableScissor();
        scrollBar.render(guiGraphics, relativeX + width - 1 - GuiScrollBar.WIDTH,
              relativeY + 1, viewportHeight(), contentHeight, viewportHeight());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isMouseOverSelf(mouseX, mouseY)) {
            return false;
        }
        scrollBar.adjust(delta, getElements().size() * rowHeight, viewportHeight(), rowHeight);
        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (mouseX >= getX() + width - SCROLLBAR_GAP) {
            draggingScrollbar = true;
            scrollBar.setFromPosition(mouseY, getY() + 1, viewportHeight(),
                  getElements().size() * rowHeight, viewportHeight());
            return;
        }
        int row = rowAt(mouseY);
        if (row >= 0) {
            T element = getElements().get(row);
            setSelected(element);
            onSelect.accept(element);
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            scrollBar.setFromPosition(mouseY, getY() + 1, viewportHeight(),
                  getElements().size() * rowHeight, viewportHeight());
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        draggingScrollbar = false;
    }

    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int row = rowAt(mouseY);
        if (row >= 0) {
            renderRowTooltip(guiGraphics, getElements().get(row), mouseX, mouseY);
        }
    }

    protected abstract void renderRow(GuiGraphics guiGraphics, T element, int index,
                                      int rowY, int mouseX, int mouseY);

    protected void renderRowTooltip(GuiGraphics guiGraphics, T element, int mouseX, int mouseY) {
    }
}
