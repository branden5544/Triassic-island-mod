package com.triassicisland.power;

import java.util.List;

/**
 * One connected group of nodes. Builds the nodal conductance matrix (G·V = I) every tick and solves it
 * with preconditioned conjugate gradients, starting from last tick's voltages so it usually converges in a few steps.
 */
final class Network {
    /** Tiny leak to ground on every node so floating networks (no loads or sources) still have a solution. */
    private static final double LEAK = 1e-6;
    private static final double TOLERANCE = 1e-9;

    private final Node[] nodes;
    private final int[] linkA, linkB;
    /** Extra resistance for overhead wires (0 for blocks that simply touch). */
    private final double[] linkWire;
    private final double[] ground, diag, rhs, x, r, z, p, ap, linkMax;

    Network(List<Node> members, List<int[]> links, List<Double> wires) {
        int n = members.size();
        nodes = members.toArray(new Node[0]);
        linkA = new int[links.size()];
        linkB = new int[links.size()];
        linkWire = new double[links.size()];
        for (int k = 0; k < links.size(); k++) {
            linkA[k] = links.get(k)[0];
            linkB[k] = links.get(k)[1];
            linkWire[k] = wires.get(k);
        }
        ground = new double[n];
        diag = new double[n];
        rhs = new double[n];
        x = new double[n];
        r = new double[n];
        z = new double[n];
        p = new double[n];
        ap = new double[n];
        linkMax = new double[n];
    }

    private double linkConductance(int k) {
        Node a = nodes[linkA[k]], b = nodes[linkB[k]];
        return 1.0 / (a.owner.linkResistance(a) + b.owner.linkResistance(b) + linkWire[k]);
    }

    void solve() {
        int n = nodes.length;
        double[] linkG = new double[linkA.length];
        for (int i = 0; i < n; i++) {
            Node node = nodes[i];
            ground[i] = node.owner.groundConductance(node);
            diag[i] = LEAK + ground[i];
            rhs[i] = node.owner.sourceCurrent(node);
            x[i] = node.voltage;
        }
        for (int k = 0; k < linkA.length; k++) {
            linkG[k] = linkConductance(k);
            diag[linkA[k]] += linkG[k];
            diag[linkB[k]] += linkG[k];
        }

        // Conjugate gradients with a Jacobi (diagonal) preconditioner.
        multiply(linkG, x, ap);
        double rhsNorm = 0;
        for (int i = 0; i < n; i++) {
            r[i] = rhs[i] - ap[i];
            z[i] = r[i] / diag[i];
            p[i] = z[i];
            rhsNorm += rhs[i] * rhs[i];
        }
        double threshold = TOLERANCE * Math.max(rhsNorm, 1e-12);
        double rz = dot(r, z);
        for (int iter = 0; iter < Math.max(50, 2 * n) && dot(r, r) > threshold; iter++) {
            multiply(linkG, p, ap);
            double alpha = rz / dot(p, ap);
            for (int i = 0; i < n; i++) {
                x[i] += alpha * p[i];
                r[i] -= alpha * ap[i];
                z[i] = r[i] / diag[i];
            }
            double rzNext = dot(r, z);
            double beta = rzNext / rz;
            rz = rzNext;
            for (int i = 0; i < n; i++) p[i] = z[i] + beta * p[i];
        }

        java.util.Arrays.fill(linkMax, 0);
        for (int k = 0; k < linkA.length; k++) {
            double amps = Math.abs(linkG[k] * (x[linkA[k]] - x[linkB[k]]));
            if (linkWire[k] > 0) {
                Node a = nodes[linkA[k]], b = nodes[linkB[k]];
                boolean aFirst = a.owner.getBlockPos().compareTo(b.owner.getBlockPos()) < 0;
                (aFirst ? a : b).owner.onRemoteCurrent((aFirst ? b : a).owner.getBlockPos(), amps);
                continue;
            }
            linkMax[linkA[k]] = Math.max(linkMax[linkA[k]], amps);
            linkMax[linkB[k]] = Math.max(linkMax[linkB[k]], amps);
        }
        for (int i = 0; i < n; i++) {
            Node node = nodes[i];
            node.owner.solved(node, x[i], ground[i], rhs[i]);
            node.owner.onLinkCurrent(node, linkMax[i]);
        }
    }

    /** out = G · v, where G is the conductance matrix (diagonal minus link conductances). */
    private void multiply(double[] linkG, double[] v, double[] out) {
        for (int i = 0; i < v.length; i++) out[i] = diag[i] * v[i];
        for (int k = 0; k < linkA.length; k++) {
            out[linkA[k]] -= linkG[k] * v[linkB[k]];
            out[linkB[k]] -= linkG[k] * v[linkA[k]];
        }
    }

    private static double dot(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) sum += a[i] * b[i];
        return sum;
    }
}
