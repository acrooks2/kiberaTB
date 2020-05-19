package kiberaTB;

//--------imports-----------------
import java.util.ArrayList;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Double2D;
import ec.util.MersenneTwisterFast;
import sim.util.Bag;
import sim.field.network.Edge;


//--------------------------------
//agent for the Kibera TB model
public final class ResidentTB implements Steppable {

    //--------------private agent variables---------------
    private int age;
    private int gender; //0 = male, 1 = female 
    public boolean isMale;
    public boolean isStudent; //student goes to school 
    public int schoolIndex;
    public int schoolClass; // if student, which class you're in
    private Household home;
    private double waterDemand = 0.0;
    private int latUse; //latrine use, normal is 1-2 per day
    private int restaurantVisits;

    private Parcel position; //current position 
    private Parcel homeLocation; //home position 
    private Parcel goal; //location of the goal

    private double jitterX; //For visualization 
    private double jitterY; //For visualization

    protected Stoppable stopper;
    public static final int ORDERING = 2;
   
    KiberaTB kibera;

    public int cStep;
    ArrayList<Parcel> path = null; //agent's current path to its goal 
    private TimeManager timeManager;
    public int hourInDay;
    public int currentAct;
    public double Prob_ExposureAge = 0.8;  // probability of exposure exponent - 80% age group

    //Figure out if an agent is in a public place
    public boolean onRestaurant = false;
    public boolean onSchool = false;
    public boolean onReligion = false;
    public boolean onWater = false;
    public boolean onBusiness = false;

    public boolean isEmployed; //Residents are either employed or not

    //health information
    double bodyHealth; //body resistance of a person. When it becomes <= 0, person dies and is removed
    double HIVDepreciation; //depreciation due to HIV
    double TBAndHIVDepreciationRate; //depreciation for someone with HIV and TB 
    double TBOnlyDepreciationRate; //depreciation rate for someone with TB only
    double healthRandomDepreciationRate;

    int healthStatus = 1; //1=susceptible; 2=exposed; 3=infectious; 4=recovered
    int prevHealthStatus = 1; //monitors health status of the previous step to see the change

    final int susceptible = 1;
    final int exposedTB = 2; // inhale the bacteria - can pass to latent or active or pass to recovered - no impact on their body
    final int latentInfected = 3;// latent tb patient
    final int activeInfected = 4;  // active phase tb
    final int recovered = 5;

    boolean hasExposed = false; // time when the person is exposed to tb
    boolean hasActiveInfection = false; //has TB disease
    boolean hasLatentInfection = false; //has latent TB infection
    boolean hasLatenInfectionExposed =false;//// still in latent stage but now again exposed-- same ruleas exposed but used to track expsed after latent
    boolean hasRecovered = false; // recovered from tb- either through treatment or exposed but not affected
    boolean hasRecoveredExposed =false;//// recovered can be exposed and  back to latent

    boolean isSusceptible = false;
    boolean isContagious = false; //has TB disease and can spread it 
    // boolean isExposureWillBecomeLatentInfection=false; // pass from exposed to latent
    //boolean isExposureWillBecomeActiveInfection=false; // pass from exposed to latent
//    boolean isExposureWillBecomeRecovered=false; // pass from exposed to recover
    boolean latentWillBecomeActiveInfection = false; //will the TB infection become TB disease 
    boolean activeWillBecomeLatentInfection = false; //will the TB disease become TB infecton - if relapse from treatment 
    boolean onTreatment = false;
    boolean onARTTreatment =false;
    boolean quitTreatmentBeforeEnd = false;

    int hoursInfected = 0; //how long has this person been infected
    int timeForDetection = 0; //the time it will take before the TB bacteria show up as a positiveTestResult
    int timeStepWhenInfectionBecomesDisease; //at what hour will the TB infection become TB disease (if it is meant to be) 
    int timeTBIncubationAfterExposure; //after first incident of exposure to show ay sypthom or response to test

    boolean willStartTreatmentTB = false;  /// start treatment 
    boolean alreadyChecked = false;
    int TBTestingTime;
    int exposurePhasePeriod = 0;

    int contagiousPeriod;
    boolean willQuitTreatment = false;
    int numberOfDosesBeforeQuitting;
    TreatmentPlan treatmentPlan;
    int alpha = 0;
    int beta = 0;
//    boolean wentThroughTB = false;

    boolean hasHIV;
    boolean beingTreatedForHIV;
    double CD4Count; //CD4 cells per mm^3
    double CD4CountForInfectionToDisease; //the CD4 count that if someone with HIV is below, the TB infection will be TB disease

    double infectionDose;
    double bacilliCountExposedAtGoal;
    boolean isDead =false;

    //-------------Constructor------------------------------
    public ResidentTB(int age, int gender, boolean isHead, Household household, boolean school, boolean employment, boolean HIVStatus,
            int healthStatus,boolean onHIVARTTreatment, KiberaTB kibera) {
//<editor-fold defaultstate="collapsed" desc="Accessors">
        this.setAge(age);
        this.setGender(gender);
        this.setHome(household);

        this.setSchoolEligible(school);
        this.isHeadOfHousehold(isHead);
        this.isEmployed = employment;

        this.setPosition(household.getStructure().getParcel());
        this.setHomeLocation(home.getStructure().getParcel());
        this.setGoalLocation(home.getStructure().getParcel());

        this.setEnergy(100);
        this.setCurrentIdentity(Identity.Domestic_Activities);
        this.setHealthStatus(healthStatus);
//        this.setBacilliCountExposedAtGoal(0);
        this.onARTTreatment =onHIVARTTreatment;

        this.jitterX = kibera.getRandom().nextDouble();
        this.jitterY = kibera.getRandom().nextDouble();

        cStep = 0;
        timeManager = new TimeManager();

        this.bodyHealth = kibera.bodyHealth_Max;
        this.hasHIV = HIVStatus;
        infectionDose = (kibera.TBInfectionDose_MIN + kibera.getRandom().nextDouble() * (kibera.TBInfectionDose_MAX - kibera.TBInfectionDose_MIN));
     
        if (healthStatus == this.susceptible) {
            this.susceptible();
        }
        if (healthStatus == this.exposedTB) {
            this.exposed();
        }
        if (healthStatus == this.latentInfected) {
           this.latentInfected();
        }
       
        if (healthStatus == this.activeInfected) {
            this.activeInfected();
           
        }
        if (healthStatus == this.recovered) {
            this.recovered();

        }
        

        //Determine depreciation rates
        if (this.hasHIV) {
            double hoursToLive =  kibera.HIVSurvivalTime *(1.5 - 0.7 * kibera.getRandom().nextDouble()); 
            HIVDepreciation = bodyHealth / hoursToLive;

            this.TBOnlyDepreciationRate = 0;

            //double TBhoursToLive = kibera.getRandom().nextInt(kibera.TBWithHIVSurvivalTime);
            double TBhoursToLive =  kibera.TBWithHIVSurvivalTime *(1.5 - 0.7  * kibera.getRandom().nextDouble()); // at least 30%
            this.TBAndHIVDepreciationRate = bodyHealth / TBhoursToLive;

          // this.CD4Count = kibera.CD4CountForTB_Min + (kibera.getRandom().nextDouble() * (kibera.HIVCD4Count_MAX - kibera.CD4CountForTB_Min));
        } else {
            HIVDepreciation = 0.0;
            this.TBAndHIVDepreciationRate = 0.0;
            //double hoursToLive = kibera.getRandom().nextInt(kibera.TBWithoutHIVSurvivalTime);
            // TODO: where does 1.5 and 0.7 come from?
            // TODO: does TB only deprecation rate apply for both latent and active residents?
            double hoursToLive = kibera.TBWithoutHIVSurvivalTime *(1.5 - 0.7 * kibera.getRandom().nextDouble()); 
            this.TBOnlyDepreciationRate = bodyHealth / hoursToLive;
        }
        
            alpha = 6 + kibera.getRandom().nextInt(3); // working hour start (in hours between 6-9 AM)
            beta = 17 + kibera.getRandom().nextInt(2); // working hour end (in hours between 5-7 PM)
        
        //</editor-fold>
    }
    //<editor-fold defaultstate="collapsed" desc="Accessors">

