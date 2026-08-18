package github.c2f4n.fluidcrafting.client.gui.window;

import com.mojang.blaze3d.systems.RenderSystem;
import github.c2f4n.fluidcrafting.client.gui.GuiTextures;
import github.c2f4n.fluidcrafting.client.gui.IGuiHost;
import github.c2f4n.fluidcrafting.client.gui.element.GuiButton;
import github.c2f4n.fluidcrafting.client.gui.element.GuiElement;
import github.c2f4n.fluidcrafting.client.gui.element.GuiTexturedElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * 可拖动小窗口。参考 Mekanism 的 GuiWindow：
 * 子元素优先处理点击；窗口本体吞掉内部点击（防止点穿到背包/槽位），
 * 窗口外点击放行；按住顶部 18px 区域拖动，移动超过 3px 才真正拖动，
 * 位置限制在屏幕内。绘制放在 renderLabels 的 z 偏移层，
 * 因此窗口永远盖住槽位物品，而原版拖拽物品晚于窗口绘制、浮在最上层。
 */
public class GuiWindow extends GuiTexturedElement {

    private static final int DRAG_ZONE_HEIGHT = 18;
    private static final double DRAG_THRESHOLD = 3;
    private static final int NINE_SLICE = 4;
    private static final int CONTAINER_AREA_HEIGHT = 90;

    protected final WindowType windowType;
    protected InteractionStrategy interactionStrategy = InteractionStrategy.CONTAINER;

    private boolean dragArmed;
    private boolean dragging;
    private double dragStartX;
    private double dragStartY;
    private int dragStartRelX;
    private int dragStartRelY;

    public GuiWindow(IGuiHost gui, WindowType windowType) {
        super(gui, 0, 0, windowType.getWidth(), windowType.getHeight(), GuiTextures.BASE);
        this.windowType = windowType;
        WindowType.Position position = windowType.getLastPosition();
        if (position != null) {
            relativeX = position.x();
            relativeY = position.y();
        } else {
            // 默认居中开在主界面上（MEK 侧配置窗口的打开方式）
            relativeX = (gui.getGuiWidth() - width) / 2;
            relativeY = 15;
        }
        clampToScreen();
        addChild(new GuiButton(gui, 6, 6, 12, 12, GuiTextures.BTN_CLOSE,
              () -> Component.translatable("gui.c2f4nfluidcrafting.close"), this::close));
    }

    public WindowType getWindowType() {
        return windowType;
    }

    public InteractionStrategy getInteractionStrategy() {
        return interactionStrategy;
    }

    /** 当前窗口是否允许鼠标与主 GUI 底部背包区域交互。 */
    public boolean allowsContainerInteraction(double mouseX, double mouseY) {
        if (interactionStrategy.allowAll()) {
            return true;
        }
        return interactionStrategy.allowContainer()
              && mouseX >= gui.getGuiLeft()
              && mouseX < gui.getGuiLeft() + gui.getGuiWidth()
              && mouseY >= gui.getGuiTop() + gui.getGuiHeight() - CONTAINER_AREA_HEIGHT;
    }

    @Override
    @Nullable
    public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
        // 子元素（关闭按钮、切换按钮、槽位等）优先
        for (int i = children.size() - 1; i >= 0; i--) {
            GuiElement result = children.get(i).mouseClickedNested(mouseX, mouseY, button);
            if (result != null) {
                return result;
            }
        }
        if (!visible) {
            return null;
        }
        if (isMouseOver(mouseX, mouseY)) {
            if (allowsContainerInteraction(mouseX, mouseY)) {
                // CONTAINER 策略允许窗口下方的主 GUI 背包交互；
                // ALL 策略放行窗口空白区域。子元素已在上面优先处理。
                return null;
            }
            if (button == 0 && mouseY < getY() + DRAG_ZONE_HEIGHT) {
                dragArmed = true;
                dragging = false;
                dragStartX = mouseX;
                dragStartY = mouseY;
                dragStartRelX = relativeX;
                dragStartRelY = relativeY;
            }
            // 窗口内部空白处吞掉点击，绝不让它穿透到主界面槽位
            return this;
        }
        return null;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (!dragArmed) {
            super.onDrag(mouseX, mouseY, deltaX, deltaY);
            return;
        }
        double totalX = mouseX - dragStartX;
        double totalY = mouseY - dragStartY;
        if (!dragging) {
            if (Math.abs(totalX) + Math.abs(totalY) <= DRAG_THRESHOLD) {
                return;
            }
            dragging = true;
        }
        int targetX = dragStartRelX + (int) Math.round(totalX);
        int targetY = dragStartRelY + (int) Math.round(totalY);
        int clampedX = clampX(targetX);
        int clampedY = clampY(targetY);
        int changeX = clampedX - relativeX;
        int changeY = clampedY - relativeY;
        if (changeX != 0 || changeY != 0) {
            // move() 会同时平移所有子元素（关闭按钮、切换按钮、槽位等）
            move(changeX, changeY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        dragArmed = false;
        dragging = false;
        super.onRelease(mouseX, mouseY);
    }

    /** 拖动位置限制在屏幕内；窗口坐标是相对主界面的，允许为负。 */
    public void clampToScreen() {
        int newX = clampX(relativeX);
        int newY = clampY(relativeY);
        if (newX != relativeX || newY != relativeY) {
            // 必须用 move，让关闭按钮、切换按钮、槽位等子元素同步平移
            move(newX - relativeX, newY - relativeY);
        }
    }

    private int clampX(int value) {
        int minX = -gui.getGuiLeft();
        int maxX = minecraft.getWindow().getGuiScaledWidth() - gui.getGuiLeft() - width;
        return Math.max(minX, Math.min(value, maxX));
    }

    private int clampY(int value) {
        int minY = -gui.getGuiTop();
        int maxY = minecraft.getWindow().getGuiScaledHeight() - gui.getGuiTop() - height;
        return Math.max(minY, Math.min(value, maxY));
    }

    public void close() {
        windowType.saveLastPosition(relativeX, relativeY);
        gui.removeWindow(this);
        children.forEach(GuiElement::onWindowClose);
    }

    @Override
    public void renderBackgroundOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 阴影垫在窗口外一圈
        drawNineSliced(guiGraphics, GuiTextures.SHADOW,
              relativeX - 3, relativeY - 3, width + 6, height + 6, NINE_SLICE, 256, 256);
        // 窗口面板
        drawNineSliced(guiGraphics, GuiTextures.BASE, relativeX, relativeY, width, height, NINE_SLICE, 256, 256);
    }

    /** 非焦点窗口的模糊蒙层，30% 透明度。 */
    public void renderBlur(GuiGraphics guiGraphics) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.3f);
        drawNineSliced(guiGraphics, GuiTextures.BLUR, relativeX, relativeY, width, height, NINE_SLICE, 256, 256);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public enum InteractionStrategy {
        NONE(false, false),
        CONTAINER(true, false),
        ALL(false, true);

        private final boolean allowContainer;
        private final boolean allowAll;

        InteractionStrategy(boolean allowContainer, boolean allowAll) {
            this.allowContainer = allowContainer;
            this.allowAll = allowAll;
        }

        public boolean allowContainer() {
            return allowContainer || allowAll;
        }

        public boolean allowAll() {
            return allowAll;
        }
    }
}
