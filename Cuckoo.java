package Cuckoo;
import Functions.*;
import Randoms.*;
import java.io.*;//import input output
import java.util.Random;//import random
import java.util.Collections;
import java.util.Vector;


public class Cuckoo
{

	//RandomGenerator randGen = new RandomGenerator(1234541);
	// MersenneTwisterFast random generator
	long SEED;  
	public void setSeed(long SEED){this.SEED = SEED;}
	MersenneTwisterFast randGen = new MersenneTwisterFast(SEED);
	JavaRand randGenJava = new JavaRand();

    Function fun; 
	private String FunName;
	public int 	population;// // Number of nests (or different solutions)
	private int dimension;
	private double minX,maxX;
	private int maxIter;
	
    public void setFunction(String FunName){this.FunName = FunName;}
	public void setPopulation(int population){this.population = population;}	
	public void setDimension(int dimension,double minX, double maxX){this.dimension = dimension; this.minX = minX;this.maxX = maxX;}
	public void setMaxIteration(int maxIter){this.maxIter = maxIter;}


	public double pa = 0.5;// Discovery rate of alien eggs/solutions
	public double nest[][];
	public double newNest[][];// hold the new nest/solution 
	public double fitness[];//holds the fitness of the entire population
	public double newFitness[];//holds the fitness of the entire population
	public double bestFitness; // holds the global best fitness 
	public double bestNest[]; // hold the best nest/solution 	
	public void setArrays(){
		nest = new double[population][dimension];
		newNest = new double[population][dimension];// hold the new nest/solution 
		fitness = new double[population];//holds the fitness of the entire population
		newFitness = new double[population];//holds the fitness of the entire population
		bestNest = new double[dimension]; // hold the best nest/solution 
	}

  //Levy Flight Global Variables
  double beta = 3.0/2.0;
  double sigma = Math.pow((gamma(1.0+beta)* Math.sin(3.14*beta/2.0)/(gamma((1.0+beta)/2.0)*beta*Math.pow(2,((beta-1.0)/2.0)))), (1.0/beta)) ;
  static double logGamma(double x) {
      double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
      double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
                       + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
                       +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
      return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
   }
   static double gamma(double x) { return Math.exp(logGamma(x)); }

//Random initial solutions
 public void init(){
        //System.out.println("Initialization----------------------");
		for (int i = 0; i < population; i++) {
			for (int j = 0; j < dimension; j++) {
				nest[i][j] = randGen.randVal(minX, maxX);
				//System.out.printf(" %.3f",nest[i][j]);
			}
			 fitness[i] = fitness(nest[i]);
			 //System.out.printf(" - %.3f\n",fitness[i]);
		}
		
		//computing best fitness
		System.out.printf(" --------------------------------- \n ");
		int index = minfitness();
		bestFitness = fitness[index];
		for (int j = 0; j < dimension; j++){
			bestNest[j] = nest[index][j];//preserving bestNest	
			//System.out.printf(" %.3f",bestNest[j]);
		}
       // System.out.printf("  : %.3f at index %d\n",bestFitness,index);	
	   // System.out.printf(" ---------------------------------- \n ");	   
	}

public double fitness(double x[]){return fun.computeteFunction(x,FunName);}

//Get the current best
public int minfitness(){
	double best = fitness[0];
	//System.out.println(best);
	int bestIndex = 0;
	for (int i = 1; i < population; i++){
	    //System.out.printf("\n %.3f   <  %.3f",fitness[i],best);
		if (fitness[i] < best){
		        //System.out.println("  Found best at "+i+"  "+fitness[i]);
				best = fitness[i];
				bestIndex = i;
		}	
	}		
	return bestIndex;		
}//end minfitness