    public enum Religion {

        Christian, Muslim
    };

    Religion religion;

    public Religion getReligion() {
        return religion;
    }

    public void setReligion(Religion val) {
        religion = val;
    }

    private School mySchool;

    public School getMySchool() {
        return mySchool;
    }

    public void setMySchool(School val) {
        mySchool = val;
    }

    private String ethnicity;

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String val) {
        ethnicity = val;
    }

    public boolean getSchoolEligible() {
        return isStudent;
    }

    public void setSchoolEligible(boolean val) {
        isStudent = val;
    }
    
    public boolean getOnARTTreatment() {
        return onARTTreatment;
    }

    public void setOnARTTreatment(boolean val) {
        onARTTreatment = val;   

    }

    /**
     * Residents energy reservoir, value from 1 to 10
     */
    private double energy;

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double val) {
        energy = val;
    }

    /**
     * The set of potential identities a resident can have
     */
    public enum Identity {

        Student, Employer, Domestic_Activities, Rebel
    };

    Identity currentIdentity;

    public Identity getCurrentIdentity() {
        return currentIdentity;
    }

    public void setCurrentIdentity(Identity val) {
        currentIdentity = val;
    }

    /**
     * The number of time steps an agent stays at a given activity/action
     */
    private int stayingPeriodAtActivity;

    public int getStayingPeriod() {
        return stayingPeriodAtActivity;
    }

    public void setStayingPeriod(int val) {
        stayingPeriodAtActivity = val;
    }

    private boolean isHeadOfHousehold;

    public boolean isHeadOfHousehold() {
        return isHeadOfHousehold;
    }

    public void isHeadOfHousehold(boolean val) {
        isHeadOfHousehold = val;
    }
    
//    public void setBacilliCountExposedAtGoal(double val){
//        this.bacilliCountExposedAtGoal =val;
//    }
//    public double getBacilliCountExposedAtGoal(){
//        return bacilliCountExposedAtGoal;
//    }

    /**
     * If resident has found informal employment, keep going to business each
     * day
     */
    private Business myBusinessEmployer;

    public Business getMyBusinessEmployer() {
        return myBusinessEmployer;
    }

    public void setMyBusinessEmployer(Business val) {
        myBusinessEmployer = val;
    }

    /**
     * If resident found formal employment at a school, keep going to that
     * school each day
     */
    private School mySchoolEmployer;

    public School getMySchoolEmployer() {
        return mySchoolEmployer;
    }

    public void setMySchoolEmployer(School val) {
        mySchoolEmployer = val;
    }

    /**
     * If resident found formal employment at a health facility, keep going to
     * that health facility each day
     */
    private HealthFacility myHealthFacilityEmployer;

    public HealthFacility getMyHealthFacilityEmployer() {
        return myHealthFacilityEmployer;
    }

    public void setMyHealthFacilityEmployer(HealthFacility val) {
        myHealthFacilityEmployer = val;
    }

    /**
     * If resident found formal employment at a religious facility, keep going
     * to that health facility each day
     */
    private ReligiousFacility myReligiousFacilityEmployer;

    public ReligiousFacility getMyReligiousFacilityEmployer() {
        return myReligiousFacilityEmployer;
    }

    public void setMyReligiousFacilityEmployer(ReligiousFacility val) {
        myReligiousFacilityEmployer = val;
    }
