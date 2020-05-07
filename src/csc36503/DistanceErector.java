/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc36503;

/**
 * A calculator class for the calculation of spherical distance
 * @author Vincent
 */
public class DistanceErector {
    /**
     * Uses the Haversine formula to calculate spherical distance in meters
     * @param a The first array point (latitude,longitude)
     * @param b The second array point (latitude,longitude)
     * @return 
     */
    public static float haversineDistance(float[] a, float[] b) {
        double radius = 6371000;
        double[] aR = {Math.toRadians(a[0]),Math.toRadians(a[1])};
        double[] bR = {Math.toRadians(b[0]),Math.toRadians(b[1])};
        double temp = hav(bR[0]-aR[0])+(Math.cos(aR[0])*Math.cos(bR[0])*hav(bR[1]-aR[1]));
        return (float)(Math.asin(Math.sqrt(temp))*2*radius);
    }
    /**
     * The Haversine function
     * @param theta The input
     * @return 
     */
    private static double hav(double theta) {
        return ((1 - Math.cos(theta))/2);
    }
}
