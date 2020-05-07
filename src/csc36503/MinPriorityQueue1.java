/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc36503;

import java.util.NoSuchElementException;

/**
 * A Minimum Priority Queue implemented using a binary heap
 * @author Vincent
 */
public class MinPriorityQueue1 {

    public static final class Node {

        float weight;
        int index;

        public Node(float d, int i) {
            weight = d;
            index = i;
        }
    }
    Node[] heap = new Node[100000];
    int count;

    /**
     * The Constructor method for the MinPriorityQueue1 class
     * initializes count to 0
     */
    public MinPriorityQueue1() {
        count = 0;
    }

    /**
     * Checks if count is zero
     * @return 
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns the index of the minimum value with out removing it
     * @return 
     */
    public int peek() {
        if (count == 0) {
            throw new NoSuchElementException();
        } else {
            return heap[0].index;
        }
    }

    /**
     * Returns parent array index of array index i
     * @param i array index
     * @return 
     */
    private int parent(int i) {
        return (i - 1) >>> 1;
    }

    /**
     * Returns left child array index of array index i
     * @param i array index
     * @return 
     */
    private int left(int i) {
        return (i << 1) + 1;
    }

    /**
     * Returns right child array index of array index i
     * @param i array index
     * @return 
     */
    private int right(int i) {
        return left(i) + 1;
    }

    /**
     * Inserts index into queue
     * @param d weight
     * @param index 
     */
    public void add(float d, int index) {
        Node temp = new Node(d, index);
        int i = count++;
        heap[i] = temp;
        while (i > 0) {
            int p = parent(i);
            if (heap[i].weight < heap[p].weight) {
                Node temp1 = heap[i]; 
                heap[i] = heap[p];
                heap[p] = temp1;
                i = p;
            } else {
                break;
            }
        }
    }
    
    /**
     * Returns and removes the minimum index and heapifies the heap
     * @return 
     */
    public int remove() {
        if (count == 0) {
            throw new NoSuchElementException();
        } else {
            Node x = heap[0];
            int i = --count;
            heap[0] = heap[i];
            i = 0;
            while (true) {
                Node least;
                int l = left(i);
                int r = right(i);
                if (l >= count) {
                    break;
                }
                Node lc = heap[l];
                if (r < count) {
                    Node rc = heap[r];
                    least = lc.weight < rc.weight? lc : rc;
                } else {
                    least = lc;
                }
                if (least.weight < heap[i].weight) {
                    if (least.equals(lc)) {
                        heap[l] = heap[i];
                        heap[i] = lc;
                        i = l;
                    } else {
                        Node temp = heap[r];
                        heap[r] = heap[i];
                        heap[i] = temp;
                        i = r;
                    }
                } else {
                    break;
                }
            }
            return x.index;
        }
    }
    
    /**
     * Reduces the weight of the given index with new weight d
     * @param d weight
     * @param index 
     */
    public void reduceDistance(float d, int index) {
        int i;
        for (i = 0; i < count; i++) {
            if (heap[i].index == index ) {
                heap[i].weight = d;
                break;
            }
        }
        while (i > 0) {
            int p = parent(i);
            if (heap[i].weight < heap[p].weight) {
                Node temp = heap[i];
                heap[i] = heap[p];
                heap[p] = temp;
                i = p;
            } else {
                break;
            }
        }
    }
    
    public static void main(String[] args) {
        MinPriorityQueue1 pq = new MinPriorityQueue1();
        pq.add(10, 1);
        pq.add(2, 2);
        pq.add(5, 3);
        pq.add(12, 4);
        pq.add(4, 5);
        pq.add(9, 6);
        pq.reduceDistance(9, 4);
        while (!pq.isEmpty()) {
            System.out.println(pq.remove());
        }
    }
}
