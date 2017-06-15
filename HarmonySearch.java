package HS;
import Functions.*;
import Randoms.*;
public class HarmonySearch {

    Function fun;
    private String FunName;

	private int NVAR;
	private int HMS;
	private int maxIter;
	private double PAR;
	private double BW;
	private double HMCR;
	private double low[];
	private double high[];
	private double NCHV[];
	private double bestFitHistory[];
	private double bestHarmony[];
	private double worstFitHistory[];
	private double HM[][];
	static int generation = 1;
	private boolean terminationCriteria = true;

	//RandomGenerator randGen = new RandomGenerator(1234);
    // MersenneTwisterFast random generator
	long SEED;  
	public void setSeed(long SEED){this.SEED = SEED;}
	MersenneTwisterFast randGen = new MersenneTwisterFast(SEED);
	JavaRand randGenJava = new JavaRand();
	
	public void setFunction(String FunName){this.FunName = FunName;}
	public void setMaxIteration(int maxIter){this.maxIter = maxIter;}
	public void setNVAR(int NVAR){this.NVAR = NVAR;}
	public void setPAR(double PAR){this.PAR = PAR;}
	public void setHMCR(double HMCR){this.HMCR = HMCR;}
	public void setBW(double BW){this.BW = BW;}
	public void setHMS(int HMS){this.HMS = HMS;}

	public void setArrays(){
		low = new double[NVAR];
		high = new double[NVAR];
		NCHV = new double[NVAR];
		bestHarmony = new double[NVAR + 1];
		bestFitHistory = new double[maxIter + 1];
		worstFitHistory = new double[maxIter + 1];
		HM = new double[HMS][NVAR + 1];
	}

	public void setBounds(double low[], double high[]) {
		setArrays();
		this.low = low;
		this.high = high;
	}

	public void initiator() {
		int i;
		double curFit;

		for (i = 0; i < HMS; i++) {
			for (int j = 0; j < NVAR; j++) {
				HM[i][j] = randGen.randVal(low[j], high[j]);
				//System.out.print(HM[i][j] + "  ");
			}
			curFit = fitness(HM[i]);
			HM[i][NVAR] = curFit; // the fitness is stored in the last column
									// of HM
			//System.out.print(HM[i][NVAR] + "  ");
			//System.out.println();
			updateHarmonyMemory(curFit);
		}
	}

	public double fitness(double x[]) {return fun.computeteFunction(x,FunName); }


	public boolean stopCondition() {
		if (generation > maxIter)
			terminationCriteria = false;
			
			//System.out.println("OK"+terminationCriteria);
		return terminationCriteria;
	}

	public void updateHarmonyMemory(double newFitness) {
		// find worst harmony
		double worst = HM[0][NVAR];
		int worstIndex = 0;
		for (int i = 0; i < HMS; i++)
			if (HM[i][NVAR] > worst) {
				worst = HM[i][NVAR];
				worstIndex = i;
			}
		worstFitHistory[generation] = worst;
		// update harmony
		if (newFitness < worst) {
			for (int k = 0; k < NVAR; k++)
				HM[worstIndex][k] = NCHV[k];
			HM[worstIndex][NVAR] = newFitness;
		}

		// find best harmony
		double best = HM[0][NVAR];
		int bestIndex = 0;
		for (int i = 0; i < HMS; i++)
			if (HM[i][NVAR] < best) {
				best = HM[i][NVAR];
				bestIndex = i;
			}
		bestFitHistory[generation] = best;
		if (generation > 0 && best != bestFitHistory[generation - 1]) {
			for (int k = 0; k < NVAR; k++)
				bestHarmony[k] = HM[bestIndex][k];
			bestHarmony[NVAR] = best;
		}
	}

	private void memoryConsideration(int varIndex) {

		NCHV[varIndex] = HM[randGenJava.randVal(0, HMS - 1)][varIndex];
	}

	private void pitchAdjustment(int varIndex) {

		double rand = randGenJava.ran1();
		double temp = NCHV[varIndex];
		if (rand < 0.5) {
			temp += rand * BW;
			if (temp < high[varIndex])
				NCHV[varIndex] = temp;
		} else {
			temp -= rand * BW;
			if (temp > low[varIndex])
				NCHV[varIndex] = temp;
		}

	}

	private void randomSelection(int varIndex) {

		NCHV[varIndex] = randGenJava.randVal(low[varIndex], high[varIndex]);

	}

	public double[] mainLoop() {
  	    double returnResult[] = new double[103];
		if(generation >= 10){
			generation = 1;
			//System.out.println("Mod "+generation);
		}	

		initiator();
		long startTime = 0;
		long endTime = 0; 
		int iRet = 0;
		boolean fRet = true;
		int convergenceItr = 0; 
		startTime = System.currentTimeMillis();
		int iter = 0;
		//while (stopCondition()) {
		while (iter < maxIter) { 
		    if(iter%10 ==0){
				returnResult[iRet] =  bestHarmony[NVAR]; iRet++; 
				System.out.printf("%d > [",iter);
				for(int dim=0;dim<=NVAR;dim++){System.out.printf(" %.2f ",bestHarmony[dim]);}
				System.out.printf(" ] > %.3f \n",bestHarmony[NVAR]);
			}
			for (int i = 0; i < NVAR; i++) {
				if (randGenJava.ran1() < HMCR) {
					memoryConsideration(i);
					if (randGenJava.ran1() < PAR)
						pitchAdjustment(i);
				} else
					randomSelection(i);
			}
             
			double currentFit;
			currentFit = fitness(NCHV);
			updateHarmonyMemory(currentFit);
			if(bestHarmony[NVAR]<0.0001 && fRet){convergenceItr = iter; fRet = false;endTime = System.currentTimeMillis();break;}
			generation++;
            iter++; 
		}
		if(convergenceItr == 0) endTime = System.currentTimeMillis();
		double timeTaken = (endTime - startTime) / 1000.0;//System.out.println("Execution time : " + timeTaken + " seconds");
		returnResult[iRet] = bestHarmony[NVAR]; iRet++; 
		if(convergenceItr == 0){returnResult[iRet] = iter; iRet++; }else{returnResult[iRet] = convergenceItr; iRet++;}
		returnResult[iRet] = timeTaken; iRet++; 		
		return returnResult;
	}

}
