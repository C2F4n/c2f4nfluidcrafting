package github.c2f4n.fluidcrafting.block.entity;

import github.c2f4n.fluidcrafting.MachineConfig;
import github.c2f4n.fluidcrafting.common.inventory.container.BasicFluidMixerContainer;
import github.c2f4n.fluidcrafting.common.inventory.container.ModContainer;
import github.c2f4n.fluidcrafting.common.inventory.container.sync.SyncableInt;
import github.c2f4n.fluidcrafting.common.inventory.slot.BasicInventorySlot;
import github.c2f4n.fluidcrafting.common.capability.CombinedFluidHandler;
import github.c2f4n.fluidcrafting.common.capability.SideFluidHandler;
import github.c2f4n.fluidcrafting.common.capability.SideItemHandler;
import github.c2f4n.fluidcrafting.common.network.PacketHandler;
import github.c2f4n.fluidcrafting.common.network.to_client.PacketSyncMachine;
import github.c2f4n.fluidcrafting.common.recipe.lookup.RecipeCache;
import github.c2f4n.fluidcrafting.common.recipe.lookup.RecipeError;
import github.c2f4n.fluidcrafting.common.recipe.lookup.MixingRecipeLookup;
import github.c2f4n.fluidcrafting.common.tile.base.TileEntityBase;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentConfig;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentEjector;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentFluid;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentFluidTransfer;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentInventory;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentMining;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentRedstone;
import github.c2f4n.fluidcrafting.common.tile.component.TileComponentUpgrade;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeDefinition;
import github.c2f4n.fluidcrafting.common.upgrade.UpgradeRegistry;
import github.c2f4n.fluidcrafting.common.tile.component.config.DataType;
import github.c2f4n.fluidcrafting.common.tile.component.config.TransmissionType;
import github.c2f4n.fluidcrafting.common.recipe.MixingRecipe;
import github.c2f4n.fluidcrafting.common.recipe.MixingRecipeInput;
import github.c2f4n.fluidcrafting.common.recipe.ingredient.FluidIngredient;
import github.c2f4n.fluidcrafting.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/** 基础流体混合器方块实体：组件化（面配置/升级/红石），合成与推送集中在这里。 */
public class BasicFluidMixerBlockEntity extends TileEntityBase implements MenuProvider {

    public static final int UPGRADE_OFFSET_START = 6;

    private final TileComponentConfig config;
    private final TileComponentUpgrade upgrade;
    private final TileComponentRedstone redstone;
    private final TileComponentFluid fluid;
    private final TileComponentInventory inventory;
    private final TileComponentMining mining;
    private final TileComponentEjector ejector;
    private final TileComponentFluidTransfer fluidTransfer;

    private MixingRecipe currentRecipe;
    private double progress;
    private int clientDuration;
    private int contentVersion;
    private boolean crafting;
    @Nullable
    private RecipeError clientRecipeError;
    private final RecipeCache recipeCache = new RecipeCache(() -> contentVersion, this::findRecipe);

