package BFO;
import Functions.*;
import Randoms.*;
import java.io.*;//import input output
import java.util.Random;//import random
import java.util.Collections;
import java.util.Vector;


public class BFO
{

	//RandomGenerator randGen = new RandomGenerator(1234541);
	// MersenneTwisterFast random generator
	long SEED;  
	public void setSeed(long SEED){this.SEED = SEED;}
	MersenneTwisterFast randGen = new MersenneTwisterFast(SEED);
	JavaRand randGenJava = new JavaRand();

    Function fun; 
	private String FunName;
	public int 	population;// The number of bacteria 
	private int dimension;
	private double minX,maxX;
	private int maxIter;

	//Algorithm parameters           
	private int 	Nc;                         // Number of chemotactic steps 
	private int 	Ns=5;                          // Limits the length of a swim 
	private int	    Nre=10;                         // The number of reproduction steps 
	private int 	Ned=10;                         // The number of elimination-dispersal events 
	private double Sr=population/2;               // The number of bacteria reproductions (splits) per generation 
	private double Ped=0.25;                      // The probability that each bacteria will be eliminated/dispersed 
	private double Ci = 0.05;       // the run length  
 
	// variable related to bacteria
	private double Bacteria[][]; //[population][dimension]
	private double health[];
	private double cost[];
	private double prevCost[];
	private double bestCost;
	private double bestBacteria[];	

	//stetting variables
	public void setFunction(String FunName){this.FunName = FunName;}
	public void setPopulation(int population){this.population = population;}	
	public void setDimension(int dimension,double minX, double maxX){this.dimension = dimension; this.minX = minX;this.maxX = maxX;}
	public void setMaxIteration(int Nc){this.Nc = Nc;}
	public void setArrays(){
	  Bacteria = new double[population][dimension]; //[population][dimension]
	  health = new double[population];
	  cost = new double[population];
	  prevCost = new double[population];
	  bestBacteria = new double[dimension];
	}

   //Random initial solutions
   public void initiator(){
        //System.out.println("Initialization----------------------");
		for (int i = 0; i < population; i++) {
			for (int j = 0; j < dimension; j++) {
				Bacteria[i][j] = randGen.randVal(minX, maxX);
			}
			cost[i] = fitness(Bacteria[i]);
			//health[i] = 0.0; 
			//System.out.printf(" - %.3f\n",fitness[i]);
		}
		
		//computing best fitness
		//System.out.printf(" --------------------------------- \n ");
		int index = minfitness();
		bestCost = cost[index];
		for (int j = 0; j < dimension; j++) 
		{
			bestBacteria[j] = Bacteria[index][j];//preserving bestNest	
			//System.out.printf(" %.3f",bestNest[j]);
		}
       //System.out.printf("Initial best cost  : %.3f \n",bestCost,index);	
	   //System.out.printf(" ---------------------------------- \n ");	   
  }  
  
  public double fitness(double x[]) {return fun.computeteFunction(x,FunName);}

  //Get the current best
  public int minfitness(){
	 double best = cost[0];
	 //System.out.println(best);
	 int bestIndex = 0;
	 for (int i = 1; i < population; i++){
	    //System.out.printf("\n %.3f   <  %.3f",fitness[i],best);
		if (cost[i] < best){
		        //System.out.println("  Found best at "+i+"  "+fitness[i]);
				best = cost[i];
				bestIndex = i;
		}	
	 }		
	 return bestIndex;		
  }  
     
