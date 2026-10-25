package com.triassicisland.power;

import com.triassicisland.network.DeviceMethod;
import com.triassicisland.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/** Electrical Age's variable DC-DC converter: ratio 2, 4, 8 or 16, set by sneak + right-click or a computer. */
public class VariableTransformerBlockEntity extends TransformerBlockEntity {
    private static final int[] RATIOS = {2, 4, 8, 16};

    private int ratioIndex = 1;

    public VariableTransformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VARIABLE_TRANSFORMER.get(), pos, state);
    }

    @Override
    protected double magnitude() {
        return RATIOS[ratioIndex];
    }

    int cycleRatio() {
        ratioIndex = (ratioIndex + 1) % RATIOS.length;
        setChanged();
        return RATIOS[ratioIndex];
    }

    @Override
    protected void addDeviceMethods(Map<String, DeviceMethod> methods) {
        super.addDeviceMethods(methods);
        methods.put("ratio", args -> RATIOS[ratioIndex]);
        methods.put("set_ratio", args -> {
            int wanted = (int) DeviceMethod.number(args, 0);
            for (int i = 0; i < RATIOS.length; i++) {
                if (RATIOS[i] == wanted) {
                    ratioIndex = i;
                    setChanged();
                    return null;
                }
            }
            throw new IllegalArgumentException("ratio must be 2, 4, 8 or 16");
        });
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Ratio", ratioIndex);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ratioIndex = tag.contains("Ratio") ? Math.floorMod(tag.getInt("Ratio"), RATIOS.length) : 1;
    }
}
