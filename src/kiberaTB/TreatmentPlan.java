package kiberaTB;

public class TreatmentPlan {

	public boolean initialPhaseDaily = false; //does treatment require daily dose
	public boolean continuationPhaseDaily = false;
	public boolean worksForHIV = false;
	public boolean takenDose = false;
	public boolean isCured = false;

	public int totalRequiredDoses;
	public int phaseOneRequiredDoses;
	public int continuationPhaseRequiredDoses;
	public int currentDoses;

	public double TBAndHIVAppreciationRate;
	public double TBAppreciationRate;

	public TreatmentPlan(KiberaTB kibera, ResidentTB res, int initialPhase, int continuationPhase) {

		currentDoses = 0;

		if (initialPhase == 1){
			regimeOne(continuationPhase);
                }

		determineAppreciationRates(kibera, res);
	}

	//This is the treatment for regimen 1 with continuation phase 1a from CDC report (Table 2.0)
	//Source: http://www.cdc.gov/mmwr/PDF/rr/rr5211.pdf
	public void regimeOne(int continuationPhase) {
		phaseOneRequiredDoses = 56;//day 
		initialPhaseDaily = true;

		if (continuationPhase == 1) {
			continuationPhaseRequiredDoses = 126;// days
			continuationPhaseDaily = true;
			worksForHIV = true;
		}
		totalRequiredDoses = (phaseOneRequiredDoses + continuationPhaseRequiredDoses)*24;

	}

	public void determineAppreciationRates(KiberaTB kibera, ResidentTB res) {
		double healthChange = (kibera.bodyHealth_Max - res.bodyHealth)/ this.totalRequiredDoses;
		
		this.TBAndHIVAppreciationRate = (res.TBAndHIVDepreciationRate ) + healthChange;
		this.TBAppreciationRate = (res.TBOnlyDepreciationRate ) + healthChange;
	}

	public void takeDose() {
		currentDoses++;
		takenDose = true;

		if (currentDoses >= totalRequiredDoses){
                    isCured = true;
                }
			
	}

	public boolean shouldQuit(boolean supposedToQuit, int doseNumber) {

		if (supposedToQuit) {
			if (doseNumber >= currentDoses)
				return true;
			else
				return false;
		} else
			return false;
	}

	public double recover(ResidentTB res) {
		
	    if (res.hasHIV && res.hasActiveInfection) {
                return this.TBAndHIVAppreciationRate;
            } else if (res.hasHIV == false && res.hasActiveInfection == true) {
                return this.TBAppreciationRate;
            } else {
                System.out.println("Problem in TreatmentPlan");
                
                return 0.0;
            }
		
	}
}
