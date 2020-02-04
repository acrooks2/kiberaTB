/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kiberaTB;

/**
 *
 * @author gmu
 */
import com.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.io.geo.ArcInfoASCGridExporter;


import sim.util.Bag;
// based on riftland worldobserver class
// thanks goes to mcoletti and jbasset

public class KiberaTBObserver implements Steppable {

   
    private CSVWriter dataCSVFile_condition; // CSV file that contains run data

   
    private CSVWriter dataCSVFile_act; // CSV file that contains run data regarding actvities  

   
    private CSVWriter dataCSVFile_status;

   
    private CSVWriter dataCSVFile_hotspot;

    KiberaTB kibera;
    Bag TBInfectionGridBag = new Bag();
    private boolean writeGrid = false;
    private String expName = "exp";


    public final static int ORDERING = 1;

    private int step = 0;

    KiberaTBObserver(KiberaTB k) {
		//    	setup(world);
        //<GCB>: you may want to adjust the number of columns based on these flags.
        // both in createLogFile, and step
        kibera = k;
        this.setExperimentName(kibera.params.globalParam.getExpFileName());
        startLogFile();
    }

    KiberaTBObserver() {
        startLogFile();
    }
    
    
    public void setExperimentName(String exp){
        this.expName = exp;
    }
    
    public String getExperimentName(){
        return expName;
    }

