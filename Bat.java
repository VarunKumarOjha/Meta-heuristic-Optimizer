package Bat;
import Functions.*;
import Randoms.*;
import java.io.*;//import input output
import java.util.Random;//import random
import java.util.Collections;
import java.util.Vector;

public class Bat
{
	//RandomGenerator randGen = new RandomGenerator(1234541);
	// MersenneTwisterFast random generator
	long SEED;  
	public void setSeed(long SEED){this.SEED = SEED;}
	MersenneTwisterFast randGen = new MersenneTwisterFast(SEED);
	JavaRand randGenJava = new JavaRand();

    Function fun; 
	private String FunName;
	public int 	population;// Number of Bats (or different solutions)
	private int dimension;
	private double minX,maxX;
	private int maxIter;
	
    public void setFunction(String FunName){this.FunName = FunName;}
	public void setPopulation(int population){this.population = population;}	
	public void setDimension(int dimension,double minX, double maxX){this.dimension = dimension; this.minX = minX;this.maxX = maxX;}
	public void setMaxIteration(int maxIter){this.maxIter = maxIter;}

 	// declaring arrays
	public double Bat[][];
	public double fitness[] ;//holds the fitness of the entire population
	public double bestFitness; // holds the global best fitness 
	public double bestBat[]; // hold the best Bat/solution 
	public double Q[];   // Frequency
	public double v[][];   // Velocities	
	// defining arrays
	public void setArrays(){
		 Bat = new double[population][dimension];
	     fitness = new double[population];//holds the fitness of the entire population
	     bestBat = new double[dimension]; // hold the best Bat/solution 
	     Q = new double[population];   // Frequency
	     v = new double[population][dimension];   // Velocities
	}
	
	public double A = 0.5; //Loudness of the bat (constant or decreasing)
	public double r = 0.5; //Pulse rate (constant or decreasing)
    // This frequency range determines the scalings
	// You should change these values if necessary
	public int Qmin=0;         // Frequency minimum
	public int Qmax=2;         // Frequency maximum
	
	public double alpha=0.9;      // Cooling factor 
    public double gamma=0.9;      // Pulse rate 

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
			for (int j = 0; j < dimension; j++){
				Bat[i][j] = randGen.randVal(minX, maxX); //System.out.printf(" %.3f",Bat[i][j]);
				v[i][j] = 0.0;   // Velocities
			}
			fitness[i] = fitness(Bat[i]);
			//System.out.printf(" - %.3f\n",fitness[i]);
			Q[i] = 0.0;   // Frequency
	        
		}
		//computing best fitness
		//System.out.printf(" --------------------------------- \n ");
		int index = minfitness();
		bestFitness = fitness[index];
		for (int j = 0; j < dimension; j++){
			bestBat[j] = Bat[index][j];//preserving bestBat	
			//System.out.printf(" %.3f",bestNest[j]);
		}
       //System.out.printf("  : %.3f at index %d\n",bestFitness,index);	
	   //System.out.printf(" ---------------------------------- \n ");	   
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
	}//minfitness

  //Main optimization function
  public double[] optimize() {
	double returnResult[] = new double[103];
    init();// 
       	long startTime = 0;
		long endTime = 0; 
		int iRet = 0;
		boolean fRet = true;
		int convergenceItr = 0;
		startTime = System.currentTimeMillis();
	int t =0;
	while(t< maxIter){
        //Loop over all bats/solutions
        for(int i = 0; i<population; i++){
            
		  double Sol[] = new double[dimension];
		  //update frequency
		  Q[i] = Qmin+(Qmin-Qmax)*randGenJava.ran1();
		  
		  for(int b=0;b<dimension;b++){
			v[i][b]=v[i][b]+(Bat[i][b]-bestBat[b])*Q[i];
			Sol[b]= Bat[i][b]+v[i][b];
			Bat[i][b]=simplebounds(Bat[i][b]);
          }
		  // Pulse rate
		  Random g = new Random(); 
          if(randGenJava.ran1()>r){
			 for(int b=0;b<dimension;b++){
				// The factor 0.001 limits the step sizes of random walks 
				Sol[b] = bestBat[b]+0.001*g.nextGaussian();
			}//for	
          }//if
          
          // Evaluate new solutions
           double Fnew = fitness(Sol);
          // If fitness improves (better solutions found), update then
            if ((Fnew<=fitness[i])&& (randGenJava.ran1()<A)){
                for(int b=0;b<dimension;b++){Bat[i][b]=Sol[b];}
                fitness[i]=Fnew;
           }//end if
           
          //Update the current global best
          if (fitness[i] < bestFitness){
			//System.out.printf("\nNew best solution found  %.3f",bestFitness); 
			bestFitness = fitness[i];
			for(int b=0;b<dimension;b++){bestBat[b]=Sol[b];}//Bat[i][b];}
		  }//if
        }//population
        //Display results every 100 iterations
		//if(t%100 == 0){ System.out.printf("\n Iteration: %d - %.3f ",t,bestFitness);}
		if(bestFitness<0.0001 && fRet){convergenceItr = t; fRet = false;endTime = System.currentTimeMillis(); }
	    if(t%100 == 0){
			//returnResult[iRet] = bestFitness; iRet++;
			System.out.printf("%d>[",t);
			for(int dim=0;dim<dimension;dim++){System.out.printf("  %.2f",bestBat[dim]); }
			System.out.printf("]>%.3f \n",bestFitness);
		} 
		 t++;
	} // End of iterations 
		if(convergenceItr == 0) endTime = System.currentTimeMillis();
		double timeTaken = (endTime - startTime) / 1000.0; //System.out.println("Execution time : " + timeTaken + " seconds");
		returnResult[iRet] = bestFitness; iRet++; 
		if(convergenceItr == 0){returnResult[iRet] = t; iRet++; }else{returnResult[iRet] = convergenceItr; iRet++;}
		returnResult[iRet] = timeTaken; iRet++; 		
		return returnResult;	
 }//Optimize

// Application of simple constraints
public double simplebounds(double param){
	if (param < minX){return minX;}
	else if (param > maxX){return maxX;}
	else {return param;	}
}

  // Draw n Levy flight sample
  public double Levy(){
	double u = randGenJava.ran1()*sigma; //randn(size(s))*sigma;
	double v = randGenJava.ran1(); //randn(size(s));
	double step = u/Math.pow(Math.abs(v),(1.0/beta)); //u./abs(v).^(1/beta);  
    double L = 0.01*step; 
	return L;
  }

    // Generating permutation within a range of numbers
	public int[] parmutation(int min,int max){
		int a[] = new int[population];
		for(int i = 0; i<population; i++){
		   if(i ==0){a[i] = randGenJava.randVal(min,max);}
		   else{
			 while(true){
			    boolean flag = false; 
				int r = randGenJava.randVal(min,max);
				for(int j = 0; j<i;j++){
				  if(r == a[j]){
					flag = true;
					break;
				  }//if	
				}//for
				if(!flag){
				   a[i] = r;
				   break;
				}//if   
			 }//while				
		   }//else 	 
		   //System.out.print(" "+a[i]+" ");
		}//for
		return a;
	}//permutation
// ----------------- end ------------------------------
	public static void main(String args[]){	}
}
