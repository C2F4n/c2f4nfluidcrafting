package github.C2F4n.registry;

import github.C2F4n.c2f4nfluidcrafting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Objects;

public final class ModTags {

    /** 所有可放入机器升级插槽的物品 */
    public static final TagKey<Item> UPGRADES = ItemTags.create(
            Objects.requireNonNull(ResourceLocation.tryParse(c2f4nfluidcrafting.MODID + ":upgrades")));

    private ModTags() {
    }
}
