package com.example;

import java.util.Map;

public class FilterBean {
    
    public boolean filterRecord(Map<String, Object> record) {
        // Check for BILL_X values
        for (int i = 1; i <= 6; i++) {
            Object billValue = record.get("BILL_" + i);
            if (billValue != null && Integer.parseInt(billValue.toString()) == 0) {
                return false; // Exclude if any BILL_X is 0
            }
        }

        // Check for PAY_X values (assuming PAY_X values are PAY_0, PAY_2 to PAY_6 as per your initial description)
        // Assuming PAY_1 is a typo and should be PAY_AMT_1 as per typical dataset structure
        for (int i = 0; i <= 6; i++) {
            if (i != 1) { // Skip PAY_1 since it's not listed in your fields
                Object payValue = record.get("PAY_" + i);
                if (payValue != null && Integer.parseInt(payValue.toString()) <= 0) {
                    return false; // Exclude if any PAY_X is <= 0
                }
            }
        }

        // If none of the exclusion criteria were met, include the record
        return true;
    }
}
