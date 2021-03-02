/**
 * Original code:
 * Copyright © 2000–2017, Robert Sedgewick and Kevin Wayne.
 * <p>
 * Modifications:
 * Copyright (c) 2017. Phasmid Software
 */
package edu.neu.coe.info6205.union_find;


import edu.neu.coe.info6205.util.Benchmark;
import edu.neu.coe.info6205.util.Benchmark_Timer;

import java.util.Arrays;
import java.util.Random;

/**
 * Height-weighted Quick Union with Path Compression
 */
public class UF_HWQUPC_AlternativeDepth implements UF {
    /**
     * Ensure that site p is connected to site q,
     *
     * @param p the integer representing one site
     * @param q the integer representing the other site
     */
    public void connect(int p, int q) {
        if (!isConnected(p, q)) {
            union(p, q);
        }
    }

    /**
     * Initializes an empty union–find data structure with {@code n} sites
     * {@code 0} through {@code n-1}. Each site is initially in its own
     * component.
     *
     * @param n               the number of sites
     * @param pathCompression whether to use path compression
     * @throws IllegalArgumentException if {@code n < 0}
     */
    public UF_HWQUPC_AlternativeDepth(int n, boolean pathCompression) {
        count = n;
        parent = new int[n];
        height = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            height[i] = 0;
        }
        this.pathCompression = pathCompression;
    }

    /**
     * Initializes an empty union–find data structure with {@code n} sites
     * {@code 0} through {@code n-1}. Each site is initially in its own
     * component.
     * This data structure uses path compression
     *
     * @param n the number of sites
     * @throws IllegalArgumentException if {@code n < 0}
     */
    public UF_HWQUPC_AlternativeDepth(int n) {
        this(n, true);
    }

    public void show() {
        for (int i = 0; i < parent.length; i++) {
            System.out.printf("%d: %d, %d\n", i, parent[i], height[i]);
        }
    }

    /**
     * Returns the number of components.
     *
     * @return the number of components (between {@code 1} and {@code n})
     */
    public int components() {
        return count;
    }

    /**
     * Returns the component identifier for the component containing site {@code p}.
     *
     * @param p the integer representing one site
     * @return the component identifier for the component containing site {@code p}
     * @throws IllegalArgumentException unless {@code 0 <= p < n}
     */
    public int find(int p) {
        validate(p);
        int root = p;
        while (parent[root] != root) {
            doPathCompression(p);
            root = parent[root];
        }
        return root;
    }

    /**
     * Returns true if the the two sites are in the same component.
     *
     * @param p the integer representing one site
     * @param q the integer representing the other site
     * @return {@code true} if the two sites {@code p} and {@code q} are in the same component;
     * {@code false} otherwise
     * @throws IllegalArgumentException unless
     *                                  both {@code 0 <= p < n} and {@code 0 <= q < n}
     */
    public boolean connected(int p, int q) {
        return find(p) == find(q);
    }

    /**
     * Merges the component containing site {@code p} with the
     * the component containing site {@code q}.
     *
     * @param p the integer representing one site
     * @param q the integer representing the other site
     * @throws IllegalArgumentException unless
     *                                  both {@code 0 <= p < n} and {@code 0 <= q < n}
     */
    public void union(int p, int q) {
        // CONSIDER can we avoid doing find again?
        mergeComponents(find(p), find(q));
        count--;
    }

    @Override
    public int size() {
        return parent.length;
    }

    /**
     * Used only by testing code
     *
     * @param pathCompression true if you want path compression
     */
    public void setPathCompression(boolean pathCompression) {
        this.pathCompression = pathCompression;
    }

    @Override
    public String toString() {
        return "UF_HWQUPC:" + "\n  count: " + count +
                "\n  path compression? " + pathCompression +
                "\n  parents: " + Arrays.toString(parent) +
                "\n  heights: " + Arrays.toString(height);
    }

    // validate that p is a valid index
    private void validate(int p) {
        int n = parent.length;
        if (p < 0 || p >= n) {
            throw new IllegalArgumentException("index " + p + " is not between 0 and " + (n - 1));
        }
    }

    private void updateParent(int p, int x) {
        parent[p] = x;
    }

    private void updateHeight(int p, int x) {
        if (height[p] < height[x] + 1) {
            height[p] = height[x] + 1;
        }
    }

    /**
     * Used only by testing code
     *
     * @param i the component
     * @return the parent of the component
     */
    private int getParent(int i) {
        return parent[i];
    }

    private final int[] parent;   // parent[i] = parent of i
    private final int[] height;   // height[i] = height of subtree rooted at i
    private int count;  // number of components
    private boolean pathCompression;

    private void mergeComponents(int i, int j) {
        // TO BE IMPLEMENTED make shorter root point to taller one
        if (i == j) return;
        if (height[i] < height[j]) {
            updateParent(i, j);
            updateHeight(j, i);
        } else {
            updateParent(j, i);
            updateHeight(i, j);
        }
    }

    /**
     * This implements the two-pass mechanism of path compression
     */
    private void doPathCompression(int i) {
        // TO BE IMPLEMENTED update parent to value of grandparent
        if (this.pathCompression) {
            parent[i] = parent[parent[i]];
        }

    }

    /**
     * @param n sites
     */
    public static void BenchmarkWQUPC(int n) {
        UF_HWQUPC_AlternativeDepth ufAlt = new UF_HWQUPC_AlternativeDepth(n);
        UF_HWQUPC uf = new UF_HWQUPC(n);
        Random rd = new Random();
        Benchmark<Boolean> bm = new Benchmark_Timer<>("Weighted Quick Union by storing the Size for " + n + " sites", b -> {
            for (int i = 0; i < n - 1; i++) {
                uf.connect(rd.nextInt(n), rd.nextInt(n));
            }
            while (uf.components() > 1) {
                int p = rd.nextInt(n);
                int q = rd.nextInt(n);
                uf.connect(p, q);
            }
        });
        double benchmarkWQUPC = bm.run(true, 100);
        System.out.println(benchmarkWQUPC + " milliseconds");
        Benchmark<Boolean> bmAlt = new Benchmark_Timer<>("Alternate Weighted Quick Union by storing the Depth for " + n + " sites", b -> {
            for (int i = 0; i < n - 1; i++) {
                ufAlt.connect(rd.nextInt(n), rd.nextInt(n));
            }
            while (ufAlt.components() > 1) {
                int p = rd.nextInt(n);
                int q = rd.nextInt(n);
                ufAlt.connect(p, q);
            }
        });
        double benchmarkWQUPCAlt = bmAlt.run(true, 100);
        System.out.println(benchmarkWQUPCAlt + " milliseconds");
    }

    /**
     * @param args number of sites
     */
    public static void main(String[] args) {
        for (String n : args) {
            BenchmarkWQUPC(Integer.parseInt(n));
        }
    }
}