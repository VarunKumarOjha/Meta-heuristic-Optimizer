package GWO;
import Functions.*;
import Randoms.*;
import java.io.*;//import input output
import java.util.Random;//import random
import java.util.Collections;
import java.util.Vector;


public class GWO
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
    // variable related to bacteria
	private  double Wolf[][];
	private  double wolfScore[];
	private  double bestWolf[];
	private  double bestScore;
	private  double alpha[];
	private  double beta[];
	private  double delta[];	
	
	public void setArrays(){
		Wolf = new double[population][dimension]; //[population][dimension]
		wolfScore = new double[population];
		bestWolf = new double[dimension];

		alpha = new double[dimension];
		beta  = new double[dimension];
		delta = new double[dimension];
	}
	
	public  double alphaScore;
	public  double betaScore;
	public  double deltaScore;



  //Random initial solutions
  public void initiator(){
        //System.out.println("Initialization----------------------");
		for (int i = 0; i < population; i++) {
			for (int j = 0; j < dimension; j++) {
				Wolf[i][j] = randGen.randVal(minX, maxX);
				//System.out.printf(" %.3f",nest[i][j]);
			}//for
			wolfScore[i] = fitness(Wolf[i]);
			//System.out.printf(" - %.3f\n",wolfScore[i]);
		}//for
		
		//computing best fitness
		//System.out.printf(" --------------------------------- \n ");
		int index = minfitness();
		bestScore = wolfScore[index];
		for (int j = 0; j < dimension; j++){
			bestWolf[j] = Wolf[index][j];//preserving bestNest	
			//System.out.printf(" %.3f",bestNest[j]);
		}//for
       //System.out.print(FunName);	
       //System.out.printf("  Initial best : %.5f \n",bestScore);	
	   //System.exit(1);
	   //System.out.printf(" ---------------------------------- \n ");	   
  }//initiator  
  
  public double fitness(double x[]) {return fun.computeteFunction(x,FunName); }

  //Get the current best
  public int minfitness(){
	 double best = wolfScore[0];
	 //System.out.println(best);
	 int bestIndex = 0;
	 for (int i = 1; i < population; i++){
	    //System.out.printf("\n %.3f   <  %.3f",fitness[i],best);
		if (wolfScore[i] < best){
		        //System.out.println("  Found best at "+i+"  "+fitness[i]);
				best = wolfScore[i];
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
		//Testing sorting
        //for (int i = 0; i < population; i++) {System.out.printf(" - %.3f\n",wolfScore[i]);}	
		
		//finding alpha
		 for(int b=0;b<dimension;b++){ alpha[b] = Wolf[0][b];}
		 alphaScore = wolfScore[0];//System.out.printf(" - %.3f\n",alphaScore);	
         //returnResult[2] = alphaScore;		 
		 //finding beta
		 for(int b=0;b<dimension;b++){ beta[b] = Wolf[1][b];}
		 betaScore = wolfScore[1];//System.out.printf(" - %.3f\n",betaScore);		
		 //finding delta
		 for(int b=0;b<dimension;b++){ delta[b] = Wolf[2][b];}
		 deltaScore = wolfScore[2];//System.out.printf(" - %.3f\n",deltaScore);
         
		//MersenneTwisterFast randGen = new MersenneTwisterFast(SEED);
       	long startTime = 0;
		long endTime = 0; 
		int iRet = 0;
		boolean fRet = true;
		int convergenceItr = 0; 
		startTime = System.currentTimeMillis();
 	    int l  = 0;
		while(l<maxIter){
			for(int i = 0; i<population; i++){
			  //Return back the search agents that go beyond the boundaries of the search space
              for(int b=0;b<dimension;b++){ Wolf[i][b] = simplebounds(Wolf[i][b]);}             
			  
			  //Calculate fitness of the wolf
			  wolfScore[i] = fitness(Wolf[i]);
			  
			  //Update Alpha, Beta, and Delta
			  if(wolfScore[i]<alphaScore){ 
					for(int b=0;b<dimension;b++){ alpha[b] = Wolf[i][b];}
					alphaScore = wolfScore[i];
			  }//update alpha
        
			  if((wolfScore[i]>alphaScore) && (wolfScore[i]<betaScore)){
					for(int b=0;b<dimension;b++){ beta[b] = Wolf[i][b];}
					betaScore = wolfScore[i];
			  }//Update beta
        
			  if((wolfScore[i]>alphaScore) && (wolfScore[i]>betaScore) && (wolfScore[i]<deltaScore)){
					for(int b=0;b<dimension;b++){ delta[b] = Wolf[i][b];}
					deltaScore = wolfScore[i];
			  }//update Delta
		   }
			if(l%10 ==0){
				returnResult[iRet] = alphaScore; iRet++; 
				System.out.printf("%d > [",l);
				for(int dim=0;dim<dimension;dim++){System.out.printf(" %.2f ",alpha[dim]);}
				System.out.printf(" ] > %.3f \n",alphaScore);
			}    
    
		 double a=2-l*((2)/maxIter); // a decreases linearly from 2 to 0
    
		 //Update the Position of search agents including omegas
		 for(int i=0; i<population; i++){
			for(int j=0; j<dimension;j++){     
                
                				
				double r1= randGenJava.ran1();//rand(); //r1 is a random number in [0,1]
				double r2= randGenJava.ran1();//rand(); //r2 is a random number in [0,1]
            
				double A1=2*a*r1-a; //Equation (3.3)
				double C1=2*r2; //Equation (3.4)
            
				double D_alpha=Math.abs(C1*alpha[j]-Wolf[i][j]); //Equation (3.5)-part 1
				double X1=alpha[j]-A1*D_alpha; //Equation (3.6)-part 1
                 
                				 
				r1=randGenJava.ran1();
				r2=randGenJava.ran1();
            
				double A2=2*a*r1-a; // Equation (3.3)
				double C2=2*r2; // Equation (3.4)
            
				double D_beta=Math.abs(C2*beta[j]-Wolf[i][j]); //Equation (3.5)-part 2
				double X2=beta[j]-A2*D_beta; //Equation (3.6)-part 2       
             
			    
				r1=randGenJava.ran1();
				r2=randGenJava.ran1(); 
            
				double A3=2*a*r1-a; //Equation (3.3)
				double C3=2*r2; //Equation (3.4)
            
				double D_delta=Math.abs(C3*delta[j]-Wolf[i][j]); //Equation (3.5)-part 3
				double X3=delta[j]-A3*D_delta; //Equation (3.5)-part 3             
            
				Wolf[i][j]=(X1+X2+X3)/3; //Equation (3.7)
			}
		  }	   
		  //if(l%100 ==0){returnResult[iRet] = alphaScore; iRet++; }//System.out.printf(" %d - %.3f\n",l,alphaScore);		}	
		  l=l+1; 		  
		  //Termination criteria
          if(alphaScore<0.0001 && fRet){convergenceItr = l; fRet = false;endTime = System.currentTimeMillis();break;}		 
	  }// While
	  //System.out.printf("\n Best Score:  %.3f ",alphaScore); 
		if(convergenceItr == 0) endTime = System.currentTimeMillis();
		double timeTaken = (endTime - startTime) / 1000.0;//System.out.println("Execution time : " + timeTaken + " seconds");
		returnResult[iRet] = alphaScore; iRet++; 
		if(convergenceItr == 0){returnResult[iRet] = l; iRet++; }else{returnResult[iRet] = convergenceItr; iRet++;}
		returnResult[iRet] = timeTaken; iRet++; 		
		return returnResult;
   }//End of optimization

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
		   double fitness1 = wolfScore[i];
           double fitness2 = wolfScore[i+1];
                 
           double tempCost = wolfScore[i];	   
           double temp[] = new double[dimension];   
           for(int b=0;b<dimension;b++){ temp[b]=Wolf[i][b];}
		   
           if(fitness1 >= fitness2){   
              for(int b=0;b<dimension;b++){ Wolf[i][b] = Wolf[i+1][b];}	
              wolfScore[i] = wolfScore[i+1]; 
		  
              for(int b=0;b<dimension;b++){ Wolf[i+1][b] = temp[b];}
              wolfScore[i+1] = tempCost;			  
           }//if
         }//for
     }//for
  }//Sorting
		   
	public static void main(String args[]){  
	}//main
}//GWO
