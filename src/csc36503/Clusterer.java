/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc36503;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Categorizes Businesses based on location into 5 clusters
 * Makes sure none of the clusters are empty
 * @author Vincent
 */
public class Clusterer {

    int k = 5;
    int count;
    String[] key;
    float[][] geoloc;
    float[][] centroids;
    ArrayList[] groups;
    ArrayList[] groupsKey;

    public Clusterer() {
        count = 0;
        key = new String[100000];
        geoloc = new float[100000][2];
        centroids = new float[k][2];
        groups = new ArrayList[k];
        groups = new ArrayList[k];
    }
    
    /**
     * Randomizes centroids by using existing data points
     * @return 
     */
    public float[][] randomizeCent() {
        float[][] cent = new float[k][2];
        for (int i = 0; i < k; i++) {
            int rand = (int) (Math.random() * 100000);
            cent[i][0] = geoloc[rand][0];
            cent[i][1] = geoloc[rand][1];
        }
        return cent;
    }

    /**
     * Randomizes centroids completely with random numbers
     * @return 
     */
    public float[][] randomizeCent1() {
        float[][] cent = new float[k][2];
        for (int i = 0; i < k; i++) {
            cent[i][0] = (float) (Math.random() * 45) - 5;
            cent[i][1] = (float) (Math.random() * 130) - 120;
        }
        return cent;
    }
    
    /**
     * Forms clusters and repeats until no clusters are empty
     */
    public void cluster() {
        int maxit = 30;
        float distance = 0;
        ArrayList[] bestKey = new ArrayList[k];
        ArrayList[] best = new ArrayList[k];
        for (int j = 0; j < k; j++) {
            best[j] = new ArrayList();
        }
        float[][] bestCent = new float[k][2];
        boolean done = false;
        while (best[0].isEmpty() || best[1].isEmpty() || best[2].isEmpty()
                || best[3].isEmpty() || best[4].isEmpty()) {
            bestCent = randomizeCent();
            for (int i = 0; i < maxit; i++) {
                float[][] checker = bestCent;
                if (i > 0) {
                    bestCent = recalcCent(best);
                    if (Arrays.deepEquals(bestCent, checker)) {
                        done = true;
                    }
                }
                if (done) {
                    break;
                }
                System.out.println(i);
                ArrayList[] bestTemp = new ArrayList[k];
                ArrayList[] tempKeys = new ArrayList[k];
                for (int j = 0; j < k; j++) {
                    bestTemp[j] = new ArrayList();
                    tempKeys[j] = new ArrayList();
                }
                for (int j = 0; j < count; j++) {
                    bestTemp[closestCent(geoloc[j], bestCent)].add(geoloc[j]);
                    tempKeys[closestCent(geoloc[j], bestCent)].add(key[j]);
                }
                float distTemp = 0;
                for (int j = 0; j < k; j++) {
                    for (int k = 0; k < bestTemp[j].size(); k++) {
                        distTemp += findDistance((float[]) bestTemp[j].get(k),
                                bestCent[j]);
                    }
                }
                if (i == 0) {
                    distance = distTemp;
                    best = bestTemp;
                    bestKey = tempKeys;
                } else if (distance > distTemp) {
                    distance = distTemp;
                    best = bestTemp;
                    bestKey = tempKeys;
                }
            }
        }
        groupsKey = bestKey;
        groups = best;
        centroids = bestCent;
    }

    /**
     * Calculates new centroids by averaging the points in each cluster
     * @param grouping clusters of data points
     * @return 
     */
    private float[][] recalcCent(ArrayList[] grouping) {
        float[][] newCent = new float[k][2];
        for (int i = 0; i < k; i++) {
            float la = 0;
            float lo = 0;
            for (int j = 0; j < grouping[i].size(); j++) {
                float[] temp = (float[]) grouping[i].get(j);
                la += temp[0];
                lo += temp[1];
            }
            newCent[i] = new float[]{la / grouping[i].size(), lo
                / grouping[i].size()};
        }
        return newCent;
    }

    /**
     * Loads data to cluster and increments count
     * @param k key
     * @param lat latitude
     * @param longi longitude
     */
    public void loadData(String k, float lat, float longi) {
        key[count] = k;
        geoloc[count][0] = lat;
        geoloc[count][1] = longi;
        count++;
    }

    /**
     * Finds closest centroid, in the given group of centroids, to the given point
     * @param v data point
     * @param cent collection of centroids
     * @return 
     */
    private int closestCent(float[] v, float[][] cent) {
        int index = 0;
        float dist = findDistance(v, cent[0]);
        for (int i = 1; i < k; i++) {
            float temp = findDistance(v, cent[i]);
            if (dist > temp) {
                index = i;
                dist = temp;
            }
        }
        return index;
    }

    /**
     * Finds distance between two points using a basic distance formula
     * returns the absolute value of the distance
     * @param a first point
     * @param b second point
     * @return 
     */
    private float findDistance(float[] a, float[] b) {
        float lat, longi;
        lat = (float) Math.pow(b[0] - a[0], 2);
        longi = (float) Math.pow(b[1] - a[1], 2);
        return (float) Math.abs(Math.sqrt(lat + longi));
    }
}
