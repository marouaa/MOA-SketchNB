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
package moa.classifiers.bayes.TestCMS1;

import CMSForTestNumericNB.CountMinSketch1;
import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import moa.classifiers.core.attributeclassobservers.AttributeClassObserver;
import moa.classifiers.core.attributeclassobservers.GaussianNumericAttributeClassObserver;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Measurement;
import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.core.driftdetection.ADWIN;
import moa.core.AutoExpandVector;
import moa.core.Utils;

/*
deals with nominal and numeric attribute values
*/

public  class AdaptiveSketchNB extends AbstractClassifier implements MultiClassClassifier {

   
	private static final long serialVersionUID = 1L;

        public IntOption streamsize = new IntOption("Strsize", 
                  'N', 
                  "size", 10000) ;
        
         public IntOption cons = new IntOption("Constant", 
                  'C', 
                  "constant", 1000) ;
          
	public FloatOption deltaOption = new FloatOption("deltaFraction",
			'd',
			"delta.",
			0.1, 0.0, 1.0);

	public FloatOption epsilonOption = new FloatOption("epsilonFraction",
			'e',
			"epsilon.",
			0.01, 0.0, 1.0);

	@SuppressWarnings("hiding")
	public static final String classifierPurposeString = "Naive Bayes classifier: performs classic bayesian prediction while making naive assumption that all inputs are independent.";

	protected double[] observedClassDistribution;
	
        protected double[][] attributeObserversSum;
	
        protected boolean start = true;
	
        protected double observedClassSum = 0;
	
        protected int numClasses;
	
        protected CountMinSketch1 cmsketch; 
        
        protected int NumAtt ;
       
        protected ADWIN AdwinErr;
        
        public boolean ErrorChange = false;
        
       
        protected AutoExpandVector<AttributeClassObserver> attributeObservers;
	@Override
	public void resetLearningImpl() {
		start = true;
	}

	@Override
	public void trainOnInstanceImpl(Instance inst) {
             this.NumAtt = inst.numAttributes()-1;
             this.numClasses = inst.numClasses();
             int depth = (int) (Math.log(1/(1.0-Math.pow((double)1.0-this.deltaOption.getValue(), (double)1.0/this.NumAtt*this.numClasses)))+Math.log(this.cons.getValue()));
             int wid = (int) (Math.exp(1.0)*this.NumAtt*this.streamsize.getValue()/(this.cons.getValue()*(Math.pow((double)this.NumAtt*(double)this.streamsize.getValue()*this.epsilonOption.getValue()+1, 1.0/(double)this.NumAtt)-1)));
                        
		
		if (start == true) {
			 this.observedClassDistribution = new double[inst.numClasses()]; 
			 this.attributeObserversSum = new double[inst.numAttributes()][inst.numClasses()];
			 this.observedClassSum  = 0.0;
			 this.start = false;
                         this.attributeObservers = new AutoExpandVector<AttributeClassObserver>();
                         this.cmsketch = new CountMinSketch1(wid,depth);
			
 
		}
                this.observedClassDistribution[(int) inst.classValue()] += inst.weight();
		this.observedClassSum +=inst.weight();
             

                          
                for (int i = 0; i < inst.numAttributes() - 1; i++) {
                    if (inst.attribute(i).isNominal()) {
                        String updateS = Integer.toString(i)+
                                         Double.toString(inst.value(i))+
                                         Double.toString(inst.classValue());
			this.cmsketch.setString(updateS,inst.weight());
                    }
                    else {
                        AttributeClassObserver obs = this.attributeObservers.get(i);
                        if (obs == null) {
                        obs = new GaussianNumericAttributeClassObserver();
                        this.attributeObservers.set(i, obs);
                        }
                        obs.observeAttributeClass(inst.value(i), (int) inst.classValue(), inst.weight());
                    }  
                    
                this.attributeObserversSum[i][(int)inst.classValue()] +=inst.weight();
                        
                } 
                
                
                int trueClass = (int) inst.classValue();
                int ClassPrediction = Utils.maxIndex(this.getVotesForInstance(inst));
                boolean Correct = (trueClass == ClassPrediction);
                if (this.AdwinErr == null) {
                    this.AdwinErr = new ADWIN();
                }
             //   System.out.println(this.ErrorChange);
                double oldError = this.getErrorEstimation();
                this.ErrorChange = this.AdwinErr.setInput(Correct == true ? 0.0 : 1.0);
                if (this.ErrorChange == true && oldError > this.getErrorEstimation()) {
                 //   System.out.println("No change is detected");
                    this.ErrorChange = false;
                }
                
                if (this.ErrorChange == true) {
                //reset ?   
                    //System.out.println("a change is detected");
                    this.cmsketch = new CountMinSketch1(wid,depth);
                    this.attributeObservers.clear();
                } 
                
	}
	

	
        @Override
	public double[] getVotesForInstance(Instance inst) {
		return doNaiveBayesPrediction(inst);
	}
        
        public double getErrorEstimation() {
            if (this.AdwinErr != null) {
                return this.AdwinErr.getEstimation();
            } else {
                return 0.0;
            }
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
					/ this.observedClassSum;
			for (int attIndex = 0; attIndex < inst.numAttributes() - 1; attIndex++) {
                            if (inst.attribute(attIndex).isNominal()) {
				//int instAttIndex = modelAttIndexToInstanceAttIndex(attIndex,inst);
                                String chaine = Integer.toString(attIndex)+
                                         Double.toString(inst.value(attIndex))+
                                         Double.toString(classIndex);
				votes[classIndex] *= ((double) cmsketch.getEstimatedCount(chaine))
                                                    /this.attributeObserversSum[attIndex][classIndex];
                            }
                            else {
                                 AttributeClassObserver obs = attributeObservers.get(attIndex);
                                 if ((obs != null) && !inst.isMissing(attIndex)) 
                                     
                                 votes[classIndex] *= obs.probabilityOfAttributeValueGivenClass(inst.value(attIndex), classIndex);
                            }
                        }
                }
        // TODO: need logic to prevent underflow?
	return votes;
        }	
		
      
	public void manageMemory(int currentByteSize, int maxByteSize) {
		// TODO Auto-generated method stub

	}

}
 
