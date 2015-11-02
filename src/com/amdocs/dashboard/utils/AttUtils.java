package com.amdocs.dashboard.utils;

public class AttUtils
{
	public enum regionList {
		southwest,
		midwest,
		west,
		southeast,
		other
	};

	public enum inRegionStateList {
	    AR,
	    AZ,
	    CT,
	    KS,
	    MO,
	    OK,
	    TX,
	    IL,
	    IN,
	    MI,
	    OH,
	    WI,
	    CA,
	    NV,
	    AL,
	    FL,
	    GA,
	    KY,
	    LA,
	    MS,
	    NC,
	    SC,
	    TN,
	    WD,    //<< not really sure what this is. The following is in the HBD for some customers: 21SWDA - Independent
	    FI,    //<< not really sure what this is. The following is in the HBD for some customers: XSPFIC - Sprint Fictitious Product
	    NONE
	};

	//-------------------------------------------------------------------------
	public static Boolean isStateInRegion(String region, String state)
	{
		Boolean result = true;

		switch(region.toLowerCase().toCharArray()[0])
		{
		case 's':
        	if(state.compareTo("AR") != 0 &&
        	   state.compareTo("AZ") != 0 &&
        	   state.compareTo("CT") != 0 &&
        	   state.compareTo("KS") != 0 &&
        	   state.compareTo("MO") != 0 &&
        	   state.compareTo("OK") != 0 &&
        	   state.compareTo("TX") != 0)
        	{
        		result = false;
        	}
    		break;

    	case 'm':
        	if(state.compareTo("IL") != 0 &&
        	   state.compareTo("IN") != 0 &&
           	   state.compareTo("MI") != 0 &&
           	   state.compareTo("OH") != 0 &&
           	   state.compareTo("WI") != 0)
            {
        		result = false;
            }
    		break;

    	case 'w':
        	if(state.compareTo("CA") != 0 &&
        	   state.compareTo("NV") != 0)
            {
        		result = false;
            }
    		break;

    	case 'b':
        	if(state.compareTo("AL") != 0 &&
               state.compareTo("FL") != 0 &&
               state.compareTo("GA") != 0 &&
               state.compareTo("KY") != 0 &&
               state.compareTo("LA") != 0 &&
               state.compareTo("MS") != 0 &&
               state.compareTo("NC") != 0 &&
               state.compareTo("SC") != 0 &&
               state.compareTo("TN") != 0)
            {
        		result = false;
            }
    		break;

    	case 'o':
    	default:
    		break;
    	}

		return result;
	}

	//-------------------------------------------------------------------------
	public static String getStatesInRegion(String regionCode)
	{
		String result = "";

		if(regionCode.compareToIgnoreCase("S") == 0) result = "AR,AZ,CT,KS,MO,OK,TX";		
		else if(regionCode.compareToIgnoreCase("M") == 0) result = "IL,IN,MI,OH,WI";
		else if(regionCode.compareToIgnoreCase("W") == 0) result = "CA,NV";
		else if(regionCode.compareToIgnoreCase("B") == 0) result = "AL,FL,GA,KY,LA,MS,NC,SC,TN";
		else if(regionCode.compareToIgnoreCase("O") == 0) result = "";

		return result;		
	}

	//-------------------------------------------------------------------------
	public static String getRegionName(String regionCode)
	{
		String result = "Unknown";
		
		if(regionCode.compareToIgnoreCase("S") == 0) result = "Southwest";		
		else if(regionCode.compareToIgnoreCase("M") == 0) result = "Midwest";
		else if(regionCode.compareToIgnoreCase("W") == 0) result = "West";
		else if(regionCode.compareToIgnoreCase("B") == 0) result = "Southeast";
		else if(regionCode.compareToIgnoreCase("O") == 0) result = "Other";

		return result;
	}

	//-------------------------------------------------------------------------
	public static String getRegionByCustomerId(String customerId)
	{
		String result = "Other";

		switch(customerId.charAt(0))
		{
		    case '1': result = "Southwest"; break;
		    case '2': result = "Midwest"; break;
		    case '3': result = "Southwest"; break;
		    case '4': result = "Midwest"; break;
		    case '5': result = "West"; break;
		    case '6': result = "Midwest"; break;
		    case '7': result = "Midwest"; break;
		    case '8': result = "West"; break;
		    case '9': result = "Southeast"; break;
		}	
		
		return result;
	}
	
