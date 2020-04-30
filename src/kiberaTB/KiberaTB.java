/*
Parth Chopra, Summer 2013, GMU under mentorship of Dr. Andrew Crooks.
Contact: parthchopra28@gmail.com
 */
//High rate of transmission within households
//Transmission of tuberculosis within family-households
//http://www.ajol.info/index.php/mmj/article/viewFile/10770/14432
//http://www.ncbi.nlm.nih.gov/pubmed/9192937
//http://www.researchgate.net/profile/Tomasz_Jagielski/publication/221825500_Transmission_of_tuberculosis_within_family-households/links/00b7d51cb7d95d5ca6000000.pdf
package kiberaTB;

//-----imports------------
import ec.util.MersenneTwisterFast;
import java.util.Arrays;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.xy.XYSeries;
import com.opencsv.CSVWriter;

import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomVectorField;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Network;
import sim.util.*;
import java.io.*;
import sim.field.geo.GeomGridField;

//------------------------
public class KiberaTB extends SimState {

    // -------------------Grids--------------------------------------------------------
    public ObjectGrid2D landGrid; // The model environment - holds parcels
    public Continuous2D world; //world for agent movement.
    public SparseGrid2D householdGrid; //contains grid with all households
    public IntGrid2D roadGrid; //contains roadStructure
    public SparseGrid2D schoolGrid; //contains grid with all schools 
    public SparseGrid2D healthFacilityGrid; //contains grid with all health facilities 
    public SparseGrid2D religiousFacilityGrid; //contains grid with all religious facilities (mainly churches, some mosques) 
    public ObjectGrid2D closestNodes; //the road nodes closest to each of the locations
    public SparseGrid2D nodes; //nodes of the system for movement
    public SparseGrid2D businessGrid; //contains grid with all businesses
    public SparseGrid2D waterSourcesGrid; //contains grid with all public water sources
    public SparseGrid2D restaurantGrid;
    public final Parameters params;
    public GeomGridField allCampGeoGrid;
    // ----------------------------------------------------------------------------------------

    // -----------------Bags -----------------------------------------------------------------
    public Bag allParcels;
    public Bag allResidents;
    public Bag allHouseholds;
    public Bag allStructures;
    public Bag allBusinesses;
    public Bag allHealthFacilities;
    public Bag allReligiousFacilities;
    public Bag allRestaurants;
    public Bag allLatrines;
    public Bag allWaterSources;
    public Bag allSchools;
    public Bag availableParcels;
    public Bag allFacilities;
    public Bag allEmployers;

    public Bag restaurantLocations;
    public Bag schoolLocations;
    public Bag waterLocations;
    public Bag healthLocations;
    public Bag religiousLocations;
    public Bag businessLocations;

    public Bag businessTier1Locations;
    public Bag businessTier2Locations;
    public Bag businessTier3Locations;
    // ----------------------------------------------------------------------------------------------

    // ----------- internal settings------------------------------
    private int width;
    private int height;
    //public int numResidents = Parameters.numResidents;
    public int deadAgents = 0;

    //public int schoolCapacity = Parameters.schoolCapacity;
    public int waterCapacity;
    public int restaurantCapacity;
    public int healthCapacity;
    public int religiousCapacity;

    //public int hhCapacity = Parameters.hhCapacity; //number of households per structure. Average from the Kianda excel sheet 
//	public int businessCapacity = Parameters.businessCapacity; //number of businesses per structure. Average from the Kianda excel sheet
//	public int formalBusinessCapacity = Parameters.formalBusinessCapacity; //average is 15, minimum for formal is 10. 
//	public int informalBusinessCapacity = Parameters.informalBusinessCapacity;
    // --------------------------------------------------------------
    //------------------------Health Parameters-----------------------
    final public static int latentPhase_Min = Parameters.latentPhase_Min; // time where staying in exposed phase rather than latent phase
    final public static int latentPhase_Max = Parameters.latentPhase_Max;

    //percent chance that someone with HIV and latent TB infection will develop TB disease in one year 
    //Source: (http://www.colorado.gov/cs/Satellite?blobcol=urldata&blobheadername1=Content-Disposition&blobheadername2=Content-Type&blobheadervalue1=inline%3B+filename%3D%22TB+and+HIV.pdf%22&blobheadervalue2=application%2Fpdf&blobkey=id&blobtable=MungoBlobs&blobwhere=1251811775014&ssbinary=true)
    public double latentToDiseaseRateHIV_MIN = Parameters.latentToDiseaseRateHIV_MIN;
    public double latentToDiseaseRateHIV_MAX = Parameters.latentToDiseaseRateHIV_MAX;

