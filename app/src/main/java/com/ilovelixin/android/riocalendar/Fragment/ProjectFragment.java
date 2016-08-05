package com.ilovelixin.android.riocalendar.Fragment;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ilovelixin.android.riocalendar.MainActivity;
import com.ilovelixin.android.riocalendar.Model.Daily;
import com.ilovelixin.android.riocalendar.Model.MatchItem;
import com.ilovelixin.android.riocalendar.Model.Project;
import com.ilovelixin.android.riocalendar.R;
import com.ilovelixin.android.riocalendar.Tool.CalendarHelper;
import com.ilovelixin.android.riocalendar.Tool.ICalendarHelper;

import java.util.ArrayList;
import java.util.List;

public class ProjectFragment extends BaseFragment {
    private final static String TAG = "ProjectFragment";

    private ListView mListView;
    private List<Project> mProjects;
    private View mRootView;
    private ProjectAdapter mAdapter;
    private OnProjectItemClickListener mListener;
    private boolean mIsBusy = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    mIsBusy = false;
                    Toast.makeText(getContext(), "导入到日历已完成", Toast.LENGTH_LONG).show();
                    break;

                case 11:
                    mIsBusy = false;
                    Toast.makeText(getContext(), "导出到iCalendar已完成", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public ProjectFragment() {
        mProjects = new ArrayList<>();
    }

    public static ProjectFragment newInstance(List<Project> projects) {
        ProjectFragment fragment = new ProjectFragment();
        fragment.setProjects(projects);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_project, container, false);

            mAdapter = new ProjectAdapter();
            mListView = (ListView) mRootView.findViewById(R.id.list);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mListener != null) {
                        mListener.onProjectItemClick(position, mProjects.get(position));
                    }
                }
            });
        }

        return mRootView;
    }

    public void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();

        MainActivity activity = (MainActivity)getActivity();
        activity.setTitle(R.string.app_name);
        activity.setCurrentFragment(this);
    }

    @Override
    public void onAttach(Context context) {
        Log.v(TAG, "onAttach");
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        Log.v(TAG, "onDetach");
        super.onDetach();
    }

    @Override
    public boolean isProjectEnable() {
        return true;
    }

    @Override
    public boolean isDailyEnable() {
        return false;
    }

    @Override
    public void importAllToCalendar() {
        if (mIsBusy) {
            Toast.makeText(getContext(), "Ops！还在忙着别的工作哦！", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread() {
            public void run() {
                mIsBusy = true;
                for (Project project : mProjects) {
                    for (MatchItem match : project.Matches) {
                        if (!CalendarHelper.getInstance().addReminder(getContext(), match, true)) {
                            break;
                        }
                    }
                }
                mHandler.sendEmptyMessage(10);
            }
        }.start();
    }

    @Override
    public void exportAllToCalendar() {
        if (mIsBusy) {
            Toast.makeText(getContext(), "Ops！还在忙着别的工作哦！", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread() {
            public void run() {
                mIsBusy = true;
                List<MatchItem> matches = new ArrayList<>();
                for (Project project : mProjects) {
                    for (MatchItem match : project.Matches) {
                        matches.add(match);
                    }
                }
                ICalendarHelper.generateICalendar(getContext(), matches);
                mHandler.sendEmptyMessage(11);
            }
        }.start();
    }

    public void setProjects(List<Project> projects) {
        if (mProjects == null) {
            mProjects = new ArrayList<>();
        } else {
            mProjects.clear();
        }
        mProjects.addAll(projects);
    }

    public void setOnProjectItemClickListener(OnProjectItemClickListener l) {
        mListener = l;
    }

    public class ProjectAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mProjects.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
                holder.TitleView = (TextView)convertView.findViewById(R.id.title);
                holder.HintView = (TextView)convertView.findViewById(R.id.hint);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.TitleView.setText(mProjects.get(position).Title);
            holder.HintView.setText(mProjects.get(position).Matches.size() + " 场比赛");

            return convertView;
        }

        public class ViewHolder {
            public TextView TitleView;
            public TextView HintView;
        }
    }

    public interface OnProjectItemClickListener{
        void onProjectItemClick(int position, Project project);
    }
}
