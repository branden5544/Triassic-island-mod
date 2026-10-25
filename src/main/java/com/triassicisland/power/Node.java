package com.triassicisland.power;

/**
 * One point in the circuit. Most blocks are a single node; a transformer has one per side.
 * The owning block entity supplies the node's electrical values and reacts to the solution.
 */
public final class Node {
    final ElectricalBlockEntity owner;
    double voltage;
    /** Current from this node to ground: positive while consuming, negative while supplying. */
    double current;

    Node(ElectricalBlockEntity owner) {
        this.owner = owner;
    }

    public double voltage() {
        return voltage;
    }

    public double current() {
        return current;
    }

    public double power() {
        return voltage * current;
    }
}
