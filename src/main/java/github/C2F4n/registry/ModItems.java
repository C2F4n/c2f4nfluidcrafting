package github.C2F4n.registry;

import github.C2F4n.c2f4nfluidcrafting;
import github.C2F4n.item.BasicFluidMixerBlockItem;
import github.C2F4n.item.UpgradeItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, c2f4nfluidcrafting.MODID);

    public static final RegistryObject<Item> BASIC_FLUID_MIXER = ITEMS.register("basicfluidmixer",
            () -> new BasicFluidMixerBlockItem(ModBlocks.BASIC_FLUID_MIXER.get(), new Item.Properties()));

    public static final RegistryObject<Item> SPEED_UPGRADE_1 = ITEMS.register("speed_upgrade_1",
            () -> new UpgradeItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SPEED_UPGRADE_2 = ITEMS.register("speed_upgrade_2",
            () -> new UpgradeItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SPEED_UPGRADE_3 = ITEMS.register("speed_upgrade_3",
            () -> new UpgradeItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SPEED_UPGRADE_4 = ITEMS.register("speed_upgrade_4",
            () -> new UpgradeItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SPEED_UPGRADE_5 = ITEMS.register("speed_upgrade_5",
            () -> new UpgradeItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> CREATIVE_UPGRADE = ITEMS.register("creative_upgrade",
            () -> new UpgradeItem(new Item.Properties().stacksTo(16)));

    private ModItems() {
    }
}
