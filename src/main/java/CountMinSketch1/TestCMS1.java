/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CountMinSketch1;



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
public class TestCMS1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here
        
        
        BufferedReader reader = new BufferedReader(new FileReader("/home/maroua/Bureau/car.arff"));
        Instances data = new Instances(reader);
        reader.close();
        
        CountMinSketch1 sketch = new CountMinSketch1(0.1f,0.001f);
      //CountMinSketch1 sketch = new CountMinSketch1(0.1f,0.01f);
      //CountMinSketch1 sketch = new CountMinSketch1(0.1f,0.1f);  
        int NumInst = data.size();
        int NumAtt = data.numAttributes();
        HashMap <String, Integer> list;
               // System.out.println(NumAtt);
        list = new HashMap<>();
         
         
        
     
       //System.out.println(NumAtt);
      //tra
      
      for (int i=0 ; i<NumInst ; i++) {
           String update = "";
          for(int j=0 ; j<NumAtt-3 ; j++){
              update = update+data.get(i).stringValue(j) ;
          }
          sketch.setString(update);
          if (!list.containsKey(update))
                  list.put(update, 1);
          else {
              Integer val =  list.get(update);
              list.put(update, (val+1));
              
          }
        //  System.out.println((int) list.get(update));
                  
      }
      //test
   /*  double error=0.0;
      for (int i=0 ; i<NumInst ; i++) {
           String chaine = "";
            
           
          for(int j=0 ; j<NumAtt-3 ; j++)    chaine = chaine+data.get(i).stringValue(j) ;
          
          int count = sketch.getEstimatedCount(chaine);
          int count1= 0;
          for (int k=0 ; k<NumInst ; k++) {
           String chaine1 = "";
            
          for(int l=0 ; l<NumAtt-3 ; l++)    chaine1 = chaine+data.get(k).stringValue(l) ;
            if (chaine1.equals(chaine)) count1 = count1+1;
          
      }
          error +=  ((count - count1)*100)/count1;
      
    }
           System.out.println(error);
*/

  
       for (int i=0 ; i<NumInst ; i++) {
           String chaine = "";
          for(int j=0 ; j<NumAtt-3 ; j++){
              chaine = chaine+data.get(i).stringValue(j) ;
          }
          int count = sketch.getEstimatedCount(chaine);
         // System.out.println(count);
        Integer countlst;
        double error =0.0;
              if (!list.containsKey(chaine)) 
                  countlst= 0;
              else 
                  countlst =  list.get(chaine);
              
        error+= Math.abs((double)count - countlst.doubleValue())/countlst.doubleValue();
                 //  System.out.println((int) list.get(update));
        System.out.println(error);
      }
       
     
    
    }
}