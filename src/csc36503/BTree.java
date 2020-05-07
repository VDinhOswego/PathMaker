/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc36503;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

/**
 * B-Tree which contains all of the Business ID's and data needed for the 
 * similarity metrics
 * @author Vincent
 */
public class BTree {
    public static final class BTreeNode {
        static final long NEWNODE = -1l;
        static final int t = 16;         //FYWN1wneV18bWNgQjJ2GNg
        static final String EMPTYSTRING = "                      ";
        long position;
        int count;
        String[] key = new String[2*t-1];
        long[] children = new long[2*t];
        long[] values = new long[2*t-1];
        boolean leaf;
        long parent;
        
        /**
         * Initialize all variables
         */
        public BTreeNode() {
            position = NEWNODE;
            for (int i = 0; i < 2*t-1; i++) {
                key[i] = EMPTYSTRING;
            }
            for (int i = 0; i < 2*t; i++) {
                children[i] = NEWNODE;
            }
            for (int i = 0; i < 2*t-1; i++) {
                values[i] = NEWNODE;
            }
            parent = NEWNODE;
        }
        /**
         * Set current the Node to the values that are retrieved
         * @param f file name
         * @param p address of Node to read
         * @throws IOException 
         */
        void read(String f, long p) throws IOException {
            FileInputStream fi = new FileInputStream(f);
            FileChannel fc = fi.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 6);
            buffer.clear();
            fc.read(buffer, p);
            buffer.flip();
            position = buffer.getLong();
            count = buffer.getInt();
            byte[] temp = new byte[key[0].getBytes().length];
            for (int i = 0; i < 2*t-1; i++) {
                buffer.get(temp);
                key[i] = new String(temp);
            }
            for (int i = 0; i < 2*t; i++) {
                children[i] = buffer.getLong();
            }
            for (int i = 0; i < 2*t-1; i++) {
                values[i] = buffer.getLong();
            }
            leaf = buffer.getInt() == 1;
            parent = buffer.getLong();
            fc.close();
            fi.close();
        }
        
