package com.example.app.Software_Quality_Monitoring_System_Using_Reliability_Metrics.utilities;

import java.text.DecimalFormat;

public class Rounder {
    public static String roundValue(double value){
        return new DecimalFormat("0.00").format(value);
    }
}
