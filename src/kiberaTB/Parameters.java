package kiberaTB;

import java.io.File;
import java.io.IOException;

//import kiberaTB.ParameterNew.GlobalParamters;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

//contains all parameters of KiberaTB model

public class Parameters {

	// private static final long serialVersionUID = 1L;
    private static final long serialVersionUID = System.nanoTime();
    final public static double[] ethnicDistribution = { .21, .14, .12, .12, .12, .06, .05, .05, .02, .01, .01, .09 };
	final public static String[] ethnicities = { "kikuyu", "luhya", "luo", "kalinjin", "kamba", "kisii", "meru", "mijikenda", "maasai",
			"turkana", "embu", "other" }; //from 1-12 represents each ethnicity 
	
	final public static double MinimumWaterRequirement = 2;
	final public static double MaximumWaterRequirement = 15;
	
	//how long does it take for someone to get infected and then be positive on a TB blood test 
	//Source: http://www.vanderbilt.edu/HRS/wellness/OHC/ohctb.pdf
	final public static int incubationPeriod_MIN = 336;
	final public static int incubationPeriod_MAX = 2016;

	//If someone without HIV is to develop TB disease, it will happen with a 10% of in the next two years (or 17520 hours)
	//Source: http://www.ncbi.nlm.nih.gov/books/NBK64533/
	final public static int timeForInfectionToDisease_MAX = 17520;

	//Minimum CD4 count for someone to be considered HIV+
	//Source: http://hab.hrsa.gov/deliverhivaidscare/clinicalguide11/cg-206_cd4_monitoring.html | http://www.scielo.br/scielo.php?script=sci_arttext&pid=S1413-86702003000200010
	final public static double HIVCD4Count_MAX = 350; //350 cells/mm^3

	//CD4 count drop per hour (timestep) for someone with untreated HIV is 50-80 cells/mm^3 per year
	//Source: http://hab.hrsa.gov/deliverhivaidscare/clinicalguide11/cg-206_cd4_monitoring.html
//	final public static double CD4CountDrop_Min = .00571;
//	final public static double CD4CountDrop_Max = .00913;
//
//	//The CD4 count when a TB Infection becomes a TB Disease has to be between 282-314 (or a difference of calculated 36-68 cells)
//	//Source: http://malthus.micro.med.umich.edu/lab/pubs/mmnp2008714.pdf | http://www.scielo.br/scielo.php?script=sci_arttext&pid=S1413-86702003000200010
//	final public static double CD4CountForTB_Min = 282;
//	final public static double CD4CountForTB_Max = 314;
//        
        
        
        //Global ART HIV treatment coverage  54% [50–58%]
        //http://www.unaids.org/en/resources/presscentre/pressreleaseandstatementarchive/2016/may/20160531_Global-AIDS-Update-2016
        
        // Nairbi Kenya, the coverage is 87%  but viral suppression is only 28%
        //http://nacc.or.ke/wp-content/uploads/2016/12/Kenya-HIV-County-Profiles-2016.pdf
        //https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4786174/pdf/nihms-764946.pdf
        // we start with 50% coverage in Kibera 

	// TODO: CONFIRM
        final public static double HBVTreatmentHIVARTCoverage = 0.5; // how many out the HIV patient got antiretroviral therapy (ART).

	//Survival time for someone with untreated HIV is 0-12 years (or 0-105120 hours)
        // with HIV treatment, it might be more 
	//Source: http://www.hiv.va.gov/patient/faqs/life-expectancy-with-HIV.asp
	final public static int HIVSurvivalTime = 105120;

	//Average Survival time for someone without HIV and untreated TB is less than 3 years ( <= 26280 hours)
        // but this is only without hiv treatment- Now adays thre is treatment
	//Source: http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0017601
	final public static int TBWithoutHIVSurvivalTime = 26280;

	//Average survival time for someone with HIV and untreated TB is 6 months ( <= 4380 hours)
	//Source: http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0017601
	final public static int TBWithHIVSurvivalTime = 4380;

