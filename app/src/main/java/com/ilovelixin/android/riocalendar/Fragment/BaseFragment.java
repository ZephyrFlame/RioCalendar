package com.ilovelixin.android.riocalendar.Fragment;

import android.support.v4.app.Fragment;

/**
 * Created by Lee on 2016/8/4.
 */
public abstract class BaseFragment extends Fragment {
    public abstract boolean isProjectEnable();
    public abstract boolean isDailyEnable();
    public abstract void importAllToCalendar();
    public abstract void exportAllToCalendar();
}
