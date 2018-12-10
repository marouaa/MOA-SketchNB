/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moa.classifiers.bayes.TestCMS1;
import weka.core.Instances;
 import java.io.BufferedReader;
import java.io.FileNotFoundException;
 import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author mbahri
 */
public class testouu {
 
     public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here
        
        
        BufferedReader reader = new BufferedReader(new FileReader("/home/mbahri/Bureau/car.arff"));
        Instances data = new Instances(reader);
        reader.close();
     
        double Var=0.0;
        double Mean=0.0;
        final double NORMAL_CONSTANT = Math.sqrt(2 * Math.PI);
        
        int NumInst = data.size();
        int NumAtt = data.numAttributes();

        double sum = 0.0;
        double temp = 0.0;
        
        
      for (int i=0 ; i<NumInst;i++)  sum+= data.get(i).value(0);
      
      for (int i=0 ; i<NumInst;i++) temp+= (data.get(i).value(0) - sum/NumInst)*(data.get(i).value(0) - sum/NumInst);
    
        System.out.println("la moyenne");
        System.out.println(sum/NumInst);
        System.out.println("std deviation");
        System.out.println(Math.sqrt(Var/(NumInst-1.0)));

      double count = 0.0;
      for (int i=0 ; i<NumInst ; i++) {
          count += 1.0;    
          double lastMean = Mean;
          double lastVar = Var;
          Mean+= (data.get(i).value(0)-lastMean)/count;
          Var+= (data.get(i).value(0)-lastMean)*(data.get(i).value(0) - Mean); 
                            
          /*  System.out.println(count);
          System.out.println("afichage de moyenne et variance");
          System.out.println( Mean);
         
      if (count>1.0)
         System.out.println( Var/(count-1.0));
      else 
         System.out.println(0.0);
           */
        if (count > 1.0){
            double stdDev = Math.sqrt(Var/(count-1.0));
            if (stdDev > 0.0) {
                double diff = data.get(i).value(0) - Mean;
                 System.out.println ((1.0 / (NORMAL_CONSTANT * stdDev))
                        * Math.exp(-(diff * diff / (2.0 * stdDev * stdDev))));
            }
             System.out.println(data.get(i).value(0) == Mean ? 1.0 : 0.0);
          }
         System.out.println(0.0);
     }
      
      
      }
      
      
     
    
    }
