package com.triassicisland.power;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Right-click any grid block to read its voltage, current and power. */
public class MultimeterItem extends Item {
    public MultimeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var be = context.getLevel().getBlockEntity(context.getClickedPos());
        ElectricalBlockEntity node = be instanceof ElectricalBlockEntity e ? e : be instanceof HasElectricalNode h ? h.electricalNode() : null;
        if (node == null) return InteractionResult.PASS;
        if (!context.getLevel().isClientSide && context.getPlayer() != null) {
            context.getPlayer().displayClientMessage(Component.empty().append(node.meterReading()), true);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
