package github.C2F4n.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BasicFluidMixerBlockItem extends BlockItem {

    public BasicFluidMixerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        boolean keepNbt = tag == null || tag.getInt("mining_mode") == 0;
        tooltip.add(Component.translatable("tooltip.c2f4nfluidcrafting.keep_nbt")
                .append(Component.literal("："))
                .append(Component.translatable(
                                keepNbt ? "tooltip.c2f4nfluidcrafting.on" : "tooltip.c2f4nfluidcrafting.off")
                        )
                .withStyle(ChatFormatting.AQUA));
        Component redstoneMode = switch (tag == null ? 0 : tag.getInt("redstone_mode")) {
            case 1 -> Component.translatable("tooltip.c2f4nfluidcrafting.redstone_mode_on_signal");
            default -> Component.translatable("tooltip.c2f4nfluidcrafting.redstone_mode_off");
        };
        tooltip.add(Component.translatable("tooltip.c2f4nfluidcrafting.redstone_mode")
                .append(Component.literal("："))
                .append(redstoneMode)
                .withStyle(ChatFormatting.AQUA));

        if (!keepNbt || tag == null) {
            return;
        }

        tooltip.add(Component.translatable("tooltip.c2f4nfluidcrafting.fluids").withStyle(ChatFormatting.LIGHT_PURPLE));
        List<Component> fluids = new ArrayList<>();
        String[] tankKeys = {"input_tank_1", "input_tank_2", "input_tank_3", "output_tank"};
        for (String key : tankKeys) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound(key));
            if (!fluid.isEmpty()) {
                fluids.add(indent(fluid.getDisplayName().copy().append(Component.literal(" ×" + fluid.getAmount() + " mB"))));
            }
        }
        if (fluids.isEmpty()) {
            tooltip.add(emptyLine());
        } else {
            tooltip.addAll(fluids);
        }

        tooltip.add(Component.translatable("tooltip.c2f4nfluidcrafting.items").withStyle(ChatFormatting.LIGHT_PURPLE));
        List<Component> items = new ArrayList<>();
        CompoundTag inventory = tag.getCompound("inventory");
        ListTag slots = inventory.getList("Items", 10);
        for (int i = 0; i < slots.size(); i++) {
            CompoundTag slotTag = slots.getCompound(i);
            int slot = slotTag.getInt("Slot");
            if (slot != 0 && slot != 1) {
                continue;
            }
            ItemStack content = ItemStack.of(slotTag);
            if (!content.isEmpty()) {
                items.add(indent(content.getHoverName().copy().append(Component.literal(" ×" + content.getCount()))));
            }
        }
        if (items.isEmpty()) {
            tooltip.add(emptyLine());
        } else {
            tooltip.addAll(items);
        }
    }

    private static Component indent(Component component) {
        return Component.literal("  ").append(component).withStyle(ChatFormatting.DARK_GRAY);
    }

    private static Component emptyLine() {
        return Component.literal("  ").append(Component.translatable("tooltip.c2f4nfluidcrafting.empty"))
                .withStyle(ChatFormatting.DARK_GRAY);
    }
}
