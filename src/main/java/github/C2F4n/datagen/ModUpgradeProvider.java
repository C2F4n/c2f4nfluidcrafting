package github.C2F4n.datagen;

import com.google.gson.JsonObject;
import github.C2F4n.c2f4nfluidcrafting;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** 升级卡定义数据生成：输出 data/c2f4nfluidcrafting/upgrades/*.json。 */
public class ModUpgradeProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;

    public ModUpgradeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "upgrades");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> tasks = new ArrayList<>();
        addUpgrade(tasks, cache, "speed_upgrade_1", "speed", 1.2, 1);
        addUpgrade(tasks, cache, "speed_upgrade_2", "speed", 1.2, 2);
        addUpgrade(tasks, cache, "speed_upgrade_3", "speed", 1.2, 3);
        addUpgrade(tasks, cache, "speed_upgrade_4", "speed", 1.3, 3);
        addUpgrade(tasks, cache, "speed_upgrade_5", "speed", 1.3, 4);
        addUpgrade(tasks, cache, "creative_upgrade", "creative", 1.0, 1);
        return CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[0]));
    }

    private void addUpgrade(List<CompletableFuture<?>> tasks, CachedOutput cache, String name,
                            String type, double speedMultiplier, int maxCount) {
        JsonObject json = new JsonObject();
        String id = c2f4nfluidcrafting.MODID + ":" + name;
        json.addProperty("id", id);
        json.addProperty("item", id);
        json.addProperty("type", type);
        json.addProperty("speed_multiplier", speedMultiplier);
        json.addProperty("max_count", maxCount);
        Path output = pathProvider.json(Objects.requireNonNull(
              ResourceLocation.tryParse(c2f4nfluidcrafting.MODID + ":" + name)));
        tasks.add(DataProvider.saveStable(cache, json, output));
    }

    @Override
    public String getName() {
        return "C2F4n Upgrades";
    }
}
