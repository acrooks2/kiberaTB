//Parth Chopra, Summer 2013, under the mentorship of Dr. Andrew Crooks
//This class has all the properties to determine what activities are possible and where they are located 

package kiberaTB;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import sim.field.network.Edge;
import sim.util.*;

public class ActivityTB {

	//Activities an agent can do

	final public static int stayHome = 0;
	final public static int school = 1;
	final public static int water = 2;
	final public static int religion = 3;
	final public static int resteraunt = 4;
	final public static int healthCenter = 5;
	final public static int socialize = 6; //number jump b/c socializeRelatives was removed
	final public static int work = 7;
	final public static int business = 8; //go to a business

	/**
	 * Finds the location of the activity based on distance.
	 * 
	 * @param res
	 * @param position
	 * @param actId
	 * @param kibera
	 * 
	 * @return Parcel
	 */
        
    
        
	public Parcel bestActivityLocation(ResidentTB res, int actId, KiberaTB kibera) {

		if (actId == stayHome) {
			return res.getHomeLocation();
		} else if (actId == school) {
			return res.getMySchool().getStructure().getParcel();
		} else if (actId == work) {
			return findWorkLocation(res);
		} else if (actId == water) {
			return bestLocation(res.getPosition(), kibera.waterLocations, kibera);
		} else if (actId == religion) {
			return bestLocation(res.getPosition(), kibera.religiousLocations, kibera);
		} else if (actId == resteraunt) {
			return bestLocation(res.getPosition(), kibera.resterauntLocations, kibera);
		} else if (actId == healthCenter) {
			return bestLocation(res.getPosition(), kibera.healthLocations, kibera);
		} else if (actId == business) {
			return findBusinessLocation(kibera);
		} else if (actId == socialize) {
			return determineWhereToSociolize(res, kibera);
		}
		return null;
	}
        
        

	/**
	 * Finds the location of a particular facility
	 * 
	 * @param parcel
	 * @param fieldBag
	 * @param kibera
	 * 
	 * @return Parcel
	 */
	private static Parcel bestLocation(Parcel parcel, Bag fieldBag, KiberaTB kibera) {
            
            Parcel p;
            p = null;
            if (fieldBag.numObjs > 0) {
                if (kibera.getRandom().nextDouble() < 0.5) {
                    if (fieldBag.numObjs == 1) {
                        p = ((Parcel) fieldBag.objs[0]);
                    } else {
                        int winningIndex = kibera.getRandom().nextInt(fieldBag.numObjs);
                        p = ((Parcel) fieldBag.objs[winningIndex]);
                    }

                } else {
                    Bag newLocation = new Bag();

                    double bestScoreSoFar = Double.POSITIVE_INFINITY;
                    for (int i = 0; i < fieldBag.numObjs; i++) {
                        Parcel positionLocation = ((Parcel) fieldBag.objs[i]);

                        double fScore = parcel.distanceTo(positionLocation);
                        if (fScore > bestScoreSoFar) {
                            continue;
                        }

                        if (fScore <= bestScoreSoFar) {
                            bestScoreSoFar = fScore;
                            newLocation.clear();
                        }
                        newLocation.add(positionLocation);
                    }
                    p = (Parcel) newLocation.objs[0];

                }
            }

		return p;
	}

	/**
	 * Finds the necessary work location for an employee
	 * 
	 * @param resident
	 * 
	 * @return Parcel
	 */
	public static Parcel findWorkLocation(ResidentTB resident) {
		Parcel p = null;
		if (resident.getMyBusinessEmployer() != null) {
			p = resident.getMyBusinessEmployer().getStructure().getParcel();
		} else if (resident.getMyHealthFacilityEmployer() != null) {
			p = resident.getMyHealthFacilityEmployer().getStructure().getParcel();
		} else if (resident.getMySchoolEmployer() != null) {
			p = resident.getMySchoolEmployer().getStructure().getParcel();
		} else if (resident.getMyReligiousFacilityEmployer() != null) { //else resident is employed at religious facility
			p = resident.getMyReligiousFacilityEmployer().getStructure().getParcel();
		} else { //resident works outside of slum, but for now just stays home
			p = resident.getHomeLocation();
			System.out.println("Someone who is working shouldn't be going home. Check ActivityTB class");
		}

		return p;
	}

	//----------------------------Finds the necessary location for a business---------------------
	/**
	 * Finds the necessary business and its location
	 * 
	 * @param kibera
	 * 
	 * @return Parcel
	 */
	public static Parcel findBusinessLocation(KiberaTB kibera) { //finds a getRandom() business to go to based on a standard bell curve distribution
		Parcel p = null;
		double rand = kibera.getRandom().nextDouble();

		if (rand <= .682) {
			p = (Parcel) kibera.businessTier1Locations.get(kibera.getRandom().nextInt(kibera.businessTier1Locations.size()));
		} else if (rand <= .954 && rand > .682) {
			p = (Parcel) kibera.businessTier2Locations.get(kibera.getRandom().nextInt(kibera.businessTier2Locations.size()));
		} else {
			p = (Parcel) kibera.businessTier3Locations.get(kibera.getRandom().nextInt(kibera.businessTier3Locations.size()));
		}

		return p;
	}

