package github.C2F4n.common.upgrade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 从 data/&lt;namespace&gt;/upgrades/*.json 加载升级卡定义。 */
public class UpgradeReloadListener extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("C2F4nFluidCrafting/Upgrades");
    private static final Gson GSON = new GsonBuilder().create();

    public UpgradeReloadListener() {
        super(GSON, "upgrades");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        List<UpgradeDefinition> definitions = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            if (!(entry.getValue() instanceof JsonObject json)) {
                LOGGER.warn("Ignoring non-object upgrade definition {}", entry.getKey());
                continue;
            }
            ResourceLocation id = json.has("id")
                  ? ResourceLocation.tryParse(json.get("id").getAsString())
                  : entry.getKey();
            if (id == null) {
                LOGGER.warn("Ignoring upgrade definition {} because its id is invalid", entry.getKey());
                continue;
            }
            String itemId = json.has("item") ? json.get("item").getAsString()
                  : id.toString();
            ResourceLocation parsedItemId = ResourceLocation.tryParse(itemId);
            Item item = parsedItemId == null ? null : ForgeRegistries.ITEMS.getValue(parsedItemId);
            if (item == null || item == Items.AIR) {
                LOGGER.warn("Ignoring upgrade definition {} because item {} does not exist", id, itemId);
                continue;
            }
            UpgradeKind kind = UpgradeKind.fromName(json.has("type") ? json.get("type").getAsString() : "");
            if (kind == null) {
                LOGGER.warn("Ignoring upgrade definition {} because its type is invalid", id);
                continue;
            }
            double speedMultiplier = json.has("speed_multiplier")
                  ? json.get("speed_multiplier").getAsDouble() : 0;
            int maxCount = json.has("max_count") ? json.get("max_count").getAsInt() : 1;
            if (!Double.isFinite(speedMultiplier) || maxCount <= 0) {
                LOGGER.warn("Ignoring upgrade definition {} because its values are invalid", id);
                continue;
            }
            definitions.add(new UpgradeDefinition(id, item, kind, speedMultiplier, maxCount));
        }
        UpgradeRegistry.server().replace(definitions);
        LOGGER.info("Loaded {} upgrade definitions", definitions.size());
    }
}