    //percent chance that someone WITHOUT HIV and latent TB infection will develop TB in their lifetime
    //Source: http://www.cdc.gov/tb/publications/factsheets/general/ltbiandactivetb.htm
    public double latentToDiseaseRate_Min = Parameters.latentToDiseaseRate_Min;
    public double latentToDiseaseRate_MAX = Parameters.latentToDiseaseRate_MAX;
    // Source: life expentancy in Kibera 30 years http://kiberalawcentre.org/facts/ 
    final public static double latentToDiseaseLifeTime_MIN = Parameters.latentToDiseaseLifeTime_MIN; //  once in lifetime 30 yr *  365day * 24 hour = 262800  with 5%
    final public static double latentToDiseaseLifeTime_MAX = Parameters.latentToDiseaseLifeTime_MAX;  // once in lifetime 30 yr *  365day * 24 hour = 262800  with 15%

    final public static double latentToDiseaseLifeTimeHIV_MIN = Parameters.latentToDiseaseLifeTimeHIV_MIN; //  once in lifetime 1 yr *  365day * 24 hour = 8760  with 7%
    final public static double latentToDiseaseLifeTimeHIV_MAX = Parameters.latentToDiseaseLifeTimeHIV_MAX;  // once in lifetime 1 yr *  365day * 24 hour = 8760  with 10%
    final public static double recoveredToLatentProb = Parameters.recoveredToLatentProb;
    final public static double lantentToRecovered = Parameters.latentToRecovered;

    final public static double diseaseToLatentDuetoTreatmentDropputPro = Parameters.diseaseToLatentDuetoTreatmentDropoutPro;
    // source:  //http://www.jimmunol.org/content/185/1/15.full
    final public static double exposedToLatentPhaseMin = Parameters.exposedToLatentPhaseMin; // 20-30% goes to latent
    final public static double exposedToLatentPhaseMax = Parameters.exposedToLatentPhaseMax; // 20-30% goes to latent
    //source: //http://www.jimmunol.org/content/185/1/15.full
    final public static double exposedToActiveTBPhaseMin = Parameters.exposedToActiveTBPhaseMin; // 1% goes to active tb
    final public static double exposedToActiveTBPhaseMax = Parameters.exposedToActiveTBPhaseMax; // 1% goes to active tb
    final public static double likelyExposedTBtoLatentorActiveWithHIV = Parameters.likelyExposedTBtoLatentorActiveWithHIV;
    final public static double reInfectionRateLatent = Parameters.reInfectionRateLatent;
    //how long does it take for someone to get infected and then be positive on a TB blood test 
    //Source: http://www.vanderbilt.edu/HRS/wellness/OHC/ohctb.pdf
    public int incubationPeriod_MIN = Parameters.incubationPeriod_MIN;
    public int incubationPeriod_MAX = Parameters.incubationPeriod_MAX;

    //If someone without HIV is to develop TB disease, it will happen with a 10% of in the next two years (or 17520 hours)
    //Source: http://www.ncbi.nlm.nih.gov/books/NBK64533/
    public int timeForInfectionToDisease_MAX = Parameters.timeForInfectionToDisease_MAX;

    //Minimum CD4 count for someone to be considered HIV+
    //Source: http://hab.hrsa.gov/deliverhivaidscare/clinicalguide11/cg-206_cd4_monitoring.html | http://www.scielo.br/scielo.php?script=sci_arttext&pid=S1413-86702003000200010
    public double HIVCD4Count_MAX = Parameters.HIVCD4Count_MAX; //350 cells/mm^3

    //CD4 count drop per hour (timestep) for someone with untreated HIV is 50-80 cells/mm^3 per year
    //Source: http://hab.hrsa.gov/deliverhivaidscare/clinicalguide11/cg-206_cd4_monitoring.html
//	public double CD4CountDrop_Min = Parameters.CD4CountDrop_Min;
//	public double CD4CountDrop_Max = Parameters.CD4CountDrop_Max;
//
//	//The CD4 count when a TB Infection becomes a TB Disease has to be between 282-314 (or a difference of calculated 36-68 cells)
//	//Source: http://malthus.micro.med.umich.edu/lab/pubs/mmnp2008714.pdf | http://www.scielo.br/scielo.php?script=sci_arttext&pid=S1413-86702003000200010
//	public double CD4CountForTB_Min = Parameters.CD4CountForTB_Min;
//	public double CD4CountForTB_Max = Parameters.CD4CountForTB_Max;
//
    //Survival time for someone with untreated HIV is 0-12 years (or 0-105120 hours)
    //Source: http://www.hiv.va.gov/patient/faqs/life-expectancy-with-HIV.asp
    public int HIVSurvivalTime = Parameters.HIVSurvivalTime;

