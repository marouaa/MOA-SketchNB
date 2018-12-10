/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moa.classifiers.bayes.CMNaiveBayes;

import com.github.javacliparser.FloatOption;
import com.yahoo.labs.samoa.instances.Instance;
import java.util.HashMap;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Measurement;
import moa.core.StringUtils;
/**
 *
 * @author maroua
 */
public class TestCMS11 extends AbstractClassifier implements MultiClassClassifier {

    /**
     * @param args the command line arguments
     */
   private static final long serialVersionUID = 1L;

	public FloatOption deltaOption = new FloatOption("deltaFraction",
			'd',
			"delta.",
			0.1, 0.0, 1.0);

	public FloatOption epsilonOption = new FloatOption("epsilonFraction",
			'e',
			"epsilon.",
			0.01, 0.0, 1.0);
       protected boolean start = true; 
       CountMinSketch sketch ;
       HashMap list;
        
               // System.out.println(NumAtt);
       // list = new HashMap<String, Integer>();
       double error=0.0;
        int NumInst ;
        int NumAtt ;
  
        @Override
	public void resetLearningImpl() {
		//this.observedClassDistribution = new DoubleVector();
		//this.attributeObservers = new AutoExpandVector<AttributeClassObserver>();
		start = true;
	}
        
        
      //CountMinSketch1 sketch = new CountMinSketch1(0.1f,0.01f);
      //CountMinSketch1 sketch = new CountMinSketch1(0.1f,0.1f);  
       
      
     
       //System.out.println(NumAtt);
      //tra
        @Override
    public void trainOnInstanceImpl(Instance inst) {
        		if (start == true) {
			
			 this.start = false;
			  //CM
			 System.out.println("width: "+Math.ceil(Math.exp(1)/this.epsilonOption.getValue())+" depth: "+Math.ceil(Math.log(1.0/this.deltaOption.getValue())));
			 this.sketch = new CountMinSketch((float) this.epsilonOption.getValue(),(float)this.deltaOption.getValue() );
                         this.NumAtt = inst.numAttributes();
                         this.list = new HashMap<String,Integer>();
		}
       this.NumInst +=inst.weight();
            

           String update = "";
          for(int j=0 ; j<this.NumAtt-3 ; j++){
              update = update+inst.stringValue(j);
          }
          this.sketch.setString(update);
         if (!this.list.containsKey(update))
                  this.list.put(update, 1);
          else {
              int val = (int) this.list.get(update);
              this.list.put(update, (val+1));
              
          } 
      
        //  System.out.println((int) list.get(update));

    }
	
    

  /* double error = 1.0;
       for (int i=0 ; i<NumInst ; i++) {
           String chaine = "";
          for(int j=0 ; j<NumAtt-3 ; j++){
              chaine = chaine+data.get(i).stringValue(j) ;
          }
          int count = sketch.getEstimatedCount(chaine);
         // System.out.println(count);
          int countlst;
          if (!list.containsKey(chaine)) {
              countlst= 0;
          }      
          else {
              countlst = (int) list.get(chaine);
             
          }
        error +=  ((count - countlst))/countlst;
                 //  System.out.println((int) list.get(update));
               System.out.println(count);
          System.out.println(countlst);
   
      }
       
      System.out.println(error);
    */

    @Override
    public double[] getVotesForInstance(Instance inst) {
        return doNaiveBayesPrediction(inst);
      
    }

    public double [] doNaiveBayesPrediction(Instance inst) {
      
           String chaine = "";
          for(int j=0 ; j<this.NumAtt-3 ; j++){
              chaine = chaine+inst.stringValue(j);
          }
          int count = this.sketch.getEstimatedCount(chaine);
         // 
          int countlst;
          if (!list.containsKey(chaine)) {
              countlst= 0;
          }      
          else {
              countlst = (int) list.get(chaine);
             // this.list.put(chaine, (countlst-1));
             
          }
        
        this.error+= (count - countlst);
    System.out.println(this.error);
      return null;
	}
    

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
      return null; 
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
         for (int i = 0; i < this.NumInst; i++) {
            StringUtils.appendIndented(out, indent, "Observe the error");
            
            out.append(" = ");
            out.append(this.error);
            
            
            }
            StringUtils.appendNewline(out);
        }
 

    @Override
    public boolean isRandomizable() {
       return false; 
    }
public void manageMemory(int currentByteSize, int maxByteSize) {
		// TODO Auto-generated method stub

	}

    }