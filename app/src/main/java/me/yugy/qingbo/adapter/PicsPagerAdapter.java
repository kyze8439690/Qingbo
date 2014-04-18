package me.yugy.qingbo.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import me.yugy.qingbo.fragment.PicFragment;

/**
 * Created by yugy on 2014/4/18.
 */
public class PicsPagerAdapter extends FragmentStatePagerAdapter{

    private ArrayList<String> mPics;

    public PicsPagerAdapter(FragmentManager fm, ArrayList<String> pics) {
        super(fm);
        mPics = pics;
    }

    @Override
    public Fragment getItem(int position) {
        return new PicFragment(mPics.get(position).replace("thumbnail", "large"));
    }

    @Override
    public int getCount() {
        return mPics.size();
    }
}
