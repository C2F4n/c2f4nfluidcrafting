package github.c2f4n.fluidcrafting.client.compat.jei;

import github.c2f4n.fluidcrafting.c2f4nfluidcrafting;
import github.c2f4n.fluidcrafting.client.compat.viewer.MixingRecipeView;
import github.c2f4n.fluidcrafting.client.gui.GuiTextures;
import github.c2f4n.fluidcrafting.common.recipe.MixingRecipe;
import github.c2f4n.fluidcrafting.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/** JEI 的“流体混合”配方分类，槽位布局与 {@link MixingRecipeView} 完全一致。 */
@OnlyIn(Dist.CLIENT)
public class MixingRecipeCategory extends AbstractRecipeCategory<MixingRecipe> {

    public static final RecipeType<MixingRecipe> TYPE =
          RecipeType.create(c2f4nfluidcrafting.MODID, "mixing", MixingRecipe.class);

    private final mezz.jei.api.gui.drawable.IDrawable tankBase;
    private final mezz.jei.api.gui.drawable.IDrawable tankScale;
    private final mezz.jei.api.gui.drawable.IDrawable slotInput;
    private final mezz.jei.api.gui.drawable.IDrawable slotOutput;

    public MixingRecipeCategory(IGuiHelper guiHelper) {
        super(TYPE,
              Component.translatable("jei.c2f4nfluidcrafting.category.mixing"),
              guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BASIC_FLUID_MIXER.get())),
              MixingRecipeView.WIDTH, MixingRecipeView.HEIGHT);
        this.tankBase = guiHelper.drawableBuilder(GuiTextures.GAUGE_BACKGROUND, 0, 0,
                    MixingRecipeView.TANK_WIDTH, MixingRecipeView.TANK_HEIGHT)
              .setTextureSize(MixingRecipeView.TANK_WIDTH, MixingRecipeView.TANK_HEIGHT)
              .build();
        this.tankScale = guiHelper.drawableBuilder(GuiTextures.GAUGE_TALL, 0, 0,
                    MixingRecipeView.TANK_WIDTH, MixingRecipeView.TANK_HEIGHT)
              .setTextureSize(MixingRecipeView.TANK_WIDTH, MixingRecipeView.TANK_HEIGHT)
              .build();
        this.slotInput = guiHelper.drawableBuilder(GuiTextures.SLOT_INPUT, 0, 0, 18, 18)
              .setTextureSize(18, 18)
              .build();
        this.slotOutput = guiHelper.drawableBuilder(GuiTextures.SLOT_OUTPUT, 0, 0, 18, 18)
              .setTextureSize(18, 18)
              .build();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MixingRecipe recipe, IFocusGroup focuses) {
        MixingRecipeView view = new MixingRecipeView(recipe);

        // 三个输入流体槽无论配方是否使用都显示，空槽只画底板。
        for (int i = 0; i < MixingRecipeView.INPUT_TANK_COUNT; i++) {
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT,
                  MixingRecipeView.INPUT_TANK_X[i], MixingRecipeView.TANK_Y)
                  .setBackground(tankBase, 0, 0)
                  .setOverlay(tankScale, 0, 0);
            if (i < view.fluidInputCount()) {
                MixingRecipeView.FluidOption option = view.fluidInput(i);
                List<Fluid> fluids = option.fluids();
                for (Fluid fluid : fluids) {
                    slot.addFluidStack(fluid, option.amount());
                }
                slot.setFluidRenderer(MixingRecipeView.tankCapacity(), false,
                      MixingRecipeView.TANK_WIDTH, MixingRecipeView.TANK_HEIGHT);
            } else {
                slot.setFluidRenderer(MixingRecipeView.tankCapacity(), false,
                      MixingRecipeView.TANK_WIDTH, MixingRecipeView.TANK_HEIGHT);
            }
        }

        // 物品输入槽始终显示。
        IRecipeSlotBuilder itemInput = builder.addSlot(RecipeIngredientRole.INPUT,
              MixingRecipeView.ITEM_INPUT_X, MixingRecipeView.ITEM_INPUT_Y)
              .setBackground(slotInput, -1, -1);
        if (view.hasItemInput()) {
            itemInput.addIngredients(view.itemInput());
        }

        // 输出流体槽始终显示。
        IRecipeSlotBuilder fluidOutput = builder.addSlot(RecipeIngredientRole.OUTPUT,
              MixingRecipeView.OUTPUT_TANK_X, MixingRecipeView.TANK_Y)
              .setBackground(tankBase, 0, 0)
              .setOverlay(tankScale, 0, 0);
        if (!view.resultFluid().isEmpty()) {
            FluidStack result = view.resultFluid();
            fluidOutput.addFluidStack(result.getFluid(), result.getAmount());
            fluidOutput.setFluidRenderer(MixingRecipeView.tankCapacity(), false,
                  MixingRecipeView.TANK_WIDTH, MixingRecipeView.TANK_HEIGHT);
        } else {
            fluidOutput.setFluidRenderer(MixingRecipeView.tankCapacity(), false,
                  MixingRecipeView.TANK_WIDTH, MixingRecipeView.TANK_HEIGHT);
        }

        // 物品输出槽始终显示。
        IRecipeSlotBuilder itemOutput = builder.addSlot(RecipeIngredientRole.OUTPUT,
              MixingRecipeView.ITEM_OUTPUT_X, MixingRecipeView.ITEM_OUTPUT_Y)
              .setBackground(slotOutput, -1, -1);
        if (!view.resultItem().isEmpty()) {
            itemOutput.addItemStack(view.resultItem());
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, MixingRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(Math.max(1, recipe.getDuration()))
              .setPosition(MixingRecipeView.ARROW_X, MixingRecipeView.ARROW_Y);
        Component duration = Component.translatable("gui.c2f4nfluidcrafting.recipe_duration",
              MixingRecipeView.durationSeconds(recipe.getDuration()));
        builder.addText(duration, MixingRecipeView.WIDTH, 12)
              .setPosition(0, MixingRecipeView.DURATION_Y)
              .setTextAlignment(HorizontalAlignment.CENTER)
              .setColor(0xFF808080)
              .setShadow(false);
    }
}
