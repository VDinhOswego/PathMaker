/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc36503;

//import com.mxgraph.swing.mxGraphComponent;
//import com.mxgraph.view.mxGraph;
//import com.mxgraph.view.mxGraphView;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.JFrame;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

/**
 * Graphs the shortest path to the business closest to the centroid from a
 * directed graph
 * A.K.A. The Cartographer
 * @author Vincent
 */
public class Grapher extends JFrame {

    String edges = "Edges.ser", BTreekey = "BTree.txt", BTreeval = "Values.txt";
    String clusts = "Cluster.ser";
    float[][] geoloc;
    String[] keys;
    int[][] neighbors;
    int disjoints;
    int[] parent;
    float[][] centroids;
    ArrayList[] clusters;

    //Makes the graph legible
    public static final String GRAPHSTYLE = "graph {fill-color: white;}"
            + "node.path {fill-color: orange; size: 10px;}" + "node.start {fill-color: red; size: 13px;}"
            + "node.goal {fill-color: green; size: 13px;}" + "node {size: 3px;}" + "edge.path {fill-color: orange;}";

    /**
     * Finds the root index of the given index
     *
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
     * Finds the shortest path to the center most business using Dijkstra's
     * Algorithm
     *
     * @param source The starting Business
     * @return
     */
    public ArrayList<Integer> shortestPath(String source) {
        int clustIndex = 0;
        int srcIndex = 0;
        float[] dist = new float[keys.length];
        int[] prev = new int[keys.length];
        boolean[] visited = new boolean[keys.length];
        MinPriorityQueue1 pq = new MinPriorityQueue1();
        for (int i = 0; i < keys.length; i++) {
            prev[i] = -1;
            if (keys[i].equals(source)) {
                srcIndex = i;
                dist[i] = 0;
                pq.add(dist[srcIndex], srcIndex);
            } else {
                dist[i] = Float.POSITIVE_INFINITY;
                pq.add(dist[i], i);
            }
        }
        for (int i = 0; i < 5; i++) {
            if (clusters[i].contains(source)) {
                clustIndex = i;
            }
        }
        while (!pq.isEmpty()) {
            int next = pq.remove();
            if (dist[next] == Float.POSITIVE_INFINITY) {
                break;
            }
            visited[next] = true;
            for (int j = 0; j < neighbors[j].length; j++) {
                float d = dist[next] + DistanceErector.haversineDistance(geoloc[next], geoloc[neighbors[next][j]]);
                if (d < dist[neighbors[next][j]]) {
                    dist[neighbors[next][j]] = d;
                    prev[neighbors[next][j]] = next;
                    pq.reduceDistance(d, neighbors[next][j]);
                }
            }
        }
        int index = srcIndex;
        float mindis = Float.POSITIVE_INFINITY;
        for (int i = 0; i < visited.length; i++) {
            if (visited[i]) {
                float dis = DistanceErector.haversineDistance(geoloc[i], centroids[clustIndex]);
                if (dis < mindis) {
                    mindis = dis;
                    index = i;
                }
            }
        }
        ArrayList<Integer> path = new ArrayList<Integer>();
        int x = index;
        while (x != srcIndex) {
            System.out.println("path: " + Arrays.toString(geoloc[x]));
            path.add(0, x);
            x = prev[x];
        }
        path.add(0, x);
        return path;
    }

