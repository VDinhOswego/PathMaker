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
 * Histograms of term frequency for each Business ID implements Serializable
 * @author Vincent
 */
public class Histogram implements Serializable{

    static final class Node {

        String key;
        int freq;
        Node next;

        Node(String k, int v, Node n) {
            key = k;
            freq = v;
            next = n;
        }
    }
    private Node[] table;
    private int count = 0;
    private int capacity = 16;

    public Histogram() {
        table = new Node[capacity];
    }

    public Integer get(String k) {
        int i = hash(k);
        for (Node e = table[i]; e != null; e = e.next) {
            if (k.equals(e.key)) {
                return e.freq;
            }
        }
        return null;
    }
    
    /**
     * Clean, splits, and adds all the words to the table
     * @param review The String representation of the review
     */
    public void add(String review) {
        String temp = review.toLowerCase().replaceAll(",", "");
        temp = temp.replaceAll("\\.", "");
        temp = temp.replaceAll("\\(", "");
        temp = temp.replaceAll("\\)", "");
        temp = temp.replaceAll("\\!", "");
        temp = temp.replaceAll("\\?", "");
        temp = temp.replaceAll("\"", "");
        String[] split = temp.split("\\s");
        for (int i = 0; i < split.length; i++) {
            this.put(split[i]);
        }
    }

    public void put(String k) {
        int i = hash(k);
        for (Node e = table[i]; e != null; e = e.next) {
            if (k.equals(e.key)) {
                e.freq++;
                return;
            }
        }
        Node p = new Node(k, 1, table[i]);
        table[i] = p;
        count++;
        if (count >= .75 * capacity) {
            resize();
        }
    }
    
    public void putWhole(String k, int f) {
        int i = hash(k);
        for (Node e = table[i]; e != null; e = e.next) {
            if (k.equals(e.key)) {
                e.freq = f;
                return;
            }
        }
        Node p = new Node(k, f, table[i]);
        table[i] = p;
        count++;
        if (count >= .75 * capacity) {
            resize();
        }
    }
    
    public String[] getAllWords() {
        String temp = "";
        for (int i = 0; i < table.length; i++) {
            for (Node e = table[i]; e != null; e = e.next) {
                temp = e.key + " " + temp;
            }
        }
        return temp.split("\\s");
    }

    public void printAllWords() {
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
                System.out.println("Word: " + e.key + " - Freq: " + e.freq);
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
                Node p = new Node(e.key, e.freq, temp[index]);
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
        if (temp1.equals("")) {
            temp1 = "0";
        }
        int h;
        if (temp1.length() > 9) {
            h = Integer.parseInt(temp1.substring(0, 9)) + 
                    (temp1.charAt(temp1.length() - 1)) + temp1.length() + 
                    (temp1.charAt(temp1.length() - 5));
        } else {
            h = Integer.parseInt(temp1) + (temp1.charAt(temp1.length() - 1)) 
                    + temp1.length();
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
                out.writeInt(e.freq);
            }
        }
    }
    
    /**
     * The method is called when reading to a file using serialization
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
            putWhole(in.readUTF(), in.readInt());
        }
    }
}