    //Average Survival time for someone without HIV and untreated TB is less than 3 years ( <= 26280 hours)
    //Source: http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0017601
    public int TBWithoutHIVSurvivalTime = Parameters.TBWithoutHIVSurvivalTime;

    //Average survival time for someone with HIV and untreated TB is 6 months ( <= 4380 hours)
    //Source: http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0017601
    public int TBWithHIVSurvivalTime = Parameters.TBWithHIVSurvivalTime;

    //Time from when a TB patient starts treatment to when they are not contagious anymore is 2-4 weeks
    //Source: http://hss.sbcounty.gov/psd/Forms/Health_Forms/Tuberculosis.htm
    public int contagiousPeriod_Min = Parameters.contagiousPeriod_Min;
    public int contagiousPeriod_Max = Parameters.contagiousPeriod_Max;

    //Time it takes to get back a TB Test
    //Source: http://www.cdc.gov/tb/topic/testing/default.htm
    public int TBTestingTime_Min = Parameters.TBTestingTime_Min;
    public int TBTestingTime_Max = Parameters.TBTestingTime_Max;

    //HIV Prevelance Rate in Kibera is 14%
    //Source: http://d-scholarship.pitt.edu/10936/1/PattersonHThesis2011_(2).pdf
    //public double HIVPrevalanceRate = Parameters.HIVPrevalanceRate;
    //TB Prevalance Rate in Kibera (TEMP made-up for testing purposes)
    //public double TBInfectionPrevalanceRate = Parameters.TBInfectionPrevalanceRate;
    //public double TBDiseasePrevalanceRate = Parameters.TBDiseasePrevalanceRate;
    //	public double TBInfectionPrevalanceRate = .01;
    //public double TBDiseasePrevalanceRate = .999;
    //The infectious dose (minimum doseage of TB bacilli for latent infection) is 1-10 bacillus 
    //Source: http://www.biosafety.be/CU/PDF/Mtub_Final_DL.pdf
    public double TBInfectionDose_MIN = Parameters.TBInfectionDose_MIN;
    public double TBInfectionDose_MAX = Parameters.TBInfectionDose_MAX;

    //Concentration of TB in saliva
    //Source: http://www.camra.msu.edu/documents/CAMRA_TB_ALERT.pdf and Yaeger et. al (1967)
    public double TBBacilliConcentration_Max = Parameters.TBBacilliConcentration_Max; //bacilli per mL

    //Concentration of saliva per cough
    //Source: http://www.camra.msu.edu/documents/CAMRA_TB_ALERT.pdf 
    public double salivaPerCough = Parameters.salivaPerCough; //ml per cough 

    //Coughs per hour. Average is 10, normal distribution is used 
    //Source: http://www.camra.msu.edu/documents/CAMRA_TB_ALERT.pdf
    public int coughsPerHour_Min = Parameters.coughsPerHour_Min;
    public int coughsPerHour_Max = Parameters.coughsPerHour_Max;
    
    final public static double bacilliClearanceRatePerHour = Parameters.bacilliClearanceRatePerHour ;

    //Contagious period after treatment in hours
    //Source: http://cdhd.idaho.gov/CD/public/factsheets/tuberculosis.htm
    public int contagiousPeriodAfterTreatment_Min = Parameters.contagiousPeriodAfterTreatment_Min;
    public int contagiousPeriodAfterTreatment_Max = Parameters.contagiousPeriodAfterTreatment_Max;

    //Percent chance that someone will quit TB Antiretroviral treatment (ART) in the beginning or during in Kibera
    //Source: http://fieldresearch.msf.org/msf/bitstream/10144/204570/1/Tayler-Smith%20ARV%20Uptake%20TB%20Kibera%20TMIH.pdf
//	public double percentQuitBeforeART = Parameters.percentQuitBeforeART; //30% will quit before
//	public double percentQuitDuringART = Parameters.percentQuitDuringART; //20.2% of patients will quit during treatment
//	//public double percentQuitBeforeART = 0; //30% will quit
    //public double percentQuitDuringART = 0; //20.2% of patients will quit during treatment
    //Body health (arbritrary chosen, everything adapts to it)
    public double bodyHealth_Max = Parameters.bodyHealth_Max;

