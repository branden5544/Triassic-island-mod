package com.triassicisland.client;

import com.triassicisland.power.HiddenCableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Draws a hidden cable as its disguise block, or as the bare cable when unset or seen through Engineer's Goggles. */
public class HiddenCableModel extends BakedModelWrapper<BakedModel> {
    public HiddenCableModel(BakedModel cable) {
        super(cable);
    }

    @Nullable
    private static BlockState disguise(ModelData data) {
        return GogglesWatcher.revealed() ? null : data.get(HiddenCableBlockEntity.DISGUISE);
    }

    private static BakedModel model(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand,
                                             @NotNull ModelData data, @Nullable RenderType renderType) {
        BlockState disguise = disguise(data);
        if (disguise != null) return model(disguise).getQuads(disguise, side, rand, ModelData.EMPTY, renderType);
        return super.getQuads(state, side, rand, data, renderType);
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        BlockState disguise = disguise(data);
        if (disguise != null) return model(disguise).getRenderTypes(disguise, rand, ModelData.EMPTY);
        return ChunkRenderTypeSet.of(RenderType.solid());
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        BlockState disguise = disguise(data);
        return disguise != null ? model(disguise).getParticleIcon(ModelData.EMPTY) : super.getParticleIcon(data);
    }
}
