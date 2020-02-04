package kiberaTB;

import sim.util.Bag;

public class Facility {

	/** The structure a facility is located within */
	private Structure facilityStructure;

	public Structure getStructure() {
		return facilityStructure;
	}

	public void setStructure(Structure val) {
		facilityStructure = val;
	}

	public int employeeCapacity;

	public int getEmployeeCapacity() {
		return employeeCapacity;
	}

	public void setEmployeeCapacity(int val) {
		employeeCapacity = val;
	}

	/** The facility ID */
	private int facilityID;

	public int getFacilityID() {
		return facilityID;
	}

	public void setFacilityID(int val) {
		facilityID = val;
	}

	/** Each parcel has a 10x10 resolution. Assuming 1 to 2(1.5) people per 1x1 square, each parcel has a max capacity of 400 */
	public int capacity = 150;

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int val) {
		capacity = val;
	}

	/** The students enrolled in the school */
	private Bag students;

	public Bag getStudents() {
		return students;
	}

	public void addStudents(ResidentTB val) {
		students.add(val);
	}

	public void removeStudents(ResidentTB val) {
		students.remove(val);
	}

	public Facility(Structure s, int id) {
		this.facilityStructure = s;
		this.facilityID = id;
		students = new Bag();
	}

	//has capacity been reached
	public boolean isCapacityReached() {

		//if (students != null) {
		int numStudents = students.size();

		if (numStudents == getCapacity()) {
			return true;
		} else {
			return false;
		}
		//}
		//else { return false; }
	}

	//public abstract boolean isCapacityReached();
}
