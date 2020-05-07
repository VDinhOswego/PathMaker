/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc36503;

/**
 * A priority queue class which first removes the lowest weight
 * @author Vincent
 */
public class MinPriorityQueue {

    public static final class Node {
        Node right;
        float distance;
        int index;

        public Node(float d, int i) {
            right = null;
            distance = d;
            index = i;
        }
    }
    Node root;

    /**
     * Constructor for the class
     * Sets root to null
     */
    public MinPriorityQueue() {
        root = null;
    }

    /**
     * Checks if root is null
     * @return 
     */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Inserts index into queue
     * @param d weight
     * @param i index
     */
    public void add(float d, int i) {
        Node x = new Node(d, i);
        if (isEmpty()) {
            root = x;
        } else if (root.right == null) {
            if (x.distance < root.distance) {
                x.right = root;
                root = x;
            } else {
                root.right = x;
            }
        } else {
            Node temp = root;
            if (x.distance < root.distance) {
                x.right = root;
                root = x;
                return;
            }
            while (temp.right.right != null) {
                if (x.distance < temp.right.distance) {
                    x.right = temp.right;
                    temp.right = x;
                    break;
                }
                temp = temp.right;
            }
            if (temp.right.right == null) {
                if (x.distance < temp.right.distance) {
                    x.right = temp.right;
                    temp.right = x;
                } else {
                    temp.right.right = x;
                }
            }
        }
    }

    /**
     * Returns next least weighted index without removing
     * @return 
     */
    public int peek() {
        if (isEmpty()) {
            return -1;
        } else {
            return root.index;
        }
    }

    /**
     * Returns next least weighted index and sets the root to the 
     * next least weighted index
     * @return 
     */
    public int remove() {
        if (isEmpty()) {
            return -1;
        } else {
            Node temp = root;
            root = root.right;
            return temp.index;
        }
    }
    
    /**
     * Changes weight of the specified index by finding the value and removing the value
     * and reinserts the new values
     * @param d weight
     * @param i index
     */
    public void changeDistance(float d, int i) {
        if (isEmpty()) {
        } else if (root.right == null) {
            if (root.index == i) {
                root.distance = d;
            }
        } else {
            Node temp = root;
            while (temp.right.right != null) {
                if (temp.right.index == i) {
                    temp.right = temp.right.right;
                    break;
                }
                temp = temp.right;
            }
            if (temp.right.right == null) {
                if (temp.right.index == i) {
                    temp.right = null;
                }
            }
            add(d,i);
        }
    }

    public static void main(String[] args) {
        MinPriorityQueue pq = new MinPriorityQueue();
        pq.add(10, 1);
        pq.add(2, 2);
        pq.add(5, 3);
        pq.add(12, 4);
        pq.add(4, 5);
        pq.add(9, 6);
        pq.changeDistance(6, 4);
        while (!pq.isEmpty()) {
            System.out.println(pq.remove());
        }
    }
}