//</editor-fold>
    //MersenneTwisterFast getRandom();

   //---------------------------------------------------

    public void step(SimState state) {
//<editor-fold defaultstate="collapsed" desc="Accessors">
        kibera = (KiberaTB) state;
        cStep = (int) kibera.schedule.getSteps();

        if (cStep < 24) {
            hourInDay = cStep;
        } else {
            hourInDay = cStep % 24;
        }

        move(kibera, hourInDay);

        //changes every day
//        if (cStep % 24 == 0) {
//            if (onTreatment) {
//				//treatment();
//                //this.treatmentPlan.takenDose = false;
//            }
//        }
        
        if (this.getRestaurantVisits() < 3) {
            if (cStep % 168 == 0 && kibera.getRandom().nextDouble() > 0.5) {
                this.setRestaurantVisits(kibera.getRandom().nextInt(10)); //
            }
        }

        if (this.getWaterLevel() < (kibera.MinimumWaterRequirement)) {
            utilizeWater();
        }
        dehydrate();
        
       

        //-------------------------Health Updating---------------------------------------------------------
        if (cStep != 0) {
            healthUpdatePhase();
            healthDepreciation();
        }
        
        // number bacilli goes down along the hour after exposure 
        
      
//</editor-fold>
    }

    // the next five methods SEIR ( Latent and Active included 
    public void susceptible() {
        this.setHealthStatus(this.susceptible); //
        this.isSusceptible = true;
        this.hasExposed = false;// no more exposed asit pass to latent
        this.hasLatentInfection = false; //no sign
        this.hasLatenInfectionExposed = false;
        this.hasActiveInfection = false;// doesn't show any symptoms so stay as latent
        this.hasRecovered = false; // not yet - it can continue as latent for long period
        this.isContagious = false; // but not contagious
    }

    //when the resident is exposed to tb bacteria

    public void exposed() {
        this.setHealthStatus(this.exposedTB); //now exposed 
        this.hasExposed = true;
        this.hasLatenInfectionExposed = false;
        this.isSusceptible = false;
        this.hasLatentInfection = false; //no sign
        this.hasActiveInfection = false;// not show any symptom so stay as latent
        this.hasRecovered = false; // not yet - it can continue as latent for long period
        this.isContagious = false; // but not contagious
        
    }
   

    // when the resident is latent infected

    public void latentInfected() {
        this.setHealthStatus(this.latentInfected); //now infected
        this.hasLatentInfection = true;
        this.isSusceptible = false;
        this.hasExposed = false;// no more exposed asit pass to latent
        this.hasActiveInfection = false;// not show any symptom so stay as latent
        this.hasRecovered = false; // not yet - it can continue as latent for long period
        this.isContagious = false; // but not contagious

    }
    
    

    //TB infection becomes TB Disease

    public void activeInfected() {
        this.setHealthStatus(this.activeInfected); //now infected
        this.hasActiveInfection = true;
        this.isContagious = true;
        this.hasLatenInfectionExposed = false;
        this.isSusceptible = false;
        this.hasExposed = false;// no more exposed asit pass to latent
        this.hasLatentInfection = false; // no more latent
        this.hasRecovered = false; // not yet - unless it recieve treatment
       
        
            

    }

    public void recovered() {
        this.setHealthStatus(this.recovered); //recovered 
        this.hasRecovered = true;
        this.isSusceptible = false;
        this.hasExposed = false;
        this.hasLatentInfection = false; // no more latent
        this.hasActiveInfection = false;
        this.hasLatenInfectionExposed = false;
        this.isContagious = false;
        this.bodyHealth = kibera.bodyHealth_Max;
        this.alreadyChecked = false; // to start over treatment if agent be infected again
        this.setExposurePhasePeriod(0);// reset exposure period
        willStartTreatmentTB = false;

    }

    // phase transition
    //1.  if the Bacilli is above certain level you will be exposed - the Bacilli is the one the resident acquired from contagious agent
    public void exposedPhaseTransition() {
        // if it reach to this time  - 1% goes to active 20-30 goes to latent 80 % goes to recover
        //System.out.println(this.geExposurePhasePeriod());
        if (cStep >= this.geExposurePhasePeriod()) { // imagine this is a time the resident take the tb test or show any sign of symptom

            double likelyF = 1.0;
            if (this.hasHIV) {
                // TODO: Check case where no HIV
                likelyF = kibera.likelyExposedTBtoLatentorActiveWithHIV;
            }

            double toActive = likelyF * (kibera.exposedToActiveTBPhaseMin + kibera.getRandom().nextDouble() * (kibera.exposedToActiveTBPhaseMax - kibera.exposedToActiveTBPhaseMin));
            double toLatent = likelyF * (kibera.exposedToLatentPhaseMin + kibera.getRandom().nextDouble() * (kibera.exposedToLatentPhaseMax - kibera.exposedToLatentPhaseMin));
            double r = kibera.getRandom().nextDouble();
            if (r < toLatent) {// susciptible
                if (r < toActive) {
                    
                    this.activeInfected(); //1 %
                } else {
                   
                    this.latentInfected(); // 20%
                }
            } else {
                 this.susceptible();  // no treatmentand no impact from exposure - back to susceptible
            }
            
            
            this.setExposurePhasePeriod(0);

        }

    }
    
    // if in latent phase it can again expose and develop active - Secondary exposure 
    public void latentExposedPhaseTransition() {
        if (cStep >= this.geExposurePhasePeriod()) { // if you are exposed again

            double likelyF = 1.0;
            if (this.hasHIV) {
                likelyF = kibera.likelyExposedTBtoLatentorActiveWithHIV;
            }
            double probLateToAct = likelyF * kibera.reInfectionRateLatent * (kibera.exposedToLatentPhaseMin + kibera.getRandom().nextDouble() * (kibera.exposedToLatentPhaseMax - kibera.exposedToLatentPhaseMin));

            if (kibera.getRandom().nextDouble() < probLateToAct) {
                this.activeInfected();
            } else { // continue as latent
                this.hasLatenInfectionExposed = false;
            }

            this.setExposurePhasePeriod(0);

        }
    }

    // latent tb can develop tb in some time in their lifetime - due to many factors
    public void latentPhaseTransition() {

       double probLateToAct = 0;

        // 
        if (this.hasHIV) {
            probLateToAct = (kibera.latentToDiseaseLifeTimeHIV_MIN + kibera.getRandom().nextDouble() *  (kibera.latentToDiseaseLifeTimeHIV_MAX - kibera.latentToDiseaseLifeTimeHIV_MIN));

        } else {
            probLateToAct = (kibera.latentToDiseaseLifeTime_MIN + kibera.getRandom().nextDouble() *  (kibera.latentToDiseaseLifeTime_MAX - kibera.latentToDiseaseLifeTime_MIN));

        }

        // TODO: probLatetoAct is super high since nextdouble is between 0 and 1, should the range be changed?
        if (kibera.getRandom().nextDouble() < probLateToAct) {
            this.activeInfected(); // re-infection
        }
        
//        if (kibera.getRandom().nextDouble() < kibera.latentToRecovered) {
//            this.recovered();
//        }

    }
    
    public void wantToStartTreatmentTB(){
        // TODO: Is this being confused with ART rates?
        // TODO: Check what the regular TB treatment rates are
        // 30 % quit before treatment - only 70% start treatment - this is the general coverage of treatment
        // 70% of the active - is it in a year? a month ? ??? 24 * 30
        // But when to start treatment - imagine one year 150 days * 24 hours
        willStartTreatmentTB = false;
        if (kibera.getRandom().nextDouble() < kibera.params.globalParam.getPercentQuitBeforeART()) {
            willStartTreatmentTB = true;
        }
       
    }

    public void activeInfectionPhaseTransition() {

        // if you are contagious and you're at your goal location, infect
        if (this.isContagious) { // prob in a day ??
            if (this.getGoalLocation() != null && this.getPosition().equals(this.getGoalLocation())) {
                
                if(kibera.healthLocations.contains(this.getGoalLocation()) !=true){ // no infection in health center -
                     infect();
                }
               
            }

        }
        if (this.onTreatment != true) {// once in 24 hours
            wantToStartTreatmentTB();
        }
        // check if you want to start treatment

    }
    // recovered can be relapsed back to latent - since treatment is thought to not completely eliminate the bacteria nor to give protection against future exposure
    public void recoveredToLatentPhaseTransition() {
        if (kibera.getRandom().nextDouble() < kibera.recoveredToLatentProb) {
            this.susceptible();
        }
    }

    public void healthUpdatePhase() {
    
        TBDynamics (this.getHealthStatus());

        //TODO: Why is this commented out?
       if (this.hasHIV) { //actions if someone has HIV
            //Drop CD4 Count
//            if (this.beingTreatedForHIV) {
//                double drop = kibera.CD4CountDrop_Min + kibera.getRandom().nextDouble() * (kibera.CD4CountDrop_Max - kibera.CD4CountDrop_Min);
//                this.CD4Count = this.CD4Count - drop;
//            }

        }

       // now take the medicine
        if (kibera.params.globalParam.getIsOnTreatment()) {
           if (this.onTreatment && this.hasActiveInfection) {
                treatment();

            }
        }
        isDead();

    }
    
    public void isDead(){
        if(kibera.getRandom().nextDouble()< kibera.mortalityRateNaturalDeath){
            this.isDead =true;
        }
    }
    public void TBDynamics(int hstatus) {
        if (hstatus == exposedTB) {
            exposedPhaseTransition();
        }
        if (this.hasLatenInfectionExposed) {
            latentExposedPhaseTransition();
        }

        if (hstatus == latentInfected) {
            latentPhaseTransition();

        }

        if (hstatus == activeInfected) {
            activeInfectionPhaseTransition();
        }

        if (hstatus == recovered) {
            recoveredToLatentPhaseTransition(); // recovered to Susceptible
        }

    }

    //health depreciates when someone has TB disease 
    public void healthDepreciation() {

        if (this.hasActiveInfection && this.hasHIV) { //depreciation will be bigger if you have HIV and TB 
            this.bodyHealth -= this.TBAndHIVDepreciationRate - 0.1 * (kibera.getRandom().nextDouble() * this.TBAndHIVDepreciationRate);
        } else if (this.hasActiveInfection == true && this.hasHIV == false) {
            this.bodyHealth -= this.TBOnlyDepreciationRate - 0.1 * (kibera.getRandom().nextDouble() * this.TBOnlyDepreciationRate);
        } else if (this.hasHIV == true && this.hasActiveInfection == false) {
            this.bodyHealth -= this.HIVDepreciation - 0.1 * (kibera.getRandom().nextDouble() * this.HIVDepreciation);
        } else {
            this.bodyHealth -= 0.0000004 * kibera.getRandom().nextInt(5); // 30 year life expectancy
        }

    }
    
    public void healthDepreciationR(){
          
        if (this.hasActiveInfection && this.hasHIV) { //depreciation will be bigger if you have HIV and TB 
            this.bodyHealth -= Math.exp(-1.0 * this.TBAndHIVDepreciationRate);
        } else if (this.hasActiveInfection == true && this.hasHIV == false) {
            this.bodyHealth -=  Math.exp(-1.0 * this.TBOnlyDepreciationRate); 
        } else if (this.hasHIV == true && this.hasActiveInfection == false) {
            this.bodyHealth -= Math.exp(-1.0 * this.HIVDepreciation); 
        } else {
            this.bodyHealth -= 0.0000004 * kibera.getRandom().nextInt(5); // 30 year life expectancy
        }
    }

    
    
    
    //starting treatment
    public void startTreatment() {
        onTreatment = true;
        quitTreatmentBeforeEnd = false;
        this.contagiousPeriod = cStep + kibera.contagiousPeriodAfterTreatment_Min
                + kibera.getRandom().nextInt(kibera.contagiousPeriod_Max - kibera.contagiousPeriod_Min);

        //what kind of treatment plan will this person be following. For now follows regiment1 and continuaton phase 1a from CDC.
        //The first variable is the initial Phase and the second the continuation phase of the Regiment (1 = a, 2 = b, 3 = c)
        //Source: http://www.cdc.gov/mmwr/PDF/rr/rr5211.pdf
        TBTestingTime =  cStep + kibera.TBTestingTime_Min + kibera.getRandom().nextInt(kibera.TBTestingTime_Max-kibera.TBTestingTime_Min);
        treatmentPlan = new TreatmentPlan(kibera, this, 1, 1);

        //will person quit treatment and when
        if (kibera.getRandom().nextDouble() <= kibera.params.globalParam.getPercentQuitDuringART()) {
            willQuitTreatment = true;
            this.numberOfDosesBeforeQuitting = kibera.getRandom().nextInt(treatmentPlan.totalRequiredDoses);
        }
        
    }

    //treatment for TB 
    public void treatment() {
        //check to see if contagious 
        
        treatmentPlan.takeDose();
        this.bodyHealth = this.bodyHealth + treatmentPlan.recover(this);
        if (treatmentPlan.isCured == true) {
            recovered();
            onTreatment = false;
            quitTreatmentBeforeEnd = false;
            TBTestingTime = 0;
        }
        if (treatmentPlan.shouldQuit(this.willQuitTreatment, this.numberOfDosesBeforeQuitting)) {
            onTreatment = false;
            quitTreatmentBeforeEnd = true;
            alreadyChecked = false;
            TBTestingTime=0;
            //this.isContagious = true; 
        }
        
         
        if (cStep >= contagiousPeriod) {  //how long has this person been on treatment
            this.isContagious = false; // no more contagious if the treatment is taken well
        }
        
        // if not back to latent - relapse
        if (quitTreatmentBeforeEnd) {
           if (kibera.getRandom().nextDouble() < kibera.diseaseToLatentDuetoTreatmentDropputPro) {
                this.latentInfected(); // return back to latent
            } else {
                this.activeInfected(); // return back to active infected
            }
        }
        
    }

    //infect people in parcel
    
    
    public void infect() {

        // int totalAgent =this.getPosition().getResidents().size();
        // select one-thre agents at a time and infect
        int maxInter = kibera.getRandom().nextInt(kibera.params.globalParam.getMaximumNumberofContact());
        int totalPeopleAtFacility = this.getPosition().getResidents().numObjs;

        if (totalPeopleAtFacility < 2 || maxInter < 2) {
            return; // you are alone
        }

        if (this.getPosition().getResidents().numObjs < maxInter) {
            maxInter = this.getPosition().getResidents().numObjs;
        }
        Bag nextPeople = new Bag();
        nextPeople.clear();

        peopleAroundMe(maxInter, this.getPosition(), nextPeople);

        if (nextPeople.numObjs < 1) {
            return;
        }
        double bacilliEmitted = bacilliEmitted();

        // this.getPosition().getResidents().shuffle(kibera.getRandom()); // shuffle resident
        for (int personNextToMe = 0; personNextToMe < nextPeople.numObjs; personNextToMe++) {
            //https://books.google.com/books?id=dre060jWWIkC&pg=PA20&lpg=PA20&dq=tuberculosis+rate+of+exposure+per+hour&source=bl&ots=Svr-nfvzw-&sig=uv0tZ5_K45ISkqYDxfsd8kPglqw&hl=en&sa=X&ved=0CDgQ6AEwA2oVChMIprjP1NDdxwIVxjw-Ch3X_gWg#v=onepage&q=tuberculosis%20rate%20of%20exposure%20per%20hour&f=false

            
            ResidentTB res = (ResidentTB) nextPeople.objs[personNextToMe];

            // it is better to assume directly the bacilli emitted for exposure rather than holding bacilli count in the reident memory
            // we do not know the bacteria growth dynamics 
            // but we knows the time the agent stays in the facility
            // the more the agent stays their with the infected one the more likely it is exposed
            // uptake is the range from 0-bacilliEmitted
          
            if (bacilliEmitted > res.infectionDose) { //0.3*res.infectionDose
                if (res.isSusceptible) {
                    res.exposed();
                    timeTBIncubationAfterExposure = cStep + kibera.latentPhase_Min + kibera.getRandom().nextInt(kibera.latentPhase_Max - kibera.latentPhase_Min);
                    res.setExposurePhasePeriod(timeTBIncubationAfterExposure);

                }
                if (res.hasLatentInfection == true && res.hasLatenInfectionExposed != true) {
                    res.hasLatenInfectionExposed = true;
                    timeTBIncubationAfterExposure = cStep + kibera.latentPhase_Min + kibera.getRandom().nextInt(kibera.latentPhase_Max - kibera.latentPhase_Min);
                    res.setExposurePhasePeriod(timeTBIncubationAfterExposure);

//                if(kibera.getRandom().nextDouble() < kibera.reInfectionRateLatent) {
//                res.hasLatenInfectionExposed=true;
//                timeTBIncubationAfterExposure = cStep + kibera.latentPhase_Min + kibera.getRandom().nextInt(kibera.latentPhase_Max - kibera.latentPhase_Min);
//                res.setExposurePhasePeriod(timeTBIncubationAfterExposure);
//                }
                }

            }
        }

    }
    
    
    
    // if you are at a goal and if the goal is contaminated by bacilli, you become infected
    public void infected() {
        double bacilliEmitted = this.getPosition().getBacilliLoad();

        if (this.getHealthStatus() != this.activeInfected) {
            if (bacilliEmitted > this.infectionDose) { //0.3*res.infectionDose
                if (this.isSusceptible) {
                    this.exposed();
                    timeTBIncubationAfterExposure = cStep + kibera.latentPhase_Min + kibera.getRandom().nextInt(kibera.latentPhase_Max - kibera.latentPhase_Min);
                    this.setExposurePhasePeriod(timeTBIncubationAfterExposure);

                }
                if (this.hasLatentInfection == true && this.hasLatenInfectionExposed != true) {
                    this.hasLatenInfectionExposed = true;
                    timeTBIncubationAfterExposure = cStep + kibera.latentPhase_Min + kibera.getRandom().nextInt(kibera.latentPhase_Max - kibera.latentPhase_Min);
                    this.setExposurePhasePeriod(timeTBIncubationAfterExposure);

//                if(kibera.getRandom().nextDouble() < kibera.reInfectionRateLatent) {
//                res.hasLatenInfectionExposed=true;
//                timeTBIncubationAfterExposure = cStep + kibera.latentPhase_Min + kibera.getRandom().nextInt(kibera.latentPhase_Max - kibera.latentPhase_Min);
//                res.setExposurePhasePeriod(timeTBIncubationAfterExposure);
//                }
                }
            }
        }

    }
    //p = 1 - e^-gamma
    // gamma = beta*loadofBaciily * density
    private double exposureRiskProbability(){
        
        double density = this.getGoalLocation().getCapacity();
        return 0;
    }
            
            
    public double bacilliEmitted() {
        double bacilliEmitted = kibera.TBBacilliConcentration_Max * kibera.salivaPerCough;
        bacilliEmitted = bacilliEmitted
                * (kibera.coughsPerHour_Min + kibera.getRandom().nextInt(kibera.coughsPerHour_Max - kibera.coughsPerHour_Min));
        return bacilliEmitted;
        
    }
    
    // how long you stay at the facilities with infected person
    public void exposureProbabilityAtFacility(){
        
        
    }
    //facility density - how many people at the facility - school -can be many but the interaction is limited to classes
    // water facilities - each facility has area at the initial 
    public void peopleAroundMe(int size, Parcel facility, Bag nextToMe ){
        nextToMe.clear();
        if(facility.getResidents().numObjs ==0 ||size==0){
            return; // nobody around
        }
        if(size > facility.getResidents().numObjs){
            size = facility.getResidents().numObjs; // can not be more than the number of peopl
        }
        facility.getResidents().shuffle(kibera.getRandom()); 
        for (int i=0; i<size;i++){
            ResidentTB res = (ResidentTB) this.getPosition().getResidents().objs[i];
            nextToMe.add(res);
        }
        
         
    }

    // probability of infecting neighbors - air 
    public void probabilityOfInfectNeigh(){
        
    }
    
 //******
    // NEW CONCEPT - infect person contaminate the facility - the rate of contamination increase overtime if the infected person say for long
    // agent visit the facility might be infected with some probability
    // the virus cleared with some rate overtime 
 //
    
