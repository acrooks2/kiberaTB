/*
Parth Chopra, Summer 2013, GMU under mentorship of Dr. Andrew Crooks.
Contact: parthchopra28@gmail.com
 */

package kiberaTB;

//------imports---------------
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomVectorField;
import sim.field.grid.Grid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Edge;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.IntBag;
import sim.util.geo.MasonGeometry;

import kiberaTB.ResidentTB.Religion;

//----------------------------

public class KiberaBuilderTB {

	static int gridWidth = 0;
	static int gridHeight = 0;
	static int intNumNeighborhoods = 15;

	static int eligibilityCounter = 0; //for ethnicity Calculation
	static Map<Integer, Boolean> alreadyCheckedMap = new HashMap<Integer, Boolean>(); //for ethnicity calculation 
	static Map<Int2D, Integer> allParcelsMap = new HashMap<Int2D, Integer>(); //faster way to retrieve index from Bag allParcels

	
        
        /***
	 * Starts up the model. Creates the entire world, its agents, and all major properties.
	 * 
	 * @param landFile
	 * @param roadFile
	 * @param facilityFile
	 * @param healthFile
	 * @param religionFile
	 * @param resterauntFile
	 * @param waterFile
	 * @param kibera
	 */
	public static void createWorld(String landFile, String roadFile, String facilityFile, String healthFile, String religionFile,
			String resterauntFile, String waterFile, KiberaTB kibera) {

		kibera.allParcels.clear();
		kibera.allResidents.clear();
	
		kibera.allStructures.clear();

		kibera.schedule.clear();

		createLand(landFile, kibera); //creates the land for the model
		createRoads(roadFile, kibera); //creates the road structure

		addStructures(kibera); //adds the households and businesses

		addFacilities(facilityFile, healthFile, religionFile, resterauntFile, waterFile, kibera); //adds schools, hospitals, and religious centers 

		addResidents(kibera); //adds the residents (giving them characteristics as well)
		determineReligion(kibera); //determines religion for residents 
		determineEthnicities(kibera); //assigns ethnicities to parcels, structures, households and residents

		addEdgesToHouseholdMembers(kibera);
		addEdgestoResidentsOfSameStructure(kibera);

		for (int k = 0; k < kibera.allEmployers.size(); k++) {
			Object o = kibera.allEmployers.get(k);

			if (o instanceof Business) {
				//System.out.println("Business " + ((Business) o).employeeCapacity);
			} else if (o instanceof School) {
				//System.out.println("School " + ((School) o).getEmployeeCapacity());
			} else if (o instanceof HealthFacility) {
				//System.out.println("Health " + ((HealthFacility) o).getEmployeeCapacity());

			} else if (o instanceof ReligiousFacility) {
				//System.out.println("Religion " + ((ReligiousFacility) o).getEmployeeCapacity());
			} else
				System.out.println("uhoh");
		}

		setResidentSchoolAndEmployment(kibera);
	}

	/***
	 * Gives each student a respective school and each employee a place to work (if available)
	 * 
	 * @param kibera
	 */
	public static void setResidentSchoolAndEmployment(KiberaTB kibera) {

		Bag allEmployers2 = new Bag();

		for (int k = 0; k < kibera.allEmployers.size(); k++) {
			allEmployers2.add(kibera.allEmployers.get(k));
		}

		for (int k = 0; k < kibera.allResidents.size(); k++) {
			ResidentTB res = (ResidentTB) kibera.allResidents.get(k);

			if (res.isStudent) {
				int schoolIndex = findSchool(k, kibera);
				res.setSchoolIndex(schoolIndex);
                                res.setSchoolClass(1+kibera.getRandom().nextInt(8));// 1to grade 8
				res.setMySchool((School) kibera.allSchools.get(schoolIndex));
				((School) kibera.allSchools.get(schoolIndex)).addStudents(res);
			} else if (res.isEmployed) {
				if (!kibera.allEmployers.isEmpty()) {
					int i = kibera.getRandom().nextInt((kibera.allEmployers.size()));
					Object o = kibera.allEmployers.get(i);

					if (o instanceof Business) {
						Business myEmployer = (Business) kibera.allEmployers.get(i);
						if (myEmployer.getEmployees().size() == myEmployer.getEmployeeCapacity()) {
							kibera.allEmployers.remove(i);
						}
						res.setMyBusinessEmployer(myEmployer);
						myEmployer.addEmployee(res);
					} else if (o instanceof School) {
						School myEmployer = (School) kibera.allEmployers.get(i);
						if (myEmployer.getEmployees().size() == myEmployer.getEmployeeCapacity()) {
							kibera.allEmployers.remove(i);
						}
						res.setMySchoolEmployer(myEmployer);
						myEmployer.addEmployee(res);
					} else if (o instanceof HealthFacility) {
						HealthFacility myEmployer = (HealthFacility) kibera.allEmployers.get(i);
						if (myEmployer.getEmployees().size() == myEmployer.getEmployeeCapacity()) {
							kibera.allEmployers.remove(i);
						}
						res.setMyHealthFacilityEmployer(myEmployer);
						myEmployer.addEmployee(res);
					} else if (o instanceof ReligiousFacility) { //else the employer is a religious facility
						ReligiousFacility myEmployer = (ReligiousFacility) kibera.allEmployers.get(i);
						if (myEmployer.getEmployees().size() == myEmployer.getEmployeeCapacity()) {
							kibera.allEmployers.remove(i);
						}
						res.setMyReligiousFacilityEmployer(myEmployer);
						myEmployer.addEmployee(res);
					}

				} else {
					int i = kibera.getRandom().nextInt((allEmployers2.size()));
					Object o = allEmployers2.get(i);

					if (o instanceof Business) {
						Business myEmployer = (Business) allEmployers2.get(i);
						res.setMyBusinessEmployer(myEmployer);
						myEmployer.addEmployee(res);
					} else if (o instanceof School) {
						School myEmployer = (School) allEmployers2.get(i);
						res.setMySchoolEmployer(myEmployer);
						myEmployer.addEmployee(res);
					} else if (o instanceof HealthFacility) {
						HealthFacility myEmployer = (HealthFacility) allEmployers2.get(i);
						res.setMyHealthFacilityEmployer(myEmployer);
						myEmployer.addEmployee(res);
					} else if (o instanceof ReligiousFacility) { //else the employer is a religious facility
						ReligiousFacility myEmployer = (ReligiousFacility) allEmployers2.get(i);
						res.setMyReligiousFacilityEmployer(myEmployer);
						myEmployer.addEmployee(res);
					}
				}
			}
		}

	}

