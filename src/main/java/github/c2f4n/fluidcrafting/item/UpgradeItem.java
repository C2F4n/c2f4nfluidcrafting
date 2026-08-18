package github.c2f4n.fluidcrafting.item;

import github.c2f4n.fluidcrafting.common.upgrade.UpgradeDefinition;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** 通用升级卡物品。数值从升级定义 JSON 中读取，tooltip 也随 JSON 动态变化。 */
public class UpgradeItem extends Item {

    public UpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        UpgradeDefinition definition = UpgradeRegistry.client().getForStack(stack);
        if (definition != null) {
            tooltip.add(definition.getDescription().withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.c2f4nfluidcrafting.upgrade.max_count",
                  definition.maxCount()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
