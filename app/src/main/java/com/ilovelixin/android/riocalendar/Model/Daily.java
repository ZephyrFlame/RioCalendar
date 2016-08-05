package com.ilovelixin.android.riocalendar.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Lee on 2016/8/3.
 */
public class Daily implements Comparable {
    public String Day;
    public List<MatchItem> Matches;

    @Override
    public int compareTo(Object another) {
        if (this.Day.equals(((Daily)another).Day)) {
            return 0;
        }

        long dt = getDate(this.Day);
        long da = getDate(((Daily)another).Day);
        if (dt < da) {
            return -1;
        }
        return 1;
    }

    public long getDate(String day) {
        SimpleDateFormat d = new SimpleDateFormat("M月d日");
        try {
            Date date = d.parse(day);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
