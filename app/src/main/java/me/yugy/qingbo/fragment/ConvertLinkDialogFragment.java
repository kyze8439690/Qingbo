package me.yugy.qingbo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import me.yugy.qingbo.R;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/5/25.
 */
public class ConvertLinkDialogFragment extends DialogFragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_link_dialog, container, false);
//        return rootView;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_link_dialog, null);
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        Weibo.getShortUrl(getActivity(), "http://yanghui.name", new TextHttpResponseHandler(){
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, String responseBody) {
//                DebugUtils.log(responseBody);
//                super.onSuccess(statusCode, headers, responseBody);
//            }
//        });
    }
}
