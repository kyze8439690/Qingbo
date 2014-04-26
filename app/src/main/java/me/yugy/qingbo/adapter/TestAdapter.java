package me.yugy.qingbo.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * Created by yugy on 2014/4/26.
 */
public class TestAdapter extends ArrayAdapter<String>{
    public TestAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1, new String[]{
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        });
    }
}
