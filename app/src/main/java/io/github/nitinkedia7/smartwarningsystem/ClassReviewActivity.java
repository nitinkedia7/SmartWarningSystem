
/**
 <header>
 Module: NotificationActivity
 Date of creation: 14-04-18
 Author: Nitin Kedia
 Modification history:  By Namit Kumar 16-04-18
 14-04-18: Created module with initialization functions
 15-04-18: Implemented functionality to fetch blacklisted students and save their review
 16-04-18: Documented code.
 Synopsis:
 This module enables the professor to see the blacklisted student(s) in current
 session and write a review for each of them.
 Global variables: None
 Functions:
 onClickListener() for each blacklisted student
 prepareReviewData()
 onOptionsItemSelected()
 </header>
 **/


package io.github.nitinkedia7.smartwarningsystem;

// import android, java, Google Firebase libraries
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ClassReviewActivity extends AppCompatActivity {
    private static final String TAG = "ClassReview";
    // an array of objects containing student details
    private List<Student> mStudentList = new ArrayList<>();
    // mAdapter is an object that configures the layout for displaying list
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    // Database Variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ValueEventListener mValueEventListener;
    // name of current session
    private String mSessionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Session name was passed from dashboard as it will be used in database access
        mSessionName = getIntent().getStringExtra("sessionName");

        // Assigning the studentlist to adapter which in-turn displays the layout to display list
        mAdapter = new RecyclerViewAdapter(mStudentList);
        mAdapter.mDisplayReview = true;
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        // connecting to firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("Sessions").child(mSessionName).child("joinedUsers");

        // Attach a listener to the location of joined students in database to obtain joined student list
        // Path is "/Sessions/{msessionName}/joinedUsers/"
        mValueEventListener = new ValueEventListener() {
            @Override
            // onDataChange runs each time data is changed in above path and returns the whole data in snapshot
            public void onDataChange(DataSnapshot joinedUserDataSnapshot) {
                //Get map of users in datasnapshot
                prepareReviewData((Map<String, Object>) joinedUserDataSnapshot.getValue());
                mDatabaseReference.removeEventListener(mValueEventListener);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
                Log.d(TAG, String.valueOf(databaseError.getCode()));
            }
        };
        mDatabaseReference.addValueEventListener(mValueEventListener);

        // Each student (tile) displayed can be tapped, summoning an dialog where the student review can be entered
        // This is expected to be done after discussing with the concerned student
        mRecyclerView.addOnItemTouchListener(new NotificationTouchListener(getApplicationContext(), mRecyclerView, new NotificationTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                // using the index which was tapped to get the concerned student object
                final Student student = mStudentList.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(ClassReviewActivity.this, R.style.MyDialogTheme);
                builder.setTitle(student.getName());
                View viewInflated = getLayoutInflater().inflate(R.layout.dialog_review, (ViewGroup) null, false);
                // Set up the input
                final EditText reviewField = (EditText) viewInflated.findViewById(R.id.inputReview);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(viewInflated);
                // Set up the OK and Cancel buttons
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Input review must not be empty
                        String review = reviewField.getText().toString().trim();
                        if (TextUtils.isEmpty(review)) {
                            reviewField.setError("Required.");
                            return;
                        } else {
                            reviewField.setError(null);
                        }
                        // Update review in database
                        mDatabaseReference.child(student.getUid()).child("review").setValue(review);
                        // This review must also reflect in the display when dialog closes
                        student.setReview(review);
                        mAdapter.notifyItemChanged(position);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }
    // this function passes each fetched student object into the adapter for display.
    private void prepareReviewData(Map<String,Object> students) {
        //iterate through each user
        for (Map.Entry<String, Object> entry : students.entrySet()){
            //Get user map and Uid and typecast it to Student object
            Map joinedStudent = (Map) entry.getValue();
            String uid = entry.getKey();
            // Review is done only for blacklisted students
            if(joinedStudent.get("isBlacklisted").toString().equals("Blacklisted")) {
                Student student = new Student(joinedStudent.get("name").toString(),
                        Integer.valueOf(joinedStudent.get("state").toString()),
                        joinedStudent.get("isBlacklisted").toString(),
                        joinedStudent.get("review").toString(), uid,
                        Integer.valueOf(joinedStudent.get("blacklistedState").toString())
                );
                mStudentList.add(student);
            }
        }
        mAdapter.notifyDataSetChanged(); // start loading into layout
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Revert to dashboard when back button is pressed
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}