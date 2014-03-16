package com.whereone;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class CustomHorizontalScrollView extends HorizontalScrollView {
/*
    private static final int SWIPE_MIN_DISTANCE = 300;
	
	private static final int SWIPE_THRESHOLD_VELOCITY = 300;
	private static final int SWIPE_PAGE_ON_FACTOR = 10;
	
	
	private GestureDetector gestureDetector;
	private int scrollTo = 0;
	private int maxItem = 0;
	private int activeItem = 0;
	private float prevScrollX = 0;
	private boolean start = true;
	private int itemWidth = 0;
	private float currentScrollX;
	private boolean flingDisable = true;
	*/
	public CustomHorizontalScrollView(Context context) {
		super(context);
		//setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	/*
	public CustomHorizontalScrollView(Context context, int maxItem, int itemWidth) {
		super(context);
		//this.maxItem = maxItem;
		//this.itemWidth = itemWidth;
		//gestureDetector = new GestureDetector(context, this);
		//this.setOnTouchListener(this);
	}
	*/
	public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
	
	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                    Log.i("VerticalScrollview", "onInterceptTouchEvent: DOWN super false" );
                   // return true;
                    super.onTouchEvent(ev);
                    break;

            case MotionEvent.ACTION_MOVE:
                    return false; // redirect MotionEvents to ourself

            case MotionEvent.ACTION_CANCEL:
                    Log.i("VerticalScrollview", "onInterceptTouchEvent: CANCEL super false" );
                    super.onTouchEvent(ev);
                    break;

            case MotionEvent.ACTION_UP:
                    Log.i("VerticalScrollview", "onInterceptTouchEvent: UP super false" );
                    return false;

            default: Log.i("VerticalScrollview", "onInterceptTouchEvent: " + action ); break;
        }

        return false;
    }
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
        Log.i("VerticalScrollview", "onTouchEvent. action: " + event.getAction() );
        return true;
	}
	/*
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		if (gestureDetector.onTouchEvent(event)) {
			return true;
		}
		Boolean returnValue = gestureDetector.onTouchEvent(event);
		
		int x = (int) event.getRawX();
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				if (start) {
					this.prevScrollX = x;
					start = false;
				}
				break;
			case MotionEvent.ACTION_UP:
				start = true;
				this.currentScrollX = x;
				int minFactor = itemWidth / SWIPE_PAGE_ON_FACTOR;
			
				if ((this.prevScrollX - this.currentScrollX) > minFactor) {
					if (activeItem < maxItem - 1){
						activeItem = activeItem + 1;
					}
			
				} else if ((this.currentScrollX - this.prevScrollX) > minFactor) {
					if (activeItem > 0){
						activeItem = activeItem - 1;
					}
				}
				System.out.println("horizontal : " + activeItem);
				scrollTo = activeItem * itemWidth;
				this.smoothScrollTo(scrollTo, 0);
				returnValue = true;
				break;
		}
		return returnValue;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		
		if (flingDisable){
			return false;
		}
		boolean returnValue = false;
		float ptx1 = 0, ptx2 = 0;
		if (e1 == null || e2 == null){
			return false;
		}
		ptx1 = e1.getX();
		ptx2 = e2.getX();
		// right to left
	
		if (ptx1 - ptx2 > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			if (activeItem < maxItem - 1){
				activeItem = activeItem + 1;
			}
			returnValue = true;
		} 
		else if (ptx2 - ptx1 > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			if (activeItem > 0){
				activeItem = activeItem - 1;
			}
			returnValue = true;
		}
		scrollTo = activeItem * itemWidth;
		this.smoothScrollTo(0, scrollTo);
		return returnValue;
		
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}
	
	@Override
	public void onLongPress(MotionEvent e) {
		
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}
	
	@Override
	public void onShowPress(MotionEvent e) {
		
	}
	
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	*/
}

