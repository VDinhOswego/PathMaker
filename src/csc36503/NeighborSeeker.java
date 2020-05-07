/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc36503;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Finds every business's 4 closest neighbors and number of disjoint sets and
 * records them
 *
 * @author Vincent
 */
public class NeighborSeeker {

    String[] keys;
    float[][] geoloc;
    String[][] neighbors;
    int[][] neighborsIndex;
    int disjoints;
    int[] parent;
    
    /**
     * The constructor for NeighborSeeker class
     * @param tree the BTree used to populate values
     * @throws IOException 
     */
    public NeighborSeeker(BTree tree) throws IOException {
        keys = tree.traverse();
        geoloc = new float[keys.length][2];
        for (int i = 0; i < keys.length; i++) {
            float[] temp = tree.getVal(keys[i]);
            if (temp != null) {
                geoloc[i] = new float[]{temp[0], temp[1]};
            }
        }
        parent = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            parent[i] = i;
        }
    }

    /**
     * Finds the nearest 4 neighbors for each business
     */
    public void seek() {
        neighbors = new String[keys.length][];
        neighborsIndex = new int[keys.length][];
        for (int i = 0; i < keys.length; i++) {
            if (!Arrays.equals(geoloc[i], new float[]{0,0})) {
                String[] best = new String[4];
                int[] bestInd = new int[4];
                float[] bestDis = {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY};
                for (int j = 0; j < keys.length; j++) {
                    if (!Arrays.equals(geoloc[j], new float[]{0,0}) && i != j) {
                        float distance = DistanceErector.haversineDistance(geoloc[i], geoloc[j]);
                        if (distance < bestDis[0]) {
                            for (int k = 0; k < 3; k++) {
                                bestDis[k] = bestDis[k + 1];
                                best[k] = best[k + 1];
                                bestInd[k] = bestInd[k + 1];
                            }
                            bestDis[0] = distance;
                            best[0] = keys[j];
                            bestInd[0] = j;
                        } else if (distance < bestDis[1]) {
                            for (int k = 1; k < 3; k++) {
                                bestDis[k] = bestDis[k + 1];
                                best[k] = best[k + 1];
                                bestInd[k] = bestInd[k + 1];
                            }
                            bestDis[1] = distance;
                            best[1] = keys[j];
                            bestInd[1] = j;
                        } else if (distance < bestDis[2]) {
                            bestDis[2] = bestDis[3];
                            best[2] = best[3];
                            bestInd[2] = bestInd[3];
                            bestDis[2] = distance;
                            best[2] = keys[j];
                            bestInd[2] = j;
                        } else if (distance < bestDis[3]) {
                            bestDis[3] = distance;
                            best[3] = keys[j];
                            bestInd[3] = j;
                        }
                    }
                }
                neighborsIndex[i] = bestInd;
                neighbors[i] = best;
            }
        }
    }

    /**
     * Finds the root index of the given business index
     * @param i business index
     * @return 
     */
    public int find(int i) {
        if (parent[i] == i) {
            return i;
        } else {
            return find(parent[i]);
        }
    }

    /**
     * Joins two sets together by making the parent of i's root equal to j's root
     * @param i business index 1
     * @param j business index 2
     */
    public void union(int i, int j) {
        parent[find(i)] = find(j);
    }

    /**
     * Checks to see if two businesses are in the same set by checking each root
     * @param i business index 1
     * @param j business index 2
     * @return 
     */
    public boolean isConnected(int i, int j) {
        return find(i) == find(j);
    }
    
    /**
     * Finds the number of disjoint sets by joining businesses by each neighbor and
     * counting each unique root
     */
    public void findDisjoints() {
        for (int i = 0; i < keys.length; i++) {
            if (!Arrays.equals(geoloc[i], new float[]{0,0})) {
                for (int j = 0; j < 4; j++) {
                    union(i, neighborsIndex[i][j]);
                }
            }
        }
        Set roots = new HashSet();
        for (int i = 0; i < keys.length; i++) {
            if (!Arrays.equals(geoloc[i], new float[]{0,0})) {
                roots.add(find(i));
            }
        }
        disjoints = roots.size();
    }

    /**
     * Writes the data to the given file f
     * @param f file name
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void writeData(String f) throws FileNotFoundException, IOException {
        FileOutputStream fo = new FileOutputStream(f);
        ObjectOutputStream os = new ObjectOutputStream(fo);
        os.writeInt(disjoints);
        os.writeObject(parent);
        os.writeObject(keys);
        os.writeObject(neighborsIndex);
        os.close();
        fo.close();
    }

    public static void main(String[] args) throws IOException {
        BTree tree = new BTree("BTree.txt", "Values.txt");
        tree.readExistingTree();
        NeighborSeeker ns = new NeighborSeeker(tree);
        ns.seek();
        ns.findDisjoints();
        ns.writeData("Edges.ser");
    }
}
