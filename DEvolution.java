package DE;
import Functions.*;
import Randoms.*;
import java.io.*;//import input output
import java.util.Random;//import random
import java.util.Collections;
import java.util.Vector;


public class DEvolution
{

	//RandomGenerator randGen = new RandomGenerator(1234541);
	// MersenneTwisterFast random generator
	long SEED;  
	public void setSeed(long SEED){this.SEED = SEED;}
	MersenneTwisterFast randGen = new MersenneTwisterFast(SEED);
	JavaRand randGenJava = new JavaRand();

    Function fun; 
	private String FunName;
	public int 	population;
	private int dimension;
	private double minX,maxX;
	private int maxIter;
	
    public void setFunction(String FunName){this.FunName = FunName;}
	public void setPopulation(int population){this.population = population;}	
	public void setDimension(int dimension,double minX, double maxX){this.dimension = dimension; this.minX = minX;this.maxX = maxX;}
	public void setMaxIteration(int maxIter){this.maxIter = maxIter;}

   // Private stuff
	private int currGen;
	private double fx;
	
	// Helper variable
	private int numr = 3; // for stragey DE/rand-to-best/1/bin
	public void setAlgo(int numr){this.numr = numr;}
	private int[] r;
	
	// Population data
	private double trialCost;
	private double costs[];
	private double trialVector[];
	private double x[];//holding the best cost
	private double currentPopulation[][];
	private double nextPopulation[][];	
	
	
	public void setArrays(){
		costs = new double[population];
		trialVector = new double[dimension];
		x = new double[dimension];//holds best vector
		currentPopulation = new double[population][dimension];
		nextPopulation = new double[population][dimension];
		r = new int[numr];
	}
	
	public double F = 0.7 /* 0.5*/;/** weight factor (default 0.7) */
	public double CR = 0.9 /*1.0*/;/** Crossing over factor (default 0.9) */
		
	/**
	 * variable controlling print out, default value = 0
	 * (0 -> no output, 1 -> print final value, 
	 * 2 -> detailed map of optimization process)
	 */
	public int prin = 1; 

	// implementation of abstract method 
	
	public double[] optimize(){//(MultivariateFunction func, double[] xvec, double tolfx, double tolx)  
	    double returnResult[] = new double[103];
		//f = func;
		//x = xvec;
		
		// Create first generation
		firstGeneration();
       	long startTime = 0;
		long endTime = 0; 
		int iRet = 0;
		boolean fRet = true;
		int convergenceItr = 0; 
		int iter;
		startTime = System.currentTimeMillis();
		//System.out.println(iRet+" "+returnResult[iRet]);
		//stopCondition(fx, x, tolfx, tolx, true);
			
		while (true){
			boolean xHasChanged;
			do{
			    if(currGen%10 ==0){
					returnResult[iRet] = fx; iRet++;
					//System.out.printf("%d > [",currGen);
					//for(int dim=0;dim<dimension;dim++){System.out.printf(" %.2f ",x[dim]);}
					//System.out.printf(" ] > %.3f \n",fx);
				}
				xHasChanged = nextGeneration ();
				if (currGen >= maxIter)//if (maxFun > 0 && numFun > maxFun)
					break;
		
				//if (prin > 1 && currGen % 20 == 0)
					//printStatistics();
			}
			while (!xHasChanged);

			//if (stopCondition(fx, x, tolfx, tolx, false) || (maxFun > 0 && numFun > maxFun))
			if (currGen >= maxIter) break;	
				
			if(fx < 0.0001&& fRet){convergenceItr = currGen; fRet = false;endTime = System.currentTimeMillis();}			
		}
		//if (prin > 0) printStatistics();
		if(convergenceItr == 0) endTime = System.currentTimeMillis();
		double timeTaken = (endTime - startTime) / 1000.0;//System.out.println("Execution time : " + timeTaken + " seconds");
		returnResult[iRet] = fx; iRet++; 
		if(convergenceItr == 0){returnResult[iRet] = currGen; iRet++; }else{returnResult[iRet] = convergenceItr; iRet++;}
		returnResult[iRet] = timeTaken; iRet++; 		
		return returnResult;
	  }	

	private void printStatistics(){
		// Compute mean
		double meanCost = 0.0;
		for (int i = 0; i < population; i++){meanCost += costs[i];}
		meanCost = meanCost/population;
				
		// Compute variance
		double varCost = 0.0;
		for (int i = 0; i < population; i++){
			double tmp = (costs[i]-meanCost);
			varCost += tmp*tmp;
		}
		varCost = varCost/(population-1);
			
		// System.out.println();
		// System.out.println();
		// System.out.println();
		//System.out.println(FunName+" value: " + fx);
		// System.out.println();
		// for (int k = 0; k < dimension; k++)
		// {
			// System.out.println("x[" + k + "] = " + x[k]);
		// }
		// System.out.println();
		// System.out.println("Current Generation: " + currGen);
		// System.out.println("Function evaluations: " + numFun);
		// System.out.println("Populations size (population): " + population);
		// System.out.println("Average value: " + meanCost);
		// System.out.println("Variance: " + varCost);

		// System.out.println("Weight factor (F): " + F);
		// System.out.println("Crossing-over (CR): " + CR);
		// System.out.println();
	}
	
