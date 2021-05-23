package com.giligans.queueapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.giligans.queueapp.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TabViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    Context context;

    public TabViewPagerAdapter(FragmentManager manager, Context context) {
        super(manager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    public void SetOnSelectView(TabLayout tabLayout, int position) {
        TabLayout.Tab currrentTab = tabLayout.getTabAt(position);
        View selected = currrentTab.getCustomView();
        TextView title = (TextView) selected.findViewById(R.id.title);
        title.setTextColor(context.getResources().getColor(R.color.white));
    }

    public void SetUnSelectView(TabLayout tabLayout,int position) {
        TabLayout.Tab currrentTab = tabLayout.getTabAt(position);
        View selected = currrentTab.getCustomView();
        TextView title = (TextView) selected.findViewById(R.id.title);

        TypedValue typedValue = new TypedValue();
        ((Activity)context).getTheme().resolveAttribute(R.attr.categoryText, typedValue, true);
        title.setTextColor(typedValue.data);
    }
}