    private void startLogFile() {
        // Create a CSV file to capture data for this run.
        try {
            createLogFile();

            // First line of file contains field names
            String[] header = new String[]{"Step", "Healthy", "ExposedTB", "LatentTB","LatentExposed", "InfectedTB", "Treated", "Dead"};
            dataCSVFile_condition.writeNext(header);

            String[] header_cStatus = new String[]{"Step", "Susciptable", "Exposed", "LatentTB","LatentExposed", "InfectedTB", "Recovered", "Dead"};
            dataCSVFile_status.writeNext(header_cStatus);
            // activity

            String[] header_act = new String[]{"Step", "Total Residents", "At Home", "School", "Water", "Religion", "Resteraunt",
                "Health Center", "Socialize", "Work", "Business"};
            dataCSVFile_act.writeNext(header_act);

            String[] header_hotspot = new String[]{"Time", "BusinessInfected", "ResterauntInfected", "SchoolInfected", "WaterInfected",
                "ReligionInfected", "BusinessDiseased", "ResterauntDiseased", "SchoolDiseased", "WaterDiseased", "ReligionDiseased"};
            dataCSVFile_hotspot.writeNext(header_hotspot);

        } catch (IOException ex) {
            Logger.getLogger(KiberaTB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void writeCSVFiles(){
         String job = Long.toString(((KiberaTB) kibera).schedule.getSteps());
         String numTotAgent = Integer.toString(kibera.allResidents.size());
        String numAtHome = Integer.toString(kibera.sumAct[ActivityTB.stayHome]);
        String numSchool = Integer.toString(kibera.sumAct[ActivityTB.school]);
        String numWater = Integer.toString(kibera.sumAct[ActivityTB.water]);
        String numReligion = Integer.toString(kibera.sumAct[ActivityTB.religion]);
        String numResteraunt = Integer.toString(kibera.sumAct[ActivityTB.resteraunt]);
        String numHealthCenter = Integer.toString(kibera.sumAct[ActivityTB.healthCenter]);
        String numSocialize = Integer.toString(kibera.sumAct[ActivityTB.socialize]);
        String numWork = Integer.toString(kibera.sumAct[ActivityTB.work]);
        String numBusiness = Integer.toString(kibera.sumAct[ActivityTB.business]);

        String[] data_act = new String[]{job, numTotAgent, numAtHome, numSchool, numWater, numReligion,
            numResteraunt, numHealthCenter, numSocialize, numWork, numBusiness};


        String numHealthy = Integer.toString(kibera.totalHealthy);
        String numExposedTB = Integer.toString(kibera.totalExposed);
        String numLatentExposedTB = Integer.toString(kibera.totalLatentExposedTB);
        String numLatentTB = Integer.toString(kibera.totalActiveTB);
        String numInfectedTB = Integer.toString(kibera.totalContagious);
        String numOnTreatment = Integer.toString(kibera.totalOnTreatment);
        String numDeath = Integer.toString(kibera.totalDead);

        String[] data_condition = new String[]{job, numHealthy, numExposedTB, numLatentTB, numLatentExposedTB, numInfectedTB,
            numOnTreatment, numDeath};

        //Get data for health status 
        String numSuscpitable = Integer.toString(kibera.totalSus);
        String numExposed = Integer.toString(kibera.totalExp);
        String numLatent = Integer.toString(kibera.totalLatentTB);

        String numInfected = Integer.toString(kibera.totalInf);
        String numRecovered = Integer.toString(kibera.totalRec);

        String[] data_status = new String[]{job, numSuscpitable, numExposed, numLatent, numLatentExposedTB, numInfected, numRecovered, numDeath};

        //Get data for hotspots 
        String businessInfected = Integer.toString(kibera.hotspotLocsInfected[0]);
        String resterauntInfected = Integer.toString(kibera.hotspotLocsInfected[1]);
        String schoolInfected = Integer.toString(kibera.hotspotLocsInfected[2]);
        String waterInfected = Integer.toString(kibera.hotspotLocsInfected[3]);
        String religionInfected = Integer.toString(kibera.hotspotLocsInfected[4]);

        String businessDiseased = Integer.toString(kibera.hotspotLocsDiseased[0]);
        String resterauntDiseased = Integer.toString(kibera.hotspotLocsDiseased[1]);
        String schoolDiseased = Integer.toString(kibera.hotspotLocsDiseased[2]);
        String waterDiseased = Integer.toString(kibera.hotspotLocsDiseased[3]);
        String religionDiseased = Integer.toString(kibera.hotspotLocsDiseased[4]);

        String[] data_hotspot = new String[]{job, businessInfected, resterauntInfected, schoolInfected,
            waterInfected, religionInfected, businessDiseased, resterauntDiseased, schoolDiseased, waterDiseased,
            religionDiseased};
        
        this.dataCSVFile_act.writeNext(data_act);
        this.dataCSVFile_condition.writeNext(data_condition);
        this.dataCSVFile_status.writeNext(data_status);
        this.dataCSVFile_hotspot.writeNext(data_hotspot);

    }
    public void writeGridFiles(){
        
        try {
            
            // some trick to write grid every x step
            if (writeGrid == true) {
                count = count + 1;
                long now = System.currentTimeMillis();
                String filename = String.format("%ty%tm%td%tH%tM%tS", now, now, now, now, now, now) + "_"
                        + getExperimentName() + count + "_TBInfectionASC.asc";

                BufferedWriter dataASCTB = new BufferedWriter(new FileWriter(filename));

                writeTBSpreadGrid();
                ArcInfoASCGridExporter.write(kibera.allCampGeoGrid, dataASCTB);
                TBInfectionGridBag.add(dataASCTB);

            }
        } catch (IOException ex) {
            Logger.getLogger(KiberaTB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    int count = 0;

    public void step(SimState state) {
        kibera = (KiberaTB) state;
      
        writeCSVFiles();
        writeGridFiles();
        finish();

       //"At Home", "School", "Water", "Religion", "Resteraunt", "Health Center", "Socialize", "Latrine", "Work", "Business"
       
//        if (kibera.schedule.getSteps() % kibera.params.globalParam.getwriteGridTimerFrequency()==0) { // every month ??
//            writeGrid = true;
//        } else {
//            writeGrid = false;
//        }
        
    }

    void finish() {
        if (kibera.schedule.getSteps() == 87600) {

            try {
//            if (this.dataCSVFile_act != null) {
//                this.dataCSVFile_act.close();
//            }
                this.dataCSVFile_act.close();
                this.dataCSVFile_condition.close();
                this.dataCSVFile_status.close();
                this.dataCSVFile_hotspot.close();

                for (Object o : TBInfectionGridBag) {
                    BufferedWriter bw = (BufferedWriter) o;
                    bw.close();
                }

            } catch (IOException ex) {
                Logger.getLogger(KiberaTB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void createLogFile() throws IOException {
        long now = System.currentTimeMillis();

        String filename_condition = String.format("%tm%td%tH%tM%tS", now, now, now, now, now, now)+ "_" +  getExperimentName()+ "_kiberaTB_condition.csv";
        String filename_status = String.format("%tm%td%tH%tM%tS", now, now, now, now, now, now)+ "_" +  getExperimentName()+ "_kiberaTB_status.csv";
        String filename_act = String.format("%tm%td%tH%tM%tS", now, now, now, now, now, now)+ "_" +  getExperimentName()+"_kiberaTB_activity.csv";
        String filename_hotspot = String.format("%tm%td%tH%tM%tS", now, now, now, now, now, now) + "_"+  getExperimentName()+"_kiberaTB_hotspot.csv";

        //printing health condition
       
        this.dataCSVFile_condition = new CSVWriter(new FileWriter(filename_condition, true));

        //printing health status
        
        this.dataCSVFile_status = new CSVWriter(new FileWriter(filename_status, true));

        //printing health activity
       
        this.dataCSVFile_act = new CSVWriter(new FileWriter(filename_act, true));

     //printing hot spot
        this.dataCSVFile_hotspot = new CSVWriter(new FileWriter(filename_hotspot,true));
    }

    public void writeTBSpreadGrid() {

        DoubleGrid2D grid = new DoubleGrid2D(kibera.landGrid.getWidth(), kibera.landGrid.getHeight());
      // first put all values zero

        for (int i = 0; i < kibera.landGrid.getWidth(); i++) {
            for (int j = 0; j < kibera.landGrid.getHeight(); j++) {
                Parcel parcel = (Parcel) kibera.landGrid.get(i, j);
                if (parcel.getParcelID() > 0) {
                    grid.field[i][j] = 0;
                }

            }
        }
        // then write the current refugee health status
        for (Object o : kibera.allResidents) {
            ResidentTB r = (ResidentTB) o;
            double tot = grid.field[r.getPosition().getXLocation()][r.getPosition().getYLocation()];
            if (r.getHealthStatus() == r.activeInfected) {
                grid.field[r.getPosition().getXLocation()][r.getPosition().getYLocation()] = tot + 1;
            } else {
                grid.field[r.getPosition().getXLocation()][r.getPosition().getYLocation()] = tot;
            }

        }

        kibera.allCampGeoGrid.setGrid(grid);

    }

   

}