 public double[] optimize() {
	double returnResult[] = new double[103];
    init();// Fitness list, Nest, best fitness, best nest 
	     long startTime = 0;
		long endTime = 0; 
		int iRet = 0;
		boolean fRet = true;
		int convergenceItr = 0;
		startTime = System.currentTimeMillis();	
	//Starting iterations
	int t;
	for(t = 0; t< maxIter; t++){
	    if(t%100 == 0){ 
			returnResult[iRet] = bestFitness; iRet++;
			System.out.printf("%d>[",t);
			//for(int dim=0;dim<dimension;dim++){System.out.printf("  %.2f",bestNest[dim]); }
			System.out.printf(" ]>%.3f \n",bestFitness);
		}
		//Generate new solutions (but keep the current best)
		get_cuckoos();   
		get_best_nest(); 
		//Discovery and randomization
		empty_nests();  
		//Evaluate this solution
		get_best_nest();
		//Find the best objective so far  
        if(bestFitness<0.0001 && fRet){convergenceItr = t; fRet = false;endTime = System.currentTimeMillis();break;}
	} // End of iterations  
	if(convergenceItr == 0) endTime = System.currentTimeMillis();
	double timeTaken = (endTime - startTime) / 1000.0;//System.out.println("Execution time : " + timeTaken + " seconds");
	returnResult[iRet] = bestFitness; iRet++; 
	if(convergenceItr == 0){returnResult[iRet] = t; iRet++; }else{returnResult[iRet] = convergenceItr; iRet++;}
	returnResult[iRet] = timeTaken; iRet++; 		
	return returnResult;
}//end optimization

public void get_cuckoos(){ // Get cuckoos by random walk
	for(int j = 0;j<population;j++){
	    double s[] = new double[dimension];
		Random g1 = new Random(); 
		Random g2 = new Random(); 
		Random g3 = new Random(); 
	    for(int k = 0; k< dimension; k++){
			s[k] = nest[j][k];
			double u = g1.nextGaussian()*sigma; //randn(size(s))*sigma;
			double v = g2.nextGaussian(); //randn(size(s));
			double step = u/Math.pow(Math.abs(v),(1.0/beta)); //u./abs(v).^(1/beta);      
			double stepsize = 0.01*step*(s[k]-bestNest[k]);
			s[k] = s[k] + stepsize*g3.nextGaussian();
			newNest[j][k] = simplebounds(s[k]);// Apply simple bounds/limits		
	   }//End dimension
   }//End population
}//end get_cuckoo  

public void get_best_nest(){// Evaluating all new solutions
	for (int i = 0; i < population; i++){
	    double fnew = fitness(newNest[i]);
		if(fnew < fitness[i]){
		   fitness[i] = fnew;
		   for(int k = 0; k<dimension; k++){nest[i][k] = newNest[i][k];}	
		}
	}
	int index = minfitness();
	bestFitness = fitness[index];
	for (int j = 0; j < dimension; j++){bestNest[j] = nest[index][j];}
}//end get_best_nest

public void empty_nests(){// Replace some nests by constructing new solutions/nests
    int nestperm1[] = new int[population]; 
    int nestperm2[] = new int[population];
	
	nestperm1 = parmutation(0,population-1);	
	nestperm2 = parmutation(0,population-1);	
	double K[][] = new double[population][dimension]; 
	double nestn1[][] = new double[population][dimension]; 
	double nestn2[][] = new double[population][dimension];
	
	for(int i = 0;i<population;i++){
		for(int j = 0;j<dimension;j++){
			nestn1[i][j] = nest[nestperm1[i]][j]; 
			nestn2[i][j] = nest[nestperm2[i]][j]; 
			if(randGenJava.ran1() > pa)
				K[i][j] = 1;
			else
				K[i][j] = 0;
		}
	}
	for(int i = 0;i<population;i++){
		for(int j = 0;j<dimension;j++){
			newNest[i][j] = simplebounds(nest[i][j] + randGenJava.ran1()*(nestn1[i][j] - nestn1[i][j])*K[i][j]);
			//System.out.printf(" %.3f ",newNest[i][j]);
		}
		//System.out.println();
	}
}//end empty nest

// Application of simple constraints
public double simplebounds(double param){
	if (param < minX){return minX;}
	else if (param > maxX){	return maxX;}
	else{return param;}
}

// Generating permutation within a range of numbers
	public int[] parmutation(int min,int max){
		int a[] = new int[population];
		for(int i = 0; i<population; i++){
		   if(i ==0){
				a[i] = randGenJava.randVal(min,max);
		   }
		   else{
			 while(true){
			    boolean flag = false; 
				int r = randGenJava.randVal(min,max);
				for(int j = 0; j<i;j++){
				  if(r == a[j]){
					flag = true;
					break;
				  }	
				}
				if(!flag){
				   a[i] = r;
				   break;
				}   
			 }				
		   } 	 
		   //System.out.print(" "+a[i]+" ");
		}
		return a;
	}//end permutation

// ----------------- end ------------------------------
	public static void main(String args[]){	}
}
