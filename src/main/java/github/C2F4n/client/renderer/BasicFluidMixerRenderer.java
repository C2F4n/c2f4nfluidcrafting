package github.C2F4n.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import github.C2F4n.block.BasicFluidMixerBlock;
import github.C2F4n.block.entity.BasicFluidMixerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.joml.Matrix4f;

public class BasicFluidMixerRenderer implements BlockEntityRenderer<BasicFluidMixerBlockEntity> {
    // 与 basicfluidmixer.json 的玻璃空腔保持一致：
    // 输入 1/2/3 = 前侧三个玻璃罐，输出 = 西侧长罐。整体按模型坐标，朝向旋转在 render 时统一应用。
    private static final AABB INPUT1_BOUNDS = inset(13 / 16d, 2 / 16d, 0 / 16d, 16 / 16d, 12 / 16d, 3 / 16d);
    private static final AABB INPUT2_BOUNDS = inset(5 / 16d, 2 / 16d, 0 / 16d, 11 / 16d, 12 / 16d, 3 / 16d);
    private static final AABB INPUT3_BOUNDS = inset(0 / 16d, 2 / 16d, 0 / 16d, 3 / 16d, 12 / 16d, 3 / 16d);
    private static final AABB OUTPUT_BOUNDS = inset(0 / 16d, 2 / 16d, 4 / 16d, 2 / 16d, 13 / 16d, 14 / 16d);

    /** 让流体略小于玻璃盒，避免和玻璃面共面闪烁。 */
    private static AABB inset(double x1, double y1, double z1, double x2, double y2, double z2) {
        double inset = 0.02;
        return new AABB(x1 + inset, y1 + inset, z1 + inset, x2 - inset, y2 - inset, z2 - inset);
    }

    public BasicFluidMixerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BasicFluidMixerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyFacingRotation(poseStack, blockEntity);
        renderFluidTank(blockEntity.getInputTank(0), INPUT1_BOUNDS, poseStack, buffer, packedLight, packedOverlay);
        renderFluidTank(blockEntity.getInputTank(1), INPUT2_BOUNDS, poseStack, buffer, packedLight, packedOverlay);
        renderFluidTank(blockEntity.getInputTank(2), INPUT3_BOUNDS, poseStack, buffer, packedLight, packedOverlay);
        renderFluidTank(blockEntity.getOutputTank(), OUTPUT_BOUNDS, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    /** 让叠加渲染的齿轮/流体跟随方块朝向旋转，和 blockstate 里的 y 旋转保持一致 */
    private static void applyFacingRotation(PoseStack poseStack, BasicFluidMixerBlockEntity blockEntity) {
        Direction facing = blockEntity.getBlockState().getValue(BasicFluidMixerBlock.FACING);
        float angle = switch (facing) {
            case EAST -> -90f;
            case SOUTH -> 180f;
            case WEST -> 90f;
            default -> 0f;
        };
        if (angle != 0f) {
            poseStack.translate(0.5, 0.0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.translate(-0.5, 0.0, -0.5);
        }
    }

    private void renderFluidTank(IFluidTank tank, AABB bounds, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        FluidStack stack = tank.getFluid();
        if (stack.isEmpty()) {
            return;
        }

        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(stack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extensions.getStillTexture(stack));
        int color = extensions.getTintColor(stack);
        float alpha = (color >>> 24 & 255) / 255f;
        float red = (color >>> 16 & 255) / 255f;
        float green = (color >>> 8 & 255) / 255f;
        float blue = (color & 255) / 255f;

        float fill = (float) stack.getAmount() / tank.getCapacity();
        float x1 = (float) bounds.minX;
        float x2 = (float) bounds.maxX;
        float y1 = (float) bounds.minY;
        float y2 = y1 + (float) (bounds.getYsize() * fill);
        float z1 = (float) bounds.minZ;
        float z2 = (float) bounds.maxZ;
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentCull(InventoryMenu.BLOCK_ATLAS));

        quad(consumer, matrix, x1, y1, z1, x1, y1, z2, x2, y1, z2, x2, y1, z1,
                sprite.getU((float) bounds.minX * 16), sprite.getV((float) bounds.minZ * 16),
                sprite.getU((float) bounds.maxX * 16), sprite.getV((float) bounds.maxZ * 16),
                packedLight, packedOverlay, red, green, blue, alpha, 0, -1, 0);

        quad(consumer, matrix, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1,
                sprite.getU((float) bounds.minX * 16), sprite.getV((float) bounds.minZ * 16),
                sprite.getU((float) bounds.maxX * 16), sprite.getV((float) bounds.maxZ * 16),
                packedLight, packedOverlay, red, green, blue, alpha, 0, 1, 0);

        quad(consumer, matrix, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1,
                sprite.getU((float) bounds.minX * 16), sprite.getV((float) bounds.minY * 16),
                sprite.getU((float) bounds.maxX * 16), sprite.getV((float) bounds.maxY * 16),
                packedLight, packedOverlay, red, green, blue, alpha, 0, 0, -1);

        quad(consumer, matrix, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2,
                sprite.getU((float) bounds.minX * 16), sprite.getV((float) bounds.minY * 16),
                sprite.getU((float) bounds.maxX * 16), sprite.getV((float) bounds.maxY * 16),
                packedLight, packedOverlay, red, green, blue, alpha, 0, 0, 1);

        quad(consumer, matrix, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1,
                sprite.getU((float) bounds.minY * 16), sprite.getV((float) bounds.minZ * 16),
                sprite.getU((float) bounds.maxY * 16), sprite.getV((float) bounds.maxZ * 16),
                packedLight, packedOverlay, red, green, blue, alpha, -1, 0, 0);

        quad(consumer, matrix, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2,
                sprite.getU((float) bounds.minY * 16), sprite.getV((float) bounds.minZ * 16),
                sprite.getU((float) bounds.maxY * 16), sprite.getV((float) bounds.maxZ * 16),
                packedLight, packedOverlay, red, green, blue, alpha, 1, 0, 0);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix,
                             float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4,
                             float u1, float v1, float u2, float v2,
                             int packedLight, int packedOverlay, float red, float green, float blue, float alpha,
                             float nx, float ny, float nz) {
        consumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).uv(u1, v1).overlayCoords(packedOverlay).uv2(packedLight).normal(nx, ny, nz).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).uv(u1, v2).overlayCoords(packedOverlay).uv2(packedLight).normal(nx, ny, nz).endVertex();
        consumer.vertex(matrix, x3, y3, z3).color(red, green, blue, alpha).uv(u2, v2).overlayCoords(packedOverlay).uv2(packedLight).normal(nx, ny, nz).endVertex();
        consumer.vertex(matrix, x4, y4, z4).color(red, green, blue, alpha).uv(u2, v1).overlayCoords(packedOverlay).uv2(packedLight).normal(nx, ny, nz).endVertex();
    }
}
