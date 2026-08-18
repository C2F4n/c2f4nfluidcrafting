package github.c2f4n.fluidcrafting.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/** 语言文件生成器，key 与翻译集中维护。 */
public class ModLangProvider extends LanguageProvider {

    private final String locale;

    public ModLangProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.c2f4nfluidcrafting", locale.equals("zh_cn") ? "PTFE流体合成|功能方块" : "PTFE Fluid Synthesis|Functional Blocks");
        add("block.c2f4nfluidcrafting.basicfluidmixer", locale.equals("zh_cn") ? "基础流体混合器" : "Basic Fluid Mixer");
        add("item.c2f4nfluidcrafting.speed_upgrade_1", locale.equals("zh_cn") ? "一级速度升级" : "Speed Upgrade I");
        add("item.c2f4nfluidcrafting.speed_upgrade_2", locale.equals("zh_cn") ? "二级速度升级" : "Speed Upgrade II");
        add("item.c2f4nfluidcrafting.speed_upgrade_3", locale.equals("zh_cn") ? "三级速度升级" : "Speed Upgrade III");
        add("item.c2f4nfluidcrafting.speed_upgrade_4", locale.equals("zh_cn") ? "四级速度升级" : "Speed Upgrade IV");
        add("item.c2f4nfluidcrafting.speed_upgrade_5", locale.equals("zh_cn") ? "五级速度升级" : "Speed Upgrade V");
        add("item.c2f4nfluidcrafting.creative_upgrade", locale.equals("zh_cn") ? "创造模式升级" : "Creative Upgrade");
        add("tooltip.c2f4nfluidcrafting.upgrade.max_count",
              locale.equals("zh_cn") ? "最大装载数量: %s" : "Max installed: %s");

        add("jei.c2f4nfluidcrafting.category.mixing", locale.equals("zh_cn") ? "流体混合" : "Fluid Mixing");
        add("gui.c2f4nfluidcrafting.recipe_duration",
              locale.equals("zh_cn") ? "配方耗时：%s 秒" : "Duration: %s s");
        add("gui.c2f4nfluidcrafting.jei.transfer_missing",
              locale.equals("zh_cn") ? "缺少配方输入：请准备对应的流体容器或物品" : "Missing recipe inputs: provide matching fluid containers or items");
        add("gui.c2f4nfluidcrafting.jei.transfer_slot_occupied",
              locale.equals("zh_cn") ? "目标槽位已被占用" : "Target slot is occupied");

        add("gui.c2f4nfluidcrafting.io_config", locale.equals("zh_cn") ? "输入输出配置" : "Input/Output Config");
        add("gui.c2f4nfluidcrafting.fluid_config", locale.equals("zh_cn") ? "流体配置" : "Fluid Config");
        add("gui.c2f4nfluidcrafting.item_config", locale.equals("zh_cn") ? "物品配置" : "Item Config");
        add("gui.c2f4nfluidcrafting.redstone", locale.equals("zh_cn") ? "红石控制" : "Redstone Control");
        add("gui.c2f4nfluidcrafting.redstone.current",
              locale.equals("zh_cn") ? "当前：%s" : "Current: %s");
        add("gui.c2f4nfluidcrafting.mining", locale.equals("zh_cn") ? "挖掘配置" : "Mining Config");
        add("gui.c2f4nfluidcrafting.mining.current",
              locale.equals("zh_cn") ? "当前：%s" : "Current: %s");
        add("gui.c2f4nfluidcrafting.upgrade", locale.equals("zh_cn") ? "升级插槽" : "Upgrade Slots");
        add("gui.c2f4nfluidcrafting.close", locale.equals("zh_cn") ? "关闭" : "Close");
        add("gui.c2f4nfluidcrafting.upgrade.supported", locale.equals("zh_cn") ? "支持的升级" : "Supported Upgrades");
        add("gui.c2f4nfluidcrafting.upgrade.no_selection", locale.equals("zh_cn") ? "选择左侧的已装载升级" : "Select an installed upgrade on the left");
        add("gui.c2f4nfluidcrafting.upgrade.count", locale.equals("zh_cn") ? "数量: %s / %s" : "Count: %s / %s");
        add("gui.c2f4nfluidcrafting.upgrade.uninstall", locale.equals("zh_cn") ? "卸载" : "Uninstall");
        add("gui.c2f4nfluidcrafting.upgrade.uninstall.tooltip",
              locale.equals("zh_cn") ? "卸下一个升级，按住 Shift 卸载全部" : "Remove one upgrade, hold Shift to remove all");
        add("gui.c2f4nfluidcrafting.recipe_error",
              locale.equals("zh_cn") ? "错误：%s" : "Error: %s");
        add("gui.c2f4nfluidcrafting.recipe_error.working_disabled",
              locale.equals("zh_cn") ? "机器未启动" : "Machine is disabled");
        add("gui.c2f4nfluidcrafting.recipe_error.no_matching_recipe",
              locale.equals("zh_cn") ? "未找到匹配配方" : "No matching recipe");
        add("gui.c2f4nfluidcrafting.recipe_error.not_enough_input",
              locale.equals("zh_cn") ? "输入不足" : "Not enough input");
        add("gui.c2f4nfluidcrafting.recipe_error.output_not_space",
              locale.equals("zh_cn") ? "输出空间不足" : "Not enough output space");
        add("upgrade.c2f4nfluidcrafting.speed.description",
              locale.equals("zh_cn") ? "每张升级卡使机器处理速度提升 %s%%。" : "Each upgrade card increases machine speed by %s%%.");
        add("upgrade.c2f4nfluidcrafting.creative.description",
              locale.equals("zh_cn") ? "装载后配方不再消耗原料。" : "Recipes no longer consume inputs while installed.");
        add("gui.c2f4nfluidcrafting.auto_eject", locale.equals("zh_cn") ? "自动弹出" : "Auto-Eject");
        add("gui.c2f4nfluidcrafting.auto_eject.on", locale.equals("zh_cn") ? "自动弹出：开" : "Auto-Eject: On");
        add("gui.c2f4nfluidcrafting.auto_eject.off", locale.equals("zh_cn") ? "自动弹出：关" : "Auto-Eject: Off");