    public double mortalityRateNaturalDeath = Parameters.mortalityRateNaturalDeath;// for suscp and recovered - set zero since no birth considered in the model
    public double mortalityRateLatentTB = Parameters.mortalityRateLatentTB; // including secondary exposure
    public double mortalityRateActiveTB = Parameters.mortalityRateActiveTB;
    public double moratlityRateActiveTB_HIV_NoART = Parameters.mortalityRateActiveTB_HIV_NoART;
    public double moratlityRateActiveTB_HIV_ART = Parameters.mortalityRateActiveTB_HIV_ART;

    // -------------- other variables ---------------------------------------
    public GeomVectorField roadLinks;
    Network roadNetwork = new Network();
    private int maxStructuresPerParcel = 1;
    Network socialNetwork = new Network(false);
    public double[] ethnicDistribution = Parameters.ethnicDistribution;
    private String[] ethnicities = Parameters.ethnicities; //from 1-12 represents each ethnicity 
    //private double maleDistribution = Parameters.maleDistribution; //Kibera surveys
    //public double ethnicityThreshold = Parameters.ethnicityThreshold; //threshold to create segregation

    public double MinimumWaterRequirement = Parameters.MinimumWaterRequirement;
    public double MaximumWaterRequirement = Parameters.MaximumWaterRequirement;

    /**
     * Age distribution
     */
    //public double ageAdult = Parameters.ageAdult; //this is the percentage of total residents (excluding head of households) that are adults
    //public double ageChildrenUnder6 = Parameters.ageChildrenUnder6; //the percentage of total residents (excluding head of households) under 6
    //public double percentOfResidentsUnder6 = Parameters.percentOfResidentsUnder6; //the percentage of total residents that are under 6 and thus cannot be employed
    //public double percentOfResidentsUnder19 = Parameters.percentOfResidentsUnder19; //the percentage of total residents 18 and younger (source - Kianda survey)
    // ----------------------------------------------------------------------
    //-------------------------Data collection variables---------------------------------------
    DefaultCategoryDataset dataset = new DefaultCategoryDataset(); //dataset for displaying activities of agents

    public XYSeries totalsusceptibleSeries = new XYSeries("Susceptible"); // shows
    public XYSeries totalExposedSeries = new XYSeries("Exposed");
    public XYSeries totalLatentSeries = new XYSeries("Latent");
    public XYSeries totalInfectedSeries = new XYSeries("Infected"); // shows
    public XYSeries totalRecoveredSeries = new XYSeries("Recovered"); // shows

    public XYSeries totalFullyHealthySeries = new XYSeries("Healthy");
    public XYSeries totalExposedTBSeries = new XYSeries("ExposedTB");
    public XYSeries totalLatentTBSeries = new XYSeries("LatentTB");
    public XYSeries totalLatentExposedTBSeries = new XYSeries("LatentxposedTB");
    public XYSeries totaInfectedTBSeries = new XYSeries("DiseaseTB");
    public XYSeries totalContagiousSeries = new XYSeries("Contagious");
    public XYSeries totalDeadSeries = new XYSeries("Dead");

    //Data for figuring out hotspots 
    DefaultCategoryDataset infectedDataSet = new DefaultCategoryDataset(); //dataset for figuring out where infected people are	
    DefaultCategoryDataset diseasedDataSet = new DefaultCategoryDataset(); //dataset for figuring out where diseased people are

    //Dataset for age distribution
    DefaultCategoryDataset infectedAgeGroupDataSet = new DefaultCategoryDataset(); //dataset for seeing age groups of infected
    DefaultCategoryDataset diseasedAgeGroupDataSet = new DefaultCategoryDataset(); //dataset for seeing age groups of diseased

    DefaultCategoryDataset ageDataSet = new DefaultCategoryDataset(); //dataset for seeing age groups of diseased

    //Timing Datasets
    DefaultValueDataset hourDialer = new DefaultValueDataset(); // shows the current hour
    DefaultValueDataset dayDialer = new DefaultValueDataset(); // counts the current day

    KiberaTBObserver kObserver;
    boolean parameterSweepOn = false; //don't print data if parameter sweep is working

    public int totalSus = 0; // total susceptible
    public int totalExp = 0; // total exposed
    public int totalLat = 0; // total exposed
    public int totalInf = 0; // total infected
    public int totalRec = deadAgents; // total recovered (include treated and dead)