	/***
	 * Finds the school for a student that can take the student
	 * 
	 * @param resIndex
	 * @param kibera
	 * @return the integer corresponding to the index of the place in the parcel
	 */
	public static int findSchool(int resIndex, KiberaTB kibera) {

            int schoolIndex = kibera.getRandom().nextInt(kibera.schoolLocations.size());
            double currentCap = ((School) kibera.allSchools.get(schoolIndex)).students.size();
            while (currentCap > kibera.getSchoolCapacity()){
                  schoolIndex = kibera.getRandom().nextInt(kibera.schoolLocations.size());
                 currentCap = ((School) kibera.allSchools.get(schoolIndex)).students.size();
            }
            
//            
//		Int2D resPoint = ((ResidentTB) kibera.allResidents.get(resIndex)).getPosition().getLocation();
//		double minDistance = resPoint.distance(((Parcel) kibera.schoolLocations.get(0)).getLocation());
//		int schoolIndex = 0;
//
//		for (int k = 1; k < kibera.schoolLocations.size(); k++) {
//			Int2D otherPoint = ((Parcel) kibera.schoolLocations.get(k)).getLocation();
//			double distance = resPoint.distance(otherPoint);
//			double currentCap = ((School) kibera.allSchools.get(k)).students.size();
//
//			if (distance < minDistance && currentCap < kibera.getSchoolCapacity()) {
//				minDistance = distance;
//				schoolIndex = k;
//			}
//		}

		return schoolIndex;
	}