	//Time from when a TB patient starts treatment to when they are not contagious anymore is 2-4 weeks
	//Source: http://hss.sbcounty.gov/psd/Forms/Health_Forms/Tuberculosis.htm
	final public static int contagiousPeriod_Min = 336;
	final public static int contagiousPeriod_Max = 672;

	//------------------------Health Parameters-----------------------

	//percent chance that someone with HIV and latent TB infection will develop TB disease in one year 
	//Source: (http://www.colorado.gov/cs/Satellite?blobcol=urldata&blobheadername1=Content-Disposition&blobheadername2=Content-Type&blobheadervalue1=inline%3B+filename%3D%22TB+and+HIV.pdf%22&blobheadervalue2=application%2Fpdf&blobkey=id&blobtable=MungoBlobs&blobwhere=1251811775014&ssbinary=true)
	final public static double latentToDiseaseRateHIV_MIN = .07;
	final public static double latentToDiseaseRateHIV_MAX = .1;

	//percent chance that someone WITHOUT HIV and latent TB infection will develop TB in their lifetime
	//Source: http://www.cdc.gov/tb/publications/factsheets/general/ltbiandactivetb.htm 
	final public static double latentToDiseaseRate_Min = .05;
	//	public double latentToDiseaseRate_MAX = .1;
	final public static double latentToDiseaseRate_MAX = .15;
         // life expentancy in Kibera 30 years http://kiberalawcentre.org/facts/ 
        
        //https://math.stackexchange.com/questions/1060114/calculating-lifetime-probability-from-annual-incidence
	 // %occu = 1−(1−p)^timeperiod using this formula
       // A Poisson distribution would be a plausible model. Let λ be the rate parameter describing the number of events per person per year; then represents the random number of events observed per person per p years, with probability mass function
        //Pr[X=x]=e−pλ(pλ)xx!,x=0,1,2,…
        //1−Pr[X=0]=1−e−pλ.
        final public static double latentToDiseaseLifeTime_MIN = 0.000000195;//  0.000000195;// once in lifetime 30 yr *  365day * 24 hour = 262800  with 5%
        final public static double latentToDiseaseLifeTime_MAX =  0.000000618;//0.000000618; //once in lifetime 30 yr *  365day * 24 hour = 262800  with 15%
	
        final public static double latentToDiseaseLifeTimeHIV_MIN = 0.00000838;//0.00000838; // once  1 yr *  365day * 24 hour = 8760  with 7%
        final public static double latentToDiseaseLifeTimeHIV_MAX =  0.0000120;//0.0000120; //once  1 yr *  365day * 24 hour = 8760  with 10%
	
       
        final public static double diseaseToLatentDuetoTreatmentDropputPro = 0.1; // 10% from acctive to latent
        
        // source: http://ac.els-cdn.com/S0040580900914515/1-s2.0-S0040580900914515-main.pdf?_tid=17697400-cd98-11e4-9886-00000aab0f02&acdnat=1426701564_53eab51c22fffa757f4918855d75bb7b
        // Feng et al 1999   16 per 100,000
        // source: http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3407677/    - important source about recurrent and reinfection
         final public static double reInfectionRateLatent = 0.06; // 4 to 9 9% pass to second episod of infection - hiv 
        
