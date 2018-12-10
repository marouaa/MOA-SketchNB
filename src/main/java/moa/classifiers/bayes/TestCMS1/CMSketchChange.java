package moa.classifiers.bayes.TestCMS1;
import moa.classifiers.core.driftdetection.ADWIN;
import moa.classifiers.core.driftdetection.DDM;
//import moa.classifiers.core.driftdetection.PageHinkleyDM;

public class CMSketchChange { 

	public final int MOD = 2147483647;
	public final int HL = 31;
	public final int KK = 17;
	public final int JJ = 10;
	public final int R1 = 5;
	public final int R2 = 3;

  	protected int depth;
 	protected int width;
 	protected int count;
	//protected int[][] counts;
	protected int[] hasha;
	protected int[] hashb;
        protected DDM [][] DDMErr;
	//private Random rnd;

	public CMSketchChange(int width, int depth){
		this.depth = depth;
		this.width = width;
		this.count = 0;
                this.DDMErr = new DDM[depth][width];
                this.hasha = new int[depth];
		this.hashb = new int[depth];
                this.initRandomGenerator();
                for (int i = 0; i < depth; i++) {
                    this.hasha[i] = randomNumber() & MOD;// pick the hash functions
                    this.hashb[i] = randomNumber() & MOD;
                    for (int j = 0; j < width; j++) {
                            this.DDMErr[i][j] = new DDM();
                     }   
                 }
              
	}

	public void update(int item){
		
		for (int i=1;i<=this.depth;i++){
                    int pos = hash31(this.hasha[i - 1],this.hashb[i - 1],item) % this.width;
                    int j = 1;
                    while (j <= this.width) { //add 1 to the cells concerned and zero to the rest
                         if (j != pos)
                            this.DDMErr[i - 1][j - 1].input(0.0);
                         else 
                            this.DDMErr[i - 1][j - 1].input(1.0);
                         j++;
                    }
                }
        }
        
        
	public double getEstimation(int query){
		// return an estimate of the count of an item by taking the minimum
		double ans = this.DDMErr[0][hash31(this.hasha[0],this.hashb[0],query) % this.width].getEstimation();
		for (int j=1;j<this.depth;j++){
			ans = Math.min(ans,this.DDMErr[j][hash31(this.hasha[j],this.hashb[j],query) % this.width].getEstimation());
		}
		// this can be done more efficiently if the width is a power of two
		return ans;
	}


	//HASH FUNCTIONS
	private int hash31(long a, long b, long x){
		//long long (C) -> long (Java)   long (C) -> int (Java)
		long result; 

		// return a hash of x using a and b mod (2^31 - 1)
		// may need to do another mod afterwards, or drop high bits
		// depending on d, number of bad guys
		// 2^31 - 1 = 2147483647

		//  result = ((long long) a)*((long long) x)+((long long) b);
		result=(a * x) + b;
		result = ((result >> HL) + result) & MOD;
		  
		return (int) result;
	}


	//RANDOM GENERATOR FUNCTIONS
	protected int[] randbuffer= new int[KK];
	protected int r_p1 = 0;
	protected int r_p2 = 0;
	// returns a pseudo-random long integer.  Initialise the generator
	private void initRandomGenerator() {
		RanrotAInit(1);
		//rnd = new Random();
	}	
	private int randomNumber() {
		//return rnd.nextInt();
		return ran3();
	}
	private int rotl (int x, int r) {
  		return (x << r) | (x >>> (4*8-r)); //sizeof(x)
	}
	/* this function initializes the random number generator.      */
	/* Must be called before the first call to RanrotA or iRanrotA */
	private void RanrotAInit (int seed) {

	  /* put semi-random numbers into the buffer */
	  for (int i=0; i<KK; i++) {
	    this.randbuffer[i] = seed;
	    seed = rotl(seed,5) + 97;
	  }

	  /* initialize pointers to circular buffer */
	  this.r_p1 = 0; 
	  this.r_p2 = JJ;

	  /* randomize */
	  for (int i = 0;  i < 300;  i++) 
		ran3();

	}
	/* returns some random bits */
	private int ran3() {
	  /* generate next random number */

	  int x = this.randbuffer[this.r_p1] = rotl(this.randbuffer[this.r_p2], R1) 
	    +  rotl(this.randbuffer[this.r_p1], R2);
	  /* rotate list pointers */
	  if (--this.r_p1 < 0) this.r_p1 = KK - 1;
	  if (--this.r_p2 < 0) this.r_p2 = KK - 1;
	  /* conversion to float */
	  return x;
}

}