    /**
     * Initializes all data and gets path for chosen business and sets up graph
     *
     * @param source Starting business
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Grapher(String source) throws IOException, ClassNotFoundException {
        FileInputStream fi = new FileInputStream(edges);
        ObjectInputStream oi = new ObjectInputStream(fi);
        disjoints = oi.readInt();
        parent = (int[]) oi.readObject();
        keys = (String[]) oi.readObject();
        neighbors = (int[][]) oi.readObject();
        oi.close();
        fi.close();
        System.out.println("Number of disjoint sets: " + disjoints);
        BTree tree = new BTree(BTreekey, BTreeval);
        tree.readExistingTree();
        geoloc = new float[keys.length][2];
        for (int i = 0; i < keys.length; i++) {
            float[] temp = tree.getVal(keys[i]);
            if (temp != null) {
                geoloc[i] = new float[]{temp[0], temp[1]};
            }
        }
        clusters = new ArrayList[5];
        fi = new FileInputStream(clusts);
        oi = new ObjectInputStream(fi);
        centroids = (float[][]) oi.readObject();
        for (int i = 0; i < 5; i++) {
            clusters[i] = (ArrayList) oi.readObject();
        }
        oi.close();
        fi.close();
        ArrayList<Integer> path = shortestPath(source);
        int clustInd = 0;
        for (int i = 0; i < 5; i++) {
            if (clusters[i].contains(source)) {
                clustInd = i;
            }
        }
//        mxGraph graph = new mxGraph();
//        graph.setAllowNegativeCoordinates(true);
//        graph.setAutoOrigin(true);
//        Object parent = graph.getDefaultParent();
//        graph.getModel().beginUpdate();
//        Object[] vertices = new Object[keys.length];
//        for (int i = 0; i < path.size(); i++) {
//            vertices[path.get(i)] = graph.insertVertex(parent, null, "", geoloc[path.get(i)][0],
//                    geoloc[path.get(i)][1], 1, 1, "fillColor=red");
//        }
//        for (int i = 0; i < path.size(); i++) {
//            for (int j = 0; j < 4; j++) {
//                if (!path.contains(neighbors[path.get(i)][j])) {
//                    vertices[neighbors[path.get(i)][j]] = graph.insertVertex(parent, null, "",
//                            geoloc[neighbors[path.get(i)][j]][0], geoloc[neighbors[path.get(i)][j]][1], 1, 1, "fillColor=green");
//                }
//            }
//        }
//        Object cent = graph.insertVertex(parent, null, "", centroids[clustInd][0], centroids[clustInd][1], 5, 5);
        System.out.println("center: " + Arrays.toString(centroids[clustInd]));
//        for (int i = 0; i < path.size(); i++) {
//            graph.insertEdge(parent, null, "", vertices[i], vertices[neighbors[path.get(i)][0]]);
//            graph.insertEdge(parent, null, "", vertices[i], vertices[neighbors[path.get(i)][1]]);
//            graph.insertEdge(parent, null, "", vertices[i], vertices[neighbors[path.get(i)][2]]);
//            graph.insertEdge(parent, null, "", vertices[i], vertices[neighbors[path.get(i)][3]]);
//        }
        System.out.println("closest in set: " + Arrays.toString(geoloc[path.get(path.size() - 1)]));
//        graph.insertEdge(parent, null, "", vertices[path.get(path.size() - 1)], cent);
//        graph.getModel().endUpdate();
//        mxGraphComponent component = new mxGraphComponent(graph);
//        component.setGridVisible(true);
//        component.zoom(1);
//        mxGraphView view = component.getGraph().getView();
//        view.setScale(10);
//        getContentPane().add(component);
        Graph g = new MultiGraph("Path");
        g.addAttribute("ui.antialias");
        g.addAttribute("ui.quality");
        ArrayList<String> added = new ArrayList<String>();
        for (int i = 0; i < path.size(); i++) {
            g.addNode(keys[path.get(i)]);
            added.add(keys[path.get(i)]);
            g.getNode(keys[path.get(i)]).setAttribute("xy", geoloc[path.get(i)][0],
                    geoloc[path.get(i)][1]);
            if (i == 0) {
                g.getNode(keys[path.get(i)]).addAttribute("ui.class", "start");
            } else if (i == path.size() - 1) {
                g.getNode(keys[path.get(i)]).addAttribute("ui.class", "goal");
            } else {
                g.getNode(keys[path.get(i)]).addAttribute("ui.class", "path");
            }
        }
        for (int i = 0; i < path.size(); i++) {
            for (int j = 0; j < 4; j++) {
                if (!added.contains(keys[neighbors[path.get(i)][j]])) {
                    g.addNode(keys[neighbors[path.get(i)][j]]);
                    added.add(keys[neighbors[path.get(i)][j]]);
                    g.getNode(keys[neighbors[path.get(i)][j]]).setAttribute("xy", geoloc[neighbors[path.get(i)][j]][0],
                            geoloc[neighbors[path.get(i)][j]][1]);
                }
            }
        }
        for (int i = 0; i < path.size(); i++) {
            for (int j = 0; j < 4; j++) {
                g.addEdge(keys[path.get(i)] + " " + j, keys[path.get(i)], keys[neighbors[path.get(i)][j]]);
                if (path.contains(neighbors[path.get(i)][j]) && Math.abs(path.indexOf(path.get(i))
                        - path.indexOf(neighbors[path.get(i)][j])) == 1) {
                    g.getEdge(keys[path.get(i)] + " " + j).addAttribute("ui.class", "path");
                }
            }
        }
        //g.addNode("centroid");
        //g.getNode("centroid").setAttribute("xy", centroids[clustInd][0], centroids[clustInd][1]);
        //g.addEdge("lastJump", keys[path.get(path.size() - 1)], "centroid");
        g.addAttribute("ui.stylesheet", GRAPHSTYLE);
        g.display(false);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Enter id:"); //1_3nOM7s9WqnJWTNu2-i8Q FYWN1wneV18bWNgQjJ2GNg He-G7vWjzVUysIKrfNbPUQ
        Scanner kb = new Scanner(System.in);
        Grapher g = new Grapher(kb.nextLine());
        //g.setSize(1920, 1080);
        g.setVisible(false);
    }
}
