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

public class SketchNB extends AbstractClassifier implements MultiClassClassifier {
// THis class is about to fix the width of the sketch which will allow us to fix thhe width according to the accuracy 
   
	private static final long serialVersionUID = 1L;

        public IntOption streamsize = new IntOption("Strsize", 
                  'N', 
                  "size", 100000) ;
        
          
	public FloatOption deltaOption = new FloatOption("deltaFraction",
			'd',
			"delta.",
			0.1, 0.0, 1.0);

	public FloatOption epsilonOption = new FloatOption("epsilonFraction",
			'e',
			"epsilon.",
			0.01, 0.0, 1.0);
        
        public IntOption wid = new IntOption("width",
			'w',
			"width.",
			100);
      
	//protected double[] observedClassDistribution;
	protected double[] observedClassDistribution;
	protected boolean start = true;
	

	protected CMSketch cmsketch; 
       
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
                    this.start = false;
			
                    this.cmsketch = new CMSketch(this.wid.getValue(), (int) Math.log(1.0/(1.0-Math.pow((1.0-this.deltaOption.getValue()), 
                            (1.0/((double)inst.numAttributes()-1.0))))));
        	    System.out.println(this.cmsketch.depth);
                    System.out.println(this.cmsketch.width);
                }
               
		this.observedClassDistribution[(int) inst.classValue()] += inst.weight();
              
		for (int i = 0; i < inst.numAttributes() - 1; i++) {
                    //attInd,attVal,classVal
		this.cmsketch.update(Integer.parseInt(""+(i+1)+(int)inst.value(i)+(int)inst.classValue()));
                    
                   // System.out.println(inst.value(i));
                    /*   String updateS = Integer.toString(i)+
                                         Double.toString(inst.value(i))+
                                         Double.toString(inst.classValue());
			this.cmsketch.setString(updateS); 
                        */
                   
			//this.attributeObserversSum.set((int)inst.classValue(),inst.weight());
           
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
			votes[classIndex] =  this.observedClassDistribution[classIndex]
					/ Arrays.stream(this.observedClassDistribution).sum();
			for (int attIndex = 0; attIndex < inst.numAttributes() - 1; attIndex++) {
			
                            votes[classIndex] *=((double) cmsketch.getEstimation(Integer.parseInt(""+(attIndex+1)+(int)inst.value(attIndex)+classIndex)))
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
