package kiberaTB;

public class TimeManager {

	final int DURATION = 24; // 1 day is 24 hours
	final int WEEKDURATION = 7; // 1 day = 24 hour, 1 week = 7 day =  7 * 24

	public int currentDayInWeek(int currentStep) { //0 = monday all the way to 6 = sunday

		int day = (int) (currentStep / DURATION);
		if (day < 7)
			return day;
		else {
			day = (int) (day % WEEKDURATION);
			return day;
		}

	}

	public int currentMonth(int currentStep) { //0 = january all the way to 11 = december

		int day = dayCount(currentStep);

		if (day > 365)
			day = day % 365;

		if (day <= 31)
			return 0; // january
		else if (day <= 59)
			return 1; //february
		else if (day <= 90)
			return 2; //march
		else if (day <= 120)
			return 3; //april
		else if (day <= 151)
			return 4; //may
		else if (day <= 181)
			return 5; //june
		else if (day <= 212)
			return 6; //july
		else if (day <= 243)
			return 7; //august
		else if (day <= 273)
			return 8; //september
		else if (day <= 304)
			return 9; //october
		else if (day <= 334)
			return 10; //november
		else if (day <= 365)
			return 11; //december
		else {
			System.out.println("Problem with determining current month in TimeManager");
			return -1;
		}
	}

	// continous day count
	public int dayCount(int currentStep) {
		int day = (int) (currentStep / DURATION);

		return day;
	}
}
