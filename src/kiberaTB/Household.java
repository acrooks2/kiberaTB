package kiberaTB;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import kiberaTB.ResidentTB.Religion;

/*
 * Households will settle into a final location based on their happiness level. Households will
 * seek neighbors "like" them. This behavior is based on the Schelling segregation model.
 */
public class Household  {

	/** The residents living within the same household */
	private Bag householdMembers;

	public Bag getHouseholdMembers() {
		return householdMembers;
	}

	public void addHouseholdMembers(ResidentTB val) {
		householdMembers.add(val);
	}

	public void removeHouseholdMember(ResidentTB val) {
		householdMembers.remove(val);
	}

	public void setHouseholdMembers(Bag val) {
		householdMembers = val;
	}

	/** The rent paid by a household */
	private int householdRent;

	public int getHouseholdRent() {
		return householdRent;
	}

	public void setHouseholdRent(int val) {
		householdRent = val;
	}

	/** The structure a household is located within */
	private Structure householdStructure;

	public Structure getStructure() {
		return householdStructure;
	}

	public void setStructure(Structure val) {
		householdStructure = val;
	}

	private ResidentTB.Religion religion;

	public ResidentTB.Religion getReligion() {
		return religion;
	}

	public void setReligion(ResidentTB.Religion val) {
		religion = val;
	}

	/** The ethnicity of the structure */
	private static String ethnicity;

	public String getEthnicity() {
		return ethnicity;
	}

	public void setEthnicity(String e) {
		ethnicity = e;
		for (int k = 0; k < householdMembers.size(); k++) {
			((ResidentTB) householdMembers.get(k)).setEthnicity(e);
		}
	}

	public Household(KiberaTB kibera, Structure s) {
		this.householdStructure = s;
		householdMembers = new Bag();
		setWaterAtHome(kibera.MinimumWaterRequirement * this.householdMembers.size());
	}

		/*
	 * public String getHouseholdEthnicity() { Bag householdEthnicity = new Bag(); String residentEthnicity = null;
	 * 
	 * for (int i = 0; i < householdMembers.numObjs; i++) { Resident r = (Resident) householdMembers.objs[i]; residentEthnicity =
	 * r.getEthnicity(); householdEthnicity.add(residentEthnicity); } return residentEthnicity; }
	 */

	private double waterTot = 0.0; // total water 

	// hold the amount of water in house
	public void setWaterAtHome(double water) {

		this.waterTot = water;

	}

	public double getWaterAtHome() {
		return waterTot;
	}

	public String toString() {
		return householdStructure.getParcel().toString();
	}
}
