package github.C2F4n.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import github.C2F4n.client.gui.GuiTextures;
import github.C2F4n.client.gui.IGuiHost;
import github.C2F4n.client.gui.FluidRecipeViewer;
import github.C2F4n.common.network.PacketHandler;
import github.C2F4n.common.network.to_server.PacketGuiInteract;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

/**
 * 主界面流体罐：负责液面渲染、悬停显示液体名，
 * 左键倒入（输入罐）、右键用容器取出（四罐）。
 */
public class GuiTankGauge extends GuiElement {

    /** 四个罐的颜色描边：输入 1/2/3 + 输出。 */
    private static final int[] TANK_COLORS = {0xFF50E69B, 0xFFFFFF33, 0xFF47CBE6, 0xFFB33636};

    private final int tankIndex;

    public GuiTankGauge(IGuiHost gui, int relativeX, int relativeY, int width, int height, int tankIndex) {
        super(gui, relativeX, relativeY, width, height);
        this.tankIndex = tankIndex;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        IFluidTank tank = gui.getBlockEntity().getTank(tankIndex);
        FluidStack stack = tank.getFluid();
        // 贴图顺序：浅灰底 → 流体 → 透明刻度，保证液体不被底色遮挡。
        guiGraphics.blit(GuiTextures.GAUGE_BACKGROUND, relativeX, relativeY, 0, 0, width, height, width, height);
        if (!stack.isEmpty()) {
            int fillHeight = Math.round(height * ((float) stack.getAmount() / tank.getCapacity()));
            if (fillHeight > 0) {
                IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(stack.getFluid());
                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                      .apply(extensions.getStillTexture(stack));
                int color = extensions.getTintColor(stack);
                float alpha = (color >>> 24 & 255) / 255f;
                float red = (color >>> 16 & 255) / 255f;
                float green = (color >>> 8 & 255) / 255f;
                float blue = (color & 255) / 255f;
                RenderSystem.setShaderColor(red, green, blue, alpha);
                guiGraphics.blit(relativeX, relativeY + height - fillHeight, 0, width, fillHeight, sprite);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }
        }
        // 刻度/边框叠加层
        guiGraphics.blit(GuiTextures.GAUGE_TALL, relativeX, relativeY, 0, 0, width, height, width, height);
        // 每个罐自己的细描边，形成“槽位”的样子
        int color = TANK_COLORS[tankIndex];
        guiGraphics.fill(relativeX, relativeY, relativeX + width, relativeY + 1, color);
        guiGraphics.fill(relativeX, relativeY + height - 1, relativeX + width, relativeY + height, color);
        guiGraphics.fill(relativeX, relativeY, relativeX + 1, relativeY + height, color);
        guiGraphics.fill(relativeX + width - 1, relativeY, relativeX + width, relativeY + height, color);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (gui.getMenu().getCarried().isEmpty()) {
            // 空手左键：不执行倒/取，改为查看该流体的合成配方（R）。
            if (button == 0) {
                FluidStack fluid = gui.getBlockEntity().getTank(tankIndex).getFluid();
                if (!fluid.isEmpty()) {
                    FluidRecipeViewer.openRecipes(fluid);
                }
            }
            return;
        }
        if (button == 0 && tankIndex <= 2) {
            PacketHandler.sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.TANK_POUR,
                  gui.getBlockEntity().getBlockPos(), tankIndex | (Screen.hasShiftDown() ? 0x80 : 0)));
        } else if (button == 1) {
            PacketHandler.sendToServer(new PacketGuiInteract(PacketGuiInteract.GuiInteraction.TANK_EXTRACT,
                  gui.getBlockEntity().getBlockPos(), tankIndex | (Screen.hasShiftDown() ? 0x80 : 0)));
        }
    }

    @Override
    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        FluidStack fluid = gui.getBlockEntity().getTank(tankIndex).getFluid();
        if (!fluid.isEmpty() && isHovered(mouseX, mouseY)) {
            IFluidTank tank = gui.getBlockEntity().getTank(tankIndex);
            gui.displayTooltip(guiGraphics, mouseX, mouseY,
                  fluid.getDisplayName(),
                  Component.translatable("tooltip.c2f4nfluidcrafting.fluid_amount",
                        fluid.getAmount(), tank.getCapacity()));
        }
    }
}
