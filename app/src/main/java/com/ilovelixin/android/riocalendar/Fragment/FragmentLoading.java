package com.ilovelixin.android.riocalendar.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ilovelixin.android.riocalendar.R;

public class FragmentLoading extends Fragment {
    private View mRootView;

    public FragmentLoading() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_loading, container, false);
        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void startAnim(){
        mRootView.findViewById(R.id.avloadingIndicatorView).setVisibility(View.VISIBLE);
    }

    private void stopAnim(){
        mRootView.findViewById(R.id.avloadingIndicatorView).setVisibility(View.GONE);
    }
}
