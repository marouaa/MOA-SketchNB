
package moa.classifiers.bayes.TestCMS4alb;
import com.github.javacliparser.FloatOption;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.MultiClassClassifier;
import moa.core.Measurement;
import com.yahoo.labs.samoa.instances.Instance;
import java.util.Arrays;

public class GMSketch extends AbstractClassifier implements MultiClassClassifier {
	private static final long serialVersionUID = 1L;

       
	public FloatOption deltaOption = new FloatOption("deltaFraction",
			'd',
			"delta.",
			0.1, 0.0, 1.0);

	public FloatOption epsilonOption = new FloatOption("epsilonFraction",
			'e',
			"epsilon.",
			0.01, 0.0, 1.0);
        
        public FloatOption alpha = new FloatOption("MinProb",
			'p',
			"prob.",
			0.5);
      
	 protected double[] observedClassDistribution;
	protected boolean start = true;
	

	protected CMSketch [] cmsketch1; 
        protected CMSketch [] cmsketch2; 
       
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
			//create 2K-1 CMsketch 
                    
                    	 this.cmsketch1= new CMSketch[inst.numAttributes()-1];
                         this.cmsketch2= new CMSketch[inst.numAttributes()-2];
			 for (int i=0; i<inst.numAttributes()-1; i++){
				this.cmsketch1[i] = new CMSketch((int) Math.ceil(Math.exp(1)/(this.epsilonOption.getValue()*this.alpha.getValue())),(int) Math.ceil(Math.log((inst.numAttributes()-1)/this.deltaOption.getValue() )));
			 }
                         for (int i=0; i<inst.numAttributes()-2; i++){
				this.cmsketch2[i] = new CMSketch((int) Math.ceil(Math.exp(1)/(this.epsilonOption.getValue()*this.alpha.getValue())),(int) Math.ceil(Math.log((inst.numAttributes()-1)/this.deltaOption.getValue() )));
			 }
        	    System.out.println(this.cmsketch1[1].depth);
                    System.out.println(this.cmsketch1[1].width);
                }
               
		this.observedClassDistribution[(int) inst.classValue()] += inst.weight();
		for (int i = 0; i < inst.numAttributes() - 1 ; i++) {
                    
                        this.cmsketch1[i].update(Integer.parseInt(""+(i+1)+(int)inst.value(i)));
                 }
                for (int i = 0; i < inst.numAttributes() - 2 ; i++) {
                        this.cmsketch2[i].update(Integer.parseInt(
                                ""+(i+1)+(int)inst.value(i)+(int)inst.classValue()));
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
					
			for (int attIndex = 0; attIndex < inst.numAttributes() - 2; attIndex++) {
			
                         votes[classIndex] *= ((double) cmsketch2[attIndex].getEstimation(Integer.valueOf(""+(attIndex+1)+(int)inst.value(attIndex)+classIndex)))  
                                 /((double) cmsketch1[attIndex].getEstimation(Integer.valueOf(""+(attIndex+1)+(int)inst.value(attIndex))));
                                                    
				}
                }
		// TODO: need logic to prevent underflow?
		return votes;
	}

	public void manageMemory(int currentByteSize, int maxByteSize) {
		// TODO Auto-generated method stub

	}

}
