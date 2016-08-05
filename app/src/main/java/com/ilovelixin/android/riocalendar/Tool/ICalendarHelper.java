package com.ilovelixin.android.riocalendar.Tool;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.ilovelixin.android.riocalendar.Model.MatchItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Lee on 2016/8/4.
 */
public class ICalendarHelper {
    private final static String TAG = "ICalendarHelper";
    private final static String FILE_NAME = "Olympic2016.ics";

    public static void generateICalendar(Context context, List<MatchItem> matches) {
        boolean isMounted = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (!isMounted) {
            Log.w(TAG, "no ext storage");
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("BEGIN:VCALENDAR");
        builder.append('\n');
        builder.append("PRODID:-//Microsoft Corporation//Outlook 14.0 MIMEDIR//EN");
        builder.append('\n');
        builder.append("VERSION:2.0");
        builder.append('\n');
        builder.append("METHOD:PUBLISH");
        builder.append('\n');
        builder.append("X-CALSTART:20160804T000000Z");
        builder.append('\n');
        builder.append("X-CALSTART:20160822T000000Z");
        builder.append('\n');
        builder.append("X-WR-RELCALID:{00000018-CD0B-E9C2-81DF-96D2909A7926}");
        builder.append('\n');
        builder.append("X-WR-CALNAME:");
        builder.append(FILE_NAME);
        builder.append('\n');
        builder.append("BEGIN:VTIMEZONE");
        builder.append('\n');
        builder.append("TZID:China Standard Time");
        builder.append('\n');
        builder.append("BEGIN:STANDARD");
        builder.append('\n');
        builder.append("DTSTART:16010101T000000");
        builder.append('\n');
        builder.append("TZOFFSETFROM:+0900");
        builder.append('\n');
        builder.append("TZOFFSETTO:+0800");
        builder.append('\n');
        builder.append("END:STANDARD");
        builder.append('\n');
        builder.append("END:VTIMEZONE");
        builder.append('\n');

        long now = System.currentTimeMillis() - 8*3600*1000;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd:HHmmss");
        String nStr = sdf.format(c.getTime()).replace(':', 'T') + "Z";

        for (MatchItem match : matches) {
            builder.append("BEGIN:VEVENT");
            builder.append('\n');
            builder.append("CATEGORIES:");
            builder.append(match.Round);
            builder.append(',');
            builder.append(match.Project);
            builder.append(',');
            builder.append(match.Date);
            builder.append('\n');
            builder.append("CLASS:PUBLIC");
            builder.append('\n');
            builder.append("CREATED:");
            builder.append(nStr);
            builder.append('\n');
            builder.append("DESCRIPTION:");
            builder.append(match.Date);
            builder.append(' ');
            builder.append(match.Time);
            builder.append('\n');
            builder.append("DTEND;TZID=\"China Standard Time\":");
            builder.append(sdf.format(CalendarHelper.getStartDate(match.Date, match.Time)).replace(':', 'T'));
            builder.append('\n');
            builder.append("DTSTAMP:");
            builder.append(nStr);
            builder.append('\n');
            builder.append("DTSTART;TZID=\"China Standard Time\":");
            builder.append(sdf.format(CalendarHelper.getEndDate(match.Date, match.Time)).replace(':', 'T'));
            builder.append('\n');
            builder.append("LAST-MODIFIED:");
            builder.append(nStr);
            builder.append('\n');
            builder.append("LOCATION:巴西-里约热内卢");
            builder.append('\n');
            builder.append("PRIORITY:5");
            builder.append('\n');
            builder.append("SEQUENCE:0");
            builder.append('\n');
            builder.append("SUMMARY;LANGUAGE=zh-cn:");
            builder.append(match.Project);
            builder.append(' ');
            builder.append(match.Match);
            builder.append(' ');
            builder.append(match.Round);
            builder.append('\n');
            builder.append("TRANSP:OPAQUE");
            builder.append('\n');
            builder.append("UID:");
            builder.append(now++);
            builder.append('\n');
            builder.append("X-MICROSOFT-CDO-BUSYSTATUS:BUSY");
            builder.append('\n');
            builder.append("X-MICROSOFT-CDO-IMPORTANCE:1");
            builder.append('\n');
            builder.append("X-MICROSOFT-DISALLOW-COUNTER:FALSE");
            builder.append('\n');
            builder.append("X-MS-OLK-AUTOFILLLOCATION:FALSE");
            builder.append('\n');
            builder.append("X-MS-OLK-CONFTYPE:0");
            builder.append('\n');
            builder.append("BEGIN:VALARM");
            builder.append('\n');
            builder.append("TRIGGER:-PT15M");
            builder.append('\n');
            builder.append("ACTION:DISPLAY");
            builder.append('\n');
            builder.append("DESCRIPTION:Reminder");
            builder.append('\n');
            builder.append("END:VALARM");
            builder.append('\n');
            builder.append("END:VEVENT");
            builder.append('\n');
        }

        File root = Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsoluteFile(), FILE_NAME);
        Log.d(TAG, file.getAbsolutePath());

        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = builder.toString().getBytes();
            fos.write(buffer, 0, buffer.length);
            fos.flush();
            fos.close();
            Log.d(TAG, "write to file finished");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
