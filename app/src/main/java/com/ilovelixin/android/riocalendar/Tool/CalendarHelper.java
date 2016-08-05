package com.ilovelixin.android.riocalendar.Tool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import com.ilovelixin.android.riocalendar.Model.MatchItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lee on 2016/8/2.
 */
public class CalendarHelper {
    private final static String TAG = "CalendarHelper";

    private final static String CALENDAR_URI = "content://com.android.calendar/calendars";
    private final static String CALENDAR_EVENT_URI = "content://com.android.calendar/events";
    private final static String CALENDAR_REMINDER_URI = "content://com.android.calendar/reminders";

    private static CalendarHelper sInstance = new CalendarHelper();

    private CalendarHelper() {
    }

    public static CalendarHelper getInstance() {
        return sInstance;
    }

    public boolean addReminder(Context context, MatchItem item) {
        return addReminder(context, item, false);
    }

    public boolean addReminder(Context context, MatchItem item, boolean silent) {
        String calId = "";
        Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDAR_URI), null, null, null, null);
        if (userCursor.getCount() > 0) {
            userCursor.moveToFirst();
            calId = userCursor.getString(userCursor.getColumnIndex("_id"));
        }
        else {
            if (!silent) {
                Toast.makeText(context, "没有账户，请先添加账户", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        Log.v(TAG, "addReminder: calId=" + calId);

        ContentValues event = new ContentValues();
        event.put("title", item.Match + " " + item.Project);
        event.put("description", item.Match + " " + item.Project + " " + item.Round + " " + item.Date + " " + item.Time);
        // 插入账户
        event.put("calendar_id", calId);
        event.put("eventLocation", "巴西-里约");

        event.put("dtstart", getStartTime(item.Date, item.Time));
        event.put("dtend", getEndTime(item.Date, item.Time));
        event.put("hasAlarm", 1);

        event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai");  //这个是时区，必须有，
        //添加事件
        Uri newEvent = context.getContentResolver().insert(Uri.parse(CALENDAR_EVENT_URI), event);
        Log.v(TAG, "addReminder: newEvent=" + newEvent);

        //事件提醒的设定
        long id = Long.parseLong(newEvent.getLastPathSegment());
        ContentValues values = new ContentValues();
        values.put("event_id", id);
        // 提前10分钟有提醒
        values.put("minutes", 10);
        Uri newReminder = context.getContentResolver().insert(Uri.parse(CALENDAR_REMINDER_URI), values);
        Log.v(TAG, "addReminder: newReminder=" + newReminder);

        return true;
    }

    public static Date getStartDate(String dateStr, String timeStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat d = new SimpleDateFormat("M月d日");
            Date date = d.parse(dateStr);
            Log.v(TAG, "getStartTime: calendar=" + format.format(date));
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            calendar.set(Calendar.MONTH, 2016);
            calendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String[] ts = timeStr.split("-");
        try {
            SimpleDateFormat t = new SimpleDateFormat("HH:mm");
            Date time = t.parse(ts[0]);
            Log.v(TAG, "getStartTime: calendar=" + format.format(time));
            Calendar c = Calendar.getInstance();
            c.setTime(time);
            calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "getStartTime: calendar=" + format.format(calendar.getTime()));

        return calendar.getTime();
    }

    public static long getStartTime(String dateStr, String timeStr) {
        Date date = getStartDate(dateStr, timeStr);
        return date.getTime();
    }

    public static Date getEndDate(String dateStr, String timeStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat d = new SimpleDateFormat("M月d日");
            Date date = d.parse(dateStr);
            Log.v(TAG, "getEndTime: calendar=" + format.format(date));
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            calendar.set(Calendar.MONTH, 2016);
            calendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String[] tss = timeStr.split("-");
        try {
            String ts = tss[1];
            if (ts.contains("次日")) {
                ts = ts.substring("次日".length());
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)+1);
            }
            SimpleDateFormat t = new SimpleDateFormat("HH:mm");
            Date time = t.parse(ts);
            Log.v(TAG, "getEndTime: calendar=" + format.format(time));
            Calendar c = Calendar.getInstance();
            c.setTime(time);
            calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "getEndTime: calendar=" + format.format(calendar.getTime()));

        return calendar.getTime();
    }

    public static long getEndTime(String dateStr, String timeStr) {
        Date date = getEndDate(dateStr, timeStr);
        return date.getTime();
    }
}
