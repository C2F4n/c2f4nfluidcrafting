package github.c2f4n.fluidcrafting.datagen;

import github.c2f4n.fluidcrafting.c2f4nfluidcrafting;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

/** 数据生成入口：`gradlew runData` 产出语言/方块状态/配方/战利品/标签。 */
@Mod.EventBusSubscriber(modid = c2f4nfluidcrafting.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDataGenerator {

    private ModDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existing = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, c2f4nfluidcrafting.MODID, existing));
        generator.addProvider(event.includeClient(), new ModLangProvider(output, c2f4nfluidcrafting.MODID, "en_us"));
        generator.addProvider(event.includeClient(), new ModLangProvider(output, c2f4nfluidcrafting.MODID, "zh_cn"));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output));
        generator.addProvider(event.includeServer(), new ModLootTableProvider(output));
        generator.addProvider(event.includeServer(), new ModTagsProvider(output, lookup, c2f4nfluidcrafting.MODID, existing));
        generator.addProvider(event.includeServer(), new ModUpgradeProvider(output));
    }
}
