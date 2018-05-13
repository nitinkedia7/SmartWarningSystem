
/**
 <header>
 Module: NotificationTouchListener
 Date of creation: 15-04-18
 Author: Namit Kumar
 Modification history:  By Nitin Kedia 16-04-18
 15-04-18: Implement touch-listener on each notification
 16-04-18: Documented code.
 Synopsis:
 This module is a helper for NotificationActivity to detect response
 on a notification.
 Global variables: None
 Functions:
 NotificationTouchListener()
 onInterceptTouchEvent()
 onTouchEvent()
 onRequestDisallowInterceptTouchEvent()
 ClickListener()
 </header>
 **/

package io.github.nitinkedia7.smartwarningsystem;

// import android, java libraries
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class NotificationTouchListener implements RecyclerView.OnItemTouchListener {

    private GestureDetector mGestureDetector;
    private ClickListener mClickListener;

    //Triggers when the notification is tapped upon
    public NotificationTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
        this.mClickListener = clickListener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if (child != null && clickListener != null) {
                    clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (child != null && mClickListener != null && mGestureDetector.onTouchEvent(motionEvent)) {
            mClickListener.onClick(child, recyclerView.getChildAdapterPosition(child));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }
}