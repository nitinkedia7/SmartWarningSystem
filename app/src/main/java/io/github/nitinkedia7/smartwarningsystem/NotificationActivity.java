
/**
 <header>
 Module: NotificationActivity
 Date of creation: 14-04-18
 Author: Namit Kumar
 Modification history:  By Nitin Kedia 15-04-18
                        By Jatin Goyal 16-04-18
 14-04-18: Created module with initialization functions
 15-04-18: Implement timers for state change and responses to notifications
 16-04-18: Documented code.
 Synopsis:
 This module manages all the in-app notification recieved by student
 and response logic.
 Global variables: None
 Functions:
 prepareNotificationData()
 onOptionsItemSelected()
 </header>
 **/


package io.github.nitinkedia7.smartwarningsystem;

// import android, java, Google Firebase libraries
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotificationActivity extends AppCompatActivity {
    //Create a list of notifications
    private List<Notification> mNotificationList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private NotificationAdapter mAdapter;
    private static final String TAG = "NotificationActivity";

    private boolean mStatus;
    private String mSessionName;
    private CountDownTimer mTimer;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mSessionReference;
    private FirebaseAuth mFirebaseAuth;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the view
        setContentView(R.layout.activity_recycler_view);

        //Get the back button on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get session name from the previous intent
        mSessionName = getIntent().getStringExtra("sessionName");

        //Get Firebase Database instance
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        //Get reference to the database
        mDatabaseReference = mFirebaseDatabase.getReference();
        mSessionReference = mDatabaseReference.child("Sessions").child(mSessionName);

        //Get FirebaseAuth instance
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Get the current user (final because the user will not change)
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();

        // Attach a listener to read the data of the session
        mSessionReference.addValueEventListener(new ValueEventListener() {

            //OnDataChange triggers when the value in the given data reference changes
            @Override
            public void onDataChange(DataSnapshot sessionDataSnapshot) {
                mStatus = Boolean.valueOf(sessionDataSnapshot.child("isActive").getValue().toString());

                //if the session at any time is ended, the activity is closed and the Student Activity starts
                if(!mStatus){
                    mSessionReference.removeEventListener(this);
                    Toast.makeText(getApplicationContext(), "Session has been terminated", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle database error
                Log.d(TAG, String.valueOf(databaseError.getCode()));
            }
        });

        //start a timer of 3000 seconds with a 1 second tick time(Runs throughout the session)
        mTimer = new CountDownTimer(3000000, 1000) {

            //OnTick function triggers every time the timer ticks
            public void onTick(long millisUntilFinished) {
                //time remaining for the timer to end
                Long remainingSec = millisUntilFinished/1000;

                //Runs in every 10 seconds if the status of the session is active
                if (remainingSec % 10 == 0 && mStatus) {

                    //Assign the user a new random state(1 - 10)
                    Random rand = new Random();
                    Integer randState = rand.nextInt(10)+1;

                    //Update in database
                    mSessionReference.child("joinedUsers").child(user.getUid()).child("state").setValue(randState.toString());

                    if(mChildEventListener == null) {
                        //Triggers whenever a child is added in the referred database location
                        mChildEventListener = new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot sentAlertsDataSnapshot, String s) {
                                Notification notification = sentAlertsDataSnapshot.getValue(Notification.class);
                                //prepare notification to show in the recycler view
                                prepareNotificationData(notification);
                            }

                            @Override
                            public void onChildChanged(DataSnapshot sentAlertsDataSnapshot, String s) { }

                            @Override
                            public void onChildRemoved(DataSnapshot sentAlertsDataSnapshot) { }

                            @Override
                            public void onChildMoved(DataSnapshot sentAlertsDataSnapshot, String s) { }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //handle database error
                                Log.d(TAG, String.valueOf(databaseError.getCode()));
                            }
                        };
                        mSessionReference.child("alerts").child(user.getUid()).child("sentAlerts").addChildEventListener(mChildEventListener);
                    }else {
                        //mChildEventListener will never be null
                    }
                }
            }
            public void onFinish() { }
        };
        //Start the timer
        mTimer.start();

        //Recycler View to display all the notifications
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new NotificationAdapter(mNotificationList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        //Runs whenever the notification is tapped
        mRecyclerView.addOnItemTouchListener(new NotificationTouchListener(getApplicationContext(), mRecyclerView, new NotificationTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Notification notification = mNotificationList.get(position);
                //Runs if the notification is still enabled
                if(notification.getStatus().equals("Enabled")) {
                    //The notification is disabled and the response is recorded
                    Toast.makeText(getApplicationContext(), "Recorded your response", Toast.LENGTH_SHORT).show();
                    notification.setStatus("Disabled");
                    notification.setTime("Recorded your response");
                    //Update the notification with the new values
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
            @Override
            public void onLongClick(View view, int position) { }
        }));
    }

    //Create notification and add to the recycler view
    private void prepareNotificationData(final Notification notification) {

        //Add notification in the notification list
        mNotificationList.add(notification);

        //Get current user
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();

        //A timer for each notification is initialized which
        //provides a limited amount of time for the user to
        //react to the notification(10 seconds)
        mTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                //remaining time in the timer
                Long remainingSec = millisUntilFinished/1000;
                //if the status of the notification is changed to disabled, the timer is stopped
                if(notification.getStatus().equals("Disabled")){
                    mTimer.cancel();
                } else{
                    //remaining time of the notification is updated
                    notification.setTime(Long.toString(remainingSec));

                    //Update the notification with the new value
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
            //Triggers when the timer is finished
            public void onFinish() {
                //Inform that the time to react to the notification is over
                notification.setTime("Time Up");
                //Disable the notification
                notification.setStatus("Disabled");
                //Update the notification with new values
                mRecyclerView.setAdapter(mAdapter);
                //Push this notification into the unresponsive alert list of the student in the session
                mSessionReference.child("alerts").child(user.getUid()).child("unresponsiveAlerts").push().setValue(notification);
                mSessionReference.child("isUserBlacklisted").setValue(true);
                //Mark the student as blacklisted
                mSessionReference.child("joinedUsers").child(user.getUid()).child("isBlacklisted").setValue("Blacklisted");
                //Update the state at which the student was last blacklisted
                mSessionReference.child("joinedUsers").child(user.getUid()).child("blacklistedState").setValue(Integer.valueOf(notification.getState()));
            }
        };
        mTimer.start();
        mAdapter.notifyDataSetChanged();
    }

    //Runs when the back button on the ActionBar is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Fire up the Student Activity
            case android.R.id.home:
                Intent studentActivityIntent = new Intent(NotificationActivity.this, StudentActivity.class);
                NotificationActivity.this.startActivity(studentActivityIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}