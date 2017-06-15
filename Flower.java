package FP;
import Functions.*;
import Randoms.*;
import java.io.*;//import input output
import java.util.Random;//import random
import java.util.Collections;
import java.util.Vector;


public class Flower
{

	//RandomGenerator randGen = new RandomGenerator(1234541);
	// MersenneTwisterFast random generator
	long SEED;  
	public void setSeed(long SEED){this.SEED = SEED;}
	MersenneTwisterFast randGen = new MersenneTwisterFast(SEED);
	JavaRand randGenJava = new JavaRand();

    Function fun; 
	private String FunName;
	public int 	population;// Number of Flowers (or different solutions)
	private int dimension;
	private double minX,maxX;
	private int maxIter;
	
    public void setFunction(String FunName){this.FunName = FunName;}
	public void setPopulation(int population){this.population = population;}	
	public void setDimension(int dimension,double minX, double maxX){this.dimension = dimension; this.minX = minX;this.maxX = maxX;}
	public void setMaxIteration(int maxIter){this.maxIter = maxIter;}
 

	public double p = 0.8; //probabibility switch
	public double Flower[][] = new double[population][dimension];
	public double fitness[] = new double[population];//holds the fitness of the entire population
	public double bestFitness; // holds the global best fitness 
	public double bestFlower[] = new double[dimension]; // hold the best Flower/solution 	

	public void setArrays(){
		Flower = new double[population][dimension];
	    fitness = new double[population];//holds the fitness of the entire population
	    bestFlower = new double[dimension]; // hold the best Flower/solution 
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
				Flower[i][j] = randGen.randVal(minX, maxX);
				//System.out.printf(" %.3f",Flower[i][j]);
			}
			 fitness[i] = fitness(Flower[i]);
			 //System.out.printf(" - %.3f\n",fitness[i]);
		}
		
		//computing best fitness
		//System.out.printf(" --------------------------------- \n ");
		int index = minfitness();
		bestFitness = fitness[index];
		for (int j = 0; j < dimension; j++) 
		{
			bestFlower[j] = Flower[index][j];//preserving bestNest	
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

  public double[] optimize() {
    double returnResult[] = new double[103];
    init();// 
       	long startTime = 0;
		long endTime = 0; 
		int iRet = 0;
		boolean fRet = true;
		int convergenceItr = 0; 
		startTime = System.currentTimeMillis();
	int t;
	for(t = 0; t< maxIter; t++){
	    if(t%10 ==0){
			returnResult[iRet] = bestFitness; iRet++;
			System.out.printf("%d > [",t);
			for(int dim=0;dim<dimension;dim++){System.out.printf("  %.2f ",bestFlower[dim]); }
			System.out.printf(" ] > %.3f \n",bestFitness);
		}
        //Loop over all solutions
        for(int i = 0; i<population; i++){
          // Pollens are carried by insects and thus can move in large scale, large distance.
          // This L should replace by Levy flights Formula: x_i^{t+1}=x_i^t+ L (x_i^t-gbest)
		  
		  double Sol[] = new double[dimension];
		  for(int b=0;b<dimension;b++){Sol[b]=Flower[i][b];}
		  	  
          if(randGenJava.ran1()>p){
			 for(int b=0;b<dimension;b++){
				double L = Levy();//L = randGenJava.ran1();
				double dS = L*(Sol[b]-bestFlower[b]);
				Sol[b] = simplebounds(Sol[b]+dS);
			}//for	
          }//if
          else {  // If not, then local pollenation of neighbor flowers 
              double epsilon=randGenJava.ran1();
              // Find random flowers in the neighbourhood
              int JK[] = new int[population];
			  JK = parmutation(0,population-1);
              // As they are random, the first two entries also random 
              // If the flower are the same or similar species, then
              // they can be pollenated, otherwise, no action.
              // Formula: x_i^{t+1}+epsilon*(x_j^t-x_k^t)
			  for(int b=0;b<dimension;b++){
				Sol[b] = simplebounds(Sol[b]+epsilon*(Flower[JK[0]][b]-Flower[JK[1]][b]));
			  }
          }
          
          // Evaluate new solutions
           double Fnew = fitness(Sol);
          // If fitness improves (better solutions found), update then
            if (Fnew<=fitness[i]){
                for(int b=0;b<dimension;b++){Flower[i][b]=Sol[b];}
                fitness[i]=Fnew;
           }//end if
           
          //Update the current global best
          if (fitness[i] < bestFitness){
			//System.out.printf("\nNew best solution found  %.3f",bestFitness); 
			bestFitness = fitness[i];
			for(int b=0;b<dimension;b++){bestFlower[b]=Flower[i][b];}
		  }//if
        }//population
        //Display results every 100 iterations
		//if(t%100 ==0){returnResult[iRet] = bestFitness; iRet++;}//System.out.printf("\n Iteration: %d - %.3f ",t,bestFitness);
		//for(int b=0;b<dimension;b++){System.out.printf(" %.3f ",bestFlower[b]);}
		if(bestFitness<0.0001 && fRet){convergenceItr = t; fRet = false;endTime = System.currentTimeMillis();break;}	
	} // End of iterations 
    //System.out.printf("\n Best Sol %.3f ",bestFitness);	
		if(convergenceItr == 0) endTime = System.currentTimeMillis();
		double timeTaken = (endTime - startTime) / 1000.0;//System.out.println("Execution time : " + timeTaken + " seconds");
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
		   if(i == 0){a[i] = randGenJava.randVal(min,max);}
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
