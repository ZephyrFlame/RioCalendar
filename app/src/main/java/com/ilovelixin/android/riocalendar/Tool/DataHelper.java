package com.ilovelixin.android.riocalendar.Tool;

import android.content.Context;

import com.ilovelixin.android.riocalendar.Model.MatchItem;
import com.ilovelixin.android.riocalendar.Model.Project;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lee on 2016/8/3.
 */
public class DataHelper {
    private final static int END = -1;
    private final static int TITLE = 0;
    private final static int DATE = 1;
    private final static int TIME = 2;
    private final static int ITEM = 3;
    private final static int ROUND = 4;

    public static List<Project> initCalendar(Context context) {
        List<Project> projects = new ArrayList<>();

        try {
            InputStreamReader inputReader = new InputStreamReader(context.getAssets().open("calendar.dat"));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String title = null;
            String date = null;
            String time = null;
            String item = null;
            String round = null;
            int last = END;
            Project project = null;
            while((line = bufReader.readLine()) != null) {
                if (last == END) {
                    title = line;
                    last = TITLE;
                    project = new Project();
                    project.Title = title;
                    project.Matches = new ArrayList<>();
                    projects.add(project);
                } else if (last == TITLE) {
                    date = line;
                    last = DATE;
                } else if (last == DATE) {
                    time = line;
                    last = TIME;
                } else if (last == TIME) {
                    item = line;
                    last = ITEM;
                } else if (last == ITEM) {
                    round = line;
                    last = ROUND;
                    MatchItem match = new MatchItem();
                    match.Project = title;
                    match.Date = date;
                    match.Time = time;
                    match.Match = item;
                    match.Round = round;
                    project.Matches.add(match);
                } else if (last == ROUND) {
                    if (line == null | line.length() == 0) {
                        last = END;
                    } else if (line.startsWith("8月")) {
                        date = line;
                        last = DATE;
                    } else if (line.contains(":")) {
                        time = line;
                        last = TIME;
                    } else {
                        item = line;
                        last = ITEM;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return projects;
    }
}
