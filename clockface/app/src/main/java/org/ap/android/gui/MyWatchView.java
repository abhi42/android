package org.ap.android.gui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by abhi on 26.07.14.
 */
public class MyWatchView extends View {

    private final Context mContext;
    private ShapeDrawable mShapeDrawable;
    private int[] mCenter;
    private boolean mIsAttached = false;
    private MyWatchBroadcastReceiver mIntentReceiver;
    private Time mTime;
    private final int mRadiusInPixels;
    private float[] mSecHandPosition;
    private final Paint mSecHandPaint;
    private float[] mMinuteHandPosition;
    private final Paint mMinHandPaint;
    private float[] mHourHandPosition;
    private final Paint mHourHandPaint;
    private final Handler mHandler = new Handler();
    private SecondsHandTrigger mTrigger;
    private Thread mTriggerThread;
    private boolean mReceivedSystemBroadcast = true; // to display the hr and minute hands the first time

    //  px = dp * (dpi / 160)
    private static final int RADIUS_IN_DP = 150;
    private static final int THICKNESS_OF_HR_HAND_IN_DP = 3;
    private static final int THICKNESS_OF_MIN_HAND_IN_DP = 2;
    private static final int THICKNESS_OF_SEC_HAND_IN_DP = 1;

    private static final String TAG = MyWatchView.class.getName();

    /**
     * Constructor when the view class is instantiated through code.
     * @param context the context
     */
    public MyWatchView(Context context) {
        super(context);
        this.mContext = context;
        this.mRadiusInPixels = getValueInPixels(RADIUS_IN_DP);
        this.mSecHandPaint = new Paint();
        this.mMinHandPaint = new Paint();
        this.mHourHandPaint = new Paint();
        initPaint();
        drawWatchFace();
        mTime = new Time();
    }

    private void initPaint() {
        if (mSecHandPaint != null) {
            mSecHandPaint.setColor(Color.BLUE);
            mSecHandPaint.setStrokeWidth(getValueInPixels(THICKNESS_OF_SEC_HAND_IN_DP));
        }

        if (mMinHandPaint != null) {
            mMinHandPaint.setColor(Color.BLACK);
            mMinHandPaint.setStrokeWidth(getValueInPixels(THICKNESS_OF_MIN_HAND_IN_DP));
        }

        if (mHourHandPaint != null) {
            mHourHandPaint.setColor(Color.RED);
            mHourHandPaint.setStrokeWidth(getValueInPixels(THICKNESS_OF_HR_HAND_IN_DP));
        }
    }

