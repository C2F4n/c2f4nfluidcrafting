package github.c2f4n.fluidcrafting.client.gui.window;

import com.mojang.blaze3d.systems.RenderSystem;
import github.c2f4n.fluidcrafting.block.BasicFluidMixerBlock;
import github.c2f4n.fluidcrafting.client.gui.GuiTextures;
import github.c2f4n.fluidcrafting.client.gui.IGuiHost;
import github.c2f4n.fluidcrafting.client.gui.element.GuiButton;
import github.c2f4n.fluidcrafting.client.gui.element.GuiElement;
import github.c2f4n.fluidcrafting.client.gui.element.GuiSideTab;
import github.c2f4n.fluidcrafting.client.gui.element.GuiTextPanel;
import github.c2f4n.fluidcrafting.common.network.PacketHandler;
import github.c2f4n.fluidcrafting.common.network.to_server.PacketGuiInteract;
import github.c2f4n.fluidcrafting.common.tile.component.config.DataType;
import github.c2f4n.fluidcrafting.common.tile.component.config.TransmissionType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/** 输入输出配置窗口：流体/物品两页、六面染色、自动弹出开关。 */
public class IoConfigWindow extends GuiWindow {

    private boolean itemPage;

    public IoConfigWindow(IGuiHost gui) {
        super(gui, WindowType.IO_CONFIG);
        addChild(new GuiSideTab(gui, -27, 0, 26, true, GuiTextures.BTN_TAB_FLUID,
              () -> Component.translatable("gui.c2f4nfluidcrafting.fluid_config"),
              () -> setItemPage(false), () -> !itemPage));
        addChild(new GuiSideTab(gui, -27, 27, 26, true, GuiTextures.BTN_TAB_ITEM,
              () -> Component.translatable("gui.c2f4nfluidcrafting.item_config"),
              () -> setItemPage(true), () -> itemPage));
        addChild(new GuiTextPanel(gui, 29, 21, 64, 16, GuiTextures.PANEL_TEXT,
              this::autoEjectText, this::autoEjectColor));
        addChild(new GuiButton(gui, 101, 22, 14, 14, GuiTextures.BTN_AUTO_EJECT,
              () -> Component.translatable("gui.c2f4nfluidcrafting.auto_eject"), this::toggleAutoEject));

        // 六面按钮居中排布：side_holder 96×96 按六个 32×32 块切片，块内 UV 起点
        Direction facing = gui.getBlockEntity().getBlockState().getValue(BasicFluidMixerBlock.FACING);
        addFaceButton(44, 40, 32, 0, Direction.UP);
        addFaceButton(44, 72, 32, 32, facing);
        addFaceButton(12, 72, 0, 32, facing.getClockWise());
        addFaceButton(76, 72, 64, 32, facing.getCounterClockWise());
        addFaceButton(44, 104, 32, 64, Direction.DOWN);
        addFaceButton(76, 104, 64, 64, facing.getOpposite());
    }

    private void addFaceButton(int relativeX, int relativeY, int textureU, int textureV, Direction direction) {
        addChild(new FaceButton(relativeX, relativeY, textureU, textureV, direction));
    }

    public void setItemPage(boolean itemPage) {
        this.itemPage = itemPage;
    }

    public boolean isItemPage() {
        return itemPage;
    }

    private boolean isAutoEject() {
        return gui.getBlockEntity().getConfigComponent().isEjecting(
              itemPage ? TransmissionType.ITEM : TransmissionType.FLUID);
    }

    private Component autoEjectText() {
        return Component.translatable(isAutoEject()
              ? "gui.c2f4nfluidcrafting.auto_eject.on"
              : "gui.c2f4nfluidcrafting.auto_eject.off");
    }

    private int autoEjectColor() {
        return isAutoEject() ? 0xFF55FF55 : 0xFFFF5555;
    }

    private void toggleAutoEject() {
        int extra = ((!itemPage) ? 1 : 0) | (isAutoEject() ? 0 : 2);
        PacketHandler.sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.AUTO_EJECT,
              gui.getBlockEntity().getBlockPos(), extra));
    }

    private DataType faceMode(Direction direction) {
        return gui.getBlockEntity().getConfigComponent().getDataType(
              itemPage ? TransmissionType.ITEM : TransmissionType.FLUID, direction);
    }

    private String faceModeKey(Direction direction) {
        return switch (faceMode(direction)) {
            case NONE -> "gui.c2f4nfluidcrafting.mode.none";
            case FLUID_IN_1 -> "gui.c2f4nfluidcrafting.mode.fluid_in_1";
            case FLUID_IN_2 -> "gui.c2f4nfluidcrafting.mode.fluid_in_2";
            case FLUID_IN_3 -> "gui.c2f4nfluidcrafting.mode.fluid_in_3";
            case FLUID_OUT -> "gui.c2f4nfluidcrafting.mode.fluid_out";
            case ITEM_IN -> "gui.c2f4nfluidcrafting.mode.item_in";
            case ITEM_OUT -> "gui.c2f4nfluidcrafting.mode.item_out";
            case EMPTY_OUT -> "gui.c2f4nfluidcrafting.mode.empty_out";
        };
    }

    private class FaceButton extends GuiElement {

        private final int textureU;
        private final int textureV;
        private final Direction direction;

        FaceButton(int relativeX, int relativeY, int textureU, int textureV, Direction direction) {
            super(IoConfigWindow.this.gui, relativeX, relativeY, 32, 32);
            this.textureU = textureU;
            this.textureV = textureV;
            this.direction = direction;
        }

        @Override
        public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            int color = faceMode(direction).getColor();
            float red = (color >>> 16 & 255) / 255f;
            float green = (color >>> 8 & 255) / 255f;
            float blue = (color & 255) / 255f;
            RenderSystem.setShaderColor(red, green, blue, 1f);
            guiGraphics.blit(GuiTextures.SIDE_HOLDER, relativeX, relativeY, textureU, textureV, 32, 32, 96, 96);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        @Override
        public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (isHovered(mouseX, mouseY)) {
                guiGraphics.fill(relativeX, relativeY, relativeX + width, relativeY + height, GuiButton.HOVER_COLOR);
            }
            if (isPressed()) {
                guiGraphics.fill(relativeX + 1, relativeY + 1, relativeX + 1 + width, relativeY + 1 + height,
                      GuiButton.PRESSED_COLOR);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            boolean reverse = Screen.hasShiftDown() && button != 1;
            int extra = direction.ordinal()
                  | (itemPage ? 0x10 : 0)
                  | (reverse ? 0x20 : 0)
                  | (button == 1 ? 0x40 : 0);
            PacketHandler.sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.FACE_MODE,
                  gui.getBlockEntity().getBlockPos(), extra));
        }

        @Override
        public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (isHovered(mouseX, mouseY)) {
                gui.displayTooltip(guiGraphics, mouseX, mouseY,
                      Component.translatable(faceModeKey(direction)));
            }
        }
    }
}
