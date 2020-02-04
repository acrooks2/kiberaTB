package kiberaTB;

import sim.util.Bag;

public class Business {

	/*
	 * Each business has a capacity for the number of employees it can hire
	 */
	/** The employees working at the business */
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

	public int employeeCapacity;

	public int getEmployeeCapacity() {
		return employeeCapacity;
	}

	public void setEmployeeCapacity(int val) {
		employeeCapacity = val;
	}

	private Structure businessStructure;

	public Business(Structure s, double val) {
		businessStructure = s;
		employees = new Bag();
		popularityDist = val;
	}

	public Structure getStructure() {
		return businessStructure;
	}

	public void setStructure(Structure structure) {
		this.businessStructure = structure;
	}

	public double popularityDist;

	public double getPopularityDist() {
		return popularityDist;
	}

	public void setPopularityDist(double val) {
		popularityDist = val;
	}
}