    private LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> createItemHandler(null));
    private LazyOptional<IFluidHandler> fluidOptional = LazyOptional.of(this::createCombinedFluidHandler);

    public BasicFluidMixerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_FLUID_MIXER.get(), pos, state);
        config = new TileComponentConfig(this, TransmissionType.FLUID, TransmissionType.ITEM);
        upgrade = new TileComponentUpgrade(this);
        redstone = new TileComponentRedstone(this);
        fluid = new TileComponentFluid(this, this::syncToClients);
        inventory = new TileComponentInventory(this);
        mining = new TileComponentMining(this);
        ejector = new TileComponentEjector(this, config, fluid, inventory);
        fluidTransfer = new TileComponentFluidTransfer(this, fluid, inventory);
    }

    @Override
    public void onContentsChanged() {
        contentVersion++;
        setChanged();
    }

    // ---------- 组件 ----------

    public TileComponentConfig getConfigComponent() {
        return config;
    }

    public TileComponentUpgrade getUpgradeComponent() {
        return upgrade;
    }

    public TileComponentRedstone getRedstoneComponent() {
        return redstone;
    }

    public TileComponentMining getMiningComponent() {
        return mining;
    }

    // ---------- 罐 ----------

    public FluidTank getInputTank(int index) {
        return fluid.getInputTank(index);
    }

    public FluidTank getOutputTank() {
        return fluid.getOutputTank();
    }

    public FluidTank getTank(int index) {
        return fluid.getTank(index);
    }

    public void applyClientTankData(FluidStack in1, FluidStack in2, FluidStack in3, FluidStack out) {
        fluid.applyClientData(in1, in2, in3, out);
    }

    // ---------- 槽 ----------

    public BasicInventorySlot getSolidInputSlot() {
        return inventory.getSolidInputSlot();
    }

    public BasicInventorySlot getSolidOutputSlot() {
        return inventory.getSolidOutputSlot();
    }

    public BasicInventorySlot getContainerInputSlot(int index) {
        return inventory.getContainerInputSlot(index);
    }

    public BasicInventorySlot getContainerOutputSlot() {
        return inventory.getContainerOutputSlot();
    }

    public BasicInventorySlot getUpgradeInputSlot() {
        return upgrade.getInputSlot();
    }

    public BasicInventorySlot getUpgradeOutputSlot() {
        return upgrade.getOutputSlot();
    }

    public List<BasicInventorySlot> getItemSlots() {
        return inventory.getMachineSlots();
    }

    // ---------- 模式 ----------

    public MiningMode getMiningMode() {
        return mining.getMode();
    }

    public void setMiningMode(MiningMode miningMode) {
        mining.setMode(miningMode);
        syncToClients();
    }

    public void setRedstoneMode(TileComponentRedstone.RedstoneMode mode) {
        redstone.setMode(mode);
        syncToClients();
    }

    public boolean isWorking() {
        if (level == null) {
            return false;
        }
        return redstone.shouldWork(level.hasNeighborSignal(worldPosition));
    }

    public int getProgress() {
        return (int) Math.floor(progress);
    }

    public int getCurrentDuration() {
        return level != null && level.isClientSide ? clientDuration
              : currentRecipe == null ? 0 : currentRecipe.getDuration();
    }

    public boolean isCrafting() {
        return crafting;
    }

    private void setCrafting(boolean value) {
        if (crafting != value) {
            crafting = value;
            syncToClients();
        }
    }

    public void applyClientWorking(boolean value) {
        crafting = value;
    }

    public double getSpeedMultiplier() {
        return upgrade.getSpeedMultiplier();
    }

    @Nullable
    public RecipeError getRecipeError() {
        return level != null && level.isClientSide ? clientRecipeError : recipeCache.getError();
    }

    public void applyClientRecipeError(int ordinal) {
        if (ordinal < 0 || ordinal >= RecipeError.values().length) {
            clientRecipeError = null;
        } else {
            clientRecipeError = RecipeError.values()[ordinal];
        }
    }

    public void applyClientSettings(boolean fluidAutoEject, boolean itemAutoEject, int redstoneMode,
                                    int miningMode, int[] fluidModes, int[] itemModes) {
        config.setEjecting(TransmissionType.FLUID, fluidAutoEject);
        config.setEjecting(TransmissionType.ITEM, itemAutoEject);
        redstone.setMode(TileComponentRedstone.RedstoneMode.byIndex(redstoneMode));
        mining.setMode(MiningMode.byIndex(miningMode));
        for (Direction direction : Direction.values()) {
            config.setDataType(TransmissionType.FLUID, direction, DataType.values()[fluidModes[direction.ordinal()]]);
            config.setDataType(TransmissionType.ITEM, direction, DataType.values()[itemModes[direction.ordinal()]]);
        }
    }

    // ---------- 同步 ----------

    public void syncToClients() {
        if (level == null || level.isClientSide) {
            return;
        }
        List<FluidStack> fluids = fluid.getFluids();
        int[] fluidModes = new int[6];
        int[] itemModes = new int[6];
        for (Direction direction : Direction.values()) {
            fluidModes[direction.ordinal()] = config.getDataType(TransmissionType.FLUID, direction).ordinal();
            itemModes[direction.ordinal()] = config.getDataType(TransmissionType.ITEM, direction).ordinal();
        }
        PacketHandler.sendToTrackingChunk(new PacketSyncMachine(worldPosition,
              fluids.get(0), fluids.get(1), fluids.get(2), fluids.get(3),
              config.isEjecting(TransmissionType.FLUID), config.isEjecting(TransmissionType.ITEM),
              redstone.getMode().ordinal(), mining.getMode().ordinal(),
              fluidModes, itemModes, crafting),
              (ServerLevel) level, worldPosition);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        syncToClients();
    }

    @Override
    public void addContainerTrackers(ModContainer container) {
        super.addContainerTrackers(container);
        container.track(new SyncableInt(() -> (int) Math.floor(progress), value -> progress = value));
        container.track(new SyncableInt(() -> getCurrentDuration(), value -> clientDuration = value));
        container.track(new SyncableInt(() -> {
            RecipeError error = recipeCache.getError();
            return error == null ? -1 : error.ordinal();
        }, this::applyClientRecipeError));
    }

    // ---------- 容器交互 ----------

    public void interactWithTank(Player player, int tank, boolean extract, boolean shift) {
        fluidTransfer.interactWithTank(player, tank, extract, shift);
    }

    // ---------- 合成 ----------

    public void tickServer() {
        super.tickServer();
        if (level == null || level.isClientSide) {
            return;
        }
        processCrafting();
    }

    private void processCrafting() {
        if (currentRecipe == null) {
            recipeCache.refresh();
            if (!isWorking()) {
                setCrafting(false);
                return;
            }
            MixingRecipe recipe = recipeCache.getRecipe();
            if (recipe != null && canAcceptOutput(recipe)) {
                if (!getUpgradeComponent().hasCreativeUpgrade()) {
                    consume(recipe);
                }
                currentRecipe = recipe;
                progress = 0;
                setCrafting(true);
            } else {
                if (recipe != null) {
                    recipeCache.setError(RecipeError.OUTPUT_NOT_SPACE);
                }
                setCrafting(false);
            }
            return;
        }
        // 已经开始合成的配方不受红石信号变化影响，直到本条完成。
        if (progress < currentRecipe.getDuration()) {
            progress += getSpeedMultiplier();
        }
        if (progress >= currentRecipe.getDuration()) {
            if (canAcceptOutput(currentRecipe)) {
                produce(currentRecipe);
                currentRecipe = null;
                progress = 0;
                setCrafting(false);
                recipeCache.invalidate();
            } else {
                recipeCache.setError(RecipeError.OUTPUT_NOT_SPACE);
            }
        }
    }

    @Nullable
    private MixingRecipe findRecipe() {
        if (level == null) {
            return null;
        }
        return new MixingRecipeLookup(level).find(createRecipeInput());
    }

    private MixingRecipeInput createRecipeInput() {
        List<FluidStack> fluids = List.of(getInputTank(0).getFluid(),
              getInputTank(1).getFluid(), getInputTank(2).getFluid());
        return new MixingRecipeInput(getSolidInputSlot().getStack(), fluids);
    }

    /** 输入是否足量已由 Recipe.matches 保证，这里只检查两种产物空间。 */
    private boolean canAcceptOutput(MixingRecipe recipe) {
        if (!recipe.getResultItem().isEmpty()) {
            ItemStack out = getSolidOutputSlot().getStack();
            if (!out.isEmpty() && (!ItemStack.isSameItemSameTags(out, recipe.getResultItem())
                  || out.getCount() + recipe.getResultItem().getCount() > out.getMaxStackSize())) {
                return false;
            }
        }
        if (!recipe.getResultFluid().isEmpty()) {
            FluidStack current = getOutputTank().getFluid();
            if (!current.isEmpty() && (!current.isFluidEqual(recipe.getResultFluid())
                  || current.getAmount() + recipe.getResultFluid().getAmount() > MachineConfig.tankCapacity())) {
                return false;
            }
        }
        return true;
    }

    private void consume(MixingRecipe recipe) {
        for (FluidIngredient ingredient : recipe.getFluidInputs()) {
            int remaining = ingredient.getAmount();
            for (int i = 0; i < 3 && remaining > 0; i++) {
                FluidStack stack = getInputTank(i).getFluid();
                if (ingredient.matches(stack)) {
                    int amount = Math.min(remaining, stack.getAmount());
                    getInputTank(i).drain(amount, IFluidHandler.FluidAction.EXECUTE);
                    remaining -= amount;
                }
            }
        }
        if (!recipe.getIngredients().isEmpty()) {
            getSolidInputSlot().shrinkStack(1);
        }
    }

    private void produce(MixingRecipe recipe) {
        if (!recipe.getResultFluid().isEmpty()) {
            getOutputTank().fill(recipe.getResultFluid(), IFluidHandler.FluidAction.EXECUTE);
        }
        if (!recipe.getResultItem().isEmpty()) {
            ItemStack out = getSolidOutputSlot().getStack();
            if (out.isEmpty()) {
                getSolidOutputSlot().setStack(recipe.getResultItem().copy());
            } else {
                out.grow(recipe.getResultItem().getCount());
                getSolidOutputSlot().setStack(out);
            }
        }
    }

    // ---------- 能力 ----------

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return itemOptional.cast();
            }
            return LazyOptional.of(() -> new SideItemHandler(config, inventory, side)).cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (side == null) {
                return fluidOptional.cast();
            }
            return LazyOptional.of(() -> new SideFluidHandler(config, fluid, side)).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
        fluidOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemOptional = LazyOptional.of(() -> createItemHandler(null));
        fluidOptional = LazyOptional.of(this::createCombinedFluidHandler);
    }

    private IItemHandler createItemHandler(@Nullable Direction side) {
        return new SideItemHandler(config, inventory, side);
    }

    private IFluidHandler createCombinedFluidHandler() {
        return new CombinedFluidHandler(fluid);
    }

    // ---------- NBT ----------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getDouble("progress");
        migrateLegacyUpgrades(tag);
        migrateLegacyConfig(tag);
    }

    /** 把旧版 3×3 升级格里的升级卡折算成新的“已安装数量”。 */
    private void migrateLegacyUpgrades(CompoundTag tag) {
        if (tag.contains(TileComponentUpgrade.NBT_KEY) || !tag.contains("inventory")) {
            return;
        }
        CompoundTag inventory = tag.getCompound("inventory");
        ListTag items = inventory.getList("Items", 10);
        Map<github.c2f4n.fluidcrafting.common.upgrade.UpgradeDefinition, Integer> legacyByDefinition = new java.util.HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            CompoundTag entry = items.getCompound(i);
            if (entry.getInt("Slot") >= UPGRADE_OFFSET_START) {
                ItemStack stack = ItemStack.of(entry);
                UpgradeDefinition definition = UpgradeRegistry.server().getForStack(stack);
                if (definition != null) {
                    legacyByDefinition.merge(definition, stack.getCount(), Integer::sum);
                }
            }
        }
        for (Map.Entry<UpgradeDefinition, Integer> entry : legacyByDefinition.entrySet()) {
            upgrade.addUpgrades(entry.getKey().id(), entry.getValue());
        }
    }

    /** 旧版 NBT（fluid_modes/item_modes/auto eject 布尔）迁移到组件。 */
    private void migrateLegacyConfig(CompoundTag tag) {
        if (tag.contains("fluid_modes")) {
            CompoundTag modes = tag.getCompound("fluid_modes");
            for (Direction direction : Direction.values()) {
                config.setDataType(TransmissionType.FLUID, direction,
                      DataType.values()[modes.getInt(direction.getName())]);
            }
        }
        if (tag.contains("item_modes")) {
            CompoundTag modes = tag.getCompound("item_modes");
            for (Direction direction : Direction.values()) {
                config.setDataType(TransmissionType.ITEM, direction,
                      DataType.values()[modes.getInt(direction.getName())]);
            }
        }
        if (tag.contains("fluid_auto_eject")) {
            config.setEjecting(TransmissionType.FLUID, tag.getInt("fluid_auto_eject") == 1);
        }
        if (tag.contains("item_auto_eject")) {
            config.setEjecting(TransmissionType.ITEM, tag.getInt("item_auto_eject") == 1);
        }
    }

    public boolean hasCustomData() {
        if (progress != 0 || mining.hasCustomData()
              || config.isEjecting(TransmissionType.FLUID) || config.isEjecting(TransmissionType.ITEM)
              || redstone.getMode() != TileComponentRedstone.RedstoneMode.DISABLED
              || upgrade.hasUpgrades()) {
            return true;
        }
        if (fluid.hasContent()) {
            return true;
        }
        if (inventory.hasContent()
              || !upgrade.getInputSlot().isEmpty() || !upgrade.getOutputSlot().isEmpty()) {
            return true;
        }
        for (TransmissionType type : config.getTransmissions()) {
            if (!config.getConfig(type).isSideEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.c2f4nfluidcrafting.basicfluidmixer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new BasicFluidMixerContainer(id, inventory, this);
    }
}
