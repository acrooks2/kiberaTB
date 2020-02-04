/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kiberaTB;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.Grid2D;
import sim.util.Bag;

/**
 *
 * @author petmmac
 */
public class TBBacilli implements Steppable {
  
     KiberaTB kibera;
    private double totalBacilliLoad; // total viral load = infectious + non-infectious
    
    private double proportionInfectiousBacilli;
   
    // setter and getter = methods 
    //<editor-fold defaultstate="collapsed" desc="Accessors">  
    // setter - total viral load
    public void setTotalBacilliLoad(double total) {
        this.totalBacilliLoad = total;
    }
    // setter - total viral load    
    public double getTotalBacilliLoad() {
        return this.totalBacilliLoad;
    }

    
    
    public void setProportionInfectiousBacilli( double pro){
        this.proportionInfectiousBacilli = pro;
    }
     // getter - total non-infectious virus
    public double getProportionInfectiousBacilli (){
        return this.proportionInfectiousBacilli ;
    }

    //</editor-fold>  
    
    // go through the all resident at the location
    //if infected resident is avaiable add the aount of bacilli
    public void newBacilliLoadProduction(Parcel facility) {
       double totLoad = 0;
       for (Object obj : facility.getResidents()) {
            ResidentTB res = (ResidentTB) obj;
           
            if(res.getHealthStatus() != res.activeInfected){
                continue;
            }
            
            totLoad = totLoad + res.bacilliEmitted();
   
        }
        
        facility.setBacilliLoad(totLoad);
      
    }

    public void infect() {
      
    }


    // total = infectious _ non infectious
    public double degradeBacilliLoadRate() {
        //  Y = Yo * e^rt  , where 0<r<1, t =1
      return (Math.exp(-1.0 * kibera.bacilliClearanceRatePerHour));
     
    }

    public void updateViralLoad(Parcel facility) {
      
        double degInfe = this.getTotalBacilliLoad() * degradeBacilliLoadRate(); // degrade infectious

        double newBacilli = degInfe + facility.getBacilliLoad(); // get the infect+ non infectious
        if(newBacilli <= 0){
            newBacilli = 0;
        }
        
        facility.setBacilliLoad(newBacilli); 
        
   }
    
   public void updateViralLoad(){
       
   }
    

    public void step(SimState state) {
        kibera = (KiberaTB) state;
       infect();
       updateViralLoad();

      

    }
}
