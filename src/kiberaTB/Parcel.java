package kiberaTB;

import java.util.ArrayList;

import sim.util.Bag;
import sim.util.Int2D;
import kiberaTB.ResidentTB;

public class Parcel {

	/** The ID of each parcel */
	private int parcelID;
      

	public int getParcelID() {
		return parcelID;
	}

	public void setParcelID(int val) {
		this.parcelID = val;
	}

	/** Each parcel has a 10x10 resolution. Assuming 4 people per 1x1 square, each parcel has a max capacity of 400 */
	public int capacity = 400;

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int val) {
		capacity = val;
	}

	/** The location (x, y) coordinates on the grid of the parcel */
	private Int2D location;

	public Int2D getLocation() {
		return location;
	}

	public void setLocation(Int2D val) {
		location = val;
	}

	public int getXLocation() {
		return location.x;
	}

	public int getYLocation() {
		return location.y;
	}

	/** The ID of the roads within a parcel */
	private double roadID;

	public double getRoadID() {
		return roadID;
	}

	public void setRoadID(double val) {
		roadID = val;
	}

	/** The set of structures residing on the parcel */

	private ArrayList<Structure> structures;

	public void addStructure(Structure val) {
		structures.add(val);
	}

	public void removeStructure(Structure val) {
		this.structures.remove(val);
	}

	public ArrayList<Structure> getStructure() {
		return structures;
	}

	/** The set of residents currently located on a parcel */

	public Bag residents = new Bag();

	public void addResident(ResidentTB val) {
		residents.add(val);
	}

	public void removeResident(ResidentTB val) {
		residents.remove(val);
	}

	public Bag getResidents() {
		return residents;
	}
        
        
        private double bacilliLoad; // parcel or facilities will have bacilli 
        public double getBacilliLoad() {
		return bacilliLoad;
	}

	public void setBacilliLoad(double val) {
		bacilliLoad = val;
	}
        

	/** The neighborhood a parcel is located on */
	private Neighborhood neighborhood;

	public Neighborhood getNeighborhood() {
		return neighborhood;
	}

	public void setNeighborhood(Neighborhood val) {
		neighborhood = val;
	}

	public boolean ethnicityChanged = false;

	/** Ethnicity of the Parcel */
	private String ethnicity;
	private int ethnicityNumber = 0; //0 means no ethnicity has been assigned

	public String getEthnicity() {
		return ethnicity;
	}

	public int getEthnicityNumber() {
		return ethnicityNumber;
	}

	public void setEthnicity(int val) {

		ethnicityChanged = true;
		ethnicityNumber = val;

		if (val == 1) {
			ethnicity = "kikuyu";
		} else if (val == 2) {
			ethnicity = "luhya";
		} else if (val == 3) {
			ethnicity = "luo";
		} else if (val == 4) {
			ethnicity = "kalinjin";
		} else if (val == 5) {
			ethnicity = "kamba";
		} else if (val == 6) {
			ethnicity = "kisii";
		} else if (val == 7) {
			ethnicity = "meru";
		} else if (val == 8) {
			ethnicity = "mijikenda";
		} else if (val == 9) {
			ethnicity = "maasai";
		} else if (val == 10) {
			ethnicity = "turkana";
		} else if (val == 11) {
			ethnicity = "embu";
		} else if (val == 12) {
			ethnicity = "other";
		}

	}

	public Parcel(Int2D location) {
		this.setLocation(location);
		structures = new ArrayList<Structure>();
		// residents = new ArrayList<Resident>();
	}

	public boolean isParcelOccupied(KiberaTB kibera) {
		if (this.getStructure().size() > kibera.getMaxStructuresPerParcel())
			return true;
		else
			return false;
	}

	// calaculate distance
	public double distanceTo(Parcel p) {
		return Math.sqrt(Math.pow(p.getXLocation() - this.getXLocation(), 2) + Math.pow(p.getYLocation() - this.getYLocation(), 2));
	}

	public double distanceTo(int xCoord, int yCoord) {
		return Math.sqrt(Math.pow(xCoord - this.getXLocation(), 2) + Math.pow(yCoord - this.getYLocation(), 2));
	}

	public String toString() {
		return String.format("%d,%d", location.x, location.y);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Parcel))
			return false;

		if (obj == this)
			return true;

		Parcel p = (Parcel) obj;
		return (p.location.equals(this.location));
	}

}