        /**
         * Write current node to file
         * If this is a new node then it writes the node to end of file
         * @param f file name
         * @throws IOException 
         */
        void write(String f) throws IOException {
            RandomAccessFile rf = new RandomAccessFile(f, "rw");
            //FileOutputStream fo = new FileOutputStream(f, true);
            FileChannel fc = rf.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 6);
            buffer.clear();
            if (position == NEWNODE) {
                fc.position(fc.size());
                position = fc.position();
            } else {
                fc.position(position);
            }
            buffer.putLong(position);
            buffer.putInt(count);
            for (int i = 0; i < 2*t-1; i++) {
                buffer.put(key[i].getBytes());
            }
            for (int i = 0; i < 2*t; i++) {
                buffer.putLong(children[i]);
            }
            for (int i = 0; i < 2*t-1; i++) {
                buffer.putLong(values[i]);
            }
            if (leaf) {
                buffer.putInt(1);
            } else {
                buffer.putInt(0);
            }
            buffer.putLong(parent);
            buffer.flip();
            fc.write(buffer);
            fc.close();
            //fo.close();
            rf.close();
        }
        
        /**
         * Writes the values of a corresponding key to a value file
         * @param f file name
         * @param index index of the corresponding key
         * @param lat latitude
         * @param longi longitude
         * @param star star average
         * @throws IOException 
         */
        void fill(String f,int index, float lat, float longi, float star) 
                throws IOException {
            FileOutputStream fo = new FileOutputStream(f, true);
            FileChannel fc = fo.getChannel();
            fc.position(fc.size());
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 6);
            buffer.clear();
            values[index] = fc.position();
            buffer.putFloat(lat);
            buffer.putFloat(longi);
            buffer.putFloat(star);
            buffer.flip();
            fc.write(buffer);
            fc.close();
            fo.close();
        }
        
        /**
         * Search for key given and return the corresponding value address or null
         * @param k key
         * @param f file name
         * @return
         * @throws IOException 
         */
        Long search(String k, String f) throws IOException {
            int i = 0;
            while (i < count-1 && k.compareTo(key[i]) > 0) i++;
            if (i < count && k.compareTo(key[i]) == 0) return values[i];
            if (leaf) return null;
            else {
                BTreeNode temp = new BTreeNode();
                temp.read(f, children[i]);
                return temp.search(k, f);
            }
        }
        
        /**
         * Put the key and values to the leafs and splits nodes that are at max
         * @param k key
         * @param lat latitude
         * @param longi longitude
         * @param star star average
         * @throws IOException 
         */
        void insertNotFull(String k, float lat, float longi, float star) 
                throws IOException {
            int i = count-1;
            if (leaf) {
                while (i >= 0 && k.compareTo(key[i]) < 0) {
                    key[i+1] = key[i];
                    values[i+1] = values[i]; 
                    i--;
                }
                key[i+1] = k;
                count++;
                fill(valFile, i+1, lat, longi, star);
                this.write(keyFile);
            } else {
                while (i > 0 && k.compareTo(key[i-1]) < 0) {
                    i--;
                }
                BTreeNode temp = new BTreeNode();
                temp.read(keyFile, children[i]);
                if (temp.count == 2*t-1) {
                    splitChild(i, temp);
                    if (k.compareTo(key[i]) > 0) {
                        i++;
                    }
                }
                temp.read(keyFile, children[i]);
                temp.insertNotFull(k, lat, longi, star);
            }
        }
        
        /**
         * Split child of current node y by creating a new node z and copying the
         * last half of y's keys, values and possibly children to z
         * The middle key and value of y are moved to the current node
         * @param i index of child
         * @param y actual child node
         * @throws IOException 
         */
        void splitChild(int i, BTreeNode y) throws IOException {
            BTreeNode z = new BTreeNode();
            z.leaf = y.leaf;
            z.count = t - 1;
            for (int j = 0; j < t-1; j++) {
                z.key[j] = y.key[j+t];
                z.values[j] = y.values[j+t];
            }
            if (!y.leaf) {
                for (int j = 0; j < t; j++) {
                    z.children[j] = y.children[j+t];
                }
            }
            y.count = t-1;
            for (int j = count; j > i; j--) {
                children[j+1] = children[j];
            }
            z.parent = this.position;
            z.write(keyFile);
            children[i+1] = z.position;
            for (int j = count - 1; j > i-1; j--) {
                key[j+1] = key[j];
                values[j+1] = values[j];
            }
            key[i] = y.key[t-1];
            values[i] = y.values[t-1];
            count++;
            y.parent = this.position;
            y.write(keyFile);
            children[i] = y.position;
            this.write(keyFile);
        }
        
        /**
         * Traverses nodes by traversing current Node's keys and recursively
         * calling the method to the current Node's children
         * @return
         * @throws IOException 
         */
        String[] traverse() throws IOException {
            ArrayList<String> temp = new ArrayList<String>();
            for (int i = 0; i < count; i++) {
                if (!key[i].equals(EMPTYSTRING)) {
                    temp.add(key[i]);
                }
                if (!leaf && children[i] != NEWNODE) {
                    BTreeNode child = new BTreeNode();
                    child.read(keyFile, children[i]);
                    Collections.addAll(temp, child.traverse());
                }
            }
            if (!leaf && children[count] != NEWNODE) {
                    BTreeNode child = new BTreeNode();
                    child.read(keyFile, children[count]);
                    Collections.addAll(temp, child.traverse());
                }
            return temp.toArray(new String[temp.size()]);
        }
    }
    BTreeNode root;
    static String keyFile, valFile;
    
    public BTree(String k, String v) {
        keyFile = k;
        valFile = v;
    }
    
    /**
     * Creates an empty root at the start of the key file
     * @throws IOException 
     */
    public void createEmptyTree() throws IOException {
        root = new BTreeNode();
        root.leaf = true;
        root.count = 0;
        root.position = 0l;
        root.write(keyFile);
    }
    
    /**
     * Reads an existing root node from key file
     * @throws IOException 
     */
    public void readExistingTree() throws IOException {
        root = new BTreeNode();
        root.read(keyFile, 0l);
    }
    
    /**
     * Inserts a key and values.
     * If root is full then the method splits the root and then inserts
     * @param k key
     * @param lat latitude
     * @param longi longitude
     * @param star star average
     * @throws IOException 
     */
    public void insert(String k, float lat, float longi, float star) 
            throws IOException {
        if (root.count == 2*BTreeNode.t-1) {
            BTreeNode r = root;
            BTreeNode s = new BTreeNode();
            root = s;
            s.leaf = false;
            s.count = 1;
            s.position = 0l;
            r.position = BTreeNode.NEWNODE;
            s.splitChild(0, r);
            s.insertNotFull(k, lat, longi, star);
        } else {
            root.insertNotFull(k, lat, longi, star);
        }
    }
    
    /**
     * Searches for a specific value by the given key then returns file address
     * If nothing is found then null is returned
     * @param k key
     * @return
     * @throws IOException 
     */
    public Long search(String k) throws IOException {
        if (root != null) {
            Long add = root.search(k, keyFile);
            if (add == null) return null;
            if (add == BTreeNode.NEWNODE) return null;
            return add;
        }
        return null;
    }
    
    /**
     * Returns the array of float values from the given key by using search()
     * Where the first index is the latitude, second is the longitude, and the 
     * third is the star rating
     * @param k
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public float[] getVal(String k) throws FileNotFoundException, IOException {
        float[] temp = new float[3];
        FileInputStream fi = new FileInputStream(valFile);
        FileChannel fc = fi.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 6);
        buffer.clear();
        Long add = search(k);
        if (add == null) return null;
        fc.read(buffer, add);
        buffer.flip();
        temp[0] = buffer.getFloat();
        temp[1] = buffer.getFloat();
        temp[2] = buffer.getFloat();
        fc.close();
        fi.close();
        return temp;
    }
    
    /**
     * Traverses the B Tree by calling the root node's traverse() method
     * If the root is null then null is returned
     * @return
     * @throws IOException 
     */
    public String[] traverse() throws IOException {
        if (root != null) {
             return root.traverse();
        }
        return null;
    }
}