    public int totalHealthy = 0;
    public int totalExposed = 0;
    public int totalLatentTB = 0;
    public int totalLatentExposedTB = 0;
    public int totalActiveTB = 0;
    public int totalContagious = 0;
    public int totalDead = deadAgents;
    public int totalOnTreatment = 0;

    public int[] sumAct = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // adding each

    public int[] hotspotLocsInfected = {0, 0, 0, 0, 0};
    public int[] hotspotLocsDiseased = {0, 0, 0, 0, 0};

    public int[][] hotspotGrid = new int[204][343];

    public double frequency = 0;
    public double percentage = 0;

    // -----------SimState Methods--------------------
    public KiberaTB(long seed, String[] args) {
        super(seed);
        params = new Parameters(args);
        allParcels = new Bag();
        allHealthFacilities = new Bag();
        allReligiousFacilities = new Bag();
        allBusinesses = new Bag();
        allHouseholds = new Bag();
        allFacilities = new Bag();
        allStructures = new Bag();
        allRestaurants = new Bag();
        allWaterSources = new Bag();
        availableParcels = new Bag();
        allSchools = new Bag();
        allResidents = new Bag();

        businessLocations = new Bag();
        businessTier1Locations = new Bag();
        businessTier2Locations = new Bag();
        businessTier3Locations = new Bag();

        restaurantLocations = new Bag();
        schoolLocations = new Bag();
        waterLocations = new Bag();
        healthLocations = new Bag();
        religiousLocations = new Bag();
        allEmployers = new Bag();
        allCampGeoGrid = new GeomGridField();

    }