        // source http://jid.oxfordjournals.org/content/201/5/653.full  2-3% 
        //http://jid.oxfordjournals.org/content/201/5/691.full
        //"Rate of Reinfection Tuberculosis after Successful Treatment Is Higher than Rate of New Tuberculosis 
         //Read More: http://www.atsjournals.org/doi/full/10.1164/rccm.200409-1200OC#.VipQVBFViko
         //Recovered individuals can move back to the latent compartment, since treatment is thought to not completely eliminate the bacteria nor to give protection against future exposure.
        final public static double recoveredToLatentProb = 0.002;//0.0016; // in capetown, the reinfection after cure is about 85% per year - considering it per hour - 
        
        
        // need reference
        final public static double lantentToRecovered = 0.0000003;// need reference
        
        
        // time staying on latent phase if they are HIV -ve. After this phase body can reocver
        //http://www.cdc.gov/tb/education/corecurr/default.htm page 42
        //It can take 2 to 8 weeks after the initial TB infection for the body’s immune system to be able
        //to react to tuberculin and for the infection to be detected by the TST or IGRA.
        //Within weeks after infection, the immune system is usually able to halt the multiplication of the tubercle bacilli, preventing further progression.
        final public static int latentPhase_Min =  336;//time where staying in exposed phase rather than latent phase
	final public static int latentPhase_Max =  2344; // with 12 weeks at the max 
        
        
          //http://www.jimmunol.org/content/185/1/15.full
       // In the United States, close-contact investigations revealed that 
        //an estimated 20–30% of close contacts had latent infection, and another 1% had active TB (2, 13). 
        //This indicates that 70–80% of exposed individuals do not become infected. 
        
        final public static double exposedToLatentPhaseMin = 0.05;// 0.2; // 20-30% goes to latent  -- but imagine this withing the latent phase 20 % divided by exposed phase length 
        final public static double exposedToLatentPhaseMax = 0.1;//0.3; // 20-30% goes to latent
        
        final public static double exposedToActiveTBPhaseMin = 0.00001;//0.005; // 1% goes to active tb
        final public static double exposedToActiveTBPhaseMax = 0.00005;//0.01; // 1% goes to active tb
        
        final public static double likelyExposedTBtoLatentorActiveWithHIV = 20; // 10 to 20 times more than hiv-ve people 
        
        
      

	//Time it takes to get back a TB Test
	//Source: http://www.cdc.gov/tb/topic/testing/default.htm
	final public static int TBTestingTime_Min = 48; // 
	final public static int TBTestingTime_Max = 72; // 72;

	//The infectious dose (minimum doseage of TB bacilli for latent infection) is 1-10 bacillus 
	//Source: http://www.biosafety.be/CU/PDF/Mtub_Final_DL.pdf
	final public static double TBInfectionDose_MIN = 1;
	final public static double TBInfectionDose_MAX = 10;
        
      
	//Concentration of TB in saliva
	//Source: http://www.camra.msu.edu/documents/CAMRA_TB_ALERT.pdf and Yaeger et. al (1967)
        
        final public static double TBBacilliConcentration_Max = 650000; //bacilli per mL - high ( average can be 70,000)

	//Concentration of saliva per cough
	//Source: http://www.camra.msu.edu/documents/CAMRA_TB_ALERT.pdf 
	final public static double salivaPerCough = .0000006; //ml per cough 
        
        final public static double bacilliClearanceRatePerHour = 0.8;

	//Coughs per hour. Average is 10, normal distribution is used 
	//Source: http://www.camra.msu.edu/documents/CAMRA_TB_ALERT.pdf
	final public static int coughsPerHour_Min = 5;
	final public static int coughsPerHour_Max = 15;

	//Contagious period after treatment in hours
	//Source: http://cdhd.idaho.gov/CD/public/factsheets/tuberculosis.htm
	final public static int contagiousPeriodAfterTreatment_Min = 336;
	final public static int contagiousPeriodAfterTreatment_Max = 2016;

	
	//Body health (arbritrary chosen, everything adapts to it)
	final public static double bodyHealth_Max = 100.0;
        
        final public static double mortalityRateNaturalDeath = 0.00003;// for suscp and recovered - set zero since no birth considered in the model
        final public static double mortalityRateLatentTB = 0.03; // including secondary exposure
        final public static double mortalityRateActiveTB = 0.03;
        final public static double moratlityRateActiveTB_HIV_NoART =0.1;
        final public static double moratlityRateActiveTB_HIV_ART =0.1;
        
                

	//-----Modifier methods-----------------
        
        // other reference- hetrogenous people - http://www.nature.com/nm/journal/v10/n10/full/nm1110.html 
        
	
    GlobalParamters globalParam = new GlobalParamters();
   
    
    private final static String A_FILE = "-file";

