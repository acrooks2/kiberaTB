package kiberaTB;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<ResidentTB> {
	
		Map<ResidentTB, Double> base;
		public ValueComparator(Map<ResidentTB, Double> base) {
			this.base = base;
		}
		
		// Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(ResidentTB a, ResidentTB b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }

		//@Override
		//public int compare(Double o1, Double o2) {
			// TODO Auto-generated method stub
		//	return 0;
		//}		 
}
