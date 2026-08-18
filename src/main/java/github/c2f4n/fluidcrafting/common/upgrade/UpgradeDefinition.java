package github.c2f4n.fluidcrafting.common.upgrade;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * 一张升级卡的数据驱动定义。
 *
 * @param id 定义 ID，也是 NBT 中已安装数量的键。
 * @param item 对应的升级卡物品。
 * @param kind 升级类别。
 * @param speedMultiplier 每张速度升级额外加的速度；总速度 = 1 + Σ(数量 × 此值)。
 * @param maxCount 该卡在机器内的最大装载数量。
 */
public record UpgradeDefinition(ResourceLocation id, Item item, UpgradeKind kind,
                                double speedMultiplier, int maxCount) {

    public ItemStack createStack(int count) {
        return new ItemStack(item, Math.max(1, count));
    }

    public Component getDisplayName() {
        return Component.translatable(item.getDescriptionId());
    }

    public MutableComponent getDescription() {
        return switch (kind) {
            case SPEED -> Component.translatable("upgrade.c2f4nfluidcrafting.speed.description",
                  getSpeedPercentText());
            case CREATIVE -> Component.translatable("upgrade.c2f4nfluidcrafting.creative.description");
        };
    }

    public String getSpeedPercentText() {
        return formatPercent((speedMultiplier - 1) * 100);
    }

    public static String formatPercent(double value) {
        if (!Double.isFinite(value)) {
            return "0";
        }
        if (value == Math.rint(value)) {
            return Integer.toString((int) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