//    // exposure is high around facilities and home
//    // but on the road, it is near to zero
//    public double probabilityOfExposure() {
//        if (this.getPosition().equals(this.getHomeLocation())) {// at house intra-family transimission > 50%
//            return (0.5 + 0.5 * kibera.getRandom().nextDouble()); //> 50%
//        } 
//        else if (this.getPosition().equals(this.getGoalLocation())&& this.getPosition().equals(this.getHomeLocation())!=true) {
//            return (0.1 + 0.9* kibera.getRandom().nextDouble()); //0 to 100%  a chance of being next to infected agent is unknow  
//        }
//        else {
//            return (0.02 * kibera.getRandom().nextDouble()); //  < 2%   not likely to be infect on the road  to include any mass transportation
//        }
//
//    }

    private void utilizeWater() {
        if (this.getWaterLevel() >= kibera.MaximumWaterRequirement) {
            return;
        }
        double dailyUse = kibera.MinimumWaterRequirement + (kibera.MaximumWaterRequirement - kibera.MinimumWaterRequirement) * kibera.getRandom().nextDouble(); // getRandom()ly
        
        if (dailyUse + this.getWaterLevel() > kibera.MaximumWaterRequirement) {
            dailyUse = kibera.MaximumWaterRequirement -  this.getWaterLevel();
           
        }
       if(dailyUse <=0){
           return;
       }

        double WaterUsed = 0;

        if (this.getHome().getWaterAtHome() < dailyUse) {
            WaterUsed = this.getHome().getWaterAtHome();// drink all
        } else {
            WaterUsed = this.getHome().getWaterAtHome() - dailyUse; // drink your need only
        }
        double maxWateruse = this.getWaterLevel() + WaterUsed;

        this.setWaterLevel(maxWateruse);
        double wateratHome = this.getHome().getWaterAtHome() - WaterUsed;
        this.getHome().setWaterAtHome(wateratHome); // update the water level of the family bucket

    }
    
    public void dehydrate(){
       double hourlyuse = (kibera.MinimumWaterRequirement + (kibera.MaximumWaterRequirement - kibera.MinimumWaterRequirement) * kibera.getRandom().nextDouble())/ 24.0; // getRandom()ly
      double currentWaterlevel = this.getWaterLevel() - hourlyuse;
      if(currentWaterlevel < 0){
          currentWaterlevel =0;
      }
      this.setWaterLevel(currentWaterlevel);
    }

    public void move(KiberaTB kibera, int steps) {
       
        boolean atGoal = this.getPosition().equals(this.getGoalLocation());
        boolean shouldStay;

        if (steps == 0) {
            shouldStay = false;
        } else {
            shouldStay = shouldStay(steps);
        }

        if (atGoal == true && shouldStay == true) { //you're at your goal and you're supposed to stay.
            this.getGoalLocation().addResident(this);
            // tell to the parcel that you are their
            
            return;
        } else if (atGoal == true && shouldStay == false) { //you're at your goal but you finished. Thus, do the activity. Thus, come up with a new goal 
            doActivity(this.currentAct);
            this.getGoalLocation().removeResident(this); // leave
            calcGoal();
        } else {// else move to your goal

            /**
             * A resident can get to any location in Kibera in the one hour
             * timestep. The justification is that the size of kibera is about
             * 2.5 km^2 (Project Map Kibera), meaning that the max distance from
             * any point to another is sqrt(2.5^2 + 2.5^2) or = 2.5 km. Given
             * that the average walking speed for a human ranges between 4.51 km
             * to 5.43 km (wikipedia), a resident can easily traverse the
             * distance within an hour.
             */
            jitterX = 0.3*kibera.getRandom().nextDouble();
            jitterY = 0.3*kibera.getRandom().nextDouble();

            this.setPosition(this.getGoalLocation());
            kibera.world.setObjectLocation(this, new Double2D(this.getGoalLocation().getXLocation() + jitterX, this.getGoalLocation()
                    .getYLocation() + jitterY));

        }
    }

    public boolean shouldStay(int steps) {
        if (steps % this.getStayingPeriod() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void calcGoal() {

//		boolean atGoal = this.getPosition().equals(this.getGoalLocation());
//		boolean atHome = this.getPosition().equals(this.getHomeLocation());
  
        if (this.getPosition().equals(this.getHomeLocation()) == true) {
            int cAct = determineActivity(); //   select the best goal 

            if (cAct == ActivityTB.restaurant) {
                this.restaurantVisits -= 1;
            }
            

            ActivityTB act = new ActivityTB();
            this.setGoalLocation(act.bestActivityLocation(this, cAct, kibera)); // search the best location of your selected activity
            this.setCurrentActivity(cAct); // track current activity - for the visualization   
            this.setStayingPeriod(stayingPeriod(this.currentAct));
            
            
        }
        else if (this.getPosition().equals(this.getGoalLocation()) && this.getPosition().equals(this.getHomeLocation()) == false){
            if (kibera.getRandom().nextDouble() < 0.4) {
                int cAct = determineActivity(); //   select the best goal 

                if (cAct == ActivityTB.restaurant) {
                    this.restaurantVisits -= 1;
                }

                ActivityTB act = new ActivityTB();
                this.setGoalLocation(act.bestActivityLocation(this, cAct, kibera)); // search the best location of your selected activity
                this.setCurrentActivity(cAct); // track current activity - for the visualization   
                this.setStayingPeriod(stayingPeriod(this.currentAct));
            } else {
                this.setGoalLocation(this.getHomeLocation()); // search the best location of your selected activity
                this.setCurrentActivity(ActivityTB.stayHome); // track current activity - for the visualization   
                this.setStayingPeriod(stayingPeriod(0));
            }
        }  
        else{
            this.setGoalLocation(this.getHomeLocation()); // search the best location of your selected activity
            this.setCurrentActivity(ActivityTB.stayHome); // track current activity - for the visualization   
            this.setStayingPeriod(stayingPeriod(0));
        }
     
    }

   

    public int stayingPeriod(int act) {
        int stayingPeriod = 0;

        if (act == ActivityTB.school) { //at school 
            stayingPeriod = 6; //are in school for average of 8 hours 
        } else if (act == ActivityTB.work) {
            int day = timeManager.currentDayInWeek(cStep);
            if (day == 5) //if saturday work for 2-5 hours
            {
                stayingPeriod = 2 + kibera.getRandom().nextInt(4);
            } else //work for 5-9 hours a day during the week
            {
                stayingPeriod = 5 + kibera.getRandom().nextInt(5);
            }

            if (stayingPeriod + hourInDay >= 22) { //can't stay past 10 pm
                stayingPeriod = 22 - hourInDay;
            }
        } else if (act == ActivityTB.religion) {
            stayingPeriod = 1 + kibera.getRandom().nextInt(3); //stay at mosque for only 1 hour.
        } else if (act == ActivityTB.business) {
            stayingPeriod = 2 + kibera.getRandom().nextInt(5); //stay at business anywhere between 1-5 hours
        } else if (act == ActivityTB.restaurant) {
            stayingPeriod = 1 + kibera.getRandom().nextInt(3);
        } else if (act == ActivityTB.socialize) {
            stayingPeriod = 2 + kibera.getRandom().nextInt(5); //stay for min 1 hour, up to 4
        } else if (act == ActivityTB.healthCenter) {
           stayingPeriod = 1 + kibera.getRandom().nextInt(3); //for now stay in center for 1-3 hours
        } else {
            stayingPeriod = 1 + kibera.getRandom().nextInt(3);
        }

        if (stayingPeriod == 0) {
            System.out.println("Problem with staying period. Activity #: " + act);
        }

        return stayingPeriod;
    }
      
    public int determineActivity() {

        int act = 0;

        
        boolean isDayTime = hourInDay >= alpha && hourInDay <= beta;

        double[] activityPriorityWeight = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        if (this.isStudent) {
            activityPriorityWeight[ActivityTB.school] = schoolActivityWeight() ;
        }

        if (this.isEmployed && isDayTime) {
            activityPriorityWeight[ActivityTB.work] = workActivityWeight() ;
        }

        activityPriorityWeight[ActivityTB.religion] = religionActivityWeight(isDayTime) ;
        activityPriorityWeight[ActivityTB.business] = businessActivityWeight(isDayTime) ;
        activityPriorityWeight[ActivityTB.restaurant] = restaurantActivityWeight(isDayTime) ;
        activityPriorityWeight[ActivityTB.socialize] = socializeActivityWeight(isDayTime) ;
        activityPriorityWeight[ActivityTB.water] = waterActivityWeight(isDayTime) ;
        activityPriorityWeight[ActivityTB.healthCenter] = healthActivityWeight(isDayTime) ;

        // Find the activity with the heighest weight
        double maximum = activityPriorityWeight[0]; // start with the first value
        for (int i = 1; i < activityPriorityWeight.length; i++) {
            if (activityPriorityWeight[i] > maximum) {
                maximum = activityPriorityWeight[i]; // new maximum
                act = i;
            }
        }
        if (activityPriorityWeight[act] < 0.3) {
            //System.out.println("Act #: " + act + " Weight:" + activityPriorityWeight[act]);
            act = ActivityTB.stayHome;
        }

        onBusiness = false;
        onRestaurant = false;
        onSchool = false;
        onWater = false;
        onReligion = false;
        
        

        if (act == ActivityTB.business) {
            onBusiness = true;
        } else if (act == ActivityTB.restaurant) {
            onRestaurant = true;
        } else if (act == ActivityTB.school) {
            onSchool = true;
        } else if (act == ActivityTB.religion) {
            onReligion = true;
        } else if (act == ActivityTB.water) {
            onWater = true;
        }
         
        return act;
    }

    
    

    private double healthActivityWeight(boolean daytime) {
        double wHealthC =0;
		//if someone is sick, make their weight = 1

        //		if (onTreatment && this.treatmentPlan.takenDose == false) {
        //			if (hourEnd - hourInDay <= 0)
        //				wHealthC = 100;
        //			else
        //				wHealthC = .6 + (1 / ((double) hourEnd - hourInDay));
        //
        //			//System.out.println("Health: " + wHealthC);
        //		} else {
        if (daytime) {
            if (this.hasActiveInfection == true) {
                wHealthC = 0.2 + 0.8 * kibera.getRandom().nextDouble();
            } else {
                wHealthC = kibera.getRandom().nextDouble();
            }
        }
        //wHealthC * kibera.getRandom().nextDouble()
        return wHealthC * kibera.getRandom().nextDouble() ;
    }

    private double waterActivityWeight(boolean isDayTime) {

        double wBorehole = 0;
        // not enough water at home
        if (this.getAge() > 10 && isDayTime) {
            if (this.getHome().getWaterAtHome() < (kibera.MinimumWaterRequirement * (this.getHome().getHouseholdMembers().size()))) {
                wBorehole = 0.5  + 0.5 * kibera.getRandom().nextDouble();
            }
        }

        return wBorehole * kibera.getRandom().nextDouble();
    }

    private double socializeActivityWeight(boolean isDayTime) {
        double wVisitSoc = 0;
        double dayBenefit;
        int day = timeManager.currentDayInWeek(cStep);
        if (day == 5 || day == 6) {
            dayBenefit = .4;
        }
        else{
            dayBenefit = .1;
        }
        if (isDayTime) {
            wVisitSoc = (0.2 + dayBenefit)  + (0.8-dayBenefit) * kibera.getRandom().nextDouble();
        }
        return wVisitSoc * kibera.getRandom().nextDouble();
    }

    private double restaurantActivityWeight(boolean isDayTime) {
        double wRestaurant;

        //assumption here is that the average adult will visit a restaurant or bar 0 to 3 times a week 
        if (getRestaurantVisits() > 0 && isDayTime) { //nothing open between 2 and 8
            wRestaurant = ((getRestaurantVisits() / 3.0) * .7) * kibera.getRandom().nextDouble();
        } else if (getRestaurantVisits() > 0 && hourInDay > 12 && hourInDay < 14) {
            wRestaurant = ((getRestaurantVisits() / 3.0) * .3) * kibera.getRandom().nextDouble();
        } else {
            wRestaurant = 0;
        }

        return wRestaurant * kibera.getRandom().nextDouble();
    }

    private double businessActivityWeight(boolean isDayTime) {
        double wMarket;
        int day = timeManager.currentDayInWeek(cStep);
        double weekendBenefit = 0;

        if (day == 5 || day == 6) //more likely to go during weekend 
        {
            weekendBenefit = .2;
        }

        if (isDayTime) {
            wMarket = (0.5 + weekendBenefit) * Math.sin(this.getAge()) + 0.3 * kibera.getRandom().nextDouble();
        } else {
            wMarket = 0;
        }
        return wMarket * kibera.getRandom().nextDouble();
    }

    private double religionActivityWeight(boolean isDayTime) {
        // worship time for a muslim
        double wReligion = 0.0;
        if (isDayTime) {
            if (this.getReligion().equals(Religion.Muslim)) {
                if (this.getAge() > 10) {

                    if (hourInDay == 5 || hourInDay == 6 || hourInDay > 12 && hourInDay < 14 || hourInDay > 15 && hourInDay < 17) {

                        wReligion = 0.5  + 0.5 * kibera.getRandom().nextDouble();
                    }
                } else {
                    wReligion = 0.1  + 0.5 * kibera.getRandom().nextDouble();
                }
            } else { //christian
                if (timeManager.currentDayInWeek(cStep) == 6) { //if sunday, higher chance of going 
                    wReligion = 0.6 + 0.2 * kibera.getRandom().nextDouble();
                } else {
                    wReligion = 0.1 + 0.5 * kibera.getRandom().nextDouble();
                }
            }
        }
        return wReligion * kibera.getRandom().nextDouble();
    }

    private double workActivityWeight() {
        int workDay = timeManager.currentDayInWeek(cStep);
        double wWeight;
        if (workDay < 5 ) {
            wWeight = 0.5 + 0.5 * kibera.getRandom().nextDouble();
        } else if (workDay == 5 && kibera.getRandom().nextDouble() < .25) { //off on Sundays and only 25% of people work on Saturday
            wWeight = 0.3 + 0.7 * kibera.getRandom().nextDouble();
        } else{
            wWeight =0;
        }
        return wWeight;
    }

    private double schoolActivityWeight() {
        boolean isSchoolDay = (timeManager.currentDayInWeek(cStep) <= 4); // school only open from monday to friday ( day 0 to 4 of the week)
        int month = timeManager.currentMonth(cStep);
        boolean isSchoolMonth;
        double sWeight =0;
        if (month != 6 || month != 7) {
            isSchoolMonth = true;
        } else {
            isSchoolMonth = false;
        }

        // if student second priority is school
        if (this.isStudent == true && isSchoolDay && isSchoolMonth && hourInDay > 8 && hourInDay < 16) { //is a student, 
            sWeight= 0.5 + 0.5 * kibera.getRandom().nextDouble();
        } 
        
        return sWeight ;
    }

    public void doActivity(int actId) {

        if (actId == ActivityTB.water) {
            double waterReq = 2 * kibera.MaximumWaterRequirement + (2 * kibera.getRandom().nextDouble() * kibera.MaximumWaterRequirement); // how many litres you can collect?
            this.getHome().setWaterAtHome(waterReq);
        }
        if (actId == ActivityTB.healthCenter) {
            if (kibera.params.globalParam.getIsOnTreatment() && kibera.getRandom().nextDouble() < 0.6) {
                if (alreadyChecked == false && willStartTreatmentTB == true) {
                    startTreatment();
                    alreadyChecked = true;

                }
            }
            //check for tb if symptoms show, or when the test results would be positive 

        }

    }
    
    // infection at facilites
    
     private void infectWhenSocialized() {
        
// find number of people that you socialize - household size - but may be interact wit one or all
        Bag myFriends = new Bag(kibera.socialNetwork.getEdgesOut(this));

        for (int i = 0; i < myFriends.size(); i++) {
            Edge e = (Edge) (myFriends.get(i));
            //Get the resident linked to me
            ResidentTB friend = (ResidentTB) e.getOtherNode(this);
            if (this.getGoalLocation() != friend.getHomeLocation()) {
                myFriends.remove(i);
            }
        }
        myFriends.resize(myFriends.size());

        if (myFriends.size() == 0) {
            return;
        }
        if (myFriends.size() == 1) {
            Edge e = (Edge) (myFriends.get(0));
//Get the resident linked to me
            ResidentTB friend = (ResidentTB) e.getOtherNode(this);
            
// infect this friend

        } else {
            int potContact = kibera.getRandom().nextInt(myFriends.size());

            for (int i = 0; i < (potContact + 1); i++) {
                Edge e = (Edge) (myFriends.get(i));
                //Get the resident linked to me
                ResidentTB friend = (ResidentTB) e.getOtherNode(this);
                
// infect friends
            }

        }
    }
    
   public void identifyStructure(int ac, Parcel goal){
       for (Object s: goal.getStructure()){
           Structure st = (Structure)s;
          
         
       }
       
   }
    
  //<editor-fold defaultstate="collapsed" desc="Accessors">  
    //----------------Accessor/Modifier Methods---------------
    public int getRestaurantVisits() {
        return restaurantVisits;
    }

    public void setRestaurantVisits(int val) {
        restaurantVisits = val;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int val) {
        age = val;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int val) {
        gender = val;
        if (val == 0) {
            isMale = true;
        } else {
            isMale = false;
        }
    }

   

    public Parcel getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(Parcel h) {
        homeLocation = h;
    }

    public Parcel getPosition() {
        return position;
    }

    public void setPosition(Parcel p) {
        position = p;
    }

    public Parcel getGoalLocation() {
        return goal;
    }

    public void setGoalLocation(Parcel g) {
        goal = g;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public void setTimeManager(TimeManager tm) {
        timeManager = tm;
    }

    public int getHourInDay() {
        return hourInDay;
    }

    public void setHourInDay(int val) {
        hourInDay = val;
    }

    public int getLatUse() {
        return latUse;
    }

    public void setLatUse(int val) {
        latUse = val;
    }

    public Household getHome() {
        return home;
    }

    public void setHome(Household h) {
        home = h;
    }

    public int getCStep() {
        return cStep;
    }

    public void setCStep(int val) {
        cStep = val;
    }
 public boolean getHIVStatus() {
        return hasHIV;
    }

    public void setHIVStatus(boolean val) {
        hasHIV = val;
    }

    public int getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(int val) {
        healthStatus = val;
    }
    //current activity
    public void setCurrentActivity(int a) {
        this.currentAct = a;
    }

    public int getCurrentActivity() {
        return currentAct;
    }

    public void setWaterLevel(double w) {
        this.waterDemand = w;
    }

    public double getWaterLevel() {
        return waterDemand;
    }

   

    public int geExposurePhasePeriod() {
        return exposurePhasePeriod;
    }

    public void setExposurePhasePeriod(int val) {
        exposurePhasePeriod = val;
    }

   
    
    public void setSchoolIndex(int val) {
        schoolIndex = val;
    }

    public int getSchoolIndex() {
        return schoolIndex;
    }
    
     public void setSchoolClass(int val) {
        schoolClass = val;
    }

    public int getSchoolClass() {
        return schoolClass;
    }

    public double getTwoParamterLogic(double c, double a, double b, double value) {
    // a = the maximum slope
        // b = the half-way point between c(min) and 1(max), also where the slope is maximized.  
        // c = asymptotic minimum 
        // probability p(value)  
        return c + ((1 - c) / (1 + Math.exp(-a * (value - b))));

    }
     //</editor-fold>

}
