package me.yugy.qingbo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import me.yugy.qingbo.R;

public class MainActivity extends Activity {

    private static final int REQUEST_LOGIN = 10086;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!PreferenceManager.getDefaultSharedPreferences(this).contains("uid")){
            startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
        }else{
            startActivity(new Intent(this, TimelineActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_LOGIN && resultCode == RESULT_OK){
            startActivity(new Intent(this, TimelineActivity.class));
            finish();
        }
    }
}