    public Parameters(String[] args) {
        if (args != null) {
            loadParameters(openParameterDatabase(args));
        }
    }
    
    
    private static ParameterDatabase openParameterDatabase(String[] args) {
        ParameterDatabase parameters = null;
        for (int x = 0; x < args.length - 1; x++) {
            if (args[x].equals(A_FILE)) {
                try {
                    File parameterDatabaseFile = new File(args[x + 1]);
                    parameters = new ParameterDatabase(parameterDatabaseFile.getAbsoluteFile());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            }
        }
        if (parameters == null) {
            System.out.println("\nNo parameter file was specified");//("\nNo parameter file was specified");
            parameters = new ParameterDatabase();
        }
        return parameters;
    }

    private void loadParameters(ParameterDatabase parameterDB) {
    
    	 // global
        globalParam.setwriteGridTimerFrequency(returnIntParameter(parameterDB, "writeGridTimerFrequency",
                globalParam.getwriteGridTimerFrequency()));

    	globalParam.setExpFileName(returnStringParameter(parameterDB, "experimentName",globalParam.getExpFileName()));
        globalParam.setNumberOfResidents(returnIntParameter(parameterDB, "numResidents", globalParam.getNumberOfResidents()));          
        globalParam.setHIVPrevalenceRate(returnDoubleParameter(parameterDB, "HIVPrevalanceRate", globalParam.getHIVPrevalenceRate()));
        globalParam.setTBLatentInfectionPrevalanceRate(returnDoubleParameter(parameterDB, "TBInfectionPrevalanceRate", globalParam.getTBLatentInfectionPrevalanceRate()));
        globalParam.setTBDiseasePrevalanceRate(returnDoubleParameter(parameterDB, "TBDiseasePrevalanceRate", globalParam.getTBDiseasePrevalanceRate()));
        globalParam.setIsOntrement(returnBooleanParameter(parameterDB, "IsOnTreatment", globalParam.getIsOnTreatment()));
        globalParam.setPercentQuitBeforeART(returnDoubleParameter(parameterDB, "percentQuitBeforeART", globalParam.getPercentQuitBeforeART()));
        globalParam.setPercentQuitDuringART(returnDoubleParameter(parameterDB, "percentQuitDuringART", globalParam.getPercentQuitDuringART()));
      
         
        // facility
        

        globalParam.setschoolCapacity(returnIntParameter(parameterDB, "schoolCapacity", globalParam.getschoolCapacity()));          
    	globalParam.sethhCapacity(returnIntParameter(parameterDB, "hhCapacity", globalParam.gethhCapacity()));          
    	globalParam.setbusinessCapacity(returnIntParameter(parameterDB, "businessCapacity", globalParam.getbusinessCapacity()));          
    	globalParam.setformalBusinessCapacity(returnIntParameter(parameterDB, "formalBusinessCapacity", globalParam.getformalBusinessCapacity()));   
    	globalParam.setinformalBusinessCapacity(returnIntParameter(parameterDB, "informalBusinessCapacity", globalParam.getinformalBusinessCapacity()));   
    	globalParam.setmaxStructuresPerParcel(returnIntParameter(parameterDB, "maxStructuresPerParcel", globalParam.getmaxStructuresPerParcel()));    
    	
    	
    	
        /// agent
        globalParam.setmaleDistribution(returnDoubleParameter(parameterDB, "maleDistribution", globalParam.getmaleDistribution()));
        globalParam.setethnicityThreshold(returnDoubleParameter(parameterDB, "ethnicityThreshold", globalParam.getethnicityThreshold()));
        globalParam.setageAdult(returnDoubleParameter(parameterDB, "ageAdult", globalParam.getageAdult()));
        globalParam.setageChildrenUnder6(returnDoubleParameter(parameterDB, "ageChildrenUnder6", globalParam.getageChildrenUnder6()));
        globalParam.setpercentOfResidentsUnder6(returnDoubleParameter(parameterDB, "percentOfResidentsUnder6", globalParam.getpercentOfResidentsUnder6()));
        globalParam.setpercentOfResidentsUnder19(returnDoubleParameter(parameterDB, "percentOfResidentsUnder19", globalParam.getpercentOfResidentsUnder19()));
        globalParam.setMaximumNumberofContact(returnIntParameter(parameterDB, "maximumNumberofContact", globalParam.getMaximumNumberofContact()));
      
        
    }
    
    
    // converter parameter values - double, int, string
    
    public int returnIntParameter(ParameterDatabase paramDB, String parameterName, int defaultValue) {
        return paramDB.getIntWithDefault(new Parameter(parameterName), null, defaultValue);
    }

    public boolean returnBooleanParameter(ParameterDatabase paramDB, String parameterName, boolean defaultValue) {
        return paramDB.getBoolean(new Parameter(parameterName), null, defaultValue);
    }

    public double returnDoubleParameter(ParameterDatabase paramDB, String parameterName, double defaultValue) {
        return paramDB.getDoubleWithDefault(new Parameter(parameterName), null, defaultValue);
    }
    
    public String returnStringParameter(ParameterDatabase paramDB, String parameterName, String defaultValue) {
        return paramDB.getStringWithDefault(new Parameter(parameterName), null, defaultValue);
    }
   
    
    
    public class GlobalParamters {
	//Model Global Parameters
    	private String experimentName = "Default";
    	private  int numResidents = 50000;
    	private  double TBLatentInfectionPrevalanceRate = .35 ;
    	private  double TBDiseasePrevalanceRate = 0.02;
    	private  double HIVPrevalanceRate = 0001; //.14;//HIV Prevelance Rate in Kibera is 14% 	//Source: http://d-scholarship.pitt.edu/10936/1/PattersonHThesis2011_(2).pdf
    	private boolean isOntrement  = true;
        
       //Percent chance that someone will quit TB Antiretroviral treatment (ART) in the beginning or during in Kibera
	//Source: http://fieldresearch.msf.org/msf/bitstream/10144/204570/1/Tayler-Smith%20ARV%20Uptake%20TB%20Kibera%20TMIH.pdf
	private double percentQuitBeforeART = .020; //0.01 --30% will quit, they are also move out of the area 
	// probability of getting treatment -
        private double percentQuitDuringART = .015; //0.05 --20.2% of patients will quit during treatment-- including people move out - some better lld movement out of the areato amke it small since the model does not h


        
    	public void setExpFileName(String f){
            this.experimentName =f;
        }
        public String getExpFileName(){
            return experimentName;
        }
        
    	public  int getNumberOfResidents() {
    		return numResidents;
    	}

    	public  void setNumberOfResidents(int val) {
    		numResidents = val;
    	}

    	public  double getHIVPrevalenceRate() {
    		return HIVPrevalanceRate;
    	}

    	public  void setHIVPrevalenceRate(double val) {
    		HIVPrevalanceRate = val;
    	}

    	public  double getTBLatentInfectionPrevalanceRate() {
    		return TBLatentInfectionPrevalanceRate;
    	}

    	public  void setTBLatentInfectionPrevalanceRate(double val) {
    		TBLatentInfectionPrevalanceRate = val;
    	}
    	
    	public  double getTBDiseasePrevalanceRate() {
    		return TBDiseasePrevalanceRate;
    	}

    	public  void setTBDiseasePrevalanceRate(double val) {
    		TBDiseasePrevalanceRate = val;
    	}
        
        public  boolean getIsOnTreatment() {
    		return isOntrement;
    	}

    	public  void setIsOntrement(boolean val) {
    		isOntrement = val;
    	}
        
        public  void setPercentQuitBeforeART(double val) {
    		percentQuitBeforeART = val;
    	}
    	
    	public  double getPercentQuitBeforeART() {
    		return percentQuitBeforeART;
    	}
        public  void setPercentQuitDuringART(double val) {
    		percentQuitDuringART = val;
    	}
    	
    	public  double getPercentQuitDuringART() {
    		return percentQuitDuringART;
    	}
        
        
 
    	//Facility Parameters
    	private  int schoolCapacity = 600; //all parcels have a max capacity of 400. Except parcels with schools on it which have this capacity
    	private  int hhCapacity = 5; //number of households per structure. Average from the Kianda excel sheet 
    	private int businessCapacity = 2; //number of businesses per structure. Average from the Kianda excel sheet
    	private  int formalBusinessCapacity = 15; //average is 15, minimum for formal is 10. 
    	private  int informalBusinessCapacity = (int) (.00002 * numResidents);
    	private  int maxStructuresPerParcel = 1;
    	
    	public  int getschoolCapacity() {
    		return schoolCapacity;
    	}

    	public  void setschoolCapacity(int val) {
    		schoolCapacity = val;
    	}
    	public  int gethhCapacity() {
    		return hhCapacity;
    	}

    	public  void sethhCapacity(int val) {
    		hhCapacity = val;
    	}
    	public  int getbusinessCapacity() {
    		return businessCapacity;
    	}

    	public  void setbusinessCapacity(int val) {
    		businessCapacity = val;
    	}
    	public  int getformalBusinessCapacity() {
    		return formalBusinessCapacity;
    	}

    	public  void setformalBusinessCapacity(int val) {
    		formalBusinessCapacity = val;
    	}
    	public  int getinformalBusinessCapacity() {
    		return (int) (.00002 * getNumberOfResidents());
    	}

    	public  void setinformalBusinessCapacity(int val) {
    		informalBusinessCapacity = val;
    	}
    	public  int getmaxStructuresPerParcel() {
    		return maxStructuresPerParcel;
    	}

    	public  void setmaxStructuresPerParcel(int val) {
    		maxStructuresPerParcel = val;
    	}
    	
    	      
    	

    	//Agent Parameters
    	
    	private double maleDistribution = 0.613; //Kibera surveys
    	private double ethnicityThreshold = .40; //threshold to create segregation
    	/** Age distribution */
    	private double ageAdult = .25; //this is the percentage of total residents (excluding head of households) that are adults
    	private double ageChildrenUnder6 = .32; //the percentage of total residents (excluding head of households) under 6
    	private double percentOfResidentsUnder6 = .21; //the percentage of total residents that are under 6 and thus cannot be employed
    	private double percentOfResidentsUnder19 = .45; //the percentage of total residents 18 and younger (source - Kianda survey)
    	private int maximumNumberofContact = 10; // maximum people to interact per hour
        private int writeGridTimerFrequency = 720; // hours?

    	
    	public  double getmaleDistribution() {
    		return maleDistribution;
    	}

    	public  void setmaleDistribution(double val) {
    		maleDistribution = val;
    	}
    	public  double getethnicityThreshold() {
    		return ethnicityThreshold;
    	}

    	public  void setethnicityThreshold(double val) {
    		ethnicityThreshold = val;
    	}
    	public  double getageAdult() {
    		return ageAdult;
    	}

    	public  void setageAdult(double val) {
    		ageAdult = val;
    	}
    	public  double getageChildrenUnder6() {
    		return ageChildrenUnder6;
    	}

    	public  void setageChildrenUnder6(double val) {
    		ageChildrenUnder6 = val;
    	}
    	public  double getpercentOfResidentsUnder6() {
    		return percentOfResidentsUnder6;
    	}

    	public  void setpercentOfResidentsUnder6(double val) {
    		percentOfResidentsUnder6 = val;
    	}
    	
    	public  double getpercentOfResidentsUnder19() {
    		return percentOfResidentsUnder19;
    	}

    	public  void setpercentOfResidentsUnder19(double val) {
    		percentOfResidentsUnder19 = val;
    	}
        public void setwriteGridTimerFrequency(int hh) {
            this.writeGridTimerFrequency = hh;
        }

        public int getwriteGridTimerFrequency() {
            return this.writeGridTimerFrequency;
        }
        public void setMaximumNumberofContact(int hh) {
            this.maximumNumberofContact = hh;
        }

        public int getMaximumNumberofContact() {
            return this.maximumNumberofContact;
        }
        

    
    }
}
