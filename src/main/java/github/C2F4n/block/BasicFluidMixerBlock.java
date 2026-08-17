package github.C2F4n.block;

import github.C2F4n.block.entity.BasicFluidMixerBlockEntity;
import github.C2F4n.common.upgrade.UpgradeDefinition;
import github.C2F4n.common.upgrade.UpgradeRegistry;
import github.C2F4n.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BasicFluidMixerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BasicFluidMixerBlock() {
        // 硬度 2.0，爆炸抗性 3600000（基岩级，任何爆炸都无法破坏）
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 3600000.0F).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BasicFluidMixerBlockEntity blockEntity) {
            blockEntity.syncToClients();
            NetworkHooks.openScreen((ServerPlayer) player, blockEntity, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == ModBlockEntities.BASIC_FLUID_MIXER.get()
                ? (lvl, pos, st, be) -> ((BasicFluidMixerBlockEntity) be).tickServer()
                : null;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof BasicFluidMixerBlockEntity mixer && mixer.hasCustomData()) {
            stack.addTagElement("BlockEntityTag", saveForItem(mixer));
        }
        return stack;
    }

    /**
     * 完全走原版掉落管线：创造模式由原版跳过掉落，生存/爆炸由这里决定
     * 是按“保留 NBT”还是按“掉物品清液体”掉落，避免与原版战利品表重复掉落。
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof BasicFluidMixerBlockEntity mixer) {
            ItemStack blockStack = new ItemStack(this);
            ItemStack blockWithData = ItemStack.EMPTY;
            if (mixer.hasCustomData()) {
                blockWithData = blockStack.copy();
                blockWithData.addTagElement("BlockEntityTag", saveForItem(mixer));
            }
            List<ItemStack> machineItems = new ArrayList<>();
            for (github.C2F4n.common.inventory.slot.BasicInventorySlot slot : mixer.getItemSlots()) {
                if (!slot.isEmpty()) {
                    machineItems.add(slot.getStack());
                }
            }
            List<ItemStack> upgradeItems = new ArrayList<>();
            for (github.C2F4n.common.inventory.slot.BasicInventorySlot slot :
                  List.of(mixer.getUpgradeInputSlot(), mixer.getUpgradeOutputSlot())) {
                if (!slot.isEmpty()) {
                    upgradeItems.add(slot.getStack());
                }
            }
            for (UpgradeDefinition definition : UpgradeRegistry.server().getDefinitions()) {
                int amount = mixer.getUpgradeComponent().getUpgrades(definition.id());
                if (amount > 0) {
                    upgradeItems.add(definition.createStack(amount));
                }
            }
            return mixer.getMiningComponent().createDrops(blockStack, blockWithData,
                  machineItems, upgradeItems);
        }
        return super.getDrops(state, builder);
    }

    /** 存进物品 NBT 时去掉坐标，否则放置后方块实体会被旧坐标覆盖，导致 UI 无法打开、物品无法堆叠 */
    private static CompoundTag saveForItem(BasicFluidMixerBlockEntity mixer) {
        CompoundTag tag = mixer.saveWithFullMetadata();
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
        return tag;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BasicFluidMixerBlockEntity(pos, state);
    }
}
