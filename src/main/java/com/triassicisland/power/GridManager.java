package com.triassicisland.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Tracks every loaded electrical block in a dimension, groups their nodes into connected networks, and solves them each tick. */
public final class GridManager {
    private static final Map<ServerLevel, GridManager> MANAGERS = new WeakHashMap<>();

    private final Map<BlockPos, ElectricalBlockEntity> blocks = new HashMap<>();
    private final List<Network> networks = new ArrayList<>();
    private boolean dirty;

    public static GridManager get(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, l -> new GridManager());
    }

    public static void clearAll() {
        MANAGERS.clear();
    }

    void add(ElectricalBlockEntity block) {
        blocks.put(block.getBlockPos().immutable(), block);
        dirty = true;
    }

    void remove(ElectricalBlockEntity block) {
        if (blocks.remove(block.getBlockPos(), block)) dirty = true;
    }

    /** Adds a helper node that isn't a real block entity in the world (see {@link HasElectricalNode}). */
    public static void attach(ServerLevel level, ElectricalBlockEntity node) {
        get(level).add(node);
    }

    public static void detach(ServerLevel level, ElectricalBlockEntity node) {
        get(level).remove(node);
    }

    /** Call when connections change without blocks being added or removed (e.g. a wire is strung or burns). */
    public void markDirty() {
        dirty = true;
    }

    public void tick() {
        if (dirty) rebuild();
        for (Network network : networks) network.solve();
    }

    /** Nodes linked to {@code node}: for each face it is exposed on, the neighbour's node on the touching face. */
    private void forEachNeighbor(Node node, java.util.function.Consumer<Node> action) {
        BlockPos pos = node.owner.getBlockPos();
        for (Direction dir : Direction.values()) {
            if (node.owner.nodeAt(dir) != node) continue;
            ElectricalBlockEntity other = blocks.get(pos.relative(dir));
            if (other == null) continue;
            Node neighbor = other.nodeAt(dir.getOpposite());
            if (neighbor != null) action.accept(neighbor);
        }
        for (ElectricalBlockEntity.RemoteLink link : node.owner.remoteLinks(node)) {
            ElectricalBlockEntity other = blocks.get(link.to());
            Node far = other == null ? null : other.remoteNode();
            if (far != null) action.accept(far);
        }
    }

    /** Extra resistance of the wire from {@code a} to {@code b}, or 0 if they simply touch. */
    private static double wireResistance(Node a, Node b) {
        for (ElectricalBlockEntity.RemoteLink link : a.owner.remoteLinks(a)) {
            if (link.to().equals(b.owner.getBlockPos())) return link.resistance();
        }
        return 0;
    }

    private void rebuild() {
        dirty = false;
        networks.clear();
        Map<Node, Boolean> visited = new IdentityHashMap<>();
        for (ElectricalBlockEntity block : blocks.values()) {
            for (Node start : block.nodes()) {
                if (visited.put(start, true) != null) continue;

                // Flood-fill everything connected to this node.
                List<Node> members = new ArrayList<>();
                Map<Node, Integer> index = new IdentityHashMap<>();
                ArrayDeque<Node> queue = new ArrayDeque<>();
                queue.add(start);
                while (!queue.isEmpty()) {
                    Node node = queue.poll();
                    index.put(node, members.size());
                    members.add(node);
                    forEachNeighbor(node, next -> {
                        if (visited.put(next, true) == null) queue.add(next);
                    });
                }

                // Each linked pair becomes one link (only counted from the lower index).
                List<int[]> links = new ArrayList<>();
                List<Double> wires = new ArrayList<>();
                for (int i = 0; i < members.size(); i++) {
                    int a = i;
                    forEachNeighbor(members.get(i), next -> {
                        Integer b = index.get(next);
                        if (b != null && b > a) {
                            links.add(new int[]{a, b});
                            wires.add(wireResistance(members.get(a), next));
                        }
                    });
                }
                networks.add(new Network(members, links, wires));
            }
        }
    }
}
