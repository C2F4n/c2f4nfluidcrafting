package github.c2f4n.fluidcrafting.common.recipe.transfer;

import github.c2f4n.fluidcrafting.block.entity.BasicFluidMixerBlockEntity;
import github.c2f4n.fluidcrafting.common.inventory.container.BasicFluidMixerContainer;
import github.c2f4n.fluidcrafting.common.inventory.container.slot.MachineSlot;
import github.c2f4n.fluidcrafting.common.recipe.MixingRecipe;
import github.c2f4n.fluidcrafting.common.recipe.ingredient.FluidIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 配方传输搬运逻辑，客户端用于校验、服务器用于真正执行。
 * 物品输入 → 0 号固体输入槽；流体输入 → 从背包找对应流体容器放进 2/3/4 号容器槽。
 */
public final class MixingRecipeTransfer {

    public static final int SOLID_INPUT_SLOT = 0;
    public static final int FLUID_CONTAINER_SLOT_START = 2;

    private MixingRecipeTransfer() {
    }

    public static Plan buildPlan(BasicFluidMixerContainer container, MixingRecipe recipe, Player player) {
        List<Slot> playerSlots = container.slots.stream()
              .filter(slot -> slot.container == player.getInventory())
              .toList();
        Set<Slot> usedSources = new HashSet<>();
        List<Move> moves = new ArrayList<>();

        List<FluidIngredient> fluidInputs = recipe.getFluidInputs();
        BasicFluidMixerBlockEntity blockEntity = container.getBlockEntity();
        Set<Integer> usedTanks = new HashSet<>();
        for (int i = 0; i < fluidInputs.size(); i++) {
            FluidIngredient ingredient = fluidInputs.get(i);
            List<Fluid> fluids = resolve(ingredient);
            int tankIndex = chooseTank(blockEntity, fluids, usedTanks);
            if (tankIndex == -1) {
                return Plan.missing();
            }
            usedTanks.add(tankIndex);
            Slot target = container.getSlot(FLUID_CONTAINER_SLOT_START + tankIndex);
            if (target.hasItem()) {
                return Plan.occupied();
            }
            Slot source = findFluidContainer(playerSlots, fluids, ingredient.getAmount(), usedSources);
            if (source == null) {
                return Plan.missing();
            }
            usedSources.add(source);
            moves.add(new Move(source.index, target.index, 1));
        }

        Ingredient itemInput = recipe.getItemInput();
        if (!itemInput.isEmpty()) {
            Slot target = container.getSlot(SOLID_INPUT_SLOT);
            Slot source = findItemSlot(playerSlots, itemInput, usedSources);
            if (source == null) {
                return Plan.missing();
            }
            if (target.hasItem() && !target.mayPlace(source.getItem())) {
                return Plan.occupied();
            }
            usedSources.add(source);
            int count = Math.min(firstCount(itemInput), source.getItem().getCount());
            moves.add(new Move(source.index, target.index, count));
        }

        return new Plan(List.copyOf(moves), Failure.NONE);
    }

    /** 服务器端：重建计划并真正移动物品。 */
    public static void execute(BasicFluidMixerContainer container, MixingRecipe recipe, Player player) {
        Plan plan = buildPlan(container, recipe, player);
        if (plan.failure() != Failure.NONE) {
            return;
        }
        for (Move move : plan.moves()) {
            moveIntoSlot(container, move);
        }
        container.broadcastChanges();
    }

    private static void moveIntoSlot(BasicFluidMixerContainer container, Move move) {
        Slot source = container.getSlot(move.sourceIndex());
        Slot target = container.getSlot(move.targetIndex());
        ItemStack sourceStack = source.getItem();
        if (!(target instanceof MachineSlot machineSlot)) {
            return;
        }
        ItemStack template = sourceStack.copyWithCount(move.count());
        ItemStack remainder = machineSlot.insertItem(template, false);
        int moved = move.count() - remainder.getCount();
        if (moved <= 0) {
            return;
        }
        sourceStack.shrink(moved);
        if (sourceStack.isEmpty()) {
            source.set(ItemStack.EMPTY);
        }
        source.setChanged();
        machineSlot.setChanged();
    }

    /** 优先放进已经装着同种流体的罐，其次是空罐。 */
    private static int chooseTank(BasicFluidMixerBlockEntity blockEntity, List<Fluid> fluids,
                                  Set<Integer> usedTanks) {
        int emptyTank = -1;
        for (int i = 0; i < 3; i++) {
            if (usedTanks.contains(i)) {
                continue;
            }
            FluidStack current = blockEntity.getInputTank(i).getFluid();
            if (current.isEmpty()) {
                if (emptyTank == -1) {
                    emptyTank = i;
                }
            } else if (fluids.stream().anyMatch(fluid -> current.getFluid() == fluid)) {
                return i;
            }
        }
        return emptyTank;
    }

    @Nullable
    private static Slot findFluidContainer(List<Slot> playerSlots, List<Fluid> fluids, long amount,
                                           Set<Slot> usedSources) {
        Slot best = null;
        long bestAmount = -1;
        for (Slot slot : playerSlots) {
            if (usedSources.contains(slot) || !slot.hasItem()) {
                continue;
            }
            Optional<IFluidHandlerItem> optional = FluidUtil.getFluidHandler(slot.getItem()).resolve();
            if (optional.isEmpty()) {
                continue;
            }
            FluidStack drainable = optional.get().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            if (drainable.isEmpty() || fluids.stream().noneMatch(fluid -> drainable.getFluid() == fluid)) {
                continue;
            }
            if (drainable.getAmount() >= amount) {
                return slot;
            }
            if (drainable.getAmount() > bestAmount) {
                best = slot;
                bestAmount = drainable.getAmount();
            }
        }
        return best;
    }

    @Nullable
    private static Slot findItemSlot(List<Slot> playerSlots, Ingredient ingredient, Set<Slot> usedSources) {
        for (Slot slot : playerSlots) {
            if (!usedSources.contains(slot) && slot.hasItem() && ingredient.test(slot.getItem())) {
                return slot;
            }
        }
        return null;
    }

    private static List<Fluid> resolve(FluidIngredient ingredient) {
        if (!ingredient.isTag()) {
            Fluid fluid = ingredient.getFluid();
            return fluid == null ? List.of() : List.of(fluid);
        }
        TagKey<Fluid> tag = ingredient.getTag();
        if (tag == null) {
            return List.of();
        }
        return BuiltInRegistries.FLUID.getTag(tag)
              .map(HolderSet.Named::stream)
              .orElseGet(Stream::empty)
              .map(Holder::value)
              .distinct()
              .toList();
    }

    private static int firstCount(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getItems();
        return stacks.length == 0 ? 1 : stacks[0].getCount();
    }

    public enum Failure {
        NONE, TARGET_OCCUPIED, MISSING_INPUTS
    }

    public record Plan(List<Move> moves, Failure failure) {
        static Plan occupied() {
            return new Plan(List.of(), Failure.TARGET_OCCUPIED);
        }

        static Plan missing() {
            return new Plan(List.of(), Failure.MISSING_INPUTS);
        }
    }

    public record Move(int sourceIndex, int targetIndex, int count) {
    }
}
