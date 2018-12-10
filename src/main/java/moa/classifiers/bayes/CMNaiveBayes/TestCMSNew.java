/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moa.classifiers.bayes.CMNaiveBayes;

	
import moa.classifiers.Classifier;		
import moa.core.TimingUtils;			
import java.io.IOException;
import com.yahoo.labs.samoa.instances.Instance;
import moa.streams.generators.LEDGenerator;
/**
 *
 * @author maroua
 */
public class TestCMSNew  {

    /**
     * @param args the command line arguments
     */
    public TestCMSNew(){
    }
    
    public void run(int numInstances, boolean isTesting){	
        Classifier learner = new TestCMS11();		              
        LEDGenerator stream = new LEDGenerator();		              
        stream.prepareForUse();		
                learner.setModelContext(stream.getHeader());	
                learner.prepareForUse();		
                int numberSamplesCorrect = 0;		             
                int numberSamples = 0;		               
                boolean preciseCPUTiming = TimingUtils.enablePreciseTiming();		
                long evaluateStartTime = TimingUtils.getNanoCPUTimeOfCurrentThread();		             
                while (stream.hasMoreInstances() && numberSamples < numInstances) {	
                    Instance trainInst = stream.nextInstance().getData();		   
                    if (isTesting) {		                               
                        if (learner.correctlyClassifies(trainInst)){		        
                            numberSamplesCorrect++;		                            
                        }		                       
                    }		                 
                    numberSamples++;		                    
                    learner.trainOnInstance(trainInst);		      
                }		              
                double accuracy = 100.0 * (double) numberSamplesCorrect/ (double) numberSamples;		       
                double time = TimingUtils.nanoTimeToSeconds(TimingUtils.getNanoCPUTimeOfCurrentThread()- evaluateStartTime);		               
                System.out.println(numberSamples + " instances processed with " + accuracy + "% accuracy in "+time+" seconds.");	
    }		
        public static void main(String[] args) throws IOException {		       
            TestCMSNew exp = new TestCMSNew();		             
            exp.run(1000000, true);		
        }
    
   
    }