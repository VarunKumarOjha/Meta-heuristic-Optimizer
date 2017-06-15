package ABC;
import Functions.*;
import Randoms.*;
import java.lang.Math;
public  class ABC {


	// MersenneTwisterFast random generator
	long SEED;  
	public void setSeed(long SEED){this.SEED = SEED;}
	MersenneTwisterFast MTrand = new MersenneTwisterFast(SEED);
	//JavaRand randGenJava = new JavaRand();
	
    Function fun; 
	private String FunName;
	private int dimension;
	private double minX,maxX; 
	
	/* Control Parameters of ABC algorithm*/
	int population; /* NP = population = The number of colony size (employed bees+onlooker bees)*/
	int FoodNumber; /*The number of food sources equals the half of the colony size*/
	int limit = 100;  /*A food source which could not be improved through "limit" trials is abandoned by its employed bee*/
	int maxIter; /*The number of cycles for foraging {a stopping criteria}*/
	
	public void setFunction(String FunName){this.FunName = FunName;}
	public void setPopulation(int population){this.population = population;this.FoodNumber = (int)population/2;}	
	public void setDimension(int dimension,double minX, double maxX){this.dimension = dimension; this.minX = minX;this.maxX = maxX;}
	public void setMaxIteration(int maxIter){this.maxIter = maxIter;}


	int dizi1[]=new int[10];
	private double Foods[][];    /*Foods is the population of food sources. Each row of Foods matrix is a vector holding dimension parameters to be optimized. The number of rows of Foods matrix equals to the FoodNumber*/
	private double f[];          /*f is a vector holding objective function values associated with food sources */
	private double fitness[];    /*fitness is a vector holding fitness (quality) values associated with food sources*/
	private double trial[];      /*trial is a vector holding trial numbers through which solutions can not be improved*/
	private double prob[];       /*prob is a vector holding probabilities of food sources (solutions) to be chosen*/
	private double solution[];   /*New solution (neighbour) produced by v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) j is a randomly chosen parameter and k is a randomlu chosen solution different from i*/
	                  
	public double ObjValSol;              /*Objective function value of new solution*/
	public double FitnessSol;              /*Fitness value of new solution*/
	public int neighbour, param2change;    /*param2change corresponds to j, neighbour corresponds to k in equation v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij})*/

	public double GlobalMin;                       /*Optimum solution obtained by ABC algorithm*/
	private double GlobalParams[];                   /*Parameters of the optimum solution*/	
	
	public void setArrays(){
		Foods = new double[FoodNumber][dimension]; 
		f = new double[FoodNumber];           
		fitness = new double[FoodNumber];     
		trial = new double[FoodNumber];       
		prob = new double[FoodNumber];        
		solution = new double[dimension];   
		GlobalParams = new double[dimension]; 
	}
            
	/*GlobalMins holds the GlobalMin of each run in multiple runs*/
	double r; /*a random number in the range [0,1)*/