    /**
     * This constructor is called when the XML definition is inflated.
     * @param context the context
     * @param attrs attributes defined in the XML
     */
    public MyWatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mRadiusInPixels = getValueInPixels(RADIUS_IN_DP);
        this.mSecHandPaint = new Paint();
        this.mMinHandPaint = new Paint();
        this.mHourHandPaint = new Paint();
        initPaint();
        drawWatchFace();
        mTime = new Time();
    }

    private void drawWatchFace() {
        mTime = new Time();
        final OvalShape ovalShape = new OvalShape();
        mShapeDrawable = new ShapeDrawable(ovalShape);


        // set the bounds in onMeasure, that is when the total height and width become available
    }

    private int[] getCenter() {
        final int availableWidth = getWidth();
        final int availableHeight = getHeight();

        Log.d(TAG, "getWidth: " + availableWidth + ", getHeight: " + availableHeight);
        return new int[] {availableWidth / 2, availableHeight / 2};
    }

    private int getValueInPixels(final int radiusInDip) {
        // as per http://developer.android.com/guide/practices/screens_support.html
        // : Converting dp units to pixel units

        // get the screen's density scale i.e. (factor to convert dps to pixels)
        final float scaleFactor = mContext.getResources().getDisplayMetrics().density;
        final int radiusInPixels = (int) (radiusInDip * scaleFactor + 0.5f);

        Log.d(TAG, "scale factor: " + scaleFactor + ", radiusInPixels: " + radiusInPixels);
        return radiusInPixels;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mShapeDrawable.draw(canvas);
        drawHands(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mShapeDrawable.getPaint().setColor(Color.LTGRAY);

        mCenter = getCenter();
        mShapeDrawable.setBounds(mCenter[0] - mRadiusInPixels, mCenter[1] - mRadiusInPixels,
                mCenter[0] + mRadiusInPixels, mCenter[1] + mRadiusInPixels);

//        setHandsPositions();
        // necessary to call super method or setMeasuredDimension(..) in this method.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void drawHands(Canvas canvas) {
        setHandsPositions();
        drawHourHand(canvas);
        drawMinuteHand(canvas);
        drawSecondHand(canvas);
        mReceivedSystemBroadcast = false;
    }

    private void drawHourHand(Canvas canvas) {
        canvas.drawLine((float) mCenter[0], (float) mCenter[1],
                mHourHandPosition[0], mHourHandPosition[1], mHourHandPaint);
    }

    private void drawMinuteHand(Canvas canvas) {
        canvas.drawLine((float) mCenter[0], (float) mCenter[1],
                mMinuteHandPosition[0], mMinuteHandPosition[1], mMinHandPaint);
    }

    private void drawSecondHand(Canvas canvas) {
       canvas.drawLine((float) mCenter[0], (float) mCenter[1],
               mSecHandPosition[0], mSecHandPosition[1], mSecHandPaint);
    }

    private float[] convertTimeValueToHandPosition(final int timeValue, final float lengthFactor) {
        /*
         *      Seconds     Angle between seconds/min = 0 position and seconds/min hand
         *      0            0
         *      15           90
         *      30          180
         *      45          270
         *  Hence: 1 second = 6 degrees
         *  sin(angle) = x-coord/radius
         *  cos(angle) = y-coord/radius
         *
         *  Therefore: x = radius * sin(angleInRad)
         *             y = radius * -cos(angleInRad)
         */
        final int angle = timeValue * 6;
        final double angleInRadians = Math.toRadians(angle);
        final double x = mCenter[0] + (lengthFactor * (mRadiusInPixels * Math.sin((double) angleInRadians)));
        final double y = mCenter[1] + (lengthFactor * (mRadiusInPixels * -Math.cos((double) angleInRadians)));

//        final double angleInRadians = Math.toRadians(90 - angle);
//        final double x = mCenter[0] + (mRadiusInPixels * Math.cos((double) angleInRadians));
//        final double y = mCenter[1] + (mRadiusInPixels * Math.sin((double) angleInRadians));

        Log.i(TAG, "time value: " + timeValue + ", angle: " + angle +
                ", sin(" + angle + "): " + Math.sin(angleInRadians) +
                ", cos(" + angle + "): " + Math.cos(angleInRadians));

        return new float [] {(float) x, (float)y};
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mIsAttached) {
            mIsAttached = true;
            registerListener();
        }
        setNewTime();
        mTrigger = new SecondsHandTrigger(this);
        mTriggerThread = new Thread(mTrigger);
        mTriggerThread.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mIsAttached) {
            mIsAttached = false;
            unregisterListener();
        }
        mTriggerThread.interrupt();
    }

    private void unregisterListener() {
        getContext().unregisterReceiver(mIntentReceiver);
    }

    private void registerListener() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
//        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        mIntentReceiver = new MyWatchBroadcastReceiver();
        getContext().registerReceiver(mIntentReceiver, filter, null, mHandler);
    }

    private void setNewTime() {
        mTime.setToNow();
    }

    private void setHandsPositions() {
        if (mReceivedSystemBroadcast) {
            setMinutesHandPosition();
            setHourHandPosition();
        }
        setSecondsHandPosition();
    }

    private void setHourHandPosition() {
        /*
        0 hr; same position as 0 min
        3 hr: same position as 15 mins
        6 hr: same position as 30 mins
        9 hr: same position as 45 mins
         */
        mHourHandPosition = convertTimeValueToHandPosition(mTime.hour * 5, 0.65f);
    }

    private void setMinutesHandPosition() {
        mMinuteHandPosition = convertTimeValueToHandPosition(mTime.minute, 1);
    }

    private void setSecondsHandPosition() {
        mSecHandPosition = convertTimeValueToHandPosition(mTime.second, 1);
    }

    private void handleTimeEvent() {
        setNewTime();
        // redraw the view
        invalidate();
    }

    private final class MyWatchBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mReceivedSystemBroadcast = true;
            handleTimeEvent();
        }
    }

    private final class SecondsHandTrigger implements Runnable {

        private final MyWatchView mView;

        private SecondsHandTrigger(MyWatchView view) {
            this.mView = view;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                // run in UI thread using the post() API.
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mReceivedSystemBroadcast) {
                            /*
                            If the system broadcast event has taken place (i.e. a new minute),
                            then the watch will automatically refresh. So no need to handle this event
                            separately.
                            If this check is not present, the seconds hand freezes for 1 second at the minute changeover
                            when the system broadcast event is being handled.
                             */
                            handleTimeEvent();
                        }
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        }
    }
}
