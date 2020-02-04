package kiberaTB;

import sim.util.Bag;

/*
 * A structure is a building in the slum, it can contain households, businesses, and/or services
 */

public class Structure {
	
	/** The parcel a structure resides on */
	private Parcel structureLocation;
	public Parcel getParcel() { return structureLocation; }
	public void setParcel(Parcel val) { structureLocation = val; }
	
	/** The households residing in the structure */
	private Bag households;
	public Bag getHouseholds() { return households; }
	public void addHousehold(Household val) { households.add(val); }
	public void removeHouseholds(Household val) { households.remove(val); }
	
	/** The businesses located in the structure */
	private Bag businesses;
	public Bag getBusinesses() { return businesses; }
	public void addBusinesses(Business val) { businesses.add(val); }
	public void removeBusinesses(Business val) { businesses.remove(val); }
	

	/** The health facilities located in the structure */
	private Bag healthFacilities;
	public Bag getHealthFacilities() { return healthFacilities; }
	public void getHealthFacilities(Bag val) { healthFacilities = val; }		
	public void addHealthFacility(HealthFacility val) { healthFacilities.add(val); }	
	
	/** The religious facilities (church/mosque) located in the structure */
	private Bag religiousFacilities;
	public Bag getReligiousFacilities() { return religiousFacilities; }
	public void getReligiousFacilities(Bag val) { religiousFacilities = val; }		
	public void addReligiousFacility(ReligiousFacility val) { religiousFacilities.add(val); }	

	/** The schools located in the structure */
	private Bag schools;
	public Bag getSchools() { return schools; }
	public void getSchools(Bag val) { schools = val; }		
	public void addSchool(School val) { schools.add(val); }
	
	/** The ethnicity of the structure */
	private static String ethnicity; 
	public String getEthnicity() { return ethnicity; }
	public void setEthnicity(String e) {
		ethnicity = e; 
		for(int k = 0; k < households.size(); k++) {
			((Household)households.get(k)).setEthnicity(e);
		}
	}
	
	/** The capacity (number) of households and businesses that can reside in the structure */
	private int hhCapacity;
	public int getHouseholdCapacity() { return hhCapacity; }	
	private int businessCapacity;
	public int getBusinessCapacity() { return businessCapacity; }
	public void setStructureCapacity(int maxNumberHH, int maxNumberBusiness) {
		this.hhCapacity = maxNumberHH;
		this.businessCapacity = maxNumberBusiness;
	}

	public Structure(Parcel p) {
		this.structureLocation = p;
		households = new Bag();
		businesses = new Bag();
		schools = new Bag();
		healthFacilities = new Bag();
		religiousFacilities = new Bag();
	}
	
	public Structure() {
		households = new Bag();
		businesses = new Bag();
		schools = new Bag();
	}
	
	public boolean isStructureOccupied(KiberaTB kibera) {
		if (this.getHouseholdCapacity() < this.getHouseholds().size()) { return true; }
		else { return false; }
	}
	

	



	




}
