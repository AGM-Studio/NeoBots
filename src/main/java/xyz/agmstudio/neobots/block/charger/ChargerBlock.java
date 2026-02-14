package xyz.agmstudio.neobots.block.charger;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.agmstudio.neobots.block.battery.BatteryItem;
import xyz.agmstudio.neobots.index.CNBBlockEntities;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class ChargerBlock extends HorizontalDirectionalBlock implements IBE<ChargerBlockEntity>, IWrenchable {
    public static final MapCodec<ChargerBlock> CODEC = simpleCodec(ChargerBlock::new);

    public ChargerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected @NotNull MapCodec<ChargerBlock> codec() {
        return CODEC;
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        Direction nearest = context.getHorizontalDirection();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
            return defaultBlockState().setValue(FACING, nearest);

        return defaultBlockState().setValue(FACING, nearest.getOpposite());
    }

    @Override protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ChargerBlockEntity charger))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ItemStack slotStack = charger.inventory.getStackInSlot(0);
        ItemStack playerStack = stack.copy();

        if (playerStack.getItem() instanceof BatteryItem) {
            charger.inventory.setStackInSlot(0, playerStack.split(1));
            if (slotStack.isEmpty()) player.setItemInHand(hand, playerStack);
            else player.setItemInHand(hand, slotStack);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1f, 1f);

            charger.notifyUpdate();
            return ItemInteractionResult.CONSUME;
        }

        if (playerStack.isEmpty() && !slotStack.isEmpty()) {
            player.setItemInHand(hand, slotStack);
            charger.inventory.setStackInSlot(0, ItemStack.EMPTY);

            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1f, 1f);

            charger.notifyUpdate();
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Class<ChargerBlockEntity> getBlockEntityClass() {
        return ChargerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ChargerBlockEntity> getBlockEntityType() {
        return CNBBlockEntities.CHARGER.get();
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChargerBlockEntity(CNBBlockEntities.CHARGER.get(), pos, state);
    }

    @Override public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ChargerBlockEntity charger)) return 0;

        ItemStack battery = charger.inventory.getStackInSlot(0);
        IEnergyStorage energy = battery.getCapability(Capabilities.EnergyStorage.ITEM);
        if (battery.isEmpty() || energy == null) return 0;
        double fraction = (double) energy.getEnergyStored() / Math.max(1, energy.getMaxEnergyStored());
        return Math.max(1, (int) Math.ceil(fraction * 15));
    }
}