/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc36503;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.json.JSONObject;

/**
 * Main Class
 * @author Vincent
 */
public class Main {

    /**
     * The main method of the Loader
     *
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     * @throws java.lang.ClassNotFoundException
     */
    public static void main(String[] args) throws IOException,
            FileNotFoundException, ClassNotFoundException {
        /**
         * Try to read Json File and calculate word frequencies
         */
        String histo = "Histos.ser";
        String keys = "BTree.txt";
        String vals = "Values.txt";
        HistogramTable hgTable = new HistogramTable();
        try {
            PrintWriter pw = new PrintWriter(histo);
            pw.close();
            BufferedReader br = new BufferedReader(new FileReader("review.json"));
            String line = br.readLine();
            int count = 0;
            while (line != null && count < 100000) {
                JSONObject jobj = new JSONObject(line);
                hgTable.put(jobj.getString("business_id"), jobj.getString("text"));
                line = br.readLine();
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream fileOut = new FileOutputStream(histo);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(hgTable);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        hgTable = null;
        /**
         * Read Json file and populate BTree
         */
        PrintWriter pw = new PrintWriter(keys);
        pw.close();
        pw = new PrintWriter(vals);
        pw.close();
        BTree tree = new BTree(keys, vals);
        tree.createEmptyTree();
        try {
            BufferedReader br = new BufferedReader(new FileReader("business.json"));
            String line = br.readLine();
            int count = 0;
            while (line != null && count < 100000) {
                JSONObject jobj = new JSONObject(line);
                Object lat = jobj.get("latitude");
                Object lon = jobj.get("longitude");
                Object star = jobj.get("stars");
                if (lat != null && lon != null && star != null) {
                    tree.insert(jobj.getString("business_id"), ((Double) lat).floatValue(),
                            ((Double) lon).floatValue(), ((Double) star).floatValue());
                    count++;
                }
                line = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tree = null;
        clusterKeys();
    }

    /**
     * Form 5 Clusters from an existing B Tree and writes it to Cluster.ser
     *
     * @throws java.io.FileNotFoundException
     * @throws java.lang.ClassNotFoundException
     */
    public static void clusterKeys() throws FileNotFoundException, IOException,
            ClassNotFoundException {
        Clusterer clusterer = new Clusterer();
        BTree tree = new BTree("BTree.txt", "Values.txt");
        tree.readExistingTree();
        String[] keys = tree.traverse();
        float[][] vals = new float[keys.length][3];
        for (int i = 0; i < keys.length; i++) {
            vals[i] = tree.getVal(keys[i]);
        }
        for (int i = 0; i < keys.length; i++) {
            if (vals[i] != null) {
                clusterer.loadData(keys[i], vals[i][0], vals[i][1]);
            }
        }
        clusterer.cluster();
        PrintWriter pw = new PrintWriter("Cluster.ser");
        pw.close();
        FileOutputStream fo = new FileOutputStream("Cluster.ser");
        ObjectOutputStream os = new ObjectOutputStream(fo);
        os.writeObject(clusterer.centroids);
        for (ArrayList al : clusterer.groupsKey) {
            os.writeObject(al);
        }
        os.close();
        fo.close();
    }
}
