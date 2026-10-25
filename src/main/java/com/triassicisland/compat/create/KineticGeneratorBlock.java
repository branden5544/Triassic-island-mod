package com.triassicisland.compat.create;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.triassicisland.power.ElectricalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Takes a Create shaft on its front and gives grid power from every other side. */
public class KineticGeneratorBlock extends DirectionalKineticBlock implements IBE<KineticGeneratorBlockEntity>, ElectricalBlock {
    public final double voltage;

    public KineticGeneratorBlock(double voltage, Properties properties) {
        super(properties);
        this.voltage = voltage;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public boolean connectsOn(BlockState state, Direction face) {
        return face != state.getValue(FACING);
    }

    /** Drops itself. Done in code rather than a loot table, because the item only exists when Create is installed. */
    @Override
    @SuppressWarnings("deprecation")
    public java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        return java.util.List.of(new net.minecraft.world.item.ItemStack(this));
    }

    @Override
    public Class<KineticGeneratorBlockEntity> getBlockEntityClass() {
        return KineticGeneratorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticGeneratorBlockEntity> getBlockEntityType() {
        return CreateCompat.GENERATOR.get();
    }
}
