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

import CountMinSketch1.*;
import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;

import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Measurement;
import com.yahoo.labs.samoa.instances.Instance;
import java.util.HashMap;
/*
deals with nominal attribute values, stream LED
*/

public class CMNaiveBayes1 extends AbstractClassifier implements MultiClassClassifier {

   
	private static final long serialVersionUID = 1L;

        
          public IntOption streamSize = new IntOption("Streamsize", 
                  'N', 
                  "Size", 100000) ;
          
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
	
	private final int NUMVALUESATTRIBUTE = 5;

	protected CountMinSketch1 cmsketch; 
	protected HashMap <String, Integer> list;
       
        protected double error;
        protected int NumAtt ;
	@Override
	public void resetLearningImpl() {
		//this.observedClassDistribution = new DoubleVector();
		//this.attributeObservers = new AutoExpandVector<AttributeClassObserver>();
		start = true;
	}

	@Override
	public void trainOnInstanceImpl(Instance inst) {
		
		if (start == true) {
			 this.observedClassDistribution = new double[inst.numClasses()]; 
			 this.attributeObserversSum = new double[inst.numAttributes()][inst.numClasses()];
			 this.observedClassSum  = 0.0;
			 this.start = false;
			  //CM
			// System.out.println("width: "+Math.ceil(Math.exp(1)/this.epsilonOption.getValue())+" depth: "+Math.ceil(Math.log(1.0/this.deltaOption.getValue())));
                         this.error = 0.0;
                         this.numClasses = inst.numClasses();
                         this.NumAtt = inst.numAttributes()-1;
                         this.list = new HashMap<>();
			this.cmsketch = new CountMinSketch1((1.0-Math.pow(1.0-this.deltaOption.getValue(), 1.0/((double)this.NumAtt*(double)this.numClasses))) ,
                               ((Math.pow((double)this.NumAtt*this.streamSize.getValue()*this.epsilonOption.getValue()+1.0, 1.0/(double)this.NumAtt)-1.0)/((double)this.NumAtt*this.streamSize.getValue())));
                        System.out.println(this.cmsketch.getDepth());
                        System.out.println(this.cmsketch.getWidth());
		}
                this.observedClassDistribution[(int) inst.classValue()] += inst.weight();
		this.observedClassSum +=inst.weight();
              //System.out.println(this.NumAtt);
                //System.out.println(this.NumAtt);
		for (int i = 0; i < inst.numAttributes() - 1; i++) {
			//int instAttIndex = modelAttIndexToInstanceAttIndex(i, inst);
			//this.attributeObservers[i][(int)inst.classValue()][(int)inst.value(i)] +=inst.weight();
                       String updateS = Integer.toString(i)+
                                         Double.toString(inst.value(i))+
                                         Double.toString(inst.classValue());
			this.cmsketch.setString(updateS); 
                        
			this.attributeObserversSum[i][(int)inst.classValue()] +=inst.weight();
                        if (!this.list.containsKey(updateS))
                             this.list.put(updateS, 1);
                        else {
                             Integer val = this.list.get(updateS);
                             this.list.put(updateS, (val+1));
              
          } 
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
				//int instAttIndex = modelAttIndexToInstanceAttIndex(attIndex,inst);
                                String chaine = Integer.toString(attIndex)+
                                         Double.toString(inst.value(attIndex))+
                                         Double.toString(classIndex);
				votes[classIndex] *= ((double) cmsketch.getEstimatedCount(chaine))
                                                    /this.attributeObserversSum[attIndex][classIndex];
						//this.attributeObservers[attIndex][classIndex][(int)inst.value(attIndex)]
				 int count = (int) this.cmsketch.getEstimatedCount(chaine);	
                                 Integer countlst;
                                     if (!list.containsKey(chaine)) 
                                         countlst= 0;
                                     else 
                                         countlst =  list.get(chaine);
                                     // this.list.put(chaine, (countlst-1));
                                   this.error+= Math.abs((double)count - countlst.doubleValue());
                                 //  System.out.println("ici c'est sketch"+count);
                                  // System.out.println("ici c'est liste"+countlst);
				}
                        
                    //     System.out.println(this.observedClassSum);
                      //  System.out.println(this.NumAtt);
                        //System.out.println(votes.length);
                      //   System.out.println("error"+this.error);
                        //  System.out.println("fractional error"+(this.error/(this.observedClassSum*(this.NumAtt-1)*votes.length)));
                          
		
                }
		// TODO: need logic to prevent underflow?
		return votes;
	}
	
			
		
         // 
    
        
        
	

	public void manageMemory(int currentByteSize, int maxByteSize) {
		// TODO Auto-generated method stub

	}

}
