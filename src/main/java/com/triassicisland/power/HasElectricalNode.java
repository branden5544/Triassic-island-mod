package com.triassicisland.power;

/**
 * For block entities that can't extend {@link ElectricalBlockEntity} (e.g. Create kinetic blocks) but still join the
 * grid through a helper node. The multimeter reads that node.
 */
public interface HasElectricalNode {
    ElectricalBlockEntity electricalNode();
}