    public void start() {
        super.start();
        //Create the bags 


        /*
         * KiberaBuilder.createWorld("kibera/data/kibera.txt", "kibera/data/roads_cost_distance.txt", "kibera/data/schools.txt",
         * "kibera/data/health.txt", "kibera/data/religion.txt", this);
         */
        KiberaBuilderTB.createWorld("src/files/data/kibera.txt", "src/files/data/roads_cost_distance.txt", "src/files/data/schools.txt",
                "src/files/data/health.txt", "src/files/data/religion.txt", "src/files/data/restaurants/restaurants.txt",
                "src/files/data/waterSources/waterSources.txt", this);

        System.out.println("Build Successful!");

        // check if the resident is dead
        Steppable removeObj = new Steppable() {

            public void step(SimState state) {
                removeResident();

            }
        };

        //updater for all charts that run daily
        Steppable dailyChartUpdater = new Steppable() {
            public void step(SimState state) {

                totalSus = 0; // total susceptible
                totalExp = 0; // total exposed
                totalLat = 0; // total latent
                totalInf = 0; // total infected
                totalRec = 0; // total recovered (include treated and dead)

                totalHealthy = 0; // healthy
                totalExposed = 0; // exposed
                totalLatentTB = 0; // latent
                totalLatentExposedTB = 0;
                totalActiveTB = 0; // active - disease
                totalContagious = 0; // contagious- active but not yet taking medication
                totalDead = deadAgents;
                totalOnTreatment = 0;

                for (Object obj : allResidents) {
                    ResidentTB res = (ResidentTB) obj;

                    //Check health status for SEIR model
                    if (res.getHealthStatus() == res.susceptible) {
                        totalSus = totalSus + 1;
                    } else if (res.getHealthStatus() == res.exposedTB) {
                        totalExp = totalExp + 1;
                    } else if (res.getHealthStatus() == res.latentInfected) {
                        totalLat = totalLat + 1;
                    } else if (res.getHealthStatus() == res.activeInfected) {
                        totalInf = totalInf + 1;
                    } else if (res.getHealthStatus() == res.recovered) {
                        totalRec = totalRec + 1;
                    }

                    if (res.onTreatment) {
                        totalOnTreatment++;
                    }
                    //check health condition (infected, diseased, dead)
                    if (res.hasExposed) {
                        totalExposed++;
                    } else if (res.hasLatentInfection) {
                        totalLatentTB++;
                        if (res.hasLatenInfectionExposed) {
                            totalLatentExposedTB++;
                        }
                    } else if (res.hasActiveInfection) {
                        totalActiveTB++;
                        if (res.isContagious == true) {
                            totalContagious++;
                        }
                    } else {
                        totalHealthy++;
                    }

                }

                //System.out.println(treatment);
                //Update charts on different health information
                totalsusceptibleSeries.add((double) (state.schedule.getTime() / 24), (totalSus));
                totalExposedSeries.add((double) (state.schedule.getTime() / 24), (totalExp));
                totalLatentSeries.add((double) (state.schedule.getTime() / 24), (totalLat));
                totalInfectedSeries.add((double) (state.schedule.getTime() / 24), (totalInf));
                totalRecoveredSeries.add((double) (state.schedule.getTime() / 24), (totalRec));

                totalFullyHealthySeries.add((double) (state.schedule.getTime() / 24), (totalHealthy));
                totalExposedTBSeries.add((double) (state.schedule.getTime() / 24), (totalExposed));
                totalLatentTBSeries.add((double) (state.schedule.getTime() / 24), (totalLatentTB));
                totalLatentExposedTBSeries.add((double) (state.schedule.getTime() / 24), (totalLatentExposedTB));
                totaInfectedTBSeries.add((double) (state.schedule.getTime() / 24), (totalActiveTB));
                totalContagiousSeries.add((double) (state.schedule.getTime() / 24), (totalContagious));
                totalDeadSeries.add((double) (state.schedule.getTime() / 24), (totalDead));

            }

        };

        //updater for all charts that run hourly
        Steppable hourlyChartUpdater = new Steppable() {
            public void step(SimState state) {

                Arrays.fill(sumAct, 0);
                Arrays.fill(hotspotLocsInfected, 0);
                Arrays.fill(hotspotLocsDiseased, 0);
                int allInfected = 0;
                int allDiseased = 0;

                int[] ageGroupsInfected = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                int[] ageGroupsDiseased = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                int[] ageGroups = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

                for (Object o : allResidents) {
                    ResidentTB res = (ResidentTB) o;

                    sumAct[res.getCurrentActivity()] += 1;
                    ageGroups[(int) (res.getAge() / 5)] += 1;

                    if (res.hasLatentInfection) {
                        allInfected++;

                        if (res.onBusiness) {
                            hotspotLocsInfected[0] += 1;
                        } else if (res.onReligion) {
                            hotspotLocsInfected[4] += 1;
                        } else if (res.onRestaurant) {
                            hotspotLocsInfected[1] += 1;
                        } else if (res.onSchool) {
                            hotspotLocsInfected[2] += 1;
                        } else if (res.onWater) {
                            hotspotLocsInfected[3] += 1;
                        }

                        ageGroupsInfected[(int) (res.getAge() / 5)] += 1;
                    }
                    if (res.hasActiveInfection) {
                        allDiseased++;

                        if (res.onBusiness) {
                            hotspotLocsDiseased[0] += 1;
                        } else if (res.onReligion) {
                            hotspotLocsDiseased[4] += 1;
                        } else if (res.onRestaurant) {
                            hotspotLocsDiseased[1] += 1;
                        } else if (res.onSchool) {
                            hotspotLocsDiseased[2] += 1;
                        } else if (res.onWater) {
                            hotspotLocsDiseased[3] += 1;
                        }

                        ageGroupsDiseased[(int) (res.getAge() / 5)] += 1;
                    }
                    if (res.hasActiveInfection) {
                        Parcel p = res.getPosition();
                        int col = p.getXLocation();
                        int row = p.getYLocation();
                        if (healthFacilityGrid.getObjectsAtLocation(col, row) == null) {
                            hotspotGrid[row][col] += 1;
                        }
                    }

                }

                //-------------Daily activities--------------------------------
                String actTitle = "Resident Activity"; // row key - activity
                String[] activities = new String[]{"At Home", "School", "Water", "Religion", "restaurant", "Health C.", "Social", "Work",
                    "Business"};

                // percentage - agent activity by type
                if (allResidents.numObjs > 0) {
                    for (int i = 0; i < sumAct.length; i++) {
                        double value = sumAct[i] * 100 / allResidents.numObjs;
                        dataset.setValue(value, actTitle, activities[i]);
                    }
                }

                //-------------------Check to see where infected people are----------------------
                String infectedTitle = "Hotspot Activity for INFECTED individuals";
                String[] infectLocs = new String[]{"Business", "restaurant", "School", "Water", "Religion"};

                for (int k = 0; k < hotspotLocsInfected.length; k++) {
                    if (allInfected != 0) {
                        double value = hotspotLocsInfected[k] * 100 / allInfected;
                        infectedDataSet.setValue(value, infectedTitle, infectLocs[k]);
                    }
                }

                //-------------------Check to see where infected people are----------------------
                String diseasedTitle = "Hotspot Activity for DISEASED individuals";
                String[] diseasedLocs = new String[]{"Business", "restaurant", "School", "Water", "Religion"};

                for (int k = 0; k < hotspotLocsDiseased.length; k++) {
                    if (allDiseased != 0) {
                        double value = hotspotLocsDiseased[k] * 100 / allDiseased;
                        diseasedDataSet.setValue(value, diseasedTitle, diseasedLocs[k]);
                    }
                }
                //------------------See age distribution for infected people---------------------
                String infectedAgeGroupTitle = "Age group distribution for INFECTED indviduals";
                String[] infectedAgeGroup = new String[]{"[0,5)", "[5,10)", "[10,15)", "[15,20)", "[20,25)", "[25,30)", "[30, 35)",
                    "[35,40)", "[40,45)", "[45,50)", "[50,55)", "[55,60)", "[60,65)", "[65,70)", "[70,75)", "[75,80)", "[80,85)"};

                for (int k = 0; k < ageGroupsInfected.length; k++) {
                    if (allInfected != 0) {
                        double value = ageGroupsInfected[k] * 100 / allInfected;
                        infectedAgeGroupDataSet.setValue(value, infectedAgeGroupTitle, infectedAgeGroup[k]);
                    }
                }
                //------------------See age distribution for diseased people---------------------
                String diseasedAgeGroupTitle = "Age group distribution for DISEASED indviduals";
                String[] diseasedAgeGroup = new String[]{"[0,5)", "[5,10)", "[10,15)", "[15,20)", "[20,25)", "[25,30)", "[30, 35)",
                    "[35,40)", "[40,45)", "[45,50)", "[50,55)", "[55,60)", "[60,65)", "[65,70)", "[70,75)", "[75,80)", "[80,85)",};

                for (int k = 0; k < ageGroupsDiseased.length; k++) {
                    if (allDiseased != 0) {
                        double value = ageGroupsDiseased[k] * 100 / allDiseased;
                        diseasedAgeGroupDataSet.setValue(value, diseasedAgeGroupTitle, diseasedAgeGroup[k]);
                    }
                }
                //------------------See age distribution for Kibera as a whole---------------------
                String AgeGroupTitle = "Age group distribution for Kibera";
                String[] ages = new String[]{"[0,5)", "[5,10)", "[10,15)", "[15,20)", "[20,25)", "[25,30)", "[30, 35)", "[35,40)",
                    "[40,45)", "[45,50)", "[50,55)", "[55,60)", "[60,65)", "[65,70)", "[70,75)", "[75,80)", "[80,85)",};
                if (allResidents.numObjs > 0) {
                    for (int k = 0; k < ageGroups.length; k++) {
                        double value = ageGroups[k] * 100 / allResidents.numObjs;
                        ageDataSet.setValue(value, AgeGroupTitle, ages[k]);
                    }
                }

                int hours = (int) state.schedule.getSteps(); //hour in day
                int days = hours / 24; //what day number is

                if (hours > 24) {
                    hours = hours % 24;
                }

                hourDialer.setValue(hours);
                dayDialer.setValue(days);

                if (schedule.getSteps() == 3000) {
                    try {
                        printHotspotInfo();
                    } catch (Exception e) {
                        System.out.println("Error printing hotspot");
                        e.printStackTrace();
                    }
                }
            }
        };

        schedule.scheduleRepeating(removeObj, 0, 1);
        schedule.scheduleRepeating(dailyChartUpdater, 0, 1);
        schedule.scheduleRepeating(hourlyChartUpdater, 0, 1);

        kObserver = new KiberaTBObserver(this);
        schedule.scheduleRepeating(this.kObserver, 1, 1);

    }

