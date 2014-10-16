package org.citruscircuits.scout;

public class AutoDataCollectionItems
{
	// An array of small arrays
	// Each small array has n strings:
	// Index 0: Unique key
	// Index 1: Type ("integer", "boolean")
	// Index [2,n): Arbitrary parameters specific to the type

	// @formatter:off
	public static String[][][] items = { 
		{{ "Make High Hot", "Stepper", "Make High Hot" },
		 { "Make High Cold", "Stepper", "Make High Cold" },
		 { "Miss High", "Stepper", "Miss High" }
		},
		
		{{ "Make Low Hot", "Stepper", "Make Low Hot" },
		 { "Make Low Cold", "Stepper", "Make Low Cold" },
		 { "Miss Low", "Stepper", "Miss Low" }
		},
		
		{{ "G. Receive", "Stepper", "G. Receive" },
		 { "Eject", "Stepper", "Eject/Lost Ball" },
		 { "Goalie Block", "Stepper", "Goalie Block" },
		 { "Mobility", "Toggle", "NO Mobility Bonus", "Mobility Bonus!"}
		}
		};
	// @formatter:on
}
