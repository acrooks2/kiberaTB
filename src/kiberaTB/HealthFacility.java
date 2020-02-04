package kiberaTB;

import sim.util.Bag;

public class HealthFacility {

	/** The structure a facility is located within*/
	private Structure structure;
	public Structure getStructure() { return structure; }
	public void setStructure(Structure val) { structure = val; }
	
	/** The facility ID */
	private int healthFacilityType;
	public int getFacilityID() { return healthFacilityType; }
	public void setFacilityID(int val) { healthFacilityType = val; }
	
	/** The employees working at the health facility */
	private Bag employees;
	public Bag getEmployees() { return employees; }
	public void addEmployee(ResidentTB val) { employees.add(val); }
	public void removeEmployee(ResidentTB val) { employees.remove(val); }
	
	/** The maximum number of employees at the health facility */
	private int employeeCapacity;
	public int getEmployeeCapacity() { return employeeCapacity; }
	public void setEmployeeCapacity(int val) { employeeCapacity = val; }
	
	/** Identifies whether the business is formal or informal */
	public enum BusinessType { formal, informal };
	BusinessType businessType;
	public BusinessType getBusienssType() { return businessType; }
	public void setBusinessType(BusinessType val) { businessType = val; }
	
//	/** Each parcel has a 10x10 resolution. Assuming 4 people per 1x1 square, each parcel has a max capacity of 400 */
//	public int capacity = 400;
//
//	public int getCapacity() {
//		return capacity;
//	}
//
//	public void setCapacity(int val) {
//		capacity = val;
//	}
	
	
	public HealthFacility(Structure s, int healthFacilityType) {
		this.structure = s;
		this.healthFacilityType = healthFacilityType;
		
		employees = new Bag();
	}
	
	//has capacity been reached
	public boolean isEmployeeCapacityReached() {

			int numEmployees = employees.size();
		
			if (numEmployees == getEmployeeCapacity()) {
				return true;
			}
			else { return false; }
	}


}
