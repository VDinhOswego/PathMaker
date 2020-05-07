/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc36503;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * The hash table where all the Histograms are stored implements Serializable
 * @author Vincent
 */
public class HistogramTable implements Serializable {
    
    static final class Node {

        String key;
        Histogram hg;
        Node next;

        Node(String k, Histogram v, Node n) {
            key = k;
            hg = v;
            next = n;
        }
    }
    private Node[] table;
    private int count = 0;
    private int capacity = 16;

    public HistogramTable() {
        table = new Node[capacity];
    }

    public Histogram get(String k) {
        int i = hash(k);
        for (Node e = table[i]; e != null; e = e.next) {
            if (k.equals(e.key)) {
                return e.hg;
            }
        }
        return null;
    }

    public void put(String k, String review) {
        int i = hash(k);
        for (Node e = table[i]; e != null; e = e.next) {
            if (k.equals(e.key)) {
                e.hg.add(review);
                return;
            }
        }
        Histogram temp = new Histogram();
        temp.add(review);
        Node p = new Node(k, temp, table[i]);
        table[i] = p;
        count++;
        if (count >= .75 * capacity) {
            resize();
        }
    }
    
    public void putWhole(String k, Histogram h) {
        int i = hash(k);
        for (Node e = table[i]; e != null; e = e.next) {
            if (k.equals(e.key)) {
                e.hg = h;
                return;
            }
        }
        Node p = new Node(k, h, table[i]);
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
                System.out.println("Business ID: " + e.key + " - Histogram: ");
                e.hg.printAll();
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
                Node p = new Node(e.key, e.hg, temp[index]);
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
            h = Integer.parseInt(temp1.substring(0, 9)) + 
                    (temp1.charAt(temp1.length() - 1)) + temp1.length() + 
                    (temp1.charAt(temp1.length() - 5));
        } else {
            h = Integer.parseInt(temp1) + (temp1.charAt(temp1.length() - 1)) + 
                    temp1.length();
        }
        h = h * 17;
        return Math.abs(h % capacity);
    }
    
    /**
     * The method is called when writing to a file using serialization
     * @param out The output stream
     * @throws IOException 
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(count);
        out.writeInt(capacity);
        for (int i = 0; i < table.length; i++) {
            for (Node e = table[i]; e != null; e = e.next) {
                out.writeUTF(e.key);
                out.writeObject(e.hg);
            }
        }
    }
    
    /**
     * The method is called when reading from a file using serialization
     * @param in The input stream
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private void readObject(ObjectInputStream in) throws IOException, 
            ClassNotFoundException {
        int c = in.readInt();
        capacity = in.readInt();
        table = new Node[capacity];
        for (int i = 0; i < c; i++) {
            putWhole(in.readUTF(), (Histogram) in.readObject());
        }
    }
}
