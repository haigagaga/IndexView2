package com.example.gengchunjiang.indexview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by AnthonyJ on 2018/5/7.
 */

public class SlideView extends View {

    //自定义view的宽和高
    private int mViewWidth,mViewHeight;
    //判断是否按下（按下会有高亮）
    private boolean mTouched;
    //字母数组
    private String[] mIndex = {"☆","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","#"};
    private Paint mPaint;
    private float mTextSize ;
    //测量字体的大小  Rect矩形
    private Rect mTextBound ;
    /*回调选择的索引*/
    private OnIndexSelectListener listener ;
    //窗口，浮动view的容器，比Activity的显示更高一级
    private WindowManager mWindowManager;
    //用于显示浮动的字体 类似Toast
    private View mFloatView;
    //悬浮view的高、宽
    private int mFloatWidth,mFloatHeight;


    public SlideView(Context context) {
        super(context);
        initView();
    }

    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SlideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        //初始化
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextBound = new Rect();
        //设置浮动选中的索引
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mFloatView = LayoutInflater.from(getContext()).inflate(R.layout.overlay_indexview,null);
        mFloatView.setVisibility(INVISIBLE);
        mFloatWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,70,getResources().getDisplayMetrics());
        mFloatHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,70,getResources().getDisplayMetrics());
        //view.post其实内部是获取到了view所在线程（即ui线程）的handler，并且调用了handler的post方法
        post(new Runnable() {
            @Override
            public void run() {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(mFloatWidth,mFloatHeight,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                mWindowManager.addView(mFloatView,layoutParams);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),measurHeight(heightMeasureSpec));
    }

    /**
     * 测量本身的大小，这里只是测量宽度
     * @param widthMeaSpec 传入父View的测量标准
     * @return 测量的宽度
     */
    private int measureWidth(int widthMeaSpec) {
        //定义view的宽度
        int width;
        //获取当前view的测量模式
        int mode = MeasureSpec.getMode(widthMeaSpec);
        /**
         * 获取当前view的测量值，这里得到的只是初步的值
         * 我们还需根据测量模式来确定我们期望的大小
         */
        int size = MeasureSpec.getSize(widthMeaSpec);
        //如果模式为精确模式，当前view的宽度就是我们的size
        if (mode == MeasureSpec.EXACTLY){
            width = size;
        }else{
            //否则需要结合padding的值来确定
            int desire = size + getPaddingLeft()+getPaddingRight();
            if (mode == MeasureSpec.AT_MOST){
                width = Math.min(desire,size);
            }else{
                width = desire;
            }
        }
        mViewWidth = width;
        return width;
    }
    private int measurHeight(int heightMeaSpec) {
        int height;
        int mode = MeasureSpec.getMode(heightMeaSpec);
        int size = MeasureSpec.getSize(heightMeaSpec);
        if (mode == MeasureSpec.EXACTLY){
            height = size;
        }else{
            //否则需要结合padding的值来确定
            int desire = size + getPaddingTop() + getPaddingBottom();
            if (mode == MeasureSpec.AT_MOST){
                height = Math.min(desire,size);
            }else{
                height = desire;
            }
        }
        mViewHeight = height;
        mTextSize = mViewHeight*1.0f/mIndex.length ;
        return height;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mTouched){
            canvas.drawColor(0x30000000);
        }
        for (int i = 0; i < mIndex.length; i++) {
            mPaint.setColor(0xff000000);
            mPaint.setTextSize(mTextSize*3.0f/4.0f);
            mPaint.setTypeface(Typeface.DEFAULT);
            mPaint.getTextBounds(mIndex[i],0,mIndex[i].length(),mTextBound);
            float formX = mViewWidth/2.0f - mTextBound.width()/2.0f;
            float formY = mTextSize*i + mTextSize/2.0f + mTextBound.height()/2.0f;
            canvas.drawText(mIndex[i],formX,formY,mPaint);
            mPaint.reset();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        int index = (int) (y/mTextSize);
        if (index >= 0 && index<mIndex.length){
            Log.d("ant","======index======="+index);
            selectItem(index);
        }
        switch (event.getAction()){ 
            case MotionEvent.ACTION_DOWN:
                mTouched = true;
                break;
            case MotionEvent.ACTION_MOVE:
                mTouched = true;
                break;
            case MotionEvent.ACTION_UP:
                mFloatView.setVisibility(INVISIBLE);
                mTouched = false;
                break;
        }
        invalidate();
        return true;

    }

    private void selectItem(int position){
        mFloatView.setVisibility(VISIBLE);
        ((TextView)mFloatView).setText(mIndex[position]);
        if(listener!=null){
            listener.onItemSelect(position,mIndex[position]);
        }
    }



    /*定义一个回调接口*/
    public interface OnIndexSelectListener{
        /*返回选中的位置，和对应的索引名*/
        void  onItemSelect(int position, String value) ;
    }

    public void setListener(OnIndexSelectListener listener) {
        this.listener = listener;
    }
}
