package kiberaTB;

import sim.util.Bag;

public class School {

	/** The structure a facility is located within */
	private Structure schoolStructure;

	public Structure getStructure() {
		return schoolStructure;
	}

	public void setStructure(Structure val) {
		schoolStructure = val;
	}

	/** The facility ID */
	private int facilityID;

	public int getFacilityID() {
		return facilityID;
	}

	public void setFacilityID(int val) {
		facilityID = val;
	}

	/** Each parcel has a 10x10 resolution. Assuming 4 people per 1x1 square, each parcel has a max capacity of 400.
	 * However, a school has a larger capacity of 700.
	 */
	
	public int capacity = 700;

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int val) {
		capacity = val;
	}

	/** The maximum number of employees at the school */
	private int employeeCapacity;

	public int getEmployeeCapacity() {
		return employeeCapacity;
	}

	public void setEmployeeCapacity(int val) {
		employeeCapacity = val;
	}

	/** The students enrolled in the school */
	public Bag students;

	public Bag getStudents() {
		return students;
	}

	public void addStudents(ResidentTB val) {
		students.add(val);
	}

	public void removeStudents(ResidentTB val) {
		students.remove(val);
	}

	/** The employees working at the school */
	private Bag employees;

	public Bag getEmployees() {
		return employees;
	}

	public void addEmployee(ResidentTB val) {
		employees.add(val);
	}

	public void removeEmployee(ResidentTB val) {
		employees.remove(val);
	}

	/** Identifies whether the business is formal or informal */
	public enum BusinessType {
		formal, informal
	};

	BusinessType businessType;

	public BusinessType getBusienssType() {
		return businessType;
	}

	public void setBusinessType(BusinessType val) {
		businessType = val;
	}

	public School(Structure s, int id) {
		this.schoolStructure = s;
		this.facilityID = id;

		students = new Bag();
		employees = new Bag();
	}

	//has capacity been reached
	public boolean isStudentCapacityReached() {

		int numStudents = students.size();

		if (numStudents == getCapacity()) {
			return true;
		} else {
			return false;
		}
	}

	//has capacity been reached
	public boolean isEmployeeCapacityReached() {

		int numEmployees = employees.size();

		if (numEmployees == getEmployeeCapacity()) {
			return true;
		} else {
			return false;
		}
	}
}
