 
package moa.classifiers.bayes.CMNaiveBayes;

import com.github.javacliparser.FloatOption;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Measurement;
import com.yahoo.labs.samoa.instances.Instance;
/*
deals with nominal attribute values, stream LED
*/

public class CMNaiveBayes extends AbstractClassifier implements MultiClassClassifier {

   
	private static final long serialVersionUID = 1L;

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

	protected CountMinSketch[] cmsketch; 
	
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
			 //this.attributeObservers = new double[inst.numAttributes()][inst.numClasses()][NUMVALUESATTRIBUTE];
			 this.attributeObserversSum = new double[inst.numAttributes()][inst.numClasses()];
			 this.observedClassSum  = 0.0;
			 this.start = false;
			  //CM
			 System.out.println("width: "+Math.ceil(Math.exp(1)/this.epsilonOption.getValue())+" depth: "+Math.ceil(Math.log(1.0/this.deltaOption.getValue())));
			 this.numClasses = inst.numClasses();
			 this.cmsketch = new CountMinSketch[this.numClasses];
			 for (int i=0; i<this.numClasses; i++){
				this.cmsketch[i] = new CountMinSketch((int) Math.ceil(Math.exp(1)/this.epsilonOption.getValue()),(int) Math.ceil(Math.log(1.0/this.deltaOption.getValue() )));
			 }
		}
		this.observedClassDistribution[(int) inst.classValue()] += inst.weight();
		this.observedClassSum +=inst.weight();
		for (int i = 0; i < inst.numAttributes() - 1; i++) {
			//int instAttIndex = modelAttIndexToInstanceAttIndex(i, inst);
			//this.attributeObservers[i][(int)inst.classValue()][(int)inst.value(i)] +=inst.weight();
			 String updateS = Integer.toString(i)+
                                         Double.toString(inst.value(i));
                        this.cmsketch[(int)inst.classValue()].setString(updateS); //? int weight
			this.attributeObserversSum[i][(int)inst.classValue()] +=inst.weight();
		}
	}

	protected int key(int att, int val){
	 	return att*NUMVALUESATTRIBUTE+val;
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
                                String updateS = Integer.toString(attIndex)+
                                         Double.toString(inst.value(attIndex));
				votes[classIndex] *= ((double) cmsketch[classIndex].getEstimatedCount(updateS))
						//this.attributeObservers[attIndex][classIndex][(int)inst.value(attIndex)]
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