        add("gui.c2f4nfluidcrafting.mode.none", locale.equals("zh_cn") ? "未配置" : "None");
        add("gui.c2f4nfluidcrafting.mode.disabled",
              locale.equals("zh_cn") ? "已关闭红石控制" : "Redstone Control Disabled");
        add("gui.c2f4nfluidcrafting.mode.fluid_in_1", locale.equals("zh_cn") ? "流体输入 1" : "Fluid Input 1");
        add("gui.c2f4nfluidcrafting.mode.fluid_in_2", locale.equals("zh_cn") ? "流体输入 2" : "Fluid Input 2");
        add("gui.c2f4nfluidcrafting.mode.fluid_in_3", locale.equals("zh_cn") ? "流体输入 3" : "Fluid Input 3");
        add("gui.c2f4nfluidcrafting.mode.fluid_out", locale.equals("zh_cn") ? "流体输出" : "Fluid Output");
        add("gui.c2f4nfluidcrafting.mode.item_in", locale.equals("zh_cn") ? "物品输入" : "Item Input");
        add("gui.c2f4nfluidcrafting.mode.item_out", locale.equals("zh_cn") ? "物品输出" : "Item Output");
        add("gui.c2f4nfluidcrafting.mode.empty_out", locale.equals("zh_cn") ? "空桶/空储罐输出" : "Empty Container Output");
        add("gui.c2f4nfluidcrafting.mode.on_signal", locale.equals("zh_cn") ? "有红石信号时工作" : "Active with Redstone Signal");

        add("gui.c2f4nfluidcrafting.mining.keep_nbt", locale.equals("zh_cn") ? "保留 NBT" : "Keep NBT");
        add("gui.c2f4nfluidcrafting.mining.drop_items", locale.equals("zh_cn") ? "掉落物品与升级插件，液体清空" : "Drop Items & Upgrades, Clear Liquids");

        add("tooltip.c2f4nfluidcrafting.keep_nbt", locale.equals("zh_cn") ? "保留NBT" : "Keep NBT");
        add("tooltip.c2f4nfluidcrafting.on", locale.equals("zh_cn") ? "开" : "On");
        add("tooltip.c2f4nfluidcrafting.off", locale.equals("zh_cn") ? "关" : "Off");
        add("tooltip.c2f4nfluidcrafting.fluids", locale.equals("zh_cn") ? "流体" : "Fluids");
        add("tooltip.c2f4nfluidcrafting.items", locale.equals("zh_cn") ? "物品" : "Items");
        add("tooltip.c2f4nfluidcrafting.empty", locale.equals("zh_cn") ? "无" : "None");
        add("tooltip.c2f4nfluidcrafting.fluid_amount", "%s / %s mB");
        add("tooltip.c2f4nfluidcrafting.redstone_mode", locale.equals("zh_cn") ? "红石模式" : "Redstone Mode");
        add("tooltip.c2f4nfluidcrafting.redstone_mode_off", locale.equals("zh_cn") ? "关" : "Off");
        add("tooltip.c2f4nfluidcrafting.redstone_mode_on_signal", locale.equals("zh_cn") ? "有则工作" : "Work With Signal");
    }
}
