package com.androidatelier.lunchin.settings;

import android.content.Context;
import android.text.format.DateFormat;

import com.androidatelier.lunchin.R;
import com.androidatelier.lunchin.util.Constants;
import com.androidatelier.lunchin.util.Formatter;


public class Setting {
    private static final String KEY_WORK_WIFI = "pref_work_wifi";
    private static final String KEY_LUNCH_DAYS_TRACKED = "pref_days_to_track";
    private static final String KEY_LUNCH_START = "pref_lunch_start";
    private static final String KEY_LUNCH_END = "pref_lunch_end";
    private static final String KEY_LUNCH_COST = "pref_avg_lunch_cost";
    private static final String KEY_GROSS_SALARY = "pref_gross_salary";
    private static final String KEY_SAVINGS_GOAL_NAME = "pref_savings_goal_name";
    private static final String KEY_SAVINGS_GOAL_VALUE = "pref_savings_goal_value";
    private static final String KEY_LAST_SSID = "pref_last_ssid";


    private Context mContext;
    private SettingsAccess mSettingsAccess;
    private Resource mEnum;

    public enum Resource {
        // key, title, description
        WORK_WIFI(KEY_WORK_WIFI, R.string.pref_title_work_wifi, R.string.pref_description_work_wifi),
        LUNCH_START(KEY_LUNCH_START, R.string.pref_title_lunch_start, R.string.pref_description_lunch_start),
        LUNCH_END(KEY_LUNCH_END, R.string.pref_title_lunch_end, R.string.pref_description_lunch_end),
        LUNCH_DAYS_TRACKED(KEY_LUNCH_DAYS_TRACKED, R.string.pref_title_lunch_days_tracked, R.string.pref_description_lunch_days_tracked),
        LUNCH_AVG_COST(KEY_LUNCH_COST, R.string.pref_title_avg_lunch_cost, R.string.pref_description_avg_lunch_cost),
        GROSS_SALARY(KEY_GROSS_SALARY, R.string.pref_title_gross_salary, R.string.pref_description_gross_salary),
        SAVINGS_GOAL_NAME(KEY_SAVINGS_GOAL_NAME, R.string.pref_title_my_goal, R.string.pref_description_my_goal),
        SAVINGS_GOAL_VALUE(KEY_SAVINGS_GOAL_VALUE, R.string.pref_title_my_goal, R.string.pref_description_my_goal),
        MY_GOAL("", R.string.pref_title_my_goal, R.string.pref_description_my_goal),
        LAST_SSID_VALUE(KEY_LAST_SSID, R.string.pref_title_last_ssid, R.string.pref_description_last_ssid)
        ;

        private final String prefKey;
        private final int titleResource;
        private final int descriptionResource;

        Resource(String prefKey, int titleResource, int descriptionResource) {
            this.prefKey = prefKey;
            this.titleResource = titleResource;
            this.descriptionResource = descriptionResource;
        }

        public String getKey() {
            return this.prefKey;
        }

        public int getTitle() {
            return this.titleResource;
        }

        public int getDescription() {
            return this.descriptionResource;
        }

        public String getTitleText(Context context) {
            return context.getString(getTitle());
        }

        public String getDescriptionText(Context context) {
            return context.getString(getDescription());
        }

    }


    private Setting() {};
    public Setting(Context context, Resource settingResourceEnum) {
        this.mContext = context;
        this.mEnum = settingResourceEnum;
        mSettingsAccess = new SettingsAccess(context);
    }

    public String getTitle() {
        return mEnum.getTitleText(mContext);
    }

    public String getDescription() {
        String description = mEnum.getDescriptionText(mContext);
        Object value;
        if (!(mEnum == Resource.MY_GOAL)) {
            value = getValue(mEnum);
        } else {
            // set both SAVINGS_GOAL_NAME and SAVINGS_GOAL_VALUE  in the description
            String name =  getValue(Resource.SAVINGS_GOAL_NAME).toString();
            int cost = (int) getValue(Resource.SAVINGS_GOAL_VALUE);
            value = Formatter.formatGoal(name, cost);
        }

        if (value != null && !value.toString().isEmpty()) {
            description = value.toString();
        }
        return description;
    }

    public Object getValue(Resource resourceEnum) {
        boolean is24HourFormat = DateFormat.is24HourFormat(mContext);
        switch (resourceEnum) {
            case WORK_WIFI :
                return mSettingsAccess.getWorkWifiId();
            case LUNCH_START :
                int[] lunchStart = mSettingsAccess.getLunchStartTime();
                int startHour = lunchStart[0];
                int startMins = lunchStart[1];
                return Formatter.formatTime(startHour, startMins, is24HourFormat);
            case LUNCH_END :
                int[] lunchEnd = mSettingsAccess.getLunchEndTime();
                int endHour = lunchEnd[0];
                int endMins = lunchEnd[1];
                return Formatter.formatTime(endHour, endMins, is24HourFormat);
            case LUNCH_DAYS_TRACKED :
                return mSettingsAccess.getDaysToTrack();
            case LUNCH_AVG_COST :
                double lunchCost = mSettingsAccess.getAverageLunchCost();
                return Formatter.formatDoubleToCurrencyUSD(lunchCost);
            case GROSS_SALARY :
                return getGrossSalaryForSettingsDescription();
            case SAVINGS_GOAL_NAME :
                return mSettingsAccess.getSavingsGoalName();
            case SAVINGS_GOAL_VALUE :
                return mSettingsAccess.getSavingsGoalValue();
            default : return false;
        }
    }

    private String getGrossSalaryForSettingsDescription() {
        String value = "";
        boolean isNotSetByUser = false;
        int salary = mSettingsAccess.getGrossAnnualSalary(false);
        if (salary == Constants.CODE_GROSS_SALARY_NOT_SET) {
            isNotSetByUser = true;
            salary = Constants.DEFAULT_GROSS_ANNUAL_SALARY;
        }
        if (isNotSetByUser && salary == Constants.DEFAULT_GROSS_ANNUAL_SALARY) {
            value = mContext.getString(R.string.info_national_average_income, Formatter.formatIntToCurrencyUSD(salary));
        } else {
            value = Formatter.formatIntToCurrencyUSD(salary);
        }
        return value;
    }
}
