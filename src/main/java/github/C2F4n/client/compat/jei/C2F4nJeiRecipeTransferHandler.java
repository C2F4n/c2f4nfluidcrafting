package github.C2F4n.client.compat.jei;

import github.C2F4n.common.inventory.container.BasicFluidMixerContainer;
import github.C2F4n.common.network.PacketHandler;
import github.C2F4n.common.network.to_server.PacketJeiRecipeTransfer;
import github.C2F4n.common.recipe.MixingRecipe;
import github.C2F4n.common.recipe.transfer.MixingRecipeTransfer;
import github.C2F4n.registry.ModMenuTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import java.util.Optional;

/**
 * JEI 一键放置。
 * 客户端只做输入校验；点击确认时发 {@link PacketJeiRecipeTransfer} 给服务器，
 * 由服务器在自己的容器上真正移动物品，避免“看起来放进去了、实际没生效”。
 */
public class C2F4nJeiRecipeTransferHandler
      implements IRecipeTransferHandler<BasicFluidMixerContainer, MixingRecipe> {

    private final IRecipeTransferHandlerHelper helper;

    public C2F4nJeiRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<? extends BasicFluidMixerContainer> getContainerClass() {
        return BasicFluidMixerContainer.class;
    }

    @Override
    public Optional<MenuType<BasicFluidMixerContainer>> getMenuType() {
        return Optional.of(ModMenuTypes.BASIC_FLUID_MIXER.get());
    }

    @Override
    public RecipeType<MixingRecipe> getRecipeType() {
        return MixingRecipeCategory.TYPE;
    }

    @Override
    public IRecipeTransferError transferRecipe(BasicFluidMixerContainer container, MixingRecipe recipe,
                                               IRecipeSlotsView recipeSlots, Player player,
                                               boolean maxTransfer, boolean doTransfer) {
        MixingRecipeTransfer.Plan plan = MixingRecipeTransfer.buildPlan(container, recipe, player);
        if (plan.failure() == MixingRecipeTransfer.Failure.TARGET_OCCUPIED) {
            return helper.createUserErrorWithTooltip(
                  Component.translatable("gui.c2f4nfluidcrafting.jei.transfer_slot_occupied"));
        }
        if (plan.failure() == MixingRecipeTransfer.Failure.MISSING_INPUTS) {
            return helper.createUserErrorWithTooltip(
                  Component.translatable("gui.c2f4nfluidcrafting.jei.transfer_missing"));
        }
        if (doTransfer) {
            PacketHandler.sendToServer(new PacketJeiRecipeTransfer(recipe.getId()));
        }
        return null;
    }
}
