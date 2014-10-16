package org.citruscircuits.scout;

public class TeleDataCollectionItems
{
	// An array of small arrays
	// Each small array has n strings:
	// Index 0: Unique key
	// Index 1: Type ("integer", "boolean")
	// Index [2,n): Arbitrary parameters specific to the type

	// @formatter:off
	public static String[][][] items = { 
		{{ "Make High", "Stepper", "Make High" },
		 { "Miss High", "Stepper", "Miss High" },
		 { "Make Low", "Stepper", "Make Low" },
		 { "Miss Low", "Stepper", "Miss Low" }
		},
		
		{{ "HP Receive", "Stepper", "HP Receive" },
		 { "HP Receive Fail", "Stepper", "HP Receive Fail" },
		 { "G. Receive", "Stepper", "G. Receive" },
		 { "Eject", "Stepper", "Eject/Lost Ball" }
		},
		
		{{ "Truss", "Stepper", "Truss" },
		 { "Catch", "Stepper", "Truss Catch" },
		 { "Goalie Block", "Stepper", "Goalie Block" }
		 }
	};
	// @formatter:on
}