	/**
	 * Determines where to go to socialize (socialize with people of similar characteristics)
	 * 
	 * @param me
	 * @param kibera
	 * @return Parcel
	 */
	public static Parcel determineWhereToSociolize(ResidentTB me, KiberaTB kibera) {
		// Go through my friends (colleagues, co-workers, family) and determine how much I want 
		// to be near them
		//force is based on weight of edge between the two residents as well as the distance between
		//their home locations

		//grab all residents linked to myself (with the exception of those living in the same household as myself)
		//determine the weight of the link and the physical distance of each pair
		//calculate overall likelihood of socializing with another resident based on weight and distance
		//determine residents with highest likelihoods
		//getRandom()ly select a resident of the highest likelihoods

		Bag myFriends = new Bag(kibera.socialNetwork.getEdgesOut(me));
		double sumWeight = 0;
		double sumDistance = 0;
		ResidentTB socializeFriend = null; //this is the friend I will socialize with

		//remove anyone living in the same parcel
		for (int i = 0; i < myFriends.size(); i++) {
			Edge e = (Edge) (myFriends.get(i));
			//Get the resident linked to me
			ResidentTB friend = (ResidentTB) e.getOtherNode(me);
			if (friend.getHomeLocation() == me.getHomeLocation()) {
                            
				myFriends.remove(i);
			}
		}
		myFriends.resize(myFriends.size());

		HashMap<ResidentTB, Double> socialize = new HashMap<ResidentTB, Double>();
		ValueComparator bvc = new ValueComparator(socialize);
		TreeMap<ResidentTB, Double> socialize_sorted = new TreeMap<ResidentTB, Double>(bvc);

		//my location
		double x = me.getHomeLocation().getXLocation();
		double y = me.getHomeLocation().getYLocation();

		if (myFriends != null) {
			for (int i = 0; i < myFriends.size(); i++) {
				Edge e = (Edge) (myFriends.get(i));
				//Get the resident linked to me
				ResidentTB friend = (ResidentTB) e.getOtherNode(me);
				Double2D friendLocation = kibera.world.getObjectLocation(friend);

				double weight = ((Double) (e.info)).doubleValue();
				sumWeight = sumWeight + weight;

				double dx = friendLocation.x - x;
				double dy = friendLocation.y - y;
				double distance = Math.sqrt(dx * dx + dy * dy);

				sumDistance = distance + sumDistance;
			}

			for (int i = 0; i < myFriends.size(); i++) {
				Edge e = (Edge) (myFriends.get(i));
				//Get the resident linked to me
				ResidentTB friend = (ResidentTB) e.getOtherNode(me);
				Double2D friendLocation = kibera.world.getObjectLocation(friend);

				double weight = ((Double) (e.info)).doubleValue();
				double weightStandardize = weight / sumWeight;

				double dx = friendLocation.x - x;
				double dy = friendLocation.y - y;
				double distance = Math.sqrt(dx * dx + dy * dy);
				double distanceStandardize = distance / sumDistance;

				if (sumDistance == 0) {
					distanceStandardize = 0;
				} else {
					distanceStandardize = 1 - distanceStandardize;
				} //take the inverse

				double socializeLikelihood = 0.9 * weightStandardize + 0.1 * distanceStandardize;

				socialize.put(friend, socializeLikelihood);
			}
		}
		//for some reason the size of the array decreases from myFriends to socialize????
		socialize_sorted.putAll(socialize);

		if (socialize != null) {
			int numFriends = socialize.size();
			int numPotentialFriendstoSocialize = (int) (numFriends * 0.1);

			if (numPotentialFriendstoSocialize <= 0) {
				numPotentialFriendstoSocialize = 1;
			}

			//pick a getRandom() number between 0 and the total number of potential friends I could socialize with
			int friendToSocialize = kibera.getRandom().nextInt(numPotentialFriendstoSocialize);
			//get the friend the resident will socialize with
			//socialize_sorted.get(friendToSocialize);
			int i = 0;
			for (Map.Entry<ResidentTB, Double> s : socialize_sorted.entrySet()) {
				if (friendToSocialize == i) {
					socializeFriend = s.getKey();
				}
				i++;
			}
		}

		Parcel myHome = me.getHomeLocation();

		if (socializeFriend == null) { //no friends to socialize with
			return myHome;
		}

		Parcel friendLocation = socializeFriend.getHomeLocation();
                // 

		//if the friend is not home, then don't go to their house, stay home instead. Should include? 
		if (socializeFriend.getCurrentActivity() != ActivityTB.stayHome) {
			me.setCurrentActivity(ActivityTB.stayHome);
			return myHome;
		} else {
			return friendLocation;
		}

	}
}
