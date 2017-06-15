package GA;
import Functions.*;
import Randoms.*;
import java.io.*;//import input output
import java.util.Random;//import random
import java.util.Collections;
import java.util.Vector;

public class GAOpt 
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
  
  private Vector popVector;
  private Vector subPopVector;
  private static float CROSS = (float) 0.5;
  private static float MUTATE = (float) 0.5; 
 
  //(R)---->calculate a random number where:  a <= randGen.randVal < b
  //public static double randGen.randVal(double a, double b){ return (b-a)*generator.nextDouble() + a;}
 
  public void createPopulation(){
      popVector = new Vector();
	  Population pop; 
	  double chrom[] = new double[dimension];
         //System.out.println("Choosen Value---->");
          for(int i=0;i<population;i++){
		    chrom = new double[dimension];
            for(int k=0;k<dimension;k++){   
                  chrom[k] = (float)randGen.randVal(minX,maxX); 
                  //System.out.printf(" [%1.3f]",ch[i][k]);
            }
			pop = new Population();
			pop.setIndividual(chrom);
			pop.setFitness(computeFitness(chrom)); 
            popVector.add(pop);
          }
  }//end createPopulation
  
  //Display the function fitness and the variables 
  public void displayPopulation(){
          for(int i=0;i<population;i++){
		    double fetchInd[] = ((Population)popVector.get(i)).getIndividual();
			double fetchFit = ((Population)popVector.get(i)).getFitness();
            for(int k=0;k<dimension;k++){   
                  System.out.printf(" %1.3f",fetchInd[k]);
            }
			System.out.printf(" -> %1.3f \n",fetchFit);
          }
  }//end display
  
  public double computeFitness(double ch[]){return fun.computeteFunction(ch,FunName);}
  
 
  public double[] Optimize(){
    double returnResult[] = new double[103];
    createPopulation(); //Initialize the population
     //System.out.println("Optimization starts...."); 
     double Optimum = 10e100;
       	long startTime = 0;
		long endTime = 0; 
		int iRet = 0;
		boolean fRet = true;
		int convergenceItr = 0; 
		startTime = System.currentTimeMillis();
	 int run = 0;
	 while(run < maxIter){
	   sorting();//Sorting according to the fitness of the function
       double currentFitInd[] = ((Population)popVector.get(0)).getIndividual();
	   double currentIndFit = ((Population)popVector.get(0)).getFitness(); 
	   if(currentIndFit < Optimum){
	      Optimum = currentIndFit;
		  //System.out.printf(" %d: %.3f \n",run,Optimum);
	   }
	   if(run%100 == 0){
			returnResult[iRet] = Optimum; iRet++;
			System.out.printf("%d > [",run);
			//for(int dim=0;dim<dimension;dim++){System.out.printf("  %.2f ",currentFitInd[dim]); }
			System.out.printf(" ] > %.3f \n",Optimum);
	   }	
	   //if(run==0) returnResult[2] = Optimum;
	   newGeneration(run);  
	   if(Optimum<0.0001 && fRet){convergenceItr = run; fRet = false;endTime = System.currentTimeMillis();break;}
	   run++;
	 }//end while
	 if(convergenceItr == 0) endTime = System.currentTimeMillis();
	 double timeTaken = (endTime - startTime) / 1000.0;//System.out.println("Execution time : " + timeTaken + " seconds");
	 returnResult[iRet] = Optimum; iRet++; 
	 if(convergenceItr == 0){returnResult[iRet] = run; iRet++; }else{returnResult[iRet] = convergenceItr; iRet++;}
	 returnResult[iRet] = timeTaken; iRet++; 		
	 return returnResult;
  }//end optimization
  
  public void sorting(){    
     //System.out.println("I M Performing Sorting");  
     for(int k = 1; k < popVector.size()-1; k++){
        for(int i=0; i < popVector.size()-k;i++){
           double fitess1 = ((Population)popVector.get(i)).getFitness();
           double fitess2 = ((Population)popVector.get(i+1)).getFitness();        
           Object temp = popVector.elementAt(i);   
           if(fitess1 >= fitess2){         
              popVector.setElementAt(popVector.elementAt(i+1),i);
              popVector.setElementAt(temp,i+1); 
           }
         }
     }
     //displayPopulation();	 
  }//end Sorting 
  
  public void newGeneration(int run){
     Vector tempPop = new Vector();
 
	 Population subPop;
	 double  p1[] = new double[dimension];
	 double  p2[] = new double[dimension];
	 double  ch1[] = new double[dimension];
	 double  ch2[] = new double[dimension];
	 tempPop.add((Population)popVector.get(0));
	 tempPop.add((Population)popVector.get(1));
     for(int i=2;i < population ;i++){
	     //System.out.println("The new generation..."+popVector.size());
	   	//elitism
	    //if(i < population*0.1){//use it for 10% Elitism 
			//tempPop.add((Population)popVector.get(i)); 
			//System.out.println("The Fault....."+population);
		
		//else{	           		
			//System.out.print("Generating Child\n");
			p1  = selectParent();
			p2  = selectParent();
		
			//Single point Crossover Operation
			if(shouldCrossover()){
				float num = (float)Math.random();
				int crossPoint = (int)(Math.random()*(dimension-2));
				for(int j = 0; j < dimension; j++){
					if(j < crossPoint){
						ch1[j] = p1[j];
						ch2[j] = p2[j];
					}
					else{
						ch1[j] = p2[j];
						ch2[j] = p1[j];
					}
				}
			}//Single Crossover Completed */
			else{ // do nothing or perform mutation
			    double var=2-run*((2)/maxIter); 
                if(shouldMutate()){ 				
					 for(int j = 0; j < dimension; j++){
						 ch1[j] = simplebounds(randGenJava.nextGaussian(p1[j],1));
						 ch2[j] = simplebounds(randGenJava.nextGaussian(p2[j],1));;
					 }
					//Single point mutation or Multipoint point mutation
					//float num = (float)Math.random();
					//int mutationPoint1 = (int)(Math.random()*(dimension-1));
					//int mutationPoint2 = (int)(Math.random()*(dimension-1));
					// int mutationPoint3 = (int)(Math.random()*(dimension-1));
		   
					//ch1[mutationPoint1] = (float)randGenJava.randVal(minX,maxX); 
					// ch1[mutationPoint2] = (float)randGenJava.randVal(minX,maxX); 
					// ch1[mutationPoint3] = (float)randGenJava.randVal(minX,maxX); 
					// ch2[mutationPoint1] = (float)randGenJava.randVal(minX,maxX); 
					//ch2[mutationPoint2] = (float)randGenJava.randVal(minX,maxX); 
					// ch2[mutationPoint3] = (float)randGenJava.randVal(minX,maxX); 
				}//end mutation
				else{//No crossover no mutation
					for(int j = 0; j < dimension; j++){
					ch1[j] = p1[j];
					ch2[j] = p2[j];
				}
				}
			}	 
		   double fit[] = new double[4];
		   fit[0] = computeFitness(ch1);
		   fit[1] = computeFitness(ch2);
		   fit[2] = computeFitness(p1);
		   fit[3] = computeFitness(p2);
		
/* 		int first = 0, second = 0;
		double min = 10e100;
		for(int j=0; j<4; j++)
		{
		  if(min < fit[j])
          { 
        	 min = fit[j];
			 first = j;
			 System.out.println("sdfd"+j);
		  }			 
		}
		
        for(int j=0; j<4 && j != first; j++)
		{ min = fit[j]; break;}
		
		for(int j=0; j<4 && j != first; j++)
		{
		  if(min < fit[j])
          { 
        	 min = fit[j];
			 second = j;
		  }			 
		}
		
		boolean flagch1 = false, flagch2 = false,flagP1 = false, flagP2 = false;
		
		if(first ==1){flagch1 = true;}else if(first ==2){flagch2 = true;}else if(first == 3){flagP1 = true;}else{flagP2 = true;}		
		if(second ==1){flagch1 = true;}else if(second ==2){flagch2 = true;}else if(second ==3){flagP1 = true;}else{flagP2 = true;}
		
	 if(flagch1)
	 { */
		subPop = new Population();
		subPop.setIndividual(ch1);
		subPop.setFitness(fit[0]);
		tempPop.add(subPop);
/* 	 }
	 if(flagch2)
     { */
		subPop = new Population();
		subPop.setIndividual(ch2);
		subPop.setFitness(fit[1]);
		tempPop.add(subPop);
/* 	 }
	 if(flagP1)
	 { 
		subPop = new Population();
		subPop.setIndividual(p1);
		subPop.setFitness(fit[2]);
		tempPop.add(subPop);
	 }
	 if(flagP2)
	 {
		subPop = new Population();
		subPop.setIndividual(p2);
		subPop.setFitness(fit[3]);
		tempPop.add(subPop);
	  } */
     // }//	
	}//end population
	//System.out.println("FinalNew Pop "+tempPop.size());
	popVector.clear();
	popVector.addAll(tempPop);
 }//end new generation 
 
  // Application of simple constraints
  public double simplebounds(double param){
	if (param < minX){return minX;}
	else if (param > maxX){return maxX;}
	else{return param;}
  }//Simplebouds
 
  private boolean shouldCrossover() {
		float num = (float)Math.random();
		int number = (int) (num*100);
		num = (float) number/100;
		return (num <= CROSS);
  } 
  private boolean shouldMutate(){
		float num = (float)Math.random();
		int number = (int) (num*100);
		num = (float) number/100;
		return (num <= MUTATE);
  }
  
 public double[] selectParent(){
	 double P[]  = new double[population];
	 
	 //System.out.println("Fitness of the population");
	 for(int i=0;i<population;i++){
			P[i] = ((Population)popVector.get(i)).getFitness();
			//System.out.printf(" %1.3f ",P[i]);
     }
	 //System.out.println("\nComputing Probability");
     P = probability(P);	 
	 double R = randGenJava.randVal(0,0.8); 
    return ((Population)popVector.get(RWS(P,R))).getIndividual();	  
 }
  

  public double[] probability(double y[]){

     double x[] = new double[population];

     for(int k=0;k< population;k++){
	     x[k]= y[k];
        //System.out.printf(" %.3f ",x[k]); 
     }
     //System.out.println();
   
     double sum = 0.0;
     for(int k=0;k< population;k++){
	    sum = sum + x[k];
     }
     //System.out.printf(" = %.3f \n",sum); 
     for(int k=0;k< population;k++){
	    x[k] = (float)sum/x[k];
        //System.out.printf(" %.3f ",x[k]);
     }
     //System.out.println();
     double sum1 = 0.0;
     for(int k=0;k< population;k++){
	    sum1 = sum1 + x[k];
     }
     for(int k=0;k< population;k++){
	     x[k] = x[k]/(float)sum1;
         //System.out.printf(" %.3f ",x[k]);
     }    
     double sum2 = 0.0;
     for(int k=0;k< population;k++){
	  sum2 = sum2 + x[k];
     }
     //System.out.printf(" = %.3f \n",sum2);      	 
     return x; 	  
  }
  
  public int RWS(double x[], double R){
     int i=0; 
     double sum=0.0;
     for(i=0; i< population; i++){
       sum += x[i];
       if(R < sum)
            break;
      //System.out.printf("  %.3f  ",sum); 
     }
     //System.out.printf("-->  %d <--- ",i); 
     return i; 
  }
    
  public static void main(String args[]) {
    GAOpt ga= new GAOpt();
    //ga.createPopulation();
	//ga.displayPopulation();
    System.out.printf(ga.FunName+" Value : %.3f",ga.Optimize());
    
  }    
	
        
}