	/***
	 * Adds health facilities, religious facilities and schools to the model
	 * 
	 * @param facilityFile
	 * @param healthFile
	 * @param religionFile
	 * @param resterauntFile
	 * @param waterFile
	 * @param kibera
	 */
	public static void addFacilities(String facilityFile, String healthFile, String religionFile, String resterauntFile, String waterFile,
			KiberaTB kibera) {
		try {
			// buffer reader - read ascii file
			BufferedReader facilities = new BufferedReader(new FileReader(facilityFile));
			String line;

			BufferedReader healthFacilities = new BufferedReader(new FileReader(healthFile));
			String healthLine;

			BufferedReader religiousFacilities = new BufferedReader(new FileReader(religionFile));
			String religiousLine;

			BufferedReader resteraunt = new BufferedReader(new FileReader(resterauntFile));
			String resterauntLine;

			BufferedReader water = new BufferedReader(new FileReader(waterFile));
			String waterLine;

			// first read the dimensions
			line = facilities.readLine(); // read line for width
			String[] tokens = line.split("\\s+");
			int width = Integer.parseInt(tokens[1]);
			gridWidth = width;

			line = facilities.readLine();
			tokens = line.split("\\s+");
			int height = Integer.parseInt(tokens[1]);
			gridHeight = height;

			int numSchools = 0;
			int numHealth = 0;
			int numReligion = 0;
			int numResteraunts = 0;
			int numWater = 0;

			//------------------------------------------------Adding restaurants---------------------------------------

			for (int i = 0; i < 6; ++i) {
				resterauntLine = resteraunt.readLine();
			}

			Business resterauntFacility = null;

			for (int curr_row = 0; curr_row < height; ++curr_row) {
				resterauntLine = resteraunt.readLine();
				tokens = resterauntLine.split("\\s+");

				for (int curr_col = 0; curr_col < width; ++curr_col) {
					if (tokens[curr_col].equals("") == false) {
						int resterauntFacilityType = Integer.parseInt(tokens[curr_col]);

						Parcel parcel = null;

						if (resterauntFacilityType > 0) {
							Int2D parcelLocation = new Int2D(curr_col, curr_row);

							parcel = (Parcel) kibera.landGrid.get(curr_col, curr_row);

							int index = getIndex(kibera, parcel);
							if (index != -1) {
								parcel = (Parcel) kibera.allParcels.get(index);
							}

							int numStructuresOnParcel = parcel.getStructure().size();

							if (numStructuresOnParcel == 0) {
								Structure s = new Structure(parcel);
								kibera.allStructures.add(s);
								resterauntFacility = new Business(s, resterauntFacilityType);
								s.setParcel(parcel);
								parcel.addStructure(s);
								numResteraunts++;
							} else {
								int rn = 1 + kibera.getRandom().nextInt(numStructuresOnParcel);

								ArrayList<Structure> structures = parcel.getStructure();

								int i = 0;
								for (Structure s : structures) {
									i++;

									if (i == rn) {
										resterauntFacility = new Business(s, resterauntFacilityType);
										resterauntFacility.setStructure(s);

										numResteraunts++;

									}
								}
							}
							int employeeCapacity = 10 + kibera.getRandom().nextInt(kibera.params.globalParam.getformalBusinessCapacity() - 10);

							resterauntFacility.setEmployeeCapacity(employeeCapacity);

							kibera.allRestaurants.add(resterauntFacility);
							kibera.restaurantLocations.add(parcel);
							kibera.allEmployers.add(resterauntFacility);
							kibera.restaurantGrid.setObjectLocation(resterauntFacility, parcelLocation);
                                                        kibera.allFacilities.add(parcel);
						}

					}
				}
			}
			//---------------------------------------------------------------------------------------------------------------
			//-----------------------------------Adding water sources---------------------------------------------------------
			for (int i = 0; i < 6; ++i) {
				waterLine = water.readLine();
			}

			Facility waterFacility = null;

			for (int curr_row = 0; curr_row < height; ++curr_row) {
				waterLine = water.readLine();
				tokens = waterLine.split("\\s+");

				for (int curr_col = 0; curr_col < width; ++curr_col) {
					if (tokens[curr_col].equals("") == false) {
						int waterFacilityType = Integer.parseInt(tokens[curr_col]);

						Parcel parcel = null;

						if (waterFacilityType > 0) {
							Int2D parcelLocation = new Int2D(curr_col, curr_row);

							parcel = (Parcel) kibera.landGrid.get(curr_col, curr_row);

							int index = getIndex(kibera, parcel);
							if (index != -1) {
								parcel = (Parcel) kibera.allParcels.get(index);
							}

							int numStructuresOnParcel = parcel.getStructure().size();

							if (numStructuresOnParcel == 0) {
								Structure s = new Structure(parcel);
								kibera.allStructures.add(s);
								waterFacility = new Facility(s, waterFacilityType);
								s.setParcel(parcel);
								parcel.addStructure(s);

							} else {
								int rn = 1 + kibera.getRandom().nextInt(numStructuresOnParcel);

								ArrayList<Structure> structures = parcel.getStructure();

								int i = 0;
								for (Structure s : structures) {
									i++;

									if (i == rn) {
										waterFacility = new Facility(s, waterFacilityType);
										waterFacility.setStructure(s);
										waterFacility.setFacilityID(waterFacilityType);

										numWater++;
									}
								}
							}
							kibera.allWaterSources.add(waterFacility);
							kibera.waterLocations.add(parcel);
							kibera.waterSourcesGrid.setObjectLocation(waterFacility, parcelLocation);
                                                        kibera.allFacilities.add(parcel);
						}
					}
				}
			}

			//----------------------------------------------------------------------------------------------------------------
			//------------------------------------Adding Health Facilities-------------------------------------------------------------

			for (int i = 0; i < 6; ++i) {
				healthLine = healthFacilities.readLine();
			}

			HealthFacility healthFacility = null;

			for (int curr_row = 0; curr_row < height; ++curr_row) {
				healthLine = healthFacilities.readLine();

				tokens = healthLine.split("\\s+");

				for (int curr_col = 0; curr_col < width; ++curr_col) {
					int healthFacilityType = Integer.parseInt(tokens[curr_col]);

					Parcel parcel = null;

					if (healthFacilityType < 100) {
						Int2D parcelLocation = new Int2D(curr_col, curr_row);

						parcel = (Parcel) kibera.landGrid.get(curr_col, curr_row);

						int index = getIndex(kibera, parcel);
						if (index != -1) {
							parcel = (Parcel) kibera.allParcels.get(index);
						}

						int numStructuresOnParcel = parcel.getStructure().size();

						if (numStructuresOnParcel == 0) {
							Structure s = new Structure(parcel);
							kibera.allStructures.add(s);
							healthFacility = new HealthFacility(s, healthFacilityType);
							s.setParcel(parcel);
							parcel.addStructure(s);

							numHealth += 1;
						} else {
							int rn = 1 + kibera.getRandom().nextInt(numStructuresOnParcel);

							ArrayList<Structure> structures = parcel.getStructure();

							int i = 0;
							for (Structure s : structures) {
								i++;

								if (i == rn) {
									healthFacility = new HealthFacility(s, healthFacilityType);
									s.addHealthFacility(healthFacility);
									healthFacility.setStructure(s);
									healthFacility.setFacilityID(healthFacilityType);

									numHealth += 1;

								}
							}
						}
						int employeeCapacity = 10 + kibera.getRandom().nextInt(kibera.params.globalParam.getformalBusinessCapacity() - 10);

						healthFacility.setEmployeeCapacity(employeeCapacity);

						kibera.allHealthFacilities.add(healthFacility);
						kibera.healthLocations.add(parcel);
						kibera.allEmployers.add(healthFacility);
						kibera.healthFacilityGrid.setObjectLocation(healthFacility, parcelLocation);
                                                kibera.allFacilities.add(parcel);
					}
				}
			}
			//----------------------------------------------------------------------------------------------------------------
			//------------------------------------Adding religious facilities (churches and mosques)-------------------------------------------------------------

			for (int i = 0; i < 6; ++i) {
				religiousLine = religiousFacilities.readLine();
			}

			ReligiousFacility religiousFacility = null;

			for (int curr_row = 0; curr_row < height; ++curr_row) {
				religiousLine = religiousFacilities.readLine();

				tokens = religiousLine.split("\\s+");

				for (int curr_col = 0; curr_col < width; ++curr_col) {
					int religiousFacilityType = Integer.parseInt(tokens[curr_col]);

					Parcel parcel = null;

					if (religiousFacilityType < 100) {

						Int2D parcelLocation = new Int2D(curr_col, curr_row);

						parcel = (Parcel) kibera.landGrid.get(curr_col, curr_row);

						int index = getIndex(kibera, parcel);
						if (index != -1) {
							parcel = (Parcel) kibera.allParcels.get(index);
						}
						int numStructuresOnParcel = parcel.getStructure().size();

						if (numStructuresOnParcel == 0) {
							Structure s = new Structure(parcel);
							kibera.allStructures.add(s);
							religiousFacility = new ReligiousFacility(s, religiousFacilityType);
							s.setParcel(parcel);
							parcel.addStructure(s);

							numReligion += 1;
						} else {
							int rn = 1 + kibera.getRandom().nextInt(numStructuresOnParcel);

							ArrayList<Structure> structures = parcel.getStructure();

							int i = 0;
							for (Structure s : structures) {
								i++;

								if (i == rn) {
									religiousFacility = new ReligiousFacility(s, religiousFacilityType);
									s.addReligiousFacility(religiousFacility);
									religiousFacility.setStructure(s);
									religiousFacility.setFacilityType(religiousFacilityType);

									numReligion += 1;

								}
							}
						}
						int employeeCapacity = 10 + kibera.getRandom().nextInt(kibera.params.globalParam.getformalBusinessCapacity() - 10);
						religiousFacility.setEmployeeCapacity(employeeCapacity);

						kibera.allReligiousFacilities.add(religiousFacility);
						kibera.religiousLocations.add(parcel);
						kibera.allEmployers.add(religiousFacility);
						kibera.religiousFacilityGrid.setObjectLocation(religiousFacility, parcelLocation);
                                                kibera.allFacilities.add(parcel);
					}
				}
			}

			//----------------------------------------------------------------------------------------------------------------
			//------------------------------------Adding Schools-------------------------------------------------------------

			//skip the next four lines as they contain irrelevant metadata
			for (int j = 0; j < 4; ++j) {
				line = facilities.readLine();
			}

			School school = null;

			for (int curr_row = 0; curr_row < height; ++curr_row) {
				line = facilities.readLine();

				tokens = line.split("\\s+");

				for (int curr_col = 0; curr_col < width; ++curr_col) {
					int facilityID = Integer.parseInt(tokens[curr_col]);

					Parcel parcel = null;

					if (facilityID < 100) {
						Int2D parcelLocation = new Int2D(curr_col, curr_row);

						parcel = (Parcel) kibera.landGrid.get(curr_col, curr_row);
						int numStructuresOnParcel = parcel.getStructure().size();

						if (numStructuresOnParcel == 0) {
							Structure s = new Structure(parcel);
							kibera.allStructures.add(s);
							school = new School(s, facilityID);
							s.setParcel(parcel);
							parcel.addStructure(s);

							numSchools += 1;
						}

						else {
							int rn = 1 + kibera.getRandom().nextInt(numStructuresOnParcel);

							ArrayList<Structure> structures = parcel.getStructure();

							int j = 0;
							for (Structure s : structures) {
								j++;

								if (j == rn) {
									school = new School(s, facilityID);
									s.addSchool(school);
									school.setStructure(s);
									school.setFacilityID(facilityID);

									numSchools += 1;
								}
							}
						}

						if (facilityID == 1) {

						}
						school.setCapacity(kibera.getSchoolCapacity());
						((Parcel) school.getStructure().getParcel()).setCapacity(kibera.getSchoolCapacity());
						kibera.schoolGrid.setObjectLocation(school, parcelLocation);
						kibera.schoolLocations.add(parcel);

						//School employeee capacity is a minimum 8, max 28 				
						school.setEmployeeCapacity(8 + kibera.getRandom().nextInt(22));

						kibera.allEmployers.add(school);
						kibera.allSchools.add(school);
                                                kibera.allFacilities.add(parcel);
					}
				}
			}

			/*
			 * System.out.println("Number Schools = " + numSchools); System.out.println("Number Health = " + numHealth);
			 * System.out.println("Number Religion = " + numReligion); System.out.println("Number Resteraunts = " + numResteraunts);
			 * System.out.println("Number Water = " + numWater);
			 */

			facilities.close();
			healthFacilities.close();
			religiousFacilities.close();
			resteraunt.close();
			water.close();

		} catch (IOException ex) {
			Logger.getLogger(KiberaBuilderTB.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/***
	 * Adds structures to remaining parcels that require it, and populates said structure with households and businesses
	 * 
	 * @param kibera
	 */
	public static void addStructures(KiberaTB kibera) {
		// add structures while there is still place to put them
		Parcel residingParcel = null;

		int numBusinesses = 0;
		int numStructures = 0;
		int numHomes = 0;

		for (int i = 0; i < kibera.allParcels.numObjs; i++) {

			Parcel p = (Parcel) kibera.allParcels.objs[i];
			int id = p.getParcelID();
			if (p.isParcelOccupied(kibera) == false && id > 0) {

				residingParcel = p;
				Structure s = new Structure(residingParcel);
				numStructures++;

				s.setParcel(residingParcel);
				residingParcel.addStructure(s);
				// determine capacity of each structure
				double shouldAddHouseholds = kibera.getRandom().nextDouble();

				int numberOfHouseholds = kibera.getRandom().nextInt(1 + kibera.params.globalParam.gethhCapacity());

				double shouldAddBusinesses = kibera.getRandom().nextDouble();

				int numberOfBusinesses = kibera.getRandom().nextInt(1 + kibera.params.globalParam.getbusinessCapacity());

				int hhCapacity = 0;
				int businessCapacity = 0;

				if (shouldAddHouseholds < 0.86) // add household(s) to structure
				{
					hhCapacity = numberOfHouseholds;
					numHomes += hhCapacity;
				}

				if (shouldAddBusinesses < 0.13) // add business(es) to structure
				{
					businessCapacity = numberOfBusinesses;
					numBusinesses += businessCapacity;
				}

				// placeholder for capacity of each structure
				s.setStructureCapacity(hhCapacity, businessCapacity);
				addBusiness(kibera, s, s.getParcel());
				addHousehold(kibera, s, s.getParcel());

				kibera.allStructures.add(s);
			}

		}
		/*
		 * System.out.println("Number Homes = " + numHomes); System.out.println("Number Businesses = " + numBusinesses);
		 * System.out.println("Number Structures = " + numStructures);
		 */
	}

	/***
	 * Adds businesses to the model
	 * 
	 * @param kibera
	 * @param structure
	 * @param parcel
	 */
	public static void addBusiness(KiberaTB kibera, Structure structure, Parcel parcel) {

		for (int k = 0; k < structure.getBusinessCapacity(); k++) {

			double rand = kibera.getRandom().nextDouble();
			Business b = new Business(structure, rand);

			//Determine capacity of employees of business. For informal, it is between 5 and 10 UN (Challenge of slums)
			b.setEmployeeCapacity((5 + kibera.getRandom().nextInt(6)));

			kibera.allBusinesses.add(b);
			kibera.allEmployers.add(b);
			kibera.businessLocations.add(parcel);

			if (rand <= .682) {
				kibera.businessTier1Locations.add(parcel);
			} else if (rand <= .954 && rand > .682) {
				kibera.businessTier2Locations.add(parcel);
			} else {
				kibera.businessTier3Locations.add(parcel);
			}

			structure.addBusinesses(b);
			kibera.businessGrid.setObjectLocation(structure, parcel.getLocation());

		}
                
	}

	/***
	 * Adds households to the model
	 * 
	 * @param kibera
	 * @param structure
	 * @param parcel
	 */
	public static void addHousehold(KiberaTB kibera, Structure structure, Parcel parcel) {

		for (int k = 0; k < structure.getHouseholdCapacity(); k++) {
			Household h = new Household(kibera, structure);
			kibera.allHouseholds.add(h);
			structure.addHousehold(h);

			kibera.householdGrid.setObjectLocation(structure, parcel.getLocation());
		}

	}

	/***
	 * This method assigns ethnicities to the parcels (which then subsequently adds it to its structures, households, and residents. The
	 * algorithm works by assuming that "ethnicity" itself is an agent, and then places this ethnicity at the most optimal place. If it has
	 * never been placed before, it places it getRandom()ly. If the same ethnicity has been placed before, the algorithm finds the best place
	 * around that ethnicity to place this new ethnicityAgent (using Moore's Neighbors).
	 * 
	 * @param kibera
	 */
	public static void determineEthnicities(KiberaTB kibera) {

		//Create array of all the ethnicities equal to the number of parcels
		Bag allEthnicities = new Bag(kibera.allParcels.size());
		int ethnicityCounter = 1;
		int mapIndex = 0;

		for (int k = 0; k < kibera.ethnicDistribution.length; k++) {
			double distribution = kibera.ethnicDistribution[k];
			int numberEthnicities = (int) (Math.round((distribution * kibera.allParcels.size())));

			for (int z = 0; z < numberEthnicities; z++) {
				allEthnicities.add(ethnicityCounter);
			}
			ethnicityCounter++;
		}
		allEthnicities.add(ethnicityCounter - 1);
		//--------------------------------------------------------------------------------------

		Bag parcelsThatHaveBeenUsed = new Bag();

		//booleans to see if first turn has been done 
		boolean[] hasGone = new boolean[13];
		while (allEthnicities.size() > 0) {

			int randomIndex = (Integer) kibera.getRandom().nextInt(allEthnicities.size());
			int ethnicityAgent = (Integer) allEthnicities.get(randomIndex);

			//this is the first time this ethnicitiy is being placed
			if (hasGone[ethnicityAgent] == false) {

				int randomParcelIndex = (Integer) kibera.getRandom().nextInt(kibera.allParcels.size());

				while (((Parcel) kibera.allParcels.get(randomParcelIndex)).ethnicityChanged == true) {
					randomParcelIndex = (Integer) kibera.getRandom().nextInt(kibera.allParcels.size());
				}

				((Parcel) kibera.allParcels.get(randomParcelIndex)).setEthnicity(ethnicityAgent); //change parcel's ethnicity

				parcelsThatHaveBeenUsed.add((Parcel) kibera.allParcels.get(randomParcelIndex));
				alreadyCheckedMap.put(mapIndex, false);
				mapIndex++;

				allEthnicities.remove(randomIndex);

				hasGone[ethnicityAgent] = true;
			}
			//------------------------Putting a non-firsttime ethncity on the board----------------------------------------------------	
			else {
				Bag neighborParcels = findAvailableNeighbors(kibera, parcelsThatHaveBeenUsed, ethnicityAgent);

				int randomNeighborIndex = (Integer) kibera.getRandom().nextInt(neighborParcels.size());
				int allParcelsIndex = (Integer) neighborParcels.get(randomNeighborIndex);

				((Parcel) kibera.allParcels.get(allParcelsIndex)).setEthnicity(ethnicityAgent);

				parcelsThatHaveBeenUsed.add(((Parcel) kibera.allParcels.get(allParcelsIndex)));

				alreadyCheckedMap.put(mapIndex, false);
				mapIndex++;

				allEthnicities.remove(randomIndex);

			}
		}

		addEthnicitiesToHouseholdsAndResidents(kibera);

	}

	/***
	 * Gets the index of a Parcel in the bag kibera.allParcels (which has been converted to a Map to increase speed.
	 * 
	 * @param kibera
	 * @param p
	 * @return the integer of the index of the parcel in allParcels
	 */
	public static int getIndex(KiberaTB kibera, Parcel p) {
		/*
		 * int index = -1; if (kibera.allParcels.contains(p) == true) { for (int k = 0; k < kibera.allParcels.size(); k++) { if (((Parcel)
		 * kibera.allParcels.get(k)).equals(p) == true) { index = k; } } } return index;
		 */
		try {
			return allParcelsMap.get(p.getLocation());
		} catch (NullPointerException e) {
			return -1;
		}
	}

	/***
	 * Used in the determineEthnicities algorithm to find optimal position for the ethnicityAgent
	 * 
	 * @param kibera
	 * @param parcels
	 * @param ethnicityAgent
	 * @return a Bag of all the indices of the possible neighbors of a resident.
	 */
	public static Bag findAvailableNeighbors(KiberaTB kibera, Bag parcels, int ethnicityAgent) {

		IntBag neighborsX = new IntBag();
		IntBag neighborsY = new IntBag();
		Bag eligibleNeighbors = new Bag();
		Bag indicies = new Bag();
		eligibilityCounter = 0;

		boolean foundEligibleCenter = false;

		while (foundEligibleCenter == false) {
			try {
				while (alreadyCheckedMap.get(eligibilityCounter) == true) {
					eligibilityCounter++;
				}
			} catch (NullPointerException e) { //I believe this occurs if there are no more spaces to place the ethnicity. Potential solution, place in random parcel

				int randomParcelIndex = 0;
				while (((Parcel) kibera.allParcels.get(randomParcelIndex)).ethnicityChanged == true) {
					randomParcelIndex = (Integer) kibera.getRandom().nextInt(kibera.allParcels.size());
				}
				indicies.add(randomParcelIndex);
				break;
			}
			Parcel possible = (Parcel) parcels.get(eligibilityCounter);
			int possibleValue = possible.getEthnicityNumber();

			if (possibleValue == ethnicityAgent) {
				//check to see if available moore neighbors
             
				kibera.landGrid.getMooreNeighbors(possible.getLocation().getX(), possible.getLocation().getY(), 1, Grid2D.BOUNDED, false,
						null, neighborsX, neighborsY);
				for (int k = 0; k < neighborsX.size(); k++) {

					Parcel neighborTemp = (Parcel) kibera.landGrid.get(neighborsX.get(k), neighborsY.get(k));
					int index = getIndex(kibera, neighborTemp);
					if (index != -1) {
						neighborTemp = (Parcel) kibera.allParcels.get(index);
						if (neighborTemp.ethnicityChanged == false) { //neighbor is available 
							eligibleNeighbors.add(neighborTemp);
							indicies.add(index);
							foundEligibleCenter = true;
						}
					}
				}
				if (foundEligibleCenter == false)
					alreadyCheckedMap.put(eligibilityCounter, true);
			} else
				eligibilityCounter++;

		}
		return indicies;
	}

	/***
	 * Calculates Entropy Index, or the level of segregation, for this model. The max segregation occurs at a value of ln(maxEthnicities) or
	 * for this model ln(12). Source for formula: http://www.census.gov/housing/patterns/about/multigroup_entropy.pdf Calculated entropy =
	 * ~2.22
	 * 
	 * @param kibera
	 * @return
	 */
	public static double calculateEntropyIndex(KiberaTB kibera) {

		double T = kibera.params.globalParam.getNumberOfResidents();
		double E = 0;
		//calculate metroDiversity (E) using formula E = SUM(P ln[1/P]) where P is racial proportion
		int[] raceNumbers = new int[13];

		for (int k = 0; k < kibera.params.globalParam.getNumberOfResidents(); k++) {
			ResidentTB res = (ResidentTB) kibera.allResidents.get(k);
			int ethnicityNumb = res.getPosition().getEthnicityNumber();
			raceNumbers[ethnicityNumb] = raceNumbers[ethnicityNumb] + 1;

		}

		for (int k = 1; k < raceNumbers.length; k++) {
			double prop = (raceNumbers[k] / T);
			double temp = prop * Math.log((1 / prop));
			E = E + temp;
		}

		return E;
	}

	/***
	 * Adds the ethnicities from a parcel to the household and its residents
	 * 
	 * @param kibera
	 */
	public static void addEthnicitiesToHouseholdsAndResidents(KiberaTB kibera) {

		for (int k = 0; k < kibera.allParcels.size(); k++) {
			Parcel parcel = (Parcel) kibera.allParcels.get(k);
			String ethnicity = parcel.getEthnicity();

			Structure s = parcel.getStructure().get(0);
			s.setEthnicity(ethnicity); //changing it so that once a structure's ethnicity is set, its households and residents automaticaly change too

		}
	}

	/***
	 * Creates residents based on proper characteristics and data and adds it to the model
	 * 
	 * @param kibera
	 */
	public static void addResidents(KiberaTB kibera) {

		Bag households = new Bag(kibera.allHouseholds);

		int householdIndex = 0;
		int numAgents = kibera.params.globalParam.getNumberOfResidents();
		int agentCounter = 0;
		boolean isHeadofHousehold = false;

		int v = kibera.allHouseholds.size();

		Bag numberBag = new Bag();

		for (int k = 0; k < v; k++) {
			numberBag.add(k);
		}

		while (agentCounter < numAgents) {

			if (numberBag.size() == 0) {
				for (int k = 0; k < v; k++) {
					numberBag.add(k);
				}
			}
			double mean = 3.55;
			double stdev = 1.61;

			int householdSize = (int) Stats.normalToLognormal(Stats.calcLognormalMu(mean, stdev), Stats.calcLognormalSigma(mean, stdev),kibera.getRandom().nextGaussian());
                       
			householdIndex = (Integer) numberBag.get(kibera.getRandom().nextInt(numberBag.size()));
			Household home = (Household) households.get(householdIndex);

			numberBag.remove((Object) householdIndex);

			//---------------------Determine Characteristics of each Resident---------------------------------
			for (int k = 0; k < householdSize; k++) {
				if (k == 0)
					isHeadofHousehold = true;
				else
					isHeadofHousehold = false;

				int residentAge = determineResidentAge(kibera, isHeadofHousehold);
				int gender = determineResidentGender(kibera);
				boolean isSchoolEligible = determineStudentStatus(residentAge, kibera);
				boolean employmentStatus = determineEmploymentStatus(residentAge, gender, isSchoolEligible, kibera); //is either employed or not

				// determine religion
//				double rnReligion = kibera.random.nextDouble();
//				if (rnReligion < .91) { //Religious Stats document
//
//				} else {
//				

				//determine HIV status 
				boolean HIVStatus;
                                boolean onHIVARTTreatment =false;
				if (kibera.getRandom().nextDouble() < kibera.params.globalParam.getHIVPrevalenceRate())
					HIVStatus = true;
				else
					HIVStatus = false;

				                            
                                int healthStatus = 1; // start with all suscipitible
                                
                                //latent
                                if (kibera.getRandom().nextDouble() <  kibera.params.globalParam.getTBLatentInfectionPrevalanceRate()){ // how many are latent
                                 healthStatus =3;
                                }
                                  
                                // infected - overwrite
                                if (kibera.getRandom().nextDouble() <= kibera.params.globalParam.getTBDiseasePrevalanceRate()) { // how many are already infected
					healthStatus =4;
                                 }
                                if (kibera.getRandom().nextDouble() <= kibera.params.HBVTreatmentHIVARTCoverage) { // how many are already infected
					onHIVARTTreatment = true;
                                 }
                               
				//----------------------------------------------------------------------------

                           
				ResidentTB res = new ResidentTB(residentAge, gender, isHeadofHousehold, home, isSchoolEligible, employmentStatus,
						HIVStatus, healthStatus, onHIVARTTreatment, kibera);
                                
                                
			
				home.addHouseholdMembers(res);
				kibera.socialNetwork.addNode(res);
				kibera.allResidents.add(res);

				Parcel temp = home.getStructure().getParcel();

				int index = getIndex(kibera, temp);
				if (index != -1) {
					((Parcel) kibera.allParcels.get(index)).addResident(res);
				}

				double jitterX = 0.3*kibera.getRandom().nextDouble();
				double jitterY = 0.3*kibera.getRandom().nextDouble();

				kibera.world.setObjectLocation(res, new Double2D(home.getStructure().getParcel().getXLocation() + jitterX, home
						.getStructure().getParcel().getYLocation()
						+ jitterY));

				kibera.schedule.scheduleRepeating(res);
			}

			agentCounter += householdSize;
		}

	}

	/***
	 * Determines and sets the religion for all the residents in the model
	 * 
	 * @param kibera
	 */
	private static void determineReligion(KiberaTB kibera) {

		for (int k = 0; k < kibera.allHouseholds.size(); k++) {
			Household house = (Household) kibera.allHouseholds.get(k);

			double rnReligion = kibera.getRandom().nextDouble();
			ResidentTB.Religion religion;

			if (rnReligion < .91) { //look at religion document
				religion = Religion.Christian;
			} else {
				religion = Religion.Muslim;
			}

			house.setReligion(religion);

			for (int z = 0; z < house.getHouseholdMembers().size(); z++) {
				((ResidentTB) (house.getHouseholdMembers().get(z))).setReligion(religion);
			}

		}
	}

	/***
	 * Adds to the kibera social network and edges between household members
	 * 
	 * @param kibera
	 */
	private static void addEdgesToHouseholdMembers(KiberaTB kibera) {
		// loop through each household, link residents in the same household
		// together
		for (int k = 0; k < kibera.allHouseholds.size(); k++) {

			Household hh = (Household) kibera.allHouseholds.get(k);

			Bag family = new Bag();
			family = hh.getHouseholdMembers();
			int familySize = family.size();

			double edgeValue = 1;

			int i = 0;
			int j = 0;

			for (i = 0; i < familySize; i++) {
				for (j = 0; j < familySize; j++) {
					if (i != j) {
						Edge e = new Edge(family.objs[i], family.objs[j], edgeValue);
						kibera.socialNetwork.addEdge(e);

					}
				}
			}
		}
	}

	/***
	 * Adds to the kibera social network and creates edges between residents of the same structure
	 * 
	 * @param kibera
	 */
	private static void addEdgestoResidentsOfSameStructure(KiberaTB kibera) {

		for (int k = 0; k < kibera.allHouseholds.size(); k++) {

			Household household = (Household) kibera.allHouseholds.get(k);
			Structure s = household.getStructure();

			Bag hhInStructure = new Bag();
			hhInStructure = s.getHouseholds();

			Bag residentsInStructure = new Bag();

			int numhh = hhInStructure.size();

			double edgeValue = 1;

			for (int i = 0; i < numhh; i++) {
				Household hh = (Household) hhInStructure.get(i);
				Bag residentsInhh = new Bag();
				residentsInhh = hh.getHouseholdMembers();
				residentsInStructure.addAll(residentsInhh);
			}

			int numResidents = residentsInStructure.size();

			for (int i = 0; i < numResidents; i++) {
				for (int j = 0; j < numResidents; j++) {
					Edge e = new Edge(residentsInStructure.objs[i], residentsInStructure.objs[j], edgeValue);
					// if an edge does not yet exist between the two residents,
					// create one
					if (e.getWeight() == 0) {
						kibera.socialNetwork.addEdge(e);
					}
				}
			}
		}
	}

	/***
	 * Determines the age of a resident based on data. Used in addResidents method
	 * 
	 * @param kibera
	 * @param isHeadOfHousehold
	 * @return the age of the agent
	 */
	private static int determineResidentAge(KiberaTB kibera, boolean isHeadOfHousehold) {
		double rn = kibera.getRandom().nextDouble();
		int age = 0;

		if (isHeadOfHousehold) {
			age = 18 + kibera.getRandom().nextInt(42); // 18-59      	
		}

		else {
			if (rn <= kibera.params.globalParam.getageAdult()) {
				age = 18 + kibera.getRandom().nextInt(62);
			} // adult (over 18)
			else if (rn <= (kibera.params.globalParam.getageAdult() + kibera.params.globalParam.getageChildrenUnder6())) {
				age = kibera.getRandom().nextInt(6);
			} // child under 6
			else {
				age = 6 + kibera.getRandom().nextInt(12);
			} // child (6-17)
		}

		return age;
	}

	/***
	 * Determines the gender of a resident based on data. Used in addResidents method
	 * 
	 * @param kibera
	 * @return the gender of the agent
	 */
	private static int determineResidentGender(KiberaTB kibera) {
		if (kibera.getRandom().nextDouble() < kibera.getMaleDistribution())
			return 0; //male
		else
			return 1; //female
	}

	/***
	 * Determines whether an agent is employed or not. Used in the addResidents method.
	 * 
	 * @param age
	 * @param gender
	 * @param isSchoolEligible
	 * @param kibera
	 * @return agent's employment status
	 */
	private static boolean determineEmploymentStatus(int age, int gender, boolean isSchoolEligible, KiberaTB kibera) {
		double random = kibera.getRandom().nextDouble();

		if (isSchoolEligible) { //if student then not employed
			return false;
		} else {
			if (gender == 1) { //if female, then 56.3% of them are employed 
				if (random < .563)
					return true;
				else
					return false;
			} else { //if male, then 72.9% of them are employed
				if (random < .729)
					return true;
				else
					return false;
			}
		}
	}

	/***
	 * Determines whether the resident is in school or not. based on age and used in addResidents method
	 * 
	 * @param age
	 * @param kibera
	 * @return agent's student status
	 */
	private static boolean determineStudentStatus(int age, KiberaTB kibera) {

		// approximately 23% of youth (ages 3-18) go to school (based on the
		// average enrollment of schools
		// in kibera and the total population of children

		// remove this, students will search for school that hasn't met capacity
		// in the action sequence object

		if (age >= 3 && age <= 18) {
			return true;
		} else {
			return false;
		}
	}

	/***
	 * Creates the physical Kibera land in the model
	 * 
	 * @param landFile
	 * @param kibera
	 */
	private static void createLand(String landFile, KiberaTB kibera) {
		try {
			// buffer reader - read ascii file
			BufferedReader land = new BufferedReader(new FileReader(landFile));
			String line;

			// first read the dimensions
			line = land.readLine(); // read line for width
			String[] tokens = line.split("\\s+");
			int width = Integer.parseInt(tokens[1]);
			gridWidth = width;

			line = land.readLine();
			tokens = line.split("\\s+");
			int height = Integer.parseInt(tokens[1]);
			gridHeight = height;

			kibera.setWidth(width);
			kibera.setHeight(height);

			createGrids(kibera, width, height);

			// skip the next four lines as they contain irrelevant metadata
			for (int i = 0; i < 4; ++i) {
				line = land.readLine();
			}

			for (int curr_row = 0; curr_row < height; ++curr_row) {
				line = land.readLine();

				tokens = line.split("\\s+");

				// Column 0 is blank in file, so have to adjust for blank column
				for (int curr_col = 0; curr_col < width; ++curr_col) {
					int neighborhoodID = Integer.parseInt(tokens[curr_col]);
					Parcel parcel = null;

					if (neighborhoodID < 100) {
						Int2D parcelLocation = new Int2D(curr_col, curr_row);
						parcel = new Parcel(parcelLocation);
						kibera.allParcels.add(parcel);
						parcel.setParcelID(neighborhoodID);
						kibera.landGrid.set(curr_col, curr_row, parcel);

					}
					Int2D parcelLocation = new Int2D(curr_col, curr_row);
					parcel = new Parcel(parcelLocation);
					parcel.setParcelID(0);
					kibera.landGrid.set(curr_col, curr_row, parcel);
				}
			}

			land.close();

			//create map for allParcels 
			for (int k = 0; k < kibera.allParcels.size(); k++) {
				allParcelsMap.put(((Parcel) kibera.allParcels.get(k)).getLocation(), k);
			}

		} catch (IOException ex) {
			Logger.getLogger(KiberaBuilderTB.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/***
	 * Creates the road structure and adds the network to the model
	 * 
	 * @param roadFile
	 * @param kibera
	 */
	private static void createRoads(String roadFile, KiberaTB kibera) {
		BufferedReader roads;
		try {
			// now read road grid
			roads = new BufferedReader(new FileReader(roadFile));
			String line;

			// first read the dimensions
			line = roads.readLine(); // read line for width
			String[] tokens = line.split("\\s+");
			int width = Integer.parseInt(tokens[1]);
			gridWidth = width;

			line = roads.readLine();
			tokens = line.split("\\s+");
			int height = Integer.parseInt(tokens[1]);
			gridHeight = height;

			// skip the irrelevant metadata
			for (int i = 0; i < 4; i++) {
				line = roads.readLine();
			}

			for (int curr_row = 0; curr_row < height; ++curr_row) {
				line = roads.readLine();

				tokens = line.split("\\s+");

				for (int curr_col = 0; curr_col < width; ++curr_col) {
					int roadID = Integer.parseInt(tokens[curr_col]);

					if (roadID >= 0) {
						Parcel parcel = (Parcel) kibera.landGrid.get(curr_col, curr_row);
						parcel.setRoadID(roadID);
						kibera.roadGrid.set(curr_col, curr_row, roadID);
						// kibera.landGrid.set(curr_col, curr_row, roadID);
					}
				}
			}

			// Import road shapefile
			Bag roadImporter = new Bag();
			roadImporter.add("Type");

			File file = new File("src/files/data/Road_Export.shp");
			@SuppressWarnings("deprecation")
			URL roadShapeUL = file.toURL();

			ShapeFileImporter.read(roadShapeUL, kibera.roadLinks, roadImporter);

			extractFromRoadLinks(kibera.roadLinks, kibera); // construct a
			// newtork of roads

			kibera.closestNodes = setupNearestNodes(kibera);

			roads.close();
		} catch (IOException ex) {
			Logger.getLogger(KiberaBuilderTB.class.getName()).log(Level.SEVERE, null, ex);
		}
		//		finally {
		//			if (roads != null)
		//				roads.close();
		//		}
	}

	/***
	 * Initializes the different grids of the model
	 * 
	 * @param kibera
	 * @param width
	 * @param height
	 */
	public static void createGrids(KiberaTB kibera, int width, int height) {

		kibera.landGrid = new ObjectGrid2D(width, height);
		kibera.roadGrid = new IntGrid2D(width, height);
		kibera.nodes = new SparseGrid2D(width, height);
		kibera.closestNodes = new ObjectGrid2D(width, height);
		kibera.roadLinks = new GeomVectorField(width, height);
		kibera.healthFacilityGrid = new SparseGrid2D(width, height);
		kibera.religiousFacilityGrid = new SparseGrid2D(width, height);
		kibera.schoolGrid = new SparseGrid2D(width, height);
		kibera.restaurantGrid = new SparseGrid2D(width, height);
		kibera.waterSourcesGrid = new SparseGrid2D(width, height);
		kibera.businessGrid = new SparseGrid2D(width, height);
		kibera.householdGrid = new SparseGrid2D(width, height);
		kibera.world = new Continuous2D(0.1, width, height);

	}

	/** ------------------------------Road Network Extraction Methods------------------------------------------------------------ */

	static void extractFromRoadLinks(GeomVectorField roadLinks, KiberaTB kibera) {
		Bag geoms = roadLinks.getGeometries();
		Envelope e = roadLinks.getMBR();
		double xmin = e.getMinX(), ymin = e.getMinY(), xmax = e.getMaxX(), ymax = e.getMaxY();
		int xcols = gridWidth - 1, ycols = gridHeight - 1;

		// extract each edge
		for (Object o : geoms) {

			MasonGeometry gm = (MasonGeometry) o;
			if (gm.getGeometry() instanceof LineString) {
				readLineString((LineString) gm.getGeometry(), xcols, ycols, xmin, ymin, xmax, ymax, kibera);
			} else if (gm.getGeometry() instanceof MultiLineString) {
				MultiLineString mls = (MultiLineString) gm.getGeometry();
				for (int i = 0; i < mls.getNumGeometries(); i++) {
					readLineString((LineString) mls.getGeometryN(i), xcols, ycols, xmin, ymin, xmax, ymax, kibera);
				}
			}
		}
	}

	/**
	 * Converts an individual linestring into a series of links and nodes in the network int width, int height, Dadaab dadaab
	 * 
	 * @param geometry
	 * @param xcols
	 *            - number of columns in the field
	 * @param ycols
	 *            - number of rows in the field
	 * @param xmin
	 *            - minimum x value in shapefile
	 * @param ymin
	 *            - minimum y value in shapefile
	 * @param xmax
	 *            - maximum x value in shapefile
	 * @param ymax
	 *            - maximum y value in shapefile
	 */
	static void readLineString(LineString geometry, int xcols, int ycols, double xmin, double ymin, double xmax, double ymax,
			KiberaTB kibera) {

		CoordinateSequence cs = geometry.getCoordinateSequence();

		// iterate over each pair of coordinates and establish a link between
		// them
		Node oldNode = null; // used to keep track of the last node referenced
		for (int i = 0; i < cs.size(); i++) {

			// calculate the location of the node in question
			double x = cs.getX(i), y = cs.getY(i);
			int xint = (int) Math.floor(xcols * (x - xmin) / (xmax - xmin)), yint = (int) (ycols - Math.floor(ycols * (y - ymin)
					/ (ymax - ymin))); // REMEMBER TO
			// FLIP THE
			// Y VALUE

			if (xint >= gridWidth) {
				continue;
			} else if (yint >= gridHeight) {
				continue;
			}

			// find that node or establish it if it doesn't yet exist
			Bag ns = kibera.nodes.getObjectsAtLocation(xint, yint);
			Node n;
			if (ns == null) {

				Int2D parcelLocation = new Int2D((xint), yint);
				n = new Node(new Parcel(parcelLocation));
				kibera.nodes.setObjectLocation(n, xint, yint);
			} else {
				n = (Node) ns.get(0);
			}

			if (oldNode == n) // don't link a node to itself
			{
				continue;
			}

			// attach the node to the previous node in the chain (or continue if
			// this is the first node in the chain of links)

			if (i == 0) { // can't connect previous link to anything
				oldNode = n; // save this node for reference in the next link
				continue;
			}

			int weight = (int) n.location.distanceTo(oldNode.location); // weight
			// is
			// just
			// distance

			// create the new link and save it
			Edge e = new Edge(oldNode, n, weight);
			kibera.roadNetwork.addEdge(e);
			oldNode.links.add(e);
			n.links.add(e);

			oldNode = n; // save this node for reference in the next link
		}
	}

	static class Node {

		Parcel location;
		ArrayList<Edge> links;

		public Node(Parcel l) {
			location = l;
			links = new ArrayList<Edge>();
		}
	}

	/**
	 * Used to find the nearest node for each space
	 * 
	 */
	static class Crawler {

		Node node;
		Parcel location;

		public Crawler(Node n, Parcel l) {
			node = n;
			location = l;
		}
	}

	/**
	 * Calculate the nodes nearest to each location and store the information
	 * 
	 * @param closestNodes
	 *            - the field to populate
	 */
	@SuppressWarnings("deprecation")
	static ObjectGrid2D setupNearestNodes(KiberaTB kibera) {

		ObjectGrid2D closestNodes = new ObjectGrid2D(gridWidth, gridHeight);
		ArrayList<Crawler> crawlers = new ArrayList<Crawler>();

		for (Object o : kibera.roadNetwork.allNodes) {
			Node n = (Node) o;
			Crawler c = new Crawler(n, n.location);
			crawlers.add(c);
		}

		// while there is unexplored space, continue!
		while (crawlers.size() > 0) {
			ArrayList<Crawler> nextGeneration = new ArrayList<Crawler>();

			// randomize the order in which cralwers are considered
			int size = crawlers.size();

			for (int i = 0; i < size; i++) {

				// randomly pick a remaining crawler
				int index = kibera.getRandom().nextInt(crawlers.size());
				Crawler c = crawlers.remove(index);

				// check if the location has already been claimed
				Node n = (Node) closestNodes.get(c.location.getXLocation(), c.location.getYLocation());

				if (n == null) { // found something new! Mark it and reproduce

					// set it
					closestNodes.set(c.location.getXLocation(), c.location.getYLocation(), c.node);

					// reproduce
					Bag neighbors = new Bag();

					kibera.landGrid.getNeighborsHamiltonianDistance(c.location.getXLocation(), c.location.getYLocation(), 1, false,
							neighbors, null, null);

					for (Object o : neighbors) {
						Parcel l = (Parcel) o;
						if (l == c.location) {
							continue;
						}
						Crawler newc = new Crawler(c.node, l);
						nextGeneration.add(newc);
					}
				}
				// otherwise just die
			}
			crawlers = nextGeneration;
		}
		return closestNodes;
	}

	/** ----------------------------------------------------------------------------------------------------------------------------------- */

	/***
	 * Counts the residents and the different characteristics. Used for validation and can be printed out in the console.
	 * 
	 * @param kibera
	 */
	public static void countResidents(KiberaTB kibera) {
		int totalhh = 0;

		int countStudentEligible = 0;

		int countHeadofHH = 0;
		int countMale = 0;
		int countFemale = 0;
		int countAdults = 0;
		int countChildren = 0;
		int countResidents = 0;
		int countYoungChildren = 0;

		for (int k = 0; k < kibera.allResidents.size(); k++) {
			ResidentTB res = (ResidentTB) kibera.allResidents.get(k);

			if (res.getGender() == 1) {
				countFemale = countFemale + 1;
			}
			if (res.getGender() == 0) {
				countMale = countMale + 1;
			}
			if (res.getSchoolEligible()) {
				countStudentEligible = countStudentEligible + 1;
			}
			if (res.getAge() < 18) {
				countChildren = countChildren + 1;
			}
			if (res.getAge() >= 18) {
				countAdults = countAdults + 1;
			}
			if (res.getAge() <= 5) {
				countYoungChildren = countYoungChildren + 1;
			}
			countResidents = countResidents + 1;

		}

		System.out.println();
		System.out.println("total hh = " + totalhh);
		System.out.println("student eligible = " + countStudentEligible);
		System.out.println("head of hh = " + countHeadofHH);
		System.out.println("male = " + countMale);
		System.out.println("female = " + countFemale);
		System.out.println("adults = " + countAdults);
		System.out.println("children = " + countChildren);
		System.out.println("residents = " + countResidents);

		System.out.println("young children = " + countYoungChildren);

	}
}
