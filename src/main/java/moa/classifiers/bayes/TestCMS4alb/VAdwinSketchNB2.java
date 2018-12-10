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


/*
deals with nominal and numeric attribute values
*/

public  class VAdwinSketchNB2 extends AbstractClassifier implements MultiClassClassifier {

   
	private static final long serialVersionUID = 1L;

        
         public IntOption wid = new IntOption("Width", 
                  'w', 
                  "wid", 100) ;
          
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
	
       
        protected CMSketchADwin cmsketch; 
   
        
	@Override
	public void resetLearningImpl() {
		start = true;
	}

	@Override
	public void trainOnInstanceImpl(Instance inst) {
           
        //     int depth = (int) (Math.log(1/(1.0-Math.pow((double)1.0-this.deltaOption.getValue(), (double)1.0/this.NumAtt*this.numClasses)))+Math.log(this.cons.getValue()));
          //   int wid = (int) (Math.exp(1.0)*this.NumAtt*this.streamsize.getValue()/(this.cons.getValue()*(Math.pow((double)this.NumAtt*(double)this.streamsize.getValue()*this.epsilonOption.getValue()+1, 1.0/(double)this.NumAtt)-1)));
                        
		
		if (start == true) {
			 this.observedClassDistribution = new double[inst.numClasses()]; 
			 this.attributeObserversSum = new double[inst.numAttributes()][inst.numClasses()];
			 this.observedClassSum  = 0.0;
			 this.start = false;
                         //this.numClasses = inst.numClasses();
                         //this.NumAtt = inst.numAttributes()-1;
                         //int depth = (int) (Math.log(1/(1.0-Math.pow((double)1.0-this.deltaOption.getValue(), (double)1.0/this.NumAtt*this.numClasses)))+Math.log(this.cons.getValue()));
                         //int wid = (int) (Math.exp(1.0)*this.NumAtt*this.streamsize.getValue()/(this.cons.getValue()*(Math.pow((double)this.NumAtt*(double)this.streamsize.getValue()*this.epsilonOption.getValue()+1, 1.0/(double)this.NumAtt)-1)));
                         this.cmsketch = new CMSketchADwin(this.wid.getValue(),(int) Math.ceil(Math.log(1.0/this.deltaOption.getValue() )));
			
                         System.out.println(this.cmsketch.depth);
                         System.out.println(this.cmsketch.width);  
		}
                this.observedClassDistribution[(int) inst.classValue()] += inst.weight();
		this.observedClassSum +=inst.weight();

                for (int i = 0; i < inst.numAttributes() - 1; i++) {
                 
                    this.cmsketch.update(Integer.valueOf(""+(i+1)+(int)inst.value(i)+(int)inst.classValue()));
                    this.attributeObserversSum[i][(int)inst.classValue()] +=inst.weight();
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
					/ this.observedClassSum;
			for (int attIndex = 0; attIndex < inst.numAttributes() - 1; attIndex++) {
                             votes[classIndex] *=(this.cmsketch.getEstimation(Integer.valueOf(""+(attIndex+1)+(int)inst.value(attIndex)+classIndex)))
                                                    /this.attributeObserversSum[attIndex][classIndex]; 
                        }
                }
        // TODO: need logic to prevent underflow?
	return votes;
        }	
		
      
	public void manageMemory(int currentByteSize, int maxByteSize) {
		// TODO Auto-generated method stub

	}

}