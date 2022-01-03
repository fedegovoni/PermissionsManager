package com.federicogovoni.permissionsmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final List<String> BILLING_CATALOG = new ArrayList<>(Arrays.asList(
            "pro_version",
            "ntpsync.donation.1",
            "ntpsync.donation.2",
            "ntpsync.donation.3",
            "ntpsync.donation.5",
            "ntpsync.donation.8",
            "ntpsync.donation.13"
    ));
    public static final String ROOT_DIALOG = "ROOT_DIALOG";
    public static final String FIRST_OPEN = "FIRST_OPEN";
    public static final String SELECTED_PLACE = "SELECTED_PLACE";
    public static final String SUCCESS = "SUCCESS";
    public static final int FREQUENCY_NONE = 0;
    public static final int FREQUENCY_DAILY = 1;
    public static final int FREQUENCY_WEEKLY = 2;

}
