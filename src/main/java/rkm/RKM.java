package rkm;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import weka.core.Instance;
import weka.core.Instances;

public class RKM
{
    private static int numOfObjects;
    private static int numOfDimensions;
    private static int numOfClusters;
    private static double[][]objects; // complete data
    private static double[][]centroid; // complete data
    
    private static double w_l = 0.85;
    private static double w_u = 0.15;
    private static int maxIter;
    private static double threshold;
    static double distance(double [] x, double [] y)
    {
          double tempDistance = 0.0;
          for( int k = 0; k < numOfDimensions; k++)
          {
                    
                tempDistance = tempDistance + (x[k] - y[k]) * 
                                              (x[k] - y[k]);
          }
          return Math.sqrt(tempDistance);
    }

    public static void initialize(int clstrs, int iter, double thrshd)
    {
        maxIter = iter;
        threshold = thrshd;
        numOfClusters = clstrs;
        centroid = new double [numOfClusters][numOfDimensions];
        for(int i = 0; i < numOfClusters; i++)
        {
            int c = (int) (Math.random() * numOfObjects);
            for(int j = 0; j < numOfDimensions; j++)
            {
                centroid[i][j] = objects[c][j];
            }
        }
    }

    public static void evolve()
    {
        for(int iter = 0; iter < maxIter; iter++)
        {
            Vector [] T = getMembership();
            for(int k = 0; k < numOfClusters; k++)
            {
                int sizeL = 0;
                int sizeU = 0;
                double [] centroidL = new double[numOfDimensions];
                double [] centroidU = new double[numOfDimensions];
                for(int j = 0; j < numOfDimensions; j++)
                {
                    centroidL[j] = 0;
                    centroidU[j] = 0;
                }
                for(int i = 0; i < numOfObjects; i++)
                {
                    if(T[i].contains(k))
                    {
                        double factor;
                        if(T[i].size() == 1)
                        {
                            for(int j = 0; j < numOfDimensions; j++)
                            {
                                centroidL[j] += objects[i][j];
                            }
                            sizeL++;
                        }
                        else
                        {
                            for(int j = 0; j < numOfDimensions; j++)
                            {
                                centroidU[j] += objects[i][j];
                            }
                            sizeU++;
                        }
                    }
                }
                for(int j = 0; j < numOfDimensions; j++)
                {
                    if(sizeL != 0 && sizeU != 0)
                        centroid[k][j] = centroidL[j]*w_l/sizeL
                                        + centroidU[j]*w_u/sizeU;
                    else if(sizeL == 0 && sizeU != 0)
                        centroid[k][j] = centroidU[j]/sizeU;
                    if(sizeL != 0 && sizeU == 0)
                        centroid[k][j] = centroidL[j]/sizeL;
                }
            }
        }
    }

    public static void printClusters()
    {
        Vector [] T = getMembership();
        for(int j = 0; j < numOfClusters; j++)
        {
            System.out.print("Centroid for cluster " + (j+1) + ": ");
            for(int i = 0; i < numOfDimensions; i++)
            {
                System.out.printf("%.2f ", centroid[j][i]);
            }
            System.out.println("\nLower bound for cluster " + (j+1) +":");
            for(int i = 0; i < numOfObjects; i++)
            {
                if(T[i].contains(j) && T[i].size() == 1)
                    System.out.print(i + " " );
            }
            System.out.println("\nBoundary region for cluster " + (j+1) +":");
            for(int i = 0; i < numOfObjects; i++)
            {
                if(T[i].contains(j) && T[i].size() > 1)
                    System.out.print(i + " " );
            }
            System.out.println();
        }
    }

    public static Vector [] getMembership()
    {
        Vector [] T = new Vector[numOfObjects];
        for(int i = 0; i < numOfObjects; i++)
        {
            T[i] = new Vector();
        }
        double error = 0.0;
        double distance = 0.0;
        double min;
        
        double [] objectError = new double[numOfObjects];
        for(int i = 0; i < numOfObjects; i++)
        {   
             double tempDistance[] = new double[numOfClusters];
             tempDistance[0] = distance(objects[i],centroid[0]);
             min = tempDistance[0];
             int closest = 0;
            
             // find the  closest centroid to ith object
             for(int j = 1; j < numOfClusters; j++)
             {   
                tempDistance[j] = distance(objects[i],centroid[j]);
                if ( tempDistance[j] < min)
                {
                    min = tempDistance[j];
                    closest = j;
                }
             }

             T[i].add(closest);
             objectError[i] = tempDistance[closest];
             for(int j = 0; j < numOfClusters; j++)
             {
                if(j != closest && tempDistance[j]/tempDistance[closest] <= threshold)
                {
                    T[i].add(j);
                    objectError[i] += tempDistance[j];
                }
            }
        }
        return T;
    }

    public static void readFileData(Instances fileName)
    {    
           //  Scanner fileScan = new Scanner(new File(fileName));
             numOfObjects = fileName.numInstances(); 
             numOfDimensions = fileName.numAttributes();
           // System.err.println(numOfDimensions);
             objects  = new double[numOfObjects][numOfDimensions];
             
             for (int i = 0; i < numOfObjects; i++) {
                 for( int j = 0; j < numOfDimensions; j++) {
                     objects[i][j] = fileName.get(i).value(j);
                 }
               }
    }
    
    // String v = "/Dropbox/Implementation/RKM/src/rkm/synthetic2010.txt";
    public static void main( String[] args) throws FileNotFoundException, IOException {
             
        BufferedReader reader = new BufferedReader(new FileReader("/home/maroua/moa/moa/src/main/java/rkm/3mill.arff"));
        Instances v = new Instances(reader);
        reader.close();
        
        System.err.println(v.numInstances());
       /* if(v.numInstances() < 4)
        {
            System.err.println("Usage: java RKM dataFile clusters iter threshold");
            System.err.println("Example: java RKM synthetic2010.txt 3 50 1.4");
            System.exit(0);
        }
        */
     
       BufferedReader reader1 = new BufferedReader(new FileReader("/home/maroua/moa/moa/src/main/java/rkm/3mill.arff"));
        Instances v1 = new Instances(reader1);
        v1.clear();
        
        int streamsize = v.numInstances() ;
        //int i = 0;
        while (streamsize>10000) {
           for (int k = 0; k < 10000;k++) {
                v1.add(k,v.get(k));
               }
        readFileData(v1);
        initialize(3,30,1.4);
        evolve();
        printClusters();
        streamsize = streamsize - 10000;
        //i = i+1000;
        v1.clear();
        
       }
    }
}