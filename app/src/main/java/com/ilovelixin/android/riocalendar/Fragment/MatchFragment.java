package com.ilovelixin.android.riocalendar.Fragment;


import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ilovelixin.android.riocalendar.MainActivity;
import com.ilovelixin.android.riocalendar.Model.MatchItem;
import com.ilovelixin.android.riocalendar.R;
import com.ilovelixin.android.riocalendar.Tool.CalendarHelper;
import com.ilovelixin.android.riocalendar.Tool.ICalendarHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MatchFragment extends BaseFragment implements View.OnTouchListener, View.OnClickListener {
    private final static String TAG = "MatchFragment";

    private final static int NORMAL_MODE = 0;
    private final static int SELECT_MODE = 1;

    private View mRootView;
    private ListView mListView;
    private RelativeLayout mActLayout;
    private LinearLayout mImportLayout;
    private ImageView mImportIcon;
    private LinearLayout mExportLayout;
    private ImageView mExportIcon;
    private View mMultiSelectActionBarView;
    private Button mBtnSelectAll;
    private TextView mSelectedCount;

    private String mTitle;
    private List<MatchItem> mMatches;
    private MatchAdapter mAdapter;
    private OnMatchItemClickListener mListener;
    private int mMode;
    private boolean mIsTouched;
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

                case 12:
                    mIsBusy = false;
                    Toast.makeText(getContext(), "导入到日历已完成", Toast.LENGTH_LONG).show();
                    for(int i= 0; i< mAdapter.getCount(); i++){
                        mListView.setItemChecked(i, false);
                    }
                    break;
            }
        }
    };

    public MatchFragment() {
        mMatches = new ArrayList<>();
        mMode = NORMAL_MODE;
        mIsTouched = false;
    }

    public static MatchFragment newInstance(List<MatchItem> matches, String title) {
        MatchFragment fragment = new MatchFragment();
        fragment.setMatches(matches, title);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_match, container, false);
            mAdapter = new MatchAdapter();
            mListView = (ListView) mRootView.findViewById(R.id.list);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mListener != null) {
                        mListener.onMatchItemClick(position, mMatches.get(position));
                    }
                }
            });
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    Log.v(TAG, "onItemCheckedStateChanged: mode=" + mode + ", position=" + position + ", checked=" + checked);
                    mAdapter.notifyDataSetChanged();
                    if (mSelectedCount != null) {
                        mSelectedCount.setText(mListView.getCheckedItemCount() + "项已选择");
                    }
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    Log.v(TAG, "onCreateActionMode: mode=" + mode);
                    mMode = SELECT_MODE;
                    mActLayout.setVisibility(View.VISIBLE);
                    if (mMultiSelectActionBarView == null) {
                        createMultiSelectView();
                    }
                    mode.setCustomView(mMultiSelectActionBarView);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    Log.v(TAG, "onPrepareActionMode: mode=" + mode);
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    Log.v(TAG, "onActionItemClicked: mode=" + mode);
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    Log.v(TAG, "onDestroyActionMode: mode=" + mode);
                    mMode = NORMAL_MODE;
                    mActLayout.setVisibility(View.GONE);
                    mListView.clearChoices();
                }
            });
            mListView.clearChoices();

            mActLayout = (RelativeLayout) mRootView.findViewById(R.id.actLayout);
            mActLayout.setVisibility(View.GONE);

            mImportLayout = (LinearLayout) mRootView.findViewById(R.id.importLayout);
            mImportLayout.setOnTouchListener(this);
            mImportIcon = (ImageView) mRootView.findViewById(R.id.importIcon);

            mExportLayout = (LinearLayout) mRootView.findViewById(R.id.exportLayout);
            mExportLayout.setOnTouchListener(this);
            mExportIcon = (ImageView) mRootView.findViewById(R.id.exportIcon);
        }
        return mRootView;
    }

    public void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();

        MainActivity activity = (MainActivity)getActivity();
        activity.setTitle(mTitle);
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
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.importLayout || id == R.id.exportLayout) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setIsTouched(true, v.getId());
                    break;

                case MotionEvent.ACTION_UP:
                    if (mIsTouched) {
                        setIsTouched(false, id);
                        if (id == R.id.importLayout) {
                            doImport();
                        } else {
                            doExport();
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mIsTouched) {
                        final float x = event.getX();
                        final float y = event.getY();
                        final float width = v.getWidth();
                        final float height = v.getHeight();
                        if (!pointInView(x, y, width, height, 0)) {
                            setIsTouched(false, id);
                        }
                    }
                    break;

                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_CANCEL:
                    if (mIsTouched) {
                        setIsTouched(false, id);
                    }
                    return true;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSelectAll:
                for(int i= 0; i< mAdapter.getCount(); i++){
                    mListView.setItemChecked(i, true);
                }
                break;
        }
    }

    @Override
    public boolean isProjectEnable() {
        return false;
    }

    @Override
    public boolean isDailyEnable() {
        return false;
    }

    @Override
    public void importAllToCalendar() {
        if (mIsBusy) {
            return;
        }

        new Thread() {
            public void run() {
                mIsBusy = true;
                for (MatchItem match : mMatches) {
                    if (!CalendarHelper.getInstance().addReminder(getContext(), match)) {
                        break;
                    }
                }
                mHandler.sendEmptyMessage(10);
            }
        }.start();
    }

    @Override
    public void exportAllToCalendar() {
        if (mIsBusy) {
            return;
        }

        new Thread() {
            public void run() {
                mIsBusy = true;
                ICalendarHelper.generateICalendar(getContext(), mMatches);
                mHandler.sendEmptyMessage(11);
            }
        }.start();
    }

    public void setMatches(List<MatchItem> matches, String title) {
        mTitle = title;
        if (mMatches == null) {
            mMatches = new ArrayList<>();
        } else {
            mMatches.clear();
        }
        mMatches.addAll(matches);
    }

    public void setOnMatchItemClickListener(OnMatchItemClickListener l) {
        mListener = l;
    }

    private void doImport() {
        if (mIsBusy) {
            Toast.makeText(getContext(), "Ops！还在忙着别的工作哦！", Toast.LENGTH_LONG).show();
            return;
        }

        if (mListView.getCheckedItemCount() == 0) {
            Toast.makeText(getContext(), "请先选中几项吧！", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread() {
            public void run() {
                mIsBusy = true;
                for (int i = 0; i < mMatches.size(); i++) {
                    if (mListView.isItemChecked(i)) {
                        if (!CalendarHelper.getInstance().addReminder(getContext(), mMatches.get(i))) {
                            break;
                        }
                    }
                }
                mHandler.sendEmptyMessage(12);
            }
        }.start();
    }

    private void doExport() {
        if (mIsBusy) {
            Toast.makeText(getContext(), "Ops！还在忙着别的工作哦！", Toast.LENGTH_LONG).show();
            return;
        }

        if (mListView.getCheckedItemCount() == 0) {
            Toast.makeText(getContext(), "请先选中几项吧！", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread() {
            public void run() {
                mIsBusy = true;
                List<MatchItem> matches = new ArrayList<>();
                for (int i = 0; i < mMatches.size(); i++) {
                    if (mListView.isItemChecked(i)) {
                        matches.add( mMatches.get(i));
                    }
                }
                ICalendarHelper.generateICalendar(getContext(), matches);
                mHandler.sendEmptyMessage(11);
            }
        }.start();
    }

    private void createMultiSelectView() {
        mMultiSelectActionBarView = LayoutInflater.from(getContext()).inflate(R.layout.multi_select_action_mode, null);
        mSelectedCount = (TextView) mMultiSelectActionBarView.findViewById(R.id.selectHint);
        mSelectedCount.setText(mListView.getCheckedItemCount() + "项已选择");
        mBtnSelectAll = (Button) mMultiSelectActionBarView.findViewById(R.id.btnSelectAll);
        mBtnSelectAll.setOnClickListener(this);
    }

    private boolean pointInView(float localX, float localY, float width, float height, float slop) {
        return localX >= -slop && localY >= -slop && localX < (width + slop) &&  localY < (height + slop);
    }

    private void setIsTouched(boolean touched, int id) {
        if (touched) {
            mIsTouched = true;
            if (id == R.id.importLayout) {
                mImportIcon.setImageResource(R.drawable.ic_import_pressed);
            } else {
                mExportIcon.setImageResource(R.drawable.ic_export_pressed);
            }
        } else {
            mIsTouched = false;
            if (id == R.id.importLayout) {
                mImportIcon.setImageResource(R.drawable.ic_import_normal);
            } else {
                mExportIcon.setImageResource(R.drawable.ic_export_normal);
            }
        }
    }

    private class MatchAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMatches.size();
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
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
                holder.TitleView = (TextView)convertView.findViewById(R.id.title);
                holder.DateView = (TextView)convertView.findViewById(R.id.date);
                holder.RoundView = (TextView)convertView.findViewById(R.id.round);
                holder.CheckedImage = (ImageView)convertView.findViewById(R.id.checked);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.TitleView.setText(mMatches.get(position).Match);
            holder.DateView.setText(mMatches.get(position).Date + " " + mMatches.get(position).Time);
            holder.RoundView.setText(mMatches.get(position).Round);
            if (mMode == NORMAL_MODE) {
                holder.CheckedImage.setVisibility(View.GONE);
            } else {
                holder.CheckedImage.setVisibility(View.VISIBLE);
                if (mListView.isItemChecked(position)) {
                    holder.CheckedImage.setImageResource(R.drawable.ic_check_selected);
                } else {
                    holder.CheckedImage.setImageResource(R.drawable.ic_check);
                }
            }

            return convertView;
        }

        public class ViewHolder {
            public TextView TitleView;
            public TextView DateView;
            public TextView RoundView;
            public ImageView CheckedImage;
        }
    }

    public interface OnMatchItemClickListener{
        public void onMatchItemClick(int position, MatchItem match);
    }
}