  //The main optimization loop
  public double[] optimizeBF(){
  
  	     //MersenneTwisterFast randGen = new MersenneTwisterFast(SEED);
    	 double returnResult[] = new double[103];
	    // Initialization of the population of the bacteria
		// Find the best fitness and best bacteria
		initiator();	
		returnResult[0] = bestCost;
       	long startTime = 0;
		long endTime = 0; 
		int iRet = 0;
		boolean fRet = true;
		int convergenceItr = 0; 
		int iter;
		startTime = System.currentTimeMillis();		
		int t = 1;
   	    //Elimination and dispersal loop 
		for(int ell = 0; ell < Ned; ell++){
			//Reproduction loop
			for(int K = 0; K< Nre; K++){
				// swim/tumble(chemotaxis)loop   
				for(int j = 0;j<Nc; j++){
				    //Setting each bacteria health to 0.0
					for (int h = 0; h < population; h++) { health[h] = 0.0; }
					// Process each bacteria
					for(int i = 0;i<population; i++){
					    // Tumble  
					    double[] tumble = new double[dimension];
						for (int p = 0; p < dimension; ++p) {
							tumble[p] = 2.0 * randGenJava.ran1() - 1.0;
						}
						double rootProduct = 0.0;
						for (int p = 0; p < dimension; ++p) {
							rootProduct += (tumble[p] * tumble[p]);
						}
						for (int p = 0; p < dimension; ++p) {
							Bacteria[i][p] = simplebounds(Bacteria[i][p] + (Ci * tumble[p]) / Math.sqrt(rootProduct));
							//Bacteria[i][p] = Bacteria[i][p] + (Ci * tumble[p]) / Math.sqrt(rootProduct);
						}
						
						prevCost[i] = cost[i];
                        cost[i] = fitness(Bacteria[i]);
						health[i] += cost[i];
						if (cost[i] < bestCost){
						    //System.out.printf("\nNew best solution found by bacteria %d, at time = %d with value %.3f",i,t,bestCost); 
							bestCost = cost[i];
							for(int b=0;b<dimension;b++){bestBacteria[b]=Bacteria[i][b];}
						}
						
						//Swim 
						int m=0; // Initialize counter for swim length 
						while (m < Ns) {//While length of swim 
							m = m + 1;
							if (cost[i]<prevCost[i]){
                              prevCost[i] = cost[i];  
							   for (int p = 0; p < dimension; ++p) {
								Bacteria[i][p] = simplebounds(Bacteria[i][p] + (Ci * tumble[p]) / Math.sqrt(rootProduct));
							   //Bacteria[i][p] = Bacteria[i][p] + (Ci * tumble[p]) / Math.sqrt(rootProduct);
							   }
							   cost[i] = fitness(Bacteria[i]);
							   if (cost[i] < bestCost){
							     //System.out.printf("\nNew best solution found by bacteria %d, at time = %d with value %.3f",i,t,bestCost); 
							 	 bestCost = cost[i];
								 for(int b=0;b<dimension;b++){bestBacteria[b]=Bacteria[i][b];}
							   }							 
							}
							else {      
								m = Ns ;     
							}							
						 }  
                         if(bestCost<0.0001) break;	// for tumbling and swim						 
					}// Go to next bacterium
					if(bestCost<0.0001) break; // for chemotoxic	
					if(t%10 ==0){
						returnResult[iRet] = bestCost; iRet++;
						System.out.printf("%d > [",t);
						for(int dim=0;dim<dimension;dim++){System.out.printf(" %.2f ",bestBacteria[dim]);}
						System.out.printf(" ] > %.3f \n",bestCost);
					}
					t++;
				}// Go to the next chemotactic             
				//Reproduction   
                sorting();								
				for (int left = 0; left < population / 2; left++){
					int right = left + population / 2;
					//Replace right half poor bacteria with left half best Bacteria  
					for(int b=0;b<dimension;b++){Bacteria[right][b]=Bacteria[left][b];}
					cost[right] = cost[left];
					prevCost[right] = prevCost[left];
					health[right] = health[left];
				}  
			}//Go to next reproduction    
			//Eliminatoin and dispersal
			for (int i = 0; i < population; ++i) {
				double prob = randGenJava.ran1();
				if (prob < Ped) {
					for (int p = 0; p < dimension; ++p) {
						Bacteria[i][p] = randGenJava.randVal(minX, maxX);;
					}
					cost[i] = fitness(Bacteria[i]);
					prevCost[i] = cost[i];
					health[i] = 0.0;
					if (cost[i] < bestCost){
					     //System.out.printf("\nNew best solution found by bacteria %d, at time = %d with value %.3f",i,t,bestCost); 
					 	 bestCost = cost[i];
						 for(int b=0;b<dimension;b++){bestBacteria[b]=Bacteria[i][b];}
					}							 
				} //if (prob < Ped)
				if(bestCost<0.0001 && fRet){convergenceItr = t; fRet = false;endTime = System.currentTimeMillis();break;}
				// elimination dispersal
			} //for
			if(bestCost<0.0001 && fRet){convergenceItr = t; fRet = false;endTime = System.currentTimeMillis();break;}	
		} //Go to next elimination and dispersal 
		if(convergenceItr == 0) endTime = System.currentTimeMillis();
		double timeTaken = (endTime - startTime) / 1000.0;//System.out.println("Execution time : " + timeTaken + " seconds");
		returnResult[iRet] = bestCost; iRet++; 
		if(convergenceItr == 0){returnResult[iRet] = t; iRet++; }else{returnResult[iRet] = convergenceItr; iRet++;}
		returnResult[iRet] = timeTaken; iRet++; 		
		return returnResult;
   }//End of optimization

   // Application of simple constraints
   public double simplebounds(double param){
	if (param < minX){return minX;}
	else if (param > maxX){	return maxX;}
	else{return param;}
  }
   
	//Sorting ----------------------------------------------------
	public void sorting(){//Sorting according to health of bacteria
	 for(int k = 1; k < population; k++){
        for(int i = 0; i < population-k; i++){
		   double fitness1 = health[i];//cost[i];
           double fitness2 = health[i+1];//cost[i+1];;
                 
           double tempCost = cost[i];
           double tempPrevCost = prevCost[i];
           double tempHealth = health[i];		   
           double temp[] = new double[dimension];   
           for(int b=0;b<dimension;b++){ temp[b]=Bacteria[i][b];}
           if(fitness1 >= fitness2){   
              for(int b=0;b<dimension;b++){ Bacteria[i][b] = Bacteria[i+1][b];}	
              cost[i] = cost[i+1]; 
              prevCost[i] = prevCost[i+1];
              health[i] = health[i+1];  
			  
              for(int b=0;b<dimension;b++){ Bacteria[i+1][b] = temp[b];}
              cost[i+1] = tempCost;	
              prevCost[i+1] = tempPrevCost;
              health[i+1] = tempHealth;			  
           }
         }
     }
  }
		   
	public static void main(String args[]){	}
}
