package com.ilovelixin.android.riocalendar;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.ilovelixin.android.riocalendar.Fragment.BaseFragment;
import com.ilovelixin.android.riocalendar.Fragment.DailyFragment;
import com.ilovelixin.android.riocalendar.Fragment.FragmentLoading;
import com.ilovelixin.android.riocalendar.Fragment.MatchFragment;
import com.ilovelixin.android.riocalendar.Fragment.ProjectFragment;
import com.ilovelixin.android.riocalendar.Model.Daily;
import com.ilovelixin.android.riocalendar.Model.MatchItem;
import com.ilovelixin.android.riocalendar.Model.Project;
import com.ilovelixin.android.riocalendar.Tool.DataHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private boolean mIsInited = false;
    private boolean mIsIniting = false;
    private List<Project> mProjects = new ArrayList<>();
    private List<Daily> mDailies = new ArrayList<>();
    private boolean mIsSortByDate = false;
    private BaseFragment mBaseFragment = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    mIsIniting = false;
                    mIsInited = true;
                    setProjectListFragment();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDefaultFragment();
    }

    protected void onStart() {
        super.onStart();

        if (!mIsInited) {
            if (!mIsIniting) {
                mIsIniting = true;
                final long now = System.currentTimeMillis();
                new Thread() {
                    public void run() {
                        List<Project> projects = DataHelper.initCalendar(MainActivity.this);
                        if (projects != null && projects.size() > 0) {
                            mProjects.addAll(projects);
                        }
                        setupDailyData();
                        long cur = System.currentTimeMillis();
                        if (cur > (now + 2000)) {
                            mHandler.sendEmptyMessage(10);
                        } else {
                            mHandler.sendEmptyMessageDelayed(10, now+2000-cur);
                        }
                    }
                }.start();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mBaseFragment == null) {
            menu.findItem(R.id.action_sort_date).setVisible(false);
            menu.findItem(R.id.action_sort_project).setVisible(false);
            menu.findItem(R.id.action_import_all).setVisible(false);
            menu.findItem(R.id.action_export_all).setVisible(false);
        } else {
            if (mBaseFragment.isDailyEnable()) {
                menu.findItem(R.id.action_sort_date).setVisible(false);
                menu.findItem(R.id.action_sort_project).setVisible(true);
            } else if (mBaseFragment.isProjectEnable()) {
                menu.findItem(R.id.action_sort_date).setVisible(true);
                menu.findItem(R.id.action_sort_project).setVisible(false);
            } else {
                menu.findItem(R.id.action_sort_date).setVisible(false);
                menu.findItem(R.id.action_sort_project).setVisible(false);
            }
            menu.findItem(R.id.action_import_all).setVisible(true);
            menu.findItem(R.id.action_export_all).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_import_all) {
            if (mBaseFragment != null) {
                mBaseFragment.importAllToCalendar();
            }
            return true;
        }
        if (item.getItemId() == R.id.action_export_all) {
            if (mBaseFragment != null) {
                mBaseFragment.exportAllToCalendar();
            }
            return true;
        }
        if (item.getItemId() == R.id.action_sort_date) {
            mIsSortByDate = true;
            setDailyListFragment();
            return true;
        }
        if (item.getItemId() == R.id.action_sort_project) {
            mIsSortByDate = false;
            setProjectListFragment();
            return true;
        }
        if (item.getItemId() == R.id.about) {
            displayAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setCurrentFragment(BaseFragment fragment) {
        mBaseFragment = fragment;
        getWindow().invalidatePanelMenu(Window.FEATURE_OPTIONS_PANEL);
    }

    private void setDefaultFragment() {
        setFragment(new FragmentLoading(), false);
    }

    private void setDailyListFragment() {
        DailyFragment frag = DailyFragment.newInstance(mDailies);
        frag.setOnDailyItemClickListener(new DailyFragment.OnDailyItemClickListener() {
            @Override
            public void onDailyItemClick(int position, Daily daily) {
                setMatchListFragment(daily);
            }
        });
        setFragment(frag, false);
    }

    private void setProjectListFragment() {
        ProjectFragment frag = ProjectFragment.newInstance(mProjects);
        frag.setOnProjectItemClickListener(new ProjectFragment.OnProjectItemClickListener() {
            @Override
            public void onProjectItemClick(int position, Project project) {
                setMatchListFragment(project);
            }
        });
        setFragment(frag, false);
    }

    private void setMatchListFragment(Project project) {
        MatchFragment frag = MatchFragment.newInstance(project.Matches, project.Title);
        setFragment(frag, true);
    }

    private void setMatchListFragment(Daily daily) {
        MatchFragment frag = MatchFragment.newInstance(daily.Matches, daily.Day);
        setFragment(frag, true);
    }

    private void setFragment(Fragment fragment, boolean addBack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        // 用这个fragment替换任何在fragment_container中的东西
        // 并添加事务到back stack中以便用户可以回退到之前的状态
        transaction.replace(R.id.contentPanel, fragment);
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.fade_in, android.R.anim.fade_out);
        if (addBack) {
            transaction.addToBackStack(null);
        }

        // 提交事务
        transaction.commit();
    }

    private void displayAbout() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.setTitle(R.string.app_name);
        dlg.setMessage("巴西里约奥运会日历，支持导入到手机日历（需要日历账号），或者导出为iCalendar格式的文件并存放在手机存储器的根目录下。");
        dlg.setButton(AlertDialog.BUTTON_NEGATIVE, "确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dlg.cancel();
            }
        });
        dlg.show();
    }

    private void setupDailyData() {
        if (mProjects.size() == 0) {
            return;
        }

        for (Project project : mProjects) {
            if (project.Matches != null && project.Matches.size() > 0) {
                for (MatchItem match : project.Matches) {
                    Daily daily = null;
                    for (Daily d : mDailies) {
                        if (d.Day.equals(match.Date)) {
                            daily = d;
                            break;
                        }
                    }
                    if (daily == null) {
                        daily = new Daily();
                        daily.Matches = new ArrayList<>();
                        daily.Day = match.Date;
                        mDailies.add(daily);
                    }
                    daily.Matches.add(match);
                }
            }
        }

        Collections.sort(mDailies);
        for (Daily d : mDailies) {
            Collections.sort(d.Matches);
        }
    }
}