    // --------------Constructor-------------
    public void killResident(ResidentTB res) {
        res.getHome().removeHouseholdMember(res);

        if (res.getHome().getHouseholdMembers().size() == 0) {
            this.allHouseholds.remove(res.getHome());
        }

        this.socialNetwork.removeNode(res);
        allResidents.remove(res);

        deadAgents++;

    }

    public void removeResident() {

        for (Object o : allResidents) {
            ResidentTB res = (ResidentTB) o;
            if (res.bodyHealth <= 0 || res.isDead == true) {
                totalDead++;
                killResident(res);
            }
        }

    }

    // -------------Accessor/Modifier Methods---------
    public int getWidth() {
        return width;
    }

    public void setWidth(int w) {
        width = w;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int h) {
        height = h;
    }

    public int getNumResidents() {
        return params.globalParam.getNumberOfResidents();
    }

//	public void setNumResidents(int val) {
//		numResidents = val;
//	}
    public int getSchoolCapacity() {

        return params.globalParam.getschoolCapacity();
    }

//	public void setSchoolCapacity(int capacity) {
//		schoolCapacity = capacity;
//	}
    public int getMaxStructuresPerParcel() {
        return maxStructuresPerParcel;
    }

    public void setMaxStructuresPerParcel(int val) {
        maxStructuresPerParcel = val;
    }

