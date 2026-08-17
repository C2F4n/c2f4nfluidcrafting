package github.C2F4n.common.network.to_server;

import github.C2F4n.common.inventory.container.BasicFluidMixerContainer;
import github.C2F4n.common.recipe.MixingRecipe;
import github.C2F4n.common.recipe.transfer.MixingRecipeTransfer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/** JEI 配方一键放置：客户端只发配方 ID，真正移动物品在服务器容器上执行。 */
public class PacketJeiRecipeTransfer {

    private final ResourceLocation recipeId;

    public PacketJeiRecipeTransfer(ResourceLocation recipeId) {
        this.recipeId = recipeId;
    }

    public static void encode(PacketJeiRecipeTransfer msg, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(msg.recipeId);
    }

    public static PacketJeiRecipeTransfer decode(FriendlyByteBuf buffer) {
        return new PacketJeiRecipeTransfer(buffer.readResourceLocation());
    }

    public static void handle(PacketJeiRecipeTransfer msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !(player.containerMenu instanceof BasicFluidMixerContainer container)) {
                return;
            }
            Optional<? extends Recipe<?>> recipe = player.level().getRecipeManager().byKey(msg.recipeId);
            if (recipe.isPresent() && recipe.get() instanceof MixingRecipe mixing) {
                MixingRecipeTransfer.execute(container, mixing, player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
