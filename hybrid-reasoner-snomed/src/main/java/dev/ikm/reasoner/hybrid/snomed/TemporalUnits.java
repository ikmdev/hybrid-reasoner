package dev.ikm.reasoner.hybrid.snomed;

public enum TemporalUnits {

	// 257997001 |Seconds (qualifier value)|
	Seconds(257997001),

	// 1156209001 |minute (qualifier value)|
	Minutes(1156209001),

	// 258702006 |hour (qualifier value)|
	Hours(258702006),

	// 258703001 |day (qualifier value)|
	Days(258703001),

	// 258705008 |week (qualifier value)|
	Weeks(258705008),

	// 258706009 |month (qualifier value)|
	Months(258706009),

	// 258707000 |year (qualifier value)|
	Years(258707000);

	public long sctid;

	private TemporalUnits(long sctid) {
		this.sctid = sctid;
	}

}
