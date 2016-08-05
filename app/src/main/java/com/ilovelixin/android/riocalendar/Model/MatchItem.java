package com.ilovelixin.android.riocalendar.Model;

import com.ilovelixin.android.riocalendar.Tool.CalendarHelper;

/**
 * Created by Lee on 2016/7/27.
 */
public class MatchItem implements Comparable {
    public String Project;
    public String Date;
    public String Time;
    public String Match;
    public String Round;

    @Override
    public int compareTo(Object another) {
        MatchItem a = (MatchItem)another;
        if (this.Date.equals(a.Date) && this.Time.equals(a.Time)) {
            return 0;
        }

        long st = CalendarHelper.getStartTime(this.Date, this.Time);
        long sa = CalendarHelper.getStartTime(a.Date, a.Time);
        if (st == sa) {
            long et = CalendarHelper.getEndTime(this.Date, this.Time);
            long ea = CalendarHelper.getEndTime(a.Date, a.Time);
            if (et == ea) {
                return 0;
            } else if (et < ea) {
                return -1;
            } else {
                return 1;
            }
        } else if (st < sa) {
            return -1;
        } else {
            return 1;
        }
    }
}
