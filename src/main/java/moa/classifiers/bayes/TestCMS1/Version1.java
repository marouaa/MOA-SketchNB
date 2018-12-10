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
import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Measurement;
import com.yahoo.labs.samoa.instances.Instance;
import moa.core.AutoExpandVector;
/*
deals with nominal and numeric attribute values
*/

public  class Version1 extends AbstractClassifier implements MultiClassClassifier {

   
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
        
        public boolean ErrorChange = false;
        
        public static final double NORMAL_CONSTANT = Math.sqrt(2 * Math.PI);
       
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
                         //this.numClasses = inst.numClasses();
                         //this.NumAtt = inst.numAttributes()-1;
                         //int depth = (int) (Math.log(1/(1.0-Math.pow((double)1.0-this.deltaOption.getValue(), (double)1.0/this.NumAtt*this.numClasses)))+Math.log(this.cons.getValue()));
                         //int wid = (int) (Math.exp(1.0)*this.NumAtt*this.streamsize.getValue()/(this.cons.getValue()*(Math.pow((double)this.NumAtt*(double)this.streamsize.getValue()*this.epsilonOption.getValue()+1, 1.0/(double)this.NumAtt)-1)));
                         this.cmsketch = new CountMinSketch1(wid,depth);
			
                         System.out.println(depth);
                         System.out.println(wid);  
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
                        String updateMean = Integer.toString(i) + Double.toString(inst.classValue()) + "mean";
                        String updateVar = Integer.toString(i) + Double.toString(inst.classValue()) + "var";
                        double lmean = this.cmsketch.getEstimatedCount(updateMean);
                        double lvar = this.cmsketch.getEstimatedCount(updateVar);
                        double newmean =  computeMean(inst.value(i), lmean, this.observedClassSum); 
                        this.cmsketch.setString1(updateMean, newmean);
                        this.cmsketch.setString1(updateVar, computeVar(inst.value(i), lvar, lmean, newmean));
                        //System.out.println(obs.getBestSuggestion());
                    }  
                    
                this.attributeObserversSum[i][(int)inst.classValue()] +=inst.weight();
                        
                } 
  
                
	}
	
           protected double computeMean(double val, double lmean, double numbInst) {
            //this.mean = lmean + (val-lmean)/numbInst;
        return  lmean + (val-lmean)/numbInst;
        }
        
        protected double computeVar(double val,double lvar,double lmean, double nmean) {
            
            //double var = lvar +  (val-lmean)*(val - this.Means.get(ident));
        return lvar + (val-lmean)*(val - nmean);
        }
    

        public double getVariance( double var) {
        return this.observedClassSum > 1.0 ? var/(this.observedClassSum - 1.0)
                : 0.0;
        }
        

        public double getStdDev(double v) {
        return Math.sqrt(getVariance(v));
        } 
        protected double computeProb(double val, double mean, double var) {
          if (this.observedClassSum > 0.0){
            double stdDev =  getStdDev(var);
            if (stdDev > 0.0) {
                double diff = val - mean;
                return (1.0 / (NORMAL_CONSTANT * stdDev))
                        * Math.exp(-(diff * diff / (2.0 * stdDev * stdDev)));
            }
            return val == mean ? 1.0 : 0.0;
          }
        return 0.0;
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
                                 String valMean = Integer.toString(attIndex) + Double.toString(inst.classValue()) + "mean";
                                 String valVar = Integer.toString(attIndex) + Double.toString(inst.classValue()) + "var";
                                 votes[classIndex] *= computeProb(inst.value(attIndex), this.cmsketch.getEstimatedCount(valMean),
                                         this.cmsketch.getEstimatedCount(valVar));
                            //     System.out.println("probability for class " + classIndex);
                             //    System.out.println(votes[classIndex]);
                                                   
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