/*
 * Copyright 2012 Evgeny Shishkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.yugy.qingbo.view.message;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import me.yugy.qingbo.R;
import me.yugy.qingbo.utils.ScreenUtils;

/**
 * In-layout notifications. Based on {@link android.widget.Toast} notifications
 * and article by Cyril Mottier (http://android.cyrilmottier.com/?p=773).
 *
 * @author e.shishkin
 */
public class AppMsg {

    /**
     * Show the view or text notification for a short period of time. This time
     * could be user-definable. This is the default.
     *
     * @see #setDuration
     */
    public static final int DURATION = 1500;

    private final Context mContext;
    private int mDuration = DURATION;
    private View mView;
    private LayoutParams mLayoutParams;
    private boolean mFloating;

    /**
     * Construct an empty AppMsg object. You must call {@link #setView} before
     * you can call {@link #show}.
     *
     * @param context The context to use. Usually your
     *                {@link android.app.Activity} object.
     */
    public AppMsg(Context context) {
        mContext = context;
    }

    /**
     * Make a {@link AppMsg} that just contains a text view.
     *
     * @param context The context to use. Usually your
     *                {@link android.app.Activity} object.
     * @param text    The text to show. Can be formatted text.
     */
    public static AppMsg makeText(Context context, CharSequence text) {
        return makeText(context, text, R.layout.app_msg);
    }

    /**
     * @author mengguoqiang 扩展支持设置字体大小
     * Make a {@link AppMsg} that just contains a text view.
     *
     * @param context The context to use. Usually your
     *                {@link android.app.Activity} object.
     * @param text    The text to show. Can be formatted text.
     */
    public static AppMsg makeText(Context context, CharSequence text, float textSize) {
        return makeText(context, text, R.layout.app_msg, textSize);
    }

    /**
     * Make a {@link AppMsg} with a custom layout.
     *
     * @param context The context to use. Usually your
     *                {@link android.app.Activity} object.
     * @param text    The text to show. Can be formatted text.
     */
    public static AppMsg makeText(Context context, CharSequence text, int layoutId) {
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(layoutId, null);

        return makeText(context, text, v, true);
    }

    /**
     * @author mengguoqiang 扩展支持字体大小
     * Make a {@link AppMsg} with a custom layout.
     *
     * @param context The context to use. Usually your
     *                {@link android.app.Activity} object.
     * @param text    The text to show. Can be formatted text.
     */
    public static AppMsg makeText(Context context, CharSequence text, int layoutId, float textSize) {
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(layoutId, null);

        return makeText(context, text, v, true, textSize);
    }

    /**
     * Make a non-floating {@link AppMsg} with a custom view presented inside the layout.
     * It can be used to create non-floating notifications if floating is false.
     *
     * @param context  The context to use. Usually your
     *                 {@link android.app.Activity} object.
     * @param customView
     *                 View to be used.
     * @param text     The text to show. Can be formatted text.
     */
    public static AppMsg makeText(Context context, CharSequence text, View customView) {
        return makeText(context, text, customView, false);
    }

    /**
     * Make a {@link AppMsg} with a custom view. It can be used to create non-floating notifications if floating is false.
     *
     * @param context  The context to use. Usually your
     *                 {@link android.app.Activity} object.
     * @param view
     *                 View to be used.
     * @param text     The text to show. Can be formatted text.
     * @param floating true if it'll float.
     */
    private static AppMsg makeText(Context context, CharSequence text, View view, boolean floating) {
        return makeText(context, text, view, floating, 0);
    }

    /**
     *
     * @author mengguoqiang 扩展支持设置字体大小
     * Make a {@link AppMsg} with a custom view. It can be used to create non-floating notifications if floating is false.
     *
     * @param context  The context to use. Usually your
     *                 {@link android.app.Activity} object.
     * @param view
     *                 View to be used.
     * @param text     The text to show. Can be formatted text.
     * @param floating true if it'll float.
     */
    private static AppMsg makeText(Context context, CharSequence text, View view, boolean floating, float textSize) {
        AppMsg result = new AppMsg(context);

        TextView tv = (TextView) view.findViewById(android.R.id.message);
        if(textSize > 0) tv.setTextSize(textSize);
        tv.setText(text);

        result.mView = view;
        result.mDuration = DURATION;
        result.mFloating = floating;

        return result;
    }

    /**
     * Make a {@link AppMsg} with a custom view. It can be used to create non-floating notifications if floating is false.
     *
     * @param context  The context to use. Usually your
     *                 {@link android.app.Activity} object.
     * @param resId    The resource id of the string resource to use. Can be
     *                 formatted text.
     * @param floating true if it'll float.
     */
    public static AppMsg makeText(Context context, int resId, View customView, boolean floating) {
        return makeText(context, context.getResources().getText(resId), customView, floating);
    }

