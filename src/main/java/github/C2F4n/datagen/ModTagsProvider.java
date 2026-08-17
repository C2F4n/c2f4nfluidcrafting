package github.C2F4n.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.C2F4n.c2f4nfluidcrafting;
import github.C2F4n.registry.ModBlocks;
import github.C2F4n.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/** 标签生成：可镐挖 + 升级物品标签。 */
public class ModTagsProvider implements DataProvider {

    private final PackOutput.PathProvider blockTagPath;
    private final PackOutput.PathProvider itemTagPath;

    public ModTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                           String modid, ExistingFileHelper existing) {
        this.blockTagPath = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/blocks");
        this.itemTagPath = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject pickaxe = new JsonObject();
        pickaxe.addProperty("replace", false);
        JsonArray pickaxeValues = new JsonArray();
        pickaxeValues.add(c2f4nfluidcrafting.MODID + ":basicfluidmixer");
        pickaxe.add("values", pickaxeValues);

        JsonObject upgrades = new JsonObject();
        upgrades.addProperty("replace", false);
        JsonArray upgradeValues = new JsonArray();
        upgradeValues.add(c2f4nfluidcrafting.MODID + ":speed_upgrade_1");
        upgrades.add("values", upgradeValues);

        CompletableFuture<?> first = DataProvider.saveStable(cache, pickaxe,
              blockTagPath.json(new net.minecraft.resources.ResourceLocation("minecraft", "mineable/pickaxe")));
        CompletableFuture<?> second = DataProvider.saveStable(cache, upgrades,
              itemTagPath.json(new net.minecraft.resources.ResourceLocation(c2f4nfluidcrafting.MODID, "upgrades")));
        return CompletableFuture.allOf(first, second);
    }

    @Override
    public String getName() {
        return "C2F4n Tags";
    }
}
