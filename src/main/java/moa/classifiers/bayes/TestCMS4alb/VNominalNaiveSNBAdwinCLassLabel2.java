/*
 *    SketchBasedNaiveBayes.java
 *    Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 *    @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 */
package moa.classifiers.bayes.TestCMS4alb;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Measurement;
import com.yahoo.labs.samoa.instances.Instance;
import java.util.Arrays;
import moa.classifiers.core.driftdetection.ADWIN;
import moa.core.AutoExpandVector;
import moa.core.Utils;

/*
deals with nominal and numeric attribute values
*/

public  class VNominalNaiveSNBAdwinCLassLabel2 extends AbstractClassifier implements MultiClassClassifier {

   
	private static final long serialVersionUID = 1L;

        public IntOption wid = new IntOption("width", 
                  'w', 
                  "widt", 100) ;
       
          
	public FloatOption deltaOption = new FloatOption("deltaFraction",
			'd',
			"delta.",
			0.1, 0.0, 1.0);

	/*public FloatOption epsilonOption = new FloatOption("epsilonFraction",
			'e',
			"epsilon.",
			0.01, 0.0, 1.0);
        */
	@SuppressWarnings("hiding")
	public static final String classifierPurposeString = "Naive Bayes classifier: performs classic bayesian prediction while making naive assumption that all inputs are independent.";

	protected double[] observedClassDistribution;
	
        protected boolean start = true;
	
    
        protected CMSketch cmsketch; 
      
       
        protected ADWIN AdwinErr;
      
	@Override
	public void resetLearningImpl() {
		start = true;
	}

	@Override
	public void trainOnInstanceImpl(Instance inst) {
            
		if (start == true) {
			 this.observedClassDistribution = new double[inst.numClasses()]; 
			 this.start = false;
                  
                        this.cmsketch = new CMSketch(this.wid.getValue(), (int) Math.log(1.0/(1.0-Math.pow((1.0-this.deltaOption.getValue()), 
                            (1.0/((double)inst.numAttributes()-1.0))))));
		}
                this.observedClassDistribution[(int) inst.classValue()] += inst.weight();
                
               //System.out.println(this.NumAtt);
               //System.out.println(this.numClasses);
             
                          
                for (int i = 0; i < inst.numAttributes() - 1; i++) {
                   
                    this.cmsketch.update(Integer.parseInt(""+(i+1)+(int)inst.value(i)+(int)inst.classValue()));
                   
                        
                }
                
                int trueClass = (int) inst.classValue();
                int ClassPrediction = Utils.maxIndex(this.getVotesForInstance(inst));
                boolean Change = false;
                boolean Correct = (trueClass == ClassPrediction);
                if (this.AdwinErr == null) {
                    this.AdwinErr = new ADWIN();
                } 
                double ErrEstim = this.AdwinErr.getEstimation();
                if (this.AdwinErr.setInput(Correct ? 0.0 : 1.0)) {
                     if (this.AdwinErr.getEstimation() > ErrEstim) {
                         Change = true;
                        }
                    // System.out.println("no change is detected");
                }
                
                
                if (Change) {
                  //reset ?   
                    this.AdwinErr = new ADWIN();
                    this.cmsketch = new CMSketch(this.wid.getValue(), (int) Math.ceil(Math.log(1.0/this.deltaOption.getValue() )));
                  this.observedClassDistribution = new double[inst.numClasses()];
                
                } 
                
             
                
	}
	

	
        @Override
	public double[] getVotesForInstance(Instance inst) {
		return doNaiveBayesPrediction(inst);
	}


	@Override
	protected Measurement[] getModelMeasurementsImpl() {
		return null;
	}

	@Override
	public void getModelDescription(StringBuilder out, int indent) {

	}

        @Override
	public boolean isRandomizable() {
		return false;
	}


	public double[] doNaiveBayesPrediction(Instance inst) {
     
		double[] votes = new double[inst.numClasses()];
		if (this.start == true)
				return votes; 
		for (int classIndex = 0; classIndex < votes.length; classIndex++) {
			votes[classIndex] = this.observedClassDistribution[classIndex]
					/ Arrays.stream(this.observedClassDistribution).sum();
			for (int attIndex = 0; attIndex < inst.numAttributes() - 1; attIndex++) {
                            
				//int instAttIndex = modelAttIndexToInstanceAttIndex(attIndex,inst);
                            votes[classIndex] *= (this.cmsketch.getEstimation(Integer.valueOf(""+(attIndex+1)+(int)inst.value(attIndex)+classIndex)))
                                                    / this.observedClassDistribution[classIndex];
                        }
                }
        // TODO: need logic to prevent underflow?
	return votes;
        }	
		
      
	public void manageMemory(int currentByteSize, int maxByteSize) {
		// TODO Auto-generated method stub

	}

}

/*
String countM = Integer.toString(attIndex)+
                                         Double.toString(classIndex)+ 'M';
                                String countV  = Integer.toString(attIndex)+
                                         Double.toString(classIndex)+ 'V';
                                
                                double stdDev = Math.sqrt(this.cmsketch.getEstimatedCount(countV)/this.observedClassSum-1.0);
                                double prob = 0;
                                if (stdDev>0.0) {
                                    double diff = inst.value(attIndex) - this.cmsketch.getEstimatedCount(countM);
                                    prob = 1.0/(Math.sqrt(2*Math.PI)*stdDev)
                                          *Math.exp(-(diff*diff/2*stdDev*stdDev)) ;
                                }
                                else 
                                    if (inst.value(attIndex)==this.cmsketch.getEstimatedCount(countM)) 
                                        prob = 1.0;
                                    else prob = 0.0;
                                        
                                   
                                votes[classIndex] *= prob;
*/