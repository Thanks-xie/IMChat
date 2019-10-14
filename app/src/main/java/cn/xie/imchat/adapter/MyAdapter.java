package cn.xie.imchat.adapter;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


/**
 * on 2017/5/8.
 * 类的描述:
 */

public class MyAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> mFragments;
    public MyAdapter(FragmentManager fm , ArrayList<Fragment> fragments) {
        super(fm);
        mFragments=fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
