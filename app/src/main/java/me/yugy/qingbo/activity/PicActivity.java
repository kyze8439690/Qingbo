package me.yugy.qingbo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.nostra13.universalimageloader.core.ImageLoader;

import me.yugy.qingbo.R;
import me.yugy.qingbo.adapter.PicsPagerAdapter;
import me.yugy.qingbo.view.DepthPageTransformer;

/**
 * Created by yugy on 2014/4/17.
 */
public class PicActivity extends Activity{

    private String[] mPics;
    
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);

        ImageLoader.getInstance().pause();

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                getActionBar().setTitle("查看图片(" + (position + 1) + "/" + mPics.length +")");
            }
        });

        mPics = getIntent().getStringArrayExtra("pics");

        mViewPager.setAdapter(new PicsPagerAdapter(getFragmentManager(), mPics));
        int position = getIntent().getIntExtra("position", 0);
        mViewPager.setCurrentItem(position);

        getActionBar().setTitle("查看图片(" + (position + 1) + "/" + mPics.length +")");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        ImageLoader.getInstance().resume();
        super.onDestroy();
    }
}