	// Generate starting population
	private void firstGeneration(){
		currGen = 0;
		
		// Construct population random start vectors
		for (int i = 0; i < population; i++){
			for (int j = 0; j < dimension; j++ ){
				// Uniformly distributed sample points
				currentPopulation[i][j] = randGen.randVal(minX,maxX) ;
			}//for dimension
			costs[i] = evaluate(currentPopulation[i]);
		}//for population
		//numFun += population;
		
		findSmallestCost();
	}
	
	
  public double evaluate(double ch[]){ return fun.computeteFunction(ch,FunName);}
	
  // check whether a parameter is out of range
  private double checkBounds(double param, int numParam){
	if (param < minX){return minX;}
	else if (param > maxX){return maxX;}
	else{return param;}
  }
	
	// Generate next generation
	private boolean nextGeneration(){
		boolean updateFlag = false;
		int best = 0; // to avoid compiler complaints
		double[][] swap;
		
		currGen++;
		
		// Loop through all population vectors
		for (int r0 = 0; r0 < population; r0++){
			// Choose ri so that r0 != r[1] != r[2] != r[3] != r[4] ...
		
			r[0] = r0;			
			for (int k = 1; k < numr; k++){
				r[k] = randomInteger(population-k);
				for (int l = 0; l < k; l++){
					if (r[k] >= r[l]){
						r[k]++;
					}
				}
			}
			
			copy(trialVector, currentPopulation[r0]);
			int n = randomInteger(dimension); 
			for (int i = 0; i < dimension; i++){ // perform binomial trials
			
				// change at least one parameter
				if (Math.random() < CR || i == n){                       
					// DE/rand-to-best/1/bin
					// (change to 'numr=3' in constructor when using this strategy)
					trialVector[n] = trialVector[n] +
						F*(x[n] - trialVector[n]) +
						F*(currentPopulation[r[1]][n] - currentPopulation[r[2]][n]);

					//DE/rand-to-best/2/bin
					//double K = rng.nextDouble();
					//trialVector[n] = trialVector[n] +
					//	K*(x[n] - trialVector[n]) +
					//	F*(currentPopulation[r[1]][n] - currentPopulation[r[2]][n]);
					
							     
	       				// DE/best/2/bin
					// (change to 'numr=5' in constructor when using this strategy)
	       				//trialVector[n] = x[n] + 
		     			//	 (currentPopulation[r[1]][n]+currentPopulation[r[2]][n]
					//	 -currentPopulation[r[3]][n]-currentPopulation[r[4]][n])*F;
				}
				n = (n+1) % dimension;
			}

			// make sure that trial vector obeys boundaries
			for (int i = 0; i < dimension; i++)	{
				trialVector[i] = checkBounds(trialVector[i], i);
			}
			
			// Test this choice
			trialCost = evaluate(trialVector);
			if (trialCost < costs[r0]){
				// Better than old vector
				costs[r0] = trialCost;
				copy(nextPopulation[r0],trialVector);
				
				// Check for new best vector
				if (trialCost < fx){
					fx = trialCost;
					best = r0;
					updateFlag = true;
				}//if 
			}//if
			else{
				// Keep old vector
				copy(nextPopulation[r0],currentPopulation[r0]);
			}			
		}
		//numFun += population;
		
		// Update best vector
		if (updateFlag){
			copy(x, nextPopulation[best]);
		}
		
		// Switch pointers
		swap = currentPopulation;
		currentPopulation = nextPopulation;
		nextPopulation = swap;
		
		return updateFlag;
	}
	
	// Determine vector with smallest cost in current population
	private void findSmallestCost(){    
		int best = 0;
		fx = costs[0];
		for (int i = 1; i < population; i++){
			if (costs[i] < fx){
				fx = costs[i];
				best = i;
			}
		}
		copy(x,currentPopulation[best]);
	}
	
	//copy vector a into b
	public void copy(double a[], double b[]){for (int j = 0; j < dimension; j++ ){a[j] = b[j];}}
	
	
	// draw random integer in the range from 0 to n-1
	private int randomInteger(int n){ return (int)(Math.random()*(n-1));}
	
	public static void main(String args[]){
		DEvolution de = new DEvolution();
		System.out.println(de.FunName+" value: " + de.optimize());
	}
}
