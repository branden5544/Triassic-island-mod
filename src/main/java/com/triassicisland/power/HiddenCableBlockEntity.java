package com.triassicisland.power;

import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

/** A cable (power, data or both) plus the block it's disguised as. The disguise is synced to clients for rendering. */
public class HiddenCableBlockEntity extends CableBlockEntity {
    public static final ModelProperty<BlockState> DISGUISE = new ModelProperty<>();

    @Nullable
    private BlockState disguise;
    /** A lever attached to this cable that is switched off cuts the power through it. */
    private boolean switchedOff;

    public HiddenCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HIDDEN_CABLE.get(), pos, state);
    }

    @Nullable
    public BlockState disguise() {
        return disguise;
    }

    public void setDisguise(@Nullable BlockState disguise) {
        this.disguise = disguise;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    /** Re-checks levers attached to this block. Called when a neighbour changes and on load. */
    public void updateSwitch() {
        if (level == null) return;
        boolean anyLever = false, anyOn = false;
        for (Direction dir : Direction.values()) {
            BlockPos leverPos = worldPosition.relative(dir);
            BlockState lever = level.getBlockState(leverPos);
            if (!(lever.getBlock() instanceof LeverBlock)) continue;
            Direction attached = switch (lever.getValue(LeverBlock.FACE)) {
                case FLOOR -> Direction.DOWN;
                case CEILING -> Direction.UP;
                case WALL -> lever.getValue(LeverBlock.FACING).getOpposite();
            };
            if (!leverPos.relative(attached).equals(worldPosition)) continue;
            anyLever = true;
            anyOn |= lever.getValue(LeverBlock.POWERED);
        }
        switchedOff = anyLever && !anyOn;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateSwitch();
    }

    @Override
    public double linkResistance() {
        return switchedOff ? 1e9 : super.linkResistance();
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(DISGUISE, disguise).build();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (disguise != null) tag.put("Disguise", NbtUtils.writeBlockState(disguise));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        disguise = tag.contains("Disguise") ? NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("Disguise")) : null;
        if (disguise != null && disguise.isAir()) disguise = null;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        if (disguise != null) tag.put("Disguise", NbtUtils.writeBlockState(disguise));
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        handleUpdateTag(packet.getTag() == null ? new CompoundTag() : packet.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        disguise = tag.contains("Disguise") ? NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("Disguise")) : null;
        requestModelDataUpdate();
        if (level != null && level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
}
