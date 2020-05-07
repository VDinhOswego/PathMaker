/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc36503;

import java.nio.charset.StandardCharsets;

/**
 * The table where the cosine similarity to a certain Business ID are stored
 * @author Vincent
 */
public class CosineSimTable {
    
    static final class Node {

        String key;
        double cossim;
        Node next;

        Node(String k, double v, Node n) {
            key = k;
            cossim = v;
            next = n;
        }
    }
    /**
     * The ID the other Business are being compared to
     */
    private String mainKey;

    private Node[] table;
    private int count = 0;
    private int capacity = 16;

    public CosineSimTable(String k) {
        mainKey = k;
        table = new Node[capacity];
    }

    public double get(String k) {
        int i = hash(k);
        for (Node e = table[i]; e != null; e = e.next) {
            if (k.equals(e.key)) {
                return e.cossim;
            }
        }
        return 0;
    }

    public void put(String k, double c) {
        int i = hash(k);
        Node p = new Node(k, c, table[i]);
        table[i] = p;
        count++;
        if (count >= .75 * capacity) {
            resize();
        }
    }

    public String[] getAllKeys() {
        String temp = "";
        for (int i = 0; i < table.length; i++) {
            for (Node e = table[i]; e != null; e = e.next) {
                temp = e.key + " " + temp;
            }
        }
        return temp.split("\\s");
    }

    public void printAllKeys() {
        for (int i = 0; i < table.length; i++) {
            for (Node e = table[i]; e != null; e = e.next) {
                System.out.println(e.key);
                System.out.println();
            }
        }
    }

    public void printAll() {
        for (int i = 0; i < table.length; i++) {
            for (Node e = table[i]; e != null; e = e.next) {
                System.out.println("Business ID: " + e.key + " - Cosine Similarity: " + e.cossim);
                System.out.println();
            }
        }
    }

    private void resize() {
        capacity = 2 * capacity;
        Node[] temp = new Node[capacity];
        for (int i = 0; i < table.length; i++) {
            for (Node e = table[i]; e != null; e = e.next) {
                int index = hash(e.key);
                Node p = new Node(e.key, e.cossim, temp[index]);
                temp[index] = p;
            }
        }
        table = temp;
    }

    private int hash(String k) {
        byte temp[] = k.getBytes(StandardCharsets.US_ASCII);
        String temp1 = "";
        for (int i = 0; i < temp.length; i++) {
            temp1 += temp[i];
        }
        int h;
        if (temp1.length() > 9) {
            h = Integer.parseInt(temp1.substring(0, 9)) + (temp1.charAt(temp1.length() - 1)) + temp1.length() + (temp1.charAt(temp1.length() - 5));
        } else {
            h = Integer.parseInt(temp1) + (temp1.charAt(temp1.length() - 1)) + temp1.length();
        }
        h = h * 17;
        return Math.abs(h % capacity);
    }
    /**
     * Creates the vectors from term frequency and stores the cosine
     * similarity in this table
     * @param ht The hash table with the histograms
     */
    public void populate(HistogramTable ht) {
        Histogram main = ht.get(mainKey);
        String[] words = main.getAllWords();
        String[] keys = ht.getAllKeys();
        for (int i = 0; i < keys.length; i++) {
            if (!mainKey.equals(keys[i])) {
                Histogram temp = ht.get(keys[i]);
                double[] a = new double[words.length];
                double[] b = new double[words.length];
                for (int j = 0; j < words.length; j++) {
                    a[j] = main.get(words[j]);
                    if (temp.get(words[j]) != null) {
                        b[j] = temp.get(words[j]);
                    } else {
                        b[j] = 0;
                    }
                }
                put(keys[i], cosineSim(a, b));
            }
        }
    }
    /**
     * Calculates cosine similarity from two vectors
     * @param a The first vector
     * @param b The second vector
     * @return 
     */
    public double cosineSim(double[] a, double[] b) {
        double dot = 0;
        double A = 0;
        double B = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            A += Math.pow(a[i], 2);
            B += Math.pow(b[i], 2);
        }
        return dot / ((Math.sqrt(A) * Math.sqrt(B)));
    }
}