    /**
     * Make a {@link AppMsg} that just contains a text view with the text from a
     * resource.
     *
     * @param context The context to use. Usually your
     *                {@link android.app.Activity} object.
     * @param resId   The resource id of the string resource to use. Can be
     *                formatted text.
     * @throws android.content.res.Resources.NotFoundException if the resource can't be found.
     */
    public static AppMsg makeText(Context context, int resId)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId));
    }

    /**
     * Make a {@link AppMsg} with a custom layout using the text from a
     * resource.
     *
     * @param context The context to use. Usually your
     *                {@link android.app.Activity} object.
     * @param resId   The resource id of the string resource to use. Can be
     *                formatted text.
     * @throws android.content.res.Resources.NotFoundException if the resource can't be found.
     */
    public static AppMsg makeText(Context context, int resId, int layoutId)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId), layoutId);
    }

    /**
     * Show the view for the specified duration.
     */
    public void show() {
        MsgManager manager = MsgManager.getInstance();
        manager.add(this);
    }

    /**
     * @return <code>true</code> if the {@link AppMsg} is being displayed, else <code>false</code>.
     */
    public boolean isShowing() {
        if (mFloating) {
            return mView != null && mView.getParent() != null;
        } else {
            return mView.getVisibility() == View.VISIBLE;
        }
    }

    /**
     * Close the view if it's showing, or don't show it if it isn't showing yet.
     * You do not normally have to call this.  Normally view will disappear on its own
     * after the appropriate duration.
     */
    public void cancel() {
        MsgManager.getInstance().clearMsg(this);

    }

    /**
     * Cancels all queued {@link AppMsg}s. If there is a {@link AppMsg}
     * displayed currently, it will be the last one displayed.
     */
    public static void cancelAll() {
        MsgManager.getInstance().clearAllMsg();
    }

    /**
     * Return the activity.
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Set the view to show.
     *
     * @see #getView
     */
    public void setView(View view) {
        mView = view;
    }

    /**
     * Return the view.
     *
     * @see #setView
     */
    public View getView() {
        return mView;
    }

    /**
     * Set how long to show the view for.
     *
     * @see #DURATION
     */
    public void setDuration(int duration) {
        mDuration = duration;
    }

    /**
     * Return the duration.
     *
     * @see #setDuration
     */
    public int getDuration() {
        return mDuration;
    }

    /**
     * Update the text in a AppMsg that was previously created using one of the makeText() methods.
     *
     * @param resId The new text for the AppMsg.
     */
    public void setText(int resId) {
        setText(mContext.getText(resId));
    }

    /**
     * Update the text in a AppMsg that was previously created using one of the makeText() methods.
     *
     * @param s The new text for the AppMsg.
     */
    public void setText(CharSequence s) {
        if (mView == null) {
            throw new RuntimeException("This AppMsg was not created with AppMsg.makeText()");
        }
        TextView tv = (TextView) mView.findViewById(android.R.id.message);
        if (tv == null) {
            throw new RuntimeException("This AppMsg was not created with AppMsg.makeText()");
        }
        tv.setText(s);
    }

    /**
     * Gets the crouton's layout parameters, constructing a default if necessary.
     *
     * @return the layout parameters
     */
    public LayoutParams getLayoutParams() {
        if (mLayoutParams == null) {
            mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
            mLayoutParams.topMargin = ScreenUtils.dp(getContext(), 8);
        }
        return mLayoutParams;
    }

    /**
     * Sets the layout parameters which will be used to display the crouton.
     *
     * @param layoutParams The layout parameters to use.
     * @return <code>this</code>, for chaining.
     */
    public AppMsg setLayoutParams(LayoutParams layoutParams) {
        mLayoutParams = layoutParams;
        return this;
    }

    /**
     * Constructs and sets the layout parameters to have some gravity.
     *
     * @param gravity the gravity of the Crouton
     * @return <code>this</code>, for chaining.
     * @see android.view.Gravity
     */
    public AppMsg setLayoutGravity(int gravity) {
        mLayoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, gravity);
        return this;
    }

    /**
     * Return the value of floating.
     *
     * @see #setFloating(boolean)
     */
    public boolean isFloating() {
        return mFloating;
    }

    /**
     * Sets the value of floating.
     *
     * @param mFloating
     */
    public void setFloating(boolean mFloating) {
        this.mFloating = mFloating;
    }

}