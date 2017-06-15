package FF;
import Functions.*;
import Randoms.*;
import java.io.*;//import input output
import java.util.Random;//import random
import java.util.Collections;
import java.util.Vector;


public class FF
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
 
	
  //Algorithm parameters
  public double Fly[][]; //[population][dimension]
  public double FlyOld[][]; //[population][dimension]
  public double Light[];
  public double LightOld[];
  public double bestFly[];
  public double bestLight; 
  
  public void setArrays(){
       Fly = new double[population][dimension]; //[population][dimension]
       FlyOld = new double[population][dimension]; //[population][dimension]
       Light = new double[population];
       LightOld = new double[population];
       bestFly = new double[dimension];
  }
  // ------------------------------------------------
  public double alpha=0.25;     // Randomness 0--1 (highly random)
  public double betamn=0.20;      // Minimum value of beta
  public double gamma=1;         //Absorption coefficient


  public int NumEval = population*maxIter;

  //Random initial solutions
  public void initiator(){
        //System.out.println("Initialization----------------------");
		for (int i = 0; i < population; i++) {
			for (int j = 0; j < dimension; j++) {
				Fly[i][j] = randGen.randVal(minX, maxX);
				//System.out.printf(" %.3f",nest[i][j]);
			}//for
			Light[i] = fitness(Fly[i]);
			//System.out.printf(" - %.3f\n",Light[i]);
		}//for
		
		//computing best fitness
		//System.out.printf(" --------------------------------- \n ");
		int index = minfitness();
		bestLight = Light[index];
		for (int j = 0; j < dimension; j++){
			bestFly[j] = Fly[index][j];//preserving bestNest	
			//System.out.printf(" %.3f",bestNest[j]);
		}//for
       //System.out.printf("Initial best cost  : %.3f \n",bestLight,index);	
	   //System.out.printf(" ---------------------------------- \n ");	   
  }//initiator  
  
  public double fitness(double x[]) {return fun.computeteFunction(x,FunName); }

  //Get the current best
  public int minfitness(){
	 double best = Light[0];
	 //System.out.println(best);
	 int bestIndex = 0;
	 for (int i = 1; i < population; i++){
	    //System.out.printf("\n %.3f   <  %.3f",fitness[i],best);
		if (Light[i] < best){
		        //System.out.println("  Found best at "+i+"  "+fitness[i]);
				best = Light[i];
				bestIndex = i;
		}//if
	 }//for		
	 return bestIndex;		
  }// minfitness  
     
  //The main optimization loop
  public double[] optimize() {
    	 double returnResult[] = new double[103];
	    // Initialization of the population of the Wolves
		// Find the best fitness and best wolf
		initiator();
        sorting(); 
		//Ranking the fireflies by their light intensity
        //for (int i = 0; i < population; i++) {System.out.printf(" - %.3f\n",Light[i]);}	
       	long startTime = 0;
		long endTime = 0; 
		int iRet = 0;
		boolean fRet = true;
		int convergenceItr = 0; 
		startTime = System.currentTimeMillis();
		//returnResult[2] = bestLight;
		
		//Main Iteration of the algorithm starts
 	    int l  = 0;
		while(l<maxIter){
		     if(l%10 ==0){
				returnResult[iRet] = bestLight; iRet++;
				System.out.printf("%d > [",l);
				for(int dim=0;dim<dimension;dim++){System.out.printf("  %.2f ",bestFly[dim]); }
				System.out.printf(" ] > %.3f \n",bestLight);
			}
			  //This line of reducing alpha is optional
			  //alpha = newalpha(alpha,betamn);
			  
			 // Evaluate new solutions (for all n fireflies)
			for(int i = 0; i<population; i++){
					Light[i] = fitness(Fly[i]);
			}

			//Ranking fireflies by their light intensity/objectives
			sorting();
			  
			//Find the current best		 
			for(int i = 0; i<population; i++){for(int b=0;b<dimension;b++){FlyOld[i][b] = Fly[i][b];}}
			for(int i = 0; i<population; i++){LightOld[i] = Light[i];}
			for(int i = 0; i<population; i++){
				if (Light[i] < bestLight){
					//System.out.printf("\nNew best %.3f -- %d",Light[i],l); 
					bestLight = Light[i];
					for(int b=0;b<dimension;b++){bestFly[b] = Fly[i][b];}
				}//if
			}//for	

			//Move all fireflies to the better locations
			ffa_move(bestLight,bestFly,alpha);
			//if(l%100 ==0){returnResult[iRet] = bestLight; iRet++;}
		  l=l+1;     
		  if(bestLight<0.0001 && fRet){convergenceItr = l; fRet = false;endTime = System.currentTimeMillis();break;}		
	  }// While
	  //System.out.printf("\n Best Score:  %.3f ",bestLight); 
		if(convergenceItr == 0) endTime = System.currentTimeMillis();
		double timeTaken = (endTime - startTime) / 1000.0;//System.out.println("Execution time : " + timeTaken + " seconds");
		returnResult[iRet] = bestLight; iRet++; 
		if(convergenceItr == 0){returnResult[iRet] = l; iRet++; }else{returnResult[iRet] = convergenceItr; iRet++;}
		returnResult[iRet] = timeTaken; iRet++; 		
		return returnResult;
   }//End of optimization

   //Reduce the randomness during iterations
    public double newalpha(double alpha,double delta){return (1-delta)*alpha;}

	//Move all fireflies toward brighter ones
    public void ffa_move(double Lightbest, double nbest[],double alpha){
	   // Scaling of the system
	   double scale=Math.abs(maxX-minX);

	  //Updating fireflies
	  for(int i = 0; i<population; i++){
          //The attractiveness parameter beta=exp(-gamma*r)
		  for(int j = 0; j<population; j++){
		    double sum = 0.0;
		    for(int b=0;b<dimension;b++){
			   sum = sum + ((Fly[i][b] - Fly[j][b])*(Fly[i][b] - Fly[j][b]));
			}
			double r = Math.sqrt(sum);
			// Update moves
			if( Light[i] > LightOld[j]){ //% Brighter and more attractive
				double beta0 = 1;
  				double beta = (beta0 -betamn)*Math.exp(-gamma*(r*r))+betamn;					
				for(int b=0;b<dimension;b++){//modification of fly
				    double tmpf = alpha*(randGenJava.ran1()-0.5)*scale;
					Fly[i][b] = Fly[i][b]*(1-beta) + FlyOld[j][b]*beta+tmpf;
				}
			}//end if
		}//end for j
	}//End for i
  }//End firefly move
   
   // Application of simple constraints
   public double simplebounds(double param){
	if (param < minX){return minX;}
	else if (param > maxX){return maxX;}
	else{return param;}
  }//Simplebouds
   
	//Sorting ----------------------------------------------------
	public void sorting(){//Sorting according to fitness of wolves
	 for(int k = 1; k < population; k++){
        for(int i = 0; i < population-k; i++){
		   double fitness1 = Light[i];
           double fitness2 = Light[i+1];
                 
           double tempIntensity = Light[i];	   
           double temp[] = new double[dimension];   
           for(int b=0;b<dimension;b++){ temp[b]=Fly[i][b];}
		   
           if(fitness1 >= fitness2){   
              for(int b=0;b<dimension;b++){ Fly[i][b] = Fly[i+1][b];}	
              Light[i] = Light[i+1]; 
		  
              for(int b=0;b<dimension;b++){ Fly[i+1][b] = temp[b];}
              Light[i+1] = tempIntensity;			  
           }//if
         }//for
     }//for
  }//Sorting
		   
	public static void main(String args[]){	}//main
}//FF