	//-------------------------------------------------------------------------
	public static String getStateName(String stateCode)
	{
		String result = "None";
		
		//Southwest
		if(stateCode.compareToIgnoreCase("AR") == 0) result = "Arkansas";		
		else if(stateCode.compareToIgnoreCase("AZ") == 0) result = "Arizona";
		else if(stateCode.compareToIgnoreCase("CT") == 0) result = "Connecticut";
		else if(stateCode.compareToIgnoreCase("KS") == 0) result = "Kansas"; 
		else if(stateCode.compareToIgnoreCase("MO") == 0) result = "Missouri";
		else if(stateCode.compareToIgnoreCase("OK") == 0) result = "Oklahoma";
		else if(stateCode.compareToIgnoreCase("TX") == 0) result = "Texas";
		
		//Midwest
		else if(stateCode.compareToIgnoreCase("IL") == 0) result = "Illinois";
		else if(stateCode.compareToIgnoreCase("IN") == 0) result = "Indiana"; 
		else if(stateCode.compareToIgnoreCase("MI") == 0) result = "Michigan";
		else if(stateCode.compareToIgnoreCase("OH") == 0) result = "Ohio";
		else if(stateCode.compareToIgnoreCase("WI") == 0) result = "Wisconsin";
		
		//West
		else if(stateCode.compareToIgnoreCase("CA") == 0) result = "California";
		else if(stateCode.compareToIgnoreCase("NV") == 0) result = "Nevada";

		//Southeast
		else if(stateCode.compareToIgnoreCase("AL") == 0) result = "Alabama";
		else if(stateCode.compareToIgnoreCase("FL") == 0) result = "Florida";
		else if(stateCode.compareToIgnoreCase("GA") == 0) result = "Georgia";	
		else if(stateCode.compareToIgnoreCase("KY") == 0) result = "Kentucky";
		else if(stateCode.compareToIgnoreCase("LA") == 0) result = "Louisiana";
		else if(stateCode.compareToIgnoreCase("MS") == 0) result = "Mississippi";
		else if(stateCode.compareToIgnoreCase("NC") == 0) result = "North Carolina";
		else if(stateCode.compareToIgnoreCase("SC") == 0) result = "South Carolina";
		else if(stateCode.compareToIgnoreCase("TN") == 0) result = "Tennessee";

		//Out of Region
		else if(stateCode.compareToIgnoreCase("AK") == 0) result = "Alaska";
		else if(stateCode.compareToIgnoreCase("CO") == 0) result = "Colorado";
		else if(stateCode.compareToIgnoreCase("DE") == 0) result = "Delaware";
		else if(stateCode.compareToIgnoreCase("DC") == 0) result = "District Of Columbia";
		else if(stateCode.compareToIgnoreCase("HI") == 0) result = "Hawaii";
		else if(stateCode.compareToIgnoreCase("ID") == 0) result = "Idaho";
		else if(stateCode.compareToIgnoreCase("IA") == 0) result = "Iowa";
		else if(stateCode.compareToIgnoreCase("ME") == 0) result = "Maine";
		else if(stateCode.compareToIgnoreCase("MD") == 0) result = "Maryland";
		else if(stateCode.compareToIgnoreCase("MA") == 0) result = "Massachusetts";
		else if(stateCode.compareToIgnoreCase("MN") == 0) result = "Minnesota";
		else if(stateCode.compareToIgnoreCase("MT") == 0) result = "Montana";
		else if(stateCode.compareToIgnoreCase("NE") == 0) result = "Nebraska";
		else if(stateCode.compareToIgnoreCase("NH") == 0) result = "New Hampshire";
		else if(stateCode.compareToIgnoreCase("NJ") == 0) result = "New Jersey";
		else if(stateCode.compareToIgnoreCase("NM") == 0) result = "New Mexico";
		else if(stateCode.compareToIgnoreCase("NY") == 0) result = "New York";
		else if(stateCode.compareToIgnoreCase("ND") == 0) result = "North Dakota";
		else if(stateCode.compareToIgnoreCase("OR") == 0) result = "Oregon";
		else if(stateCode.compareToIgnoreCase("PA") == 0) result = "Pennsylvania";
		else if(stateCode.compareToIgnoreCase("RI") == 0) result = "Rhode Island";
		else if(stateCode.compareToIgnoreCase("SD") == 0) result = "South Dakota";
		else if(stateCode.compareToIgnoreCase("UT") == 0) result = "Utah";
		else if(stateCode.compareToIgnoreCase("VT") == 0) result = "Vermont";
		else if(stateCode.compareToIgnoreCase("VA") == 0) result = "Virginia";
		else if(stateCode.compareToIgnoreCase("WA") == 0) result = "Washington";
		else if(stateCode.compareToIgnoreCase("WV") == 0) result = "West Virginia";
		else if(stateCode.compareToIgnoreCase("WY") == 0) result = "Wyoming";
		
		return result;
	}
}
