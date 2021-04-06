package com.nexysquare.ddoyac.util;

import android.widget.DatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtil {
    public static String convertedSimpleFormat(Date date){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return df.format(date);
    }
    public static Date convertedStringToDate(String date){


        if(date==null || date.isEmpty()) return null;
        else{

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            try {
                Date converted = simpleDateFormat.parse(date);


                return converted;
            } catch (ParseException e) {
                e.printStackTrace();




            }
        }
        return null;
    }

    public static Date convertedStringToDateFromServer(String date){


        if(date==null || date.isEmpty()) return null;
        else{



            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            try {
                Date converted = simpleDateFormat.parse(date);


                return converted;
            } catch (ParseException e) {
                e.printStackTrace();




            }
        }
        return null;
    }


    public static Date addDay(Date dt, int n){

        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, n);

        return c.getTime();
    }


    public static int getCountOfDays(Date from, Date to) {


        long diff = to.getTime() - from.getTime();
        return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public static java.util.Date getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }
}