    public double[] getEthnicDistribution() {
        return ethnicDistribution;
    }

    public double getEthnicDistribution(int i) {
        return ethnicDistribution[i];
    }

    public void setEthnicDistribution(double[] val) {
        ethnicDistribution = val;
    }

    public String[] getEthnicities() {
        return ethnicities;
    }

    public void setEthnicities(String[] val) {
        this.ethnicities = val;
    }

    public String getEthnicities(int i) {
        return ethnicities[i];
    }

    public int getHouseholdCapacity() {
        return params.globalParam.gethhCapacity();
    }

//	public void setHouseholdCapacity(int val) {
//		hhCapacity = val;
//	}
    public int getBusinessCapacity() {
        return params.globalParam.getbusinessCapacity();
    }
//
//	public void setBusinessCapacity(int val) {
//		businessCapacity = val;
//	}

    public double getMaleDistribution() {
        return params.globalParam.getmaleDistribution();
    }

//	public void setMaleDistribution(double val) {
//		maleDistribution = val;
//	}
    public void finish() {
        super.finish();
        if (this.kObserver != null) {
            this.kObserver.finish();
        }
        KiberaBuilderTB.alreadyCheckedMap.clear();
        KiberaBuilderTB.allParcelsMap.clear();
    }

//	public void diagnosisExperiment(double frequency, double percentage) {
//
//		IntBag schoolIndices = new IntBag();
//		frequency *= 720;
//		
//		if (frequency % schedule.getSteps() == 0) {
//			
//			for (int k = 0; k < this.allSchools.size(); k++) {
//				if (random.nextDouble() <= percentage) {
//					schoolIndices.add(k);
//				}
//			}
//
//			for (int k = 0; k < this.allResidents.size(); k++) {
//				ResidentTB res = (ResidentTB) allResidents.get(k);
//				if (res.isStudent && schoolIndices.contains(res.getSchoolIndex())) {
//					if (res.hasActiveInfection == true) {
//						res.startTreatment();
//					}
//				}
//			}
//			
//		}
//	}
    public void printHotspotInfo() throws Exception {
        try {

            CSVWriter csvWriter = new CSVWriter(new FileWriter("CSVHotspot.csv", true));

            String[] header = new String[]{"X", "Y", "Value"};
            csvWriter.writeNext(header);

            String[] line = new String[3];
            for (int r = 0; r < 204; r++) {
                for (int c = 0; c < 343; c++) {
                    //if (hotspotGrid[r][c] != 0) {
                    line[0] = "" + c;
                    line[1] = "" + r;
                    line[2] = "" + hotspotGrid[r][c];
                    csvWriter.writeNext(line);
                    //}

                }
            }

            //			PrintStream hotspot = new PrintStream(new FileOutputStream("hotspotGrid.txt"));
            //
            //			hotspot.println("ncols         343");
            //			hotspot.println("nrows         204");
            //			hotspot.println("xllcorner     36.770385876018");
            //			hotspot.println("yllcorner     -1.3240284474813");
            //			hotspot.println("cellsize      0.00011228");
            //			hotspot.println("NODATA_value  -9999");
            //			for (int r = 0; r < 204; r++) {
            //				for (int c = 0; c < 343; c++) {
            //					if (hotspotGrid[r][c] == 0) {
            //						hotspot.print(100 + " ");
            //					} else
            //						hotspot.print(hotspotGrid[r][c] + " ");
            //				}
            //				hotspot.println();
            //			}
            //			hotspot.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error creating hotspot grid");
            e.printStackTrace();
        }

    }

    public MersenneTwisterFast getRandom() {

        return random;
    }

    // ------------Main method--------------------------------
//	public static void main(String[] args) {
//
//		doLoop(KiberaTB.class, args);
//		System.exit(0);
//	}
    public static void main(String[] args) {

        // doLoop(Landscape.class, args);
        doLoop(new MakesSimState() {
            @Override
            public SimState newInstance(long seed, String[] args) {

                return new KiberaTB(seed, args);
            }

            @Override
            public Class simulationClass() {
                return KiberaTB.class;
            }
        }, args);

        System.exit(0);
    }
}