	/*a function pointer returning double and taking a D-dimensional array as argument */
	/*If your function takes additional arguments then change function pointer definition and lines calling "...=function(solution);" in the code*/

//Step 1 All food sources are initialized 
	void initial(){
		int i;
		for(i=0;i<FoodNumber;i++){ 
			init(i);
		}
		GlobalMin = f[0];
	    for(i=0;i<dimension;i++)
			GlobalParams[i]=Foods[0][i];
	}

//Step 1.a Initializing the candidate solution  
	void init(int index){

	   /*Variables are initialized in the range [minX,maxX]. 
	    If each parameter has different range, use arrays minX[j], maxX[j] instead of minX and maxX */
	   /* Counters of food sources are also initialized in this function*/
	   int j;
	   for (j=0;j<dimension;j++){
	        r = ((maxX-minX)*MTrand.nextDouble()+ minX);//((double)Math.random()*32767 / ((double)32767+(double)(1)));
	        Foods[index][j]=r;//*(maxX-minX)+minX;
			solution[j]=Foods[index][j];
	   }
	   f[index]=calculateFunction(solution);
	   fitness[index]=CalculateFitness(f[index]);
	   trial[index]=0;
	}

//Step 1.a.1 evaluate function value of the candidate solution
	double calculateFunction(double sol[]){return fun.computeteFunction(sol,FunName);}
	

//Step 1.a.2  Fitness function based on the evaluated function value
	double CalculateFitness(double fun){
		 double result=0;
		 if(fun>=0) {
			 result=1/(fun+1);
		 }
		 else{
			 result=1+Math.abs(fun);
		 }
		 return result;
	}

//1 Initialization done....

//Step 2  The best food source is memorized - Preserving global best
	void MemorizeBestSource(){
	   int i,j;	    
		for(i=0;i<FoodNumber;i++)
		{
		if (f[i]<GlobalMin)
			{
	        GlobalMin=f[i];
	        for(j=0;j<dimension;j++)
	           GlobalParams[j]=Foods[i][j];
	        }
		}
	 }

//Step 3 - Send employee bee 
	void SendEmployedBees(){
	   int i,j;
	  /*Employed Bee Phase*/
	   for (i=0;i<FoodNumber;i++){
	        /*The parameter to be changed is determined randomly*/
	        //r = MTrand.nextDouble();
	        r = ((double) Math.random()*32767 / ((double)(32767)+(double)(1)) );
	        param2change=(int)(r*dimension);
	        
	        /*A randomly chosen solution is used in producing a mutant solution of the solution i*/
	        //r = MTrand.nextDouble();
	        r = ((double)Math.random()*32767 / ((double)(32767)+(double)(1)));
	        neighbour=(int)(r*FoodNumber); // randomly chosen neighbour

	        /*Randomly selected solution must be different from the solution i*/        
	        while(neighbour==i){
	          //r = MTrand.nextDouble();
	          r = ((double)Math.random()*32767 / ((double)(32767)+(double)(1)));
	          neighbour=(int)(r*FoodNumber);
	        }
	        for(j=0;j<dimension;j++)
	        solution[j]=Foods[i][j];

	        /* v_{ij} = x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
	        //r = MTrand.nextDouble();
	        r = ((double)Math.random()*32767 / ((double)(32767)+(double)(1)));
	        solution[param2change]=Foods[i][param2change]+(Foods[i][param2change]-Foods[neighbour][param2change])*(r-0.5)*2;

	        /*if generated parameter value is out of boundaries, it is shifted onto the boundaries*/
	        if (solution[param2change]<minX)
	           solution[param2change]=minX;
	        if (solution[param2change]>maxX)
	           solution[param2change]=maxX;
			
	        ObjValSol=calculateFunction(solution);
	        FitnessSol=CalculateFitness(ObjValSol);
	        
	        /*a greedy selection is applied between the current solution i and its mutant*/
	        if (FitnessSol>fitness[i]){  
				/*If the mutant solution is better than the current solution i, 
				replace the solution with the mutant and reset the trial counter of solution i*/
				trial[i]=0;
				for(j=0;j<dimension;j++)
					Foods[i][j]=solution[j];
				f[i]=ObjValSol;
				fitness[i]=FitnessSol;
			}
	        else
	        {   /*if the solution i can not be improved, increase its trial counter*/
	            trial[i]=trial[i]+1;
	        }
	   }
	}
//End Step 3 - send of employed bee phase

//Step 4
	/* A food source is chosen with the probability which is proportional to its quality*/
	/*Different schemes can be used to calculate the probability values*/
	/*For example prob(i)=fitness(i)/sum(fitness)*/
	/*or in a way used in the metot below prob(i)=a*fitness(i)/max(fitness)+b*/
	/*probability values are calculated by using fitness values and normalized by dividing maximum fitness value*/
	void CalculateProbabilities(){
	  int i;
	  double maxfit;
	  maxfit=fitness[0];
	  for (i=1;i<FoodNumber;i++){
	     if (fitness[i]>maxfit)
	         maxfit=fitness[i];
	  }
	  for (i=0;i<FoodNumber;i++){
	      prob[i]=(0.9*(fitness[i]/maxfit))+0.1;
	  }
	}

// Step 5	
	void SendOnlookerBees(){
	   int i,j,t;
	   i=0;
	   t=0;
	   /*onlooker Bee Phase*/
	   while(t<FoodNumber){
	        //r = MTrand.nextDouble();
	        r = ((double)Math.random()*32767 / ((double)(32767)+(double)(1)));
	        if(r<prob[i]){/*choose a food source depending on its probability to be chosen*/       
				t++;
	        
				/*The parameter to be changed is determined randomly*/
				//r = MTrand.nextDouble();
				r = ((double)Math.random()*32767 / ((double)(32767)+(double)(1)) );
				param2change=(int)(r*dimension);
	        
				/*A randomly chosen solution is used in producing a mutant solution of the solution i*/
				//r = MTrand.nextDouble();//((double)Math.random()*32767 / ((double)(32767)+(double)(1)));
				r = ((double)Math.random()*32767 / ((double)(32767)+(double)(1)));
				neighbour=(int)(r*FoodNumber);

				/*Randomly selected solution must be different from the solution i*/        
				while(neighbour == i){
					//System.out.println(Math.random()*32767+"  "+32767);
					//r = MTrand.nextDouble();
					r = ((double)Math.random()*32767 / ((double)(32767)+(double)(1)));
					neighbour=(int)(r*FoodNumber);
				}
				for(j=0;j<dimension;j++)
					solution[j]=Foods[i][j];

				/*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
				//r = MTrand.nextDouble();
				r = ((double)Math.random()*32767 / ((double)(32767)+(double)(1)));
				solution[param2change]=Foods[i][param2change]+(Foods[i][param2change]-Foods[neighbour][param2change])*(r-0.5)*2;

				/*if generated parameter value is out of boundaries, it is shifted onto the boundaries*/
				if (solution[param2change]<minX)
					solution[param2change]=minX;
				if (solution[param2change]>maxX)
					solution[param2change]=maxX;
				ObjValSol=calculateFunction(solution);
				FitnessSol=CalculateFitness(ObjValSol);
	        
				/*a greedy selection is applied between the current solution i and its mutant*/
				if (FitnessSol>fitness[i]){
						/*If the mutant solution is better than the current solution i, 
						replace the solution with the mutant and reset the trial counter of solution i*/
						trial[i]=0;
						for(j=0;j<dimension;j++)
							Foods[i][j]=solution[j];
						f[i]=ObjValSol;
						fitness[i]=FitnessSol;
				}
				else{   /*if the solution i can not be improved, increase its trial counter*/
					trial[i]=trial[i]+1;
				}
	          } /*if */
	          i++;
	          if (i==FoodNumber)
				i=0;
	        }/*while*/    
	}
// End Step 5 -  end of onlooker bee phase     */
	
	
/* Step 6 -  determine the food sources whose trial counter exceeds the "limit" value. 
             In Basic ABC, only one scout is allowed to occur in each cycle*/			 
	void SendScoutBees(){
		int maxtrialindex,i;
		maxtrialindex=0;
		for (i=1;i<FoodNumber;i++){
	         if (trial[i]>trial[maxtrialindex])
				maxtrialindex=i;
	    }
		if(trial[maxtrialindex]>=limit){
			init(maxtrialindex);
		}
	}
	
