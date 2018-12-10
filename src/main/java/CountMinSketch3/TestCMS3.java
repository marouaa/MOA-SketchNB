/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CountMinSketch3;



 import weka.core.Instances;
 import java.io.BufferedReader;
import java.io.FileNotFoundException;
 import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author maroua
 */
public class TestCMS3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here
      BufferedReader reader = new BufferedReader(new FileReader("/home/maroua/Bureau/car.arff"));
        Instances data = new Instances(reader);
        reader.close();
        
        CountMinSketch3 sketch = new CountMinSketch3((int)Math.ceil(Math.exp(1)/0.01),(int)Math.ceil(Math.log(1.0/0.01)));
      //CountMinSketch1 sketch = new CountMinSketch1(0.1f,0.01f);
      //CountMinSketch1 sketch = new CountMinSketch1(0.1f,0.1f);  
        int NumInst = data.size();
        int NumAtt = data.numAttributes();
        HashMap <Integer, Integer> list;
               // System.out.println(NumAtt);
        list = new HashMap<>();
         
         
        
     
       //System.out.println(NumAtt);
      //tra
      
      for (int i=0 ; i<NumInst ; i++) {
           
          for(int j=0 ; j<NumAtt-2 ; j++){
            sketch.update(Integer.valueOf(""+(j+1)+(int)data.get(i).value(j)), 1);
          
          if (!list.containsKey(Integer.valueOf(""+(j+1)+(int)data.get(i).value(j))))
                  list.put(Integer.valueOf(""+(j+1)+(int)data.get(i).value(j)), 1);
          else {
              Integer val =  list.get(Integer.valueOf(""+(j+1)+(int)data.get(i).value(j)));
              list.put(Integer.valueOf(""+(j+1)+(int)data.get(i).value(j)), (val+1));
              
          }
          }
        
        //  System.out.println((int) list.get(update));
                  
      }
    
 
        double error =0.0;
       for (int i=0 ; i<NumInst ; i++) {
         
          for(int j=0 ; j<NumAtt-2 ; j++){
          int count = sketch.getEstimation(Integer.valueOf(""+(j+1)+(int)data.get(i).value(j)));
         // System.out.println(count);
        Integer countlst;
        
              if (!list.containsKey(Integer.valueOf(""+(j+1)+(int)data.get(i).value(j))) )
                  countlst= 0;
              else 
                  countlst =  list.get(Integer.valueOf(""+(j+1)+(int)data.get(i).value(j)));
              
        error+= Math.abs((double)count - countlst.doubleValue())/countlst.doubleValue();
                 //  System.out.println((int) list.get(update));
           
        System.out.println(error);
        System.out.println("ici"+(error*100/(NumInst*(NumAtt-2))));
          }
      }
       
     
    
    }
}