	public double[] execute(){
	    double returnResult[] = new double[103];
		//MTrand = new MersenneTwisterFast(SEED);	
		//System.out.println(SEED);
		initial(); // Step 1		    
		MemorizeBestSource(); //Step 2 : : find global best
       	long startTime = 0;
		long endTime = 0; 
		int iRet = 0;
		boolean fRet = true;
		int convergenceItr = 0; 
		startTime = System.currentTimeMillis();
		int iter;
		for (iter=0;iter<maxIter;iter++){
			if(iter%10 ==0){
				returnResult[iRet] = GlobalMin; iRet++; 
				//System.out.printf("%d > [",iter);
				//for(int dim=0;dim<dimension;dim++){System.out.printf(" %.2f ",GlobalParams[dim]);}
				//System.out.printf(" ] > %.3f \n",GlobalMin);
			}
			SendEmployedBees(); //Step 3
			CalculateProbabilities(); //Step 4
			SendOnlookerBees(); //Step 5
			MemorizeBestSource(); // repeat Step 2 : find global best
			SendScoutBees(); //Step 6
			//if(iter%100 ==0){System.out.printf("%d Value: %.3f \n",iter,GlobalMin);}
			//Termination criteria
		    if(GlobalMin < 0.0001 && fRet){convergenceItr = iter; fRet = false;endTime = System.currentTimeMillis();}
		    //if(GlobalMin > 0.0001){System.out.printf("%d Value: %.3f \n",iter,GlobalMin);} 
		}
		//for(j=0;j<dimension;j++){System.out.println("GlobalParam[%d]: %f\n",j+1,GlobalParams[j]);}
		//System.out.println((run+1)+".run:"+GlobalMin);
		if(convergenceItr == 0) endTime = System.currentTimeMillis();
		double timeTaken = (endTime - startTime) / 1000.0;//System.out.println("Execution time : " + timeTaken + " seconds");
		if(iter==maxIter){ returnResult[iRet] = GlobalMin; iRet++;}else{iRet = 100; returnResult[iRet] = 0.00001; iRet++;}
		if(convergenceItr == 0){returnResult[iRet] = iter; iRet++; }else{returnResult[iRet] = convergenceItr; iRet++;}
		returnResult[iRet] = timeTaken; iRet++; 		
		return returnResult;
	}
	
    public static void main(String[] args) {

	}
}
