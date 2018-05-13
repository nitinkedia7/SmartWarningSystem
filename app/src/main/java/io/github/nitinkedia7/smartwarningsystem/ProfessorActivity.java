/**
 <header>
 Module: ProfessorActivity
 Date of creation: 14-04-18
 Author: Nitin Kedia
 Modification history:  By Namit Kumar 15-04-18
                        By Jatin Goyal 16-04-18
 14-04-18: Created module with initialization functions
 15-04-18: Implement different condition checks for all 4 buttons
 16-04-18: Documented code.
 Synopsis:
 This module is a dashboard for the professor targeted functions like
 CreateSession, EndSession etc.
 Global variables: None
 Functions:
 onCreateOptionsMenu()
 onOptionsItemSelected()
 disengageStudents()
 saveSessionDetails()
 --button.setOnClickListener() have been modified and used
 --for all buttons
 </header>
 **/
    
package io.github.nitinkedia7.smartwarningsystem;

// import android, java, Google Firebase libraries
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfessorActivity extends AppCompatActivity {

    private static final String TAG = "ProfessorActivity";
    private Button mCreateSessionButton;
    private Button mEndSessionButton;
    private Button mClassStatusButton;
    private Button mClassReviewButton;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mSessionDatabaseReference;
    private DatabaseReference mProfessorDatabaseReference;
    private DatabaseReference mStudentDatabaseReference;

    private String mSessionName = "None";
    private Boolean mIsEngaged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set view (UI) for professor dashboard
        setContentView(R.layout.activity_dashboard);
        // get user object from firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        // get references to different sections of the database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mSessionDatabaseReference = mDatabaseReference.child("Sessions");
        mProfessorDatabaseReference = mDatabaseReference.child("Professors");
        mStudentDatabaseReference = mDatabaseReference.child("Students");
        // Assign all UI elements (text-box, buttons) to variables
        mCreateSessionButton = (Button) findViewById(R.id.createSessionButton);
        mEndSessionButton = (Button) findViewById(R.id.endSessionButton);
        mClassStatusButton = (Button) findViewById(R.id.classStatusButton);
        mClassReviewButton = (Button) findViewById(R.id.classReviewButton);

        // get the Professor's current session name from database, defaults to "None"
        mProfessorDatabaseReference.child(user.getUid()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot professorDataSnapshot) {
                        // assign session name to global variable
                        mSessionName = professorDataSnapshot.child("currentSession").getValue().toString();
                        mIsEngaged = Boolean.valueOf(professorDataSnapshot.child("isEngaged").getValue().toString());
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                        Log.d(TAG, String.valueOf(databaseError.getCode()));
                    }
                });
        // create session handle logic, only if following conditions are met:
        // 1. Professor should not be engaged in another session
        // 2. Session name should be unique else another professor may accidentally overwrite another's session
        mCreateSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsEngaged) {
                    // Display a dialog asking details for new session.
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfessorActivity.this, R.style.MyDialogTheme);
                    builder.setTitle("Enter Session Details");
                    View viewInflated = getLayoutInflater().inflate(R.layout.create_session_dialog, (ViewGroup) null, false);
                    final EditText sessionNameField = (EditText) viewInflated.findViewById(R.id.sessionName);
                    final EditText sessionPasswordField = (EditText) viewInflated.findViewById(R.id.sessionPassword);
                    builder.setView(viewInflated);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // ok logic
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String sessionName = sessionNameField.getText().toString().trim();
                            final String sessionPassword = sessionPasswordField.getText().toString().trim();
                            // Validate- input session name and password must be nonempty
                            if(TextUtils.isEmpty(sessionName)){
                                sessionNameField.setError("Required.");
                                Toast.makeText(ProfessorActivity.this, "Both fields are required!", Toast.LENGTH_SHORT).show();
                                return;
                            } else sessionNameField.setError(null);
                            if(TextUtils.isEmpty(sessionPassword)){
                                sessionPasswordField.setError("Required.");
                                Toast.makeText(ProfessorActivity.this, "Both fields are required!", Toast.LENGTH_SHORT).show();
                                return;
                            } else sessionPasswordField.setError(null);
                            // check for condition 2, session name must not already exist.
                            mSessionDatabaseReference.child(sessionName).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot sessionDataSnapshot) {
                                    mSessionDatabaseReference.child(sessionName).removeEventListener(this);
                                    if(sessionDataSnapshot.exists()){
                                        Toast.makeText(ProfessorActivity.this, "Session name already exists!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        saveSessionDetails(user.getUid(), sessionName, sessionPassword);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //handle database error
                                    Log.d(TAG, String.valueOf(databaseError.getCode()));
                                }
                            });
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { // cancel logic
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(ProfessorActivity.this, "Current Session is Active", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // session end logic only if a session is running (active) i.e. professor is engaged.
        mEndSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsEngaged){
                    mSessionDatabaseReference.child(mSessionName).addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot sessionDataSnapshot) {
                            if (Boolean.valueOf(sessionDataSnapshot.child("isActive").getValue().toString())) {
                                // make session inactive
                                mSessionDatabaseReference.child(mSessionName).child("isActive").setValue(false);
                                // if any student is joined, then disengage the students also from the session
                                Boolean isStudentJoined = Boolean.valueOf(sessionDataSnapshot.child("isUserJoined").getValue().toString());
                                if (isStudentJoined) disengageStudents((Map<String, Object>) sessionDataSnapshot.child("joinedUsers").getValue());
                                // Disengage Professor from the current session
                                mProfessorDatabaseReference.child(user.getUid()).child("isEngaged").setValue("false");
                                mProfessorDatabaseReference.child(user.getUid()).child("currentSession").setValue("None");
                                mSessionDatabaseReference.child(mSessionName).removeEventListener(this);
                                Toast.makeText(ProfessorActivity.this, "Session ended successfully.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ProfessorActivity.this, "Session already ended.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //handle databaseError
                            Log.d(TAG, String.valueOf(databaseError.getCode()));
                        }
                    });
                } else Toast.makeText(ProfessorActivity.this, "No active session running", Toast.LENGTH_SHORT).show();
            }
        });
        // See Class Status i.e. the current states of joined students, only if the following conditions hold
        // 1. A session must be running (active) i.e. professor is engaged.
        // 2. Number of joined students is non-zero.
        mClassStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsEngaged){
                    Toast.makeText(ProfessorActivity.this, "No active session running", Toast.LENGTH_LONG).show();
                } else {
                    mSessionDatabaseReference.child(mSessionName).child("isUserJoined").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot isUserJoinedDataSnapshot) {
                            if (Boolean.valueOf(isUserJoinedDataSnapshot.getValue().toString())) {
                                // Some students have joined
                                mSessionDatabaseReference.child(mSessionName).child("isUserJoined").removeEventListener(this);
                                // Launch new window to show list of students with details
                                // also pass session name to new activity
                                Intent classStatusActivityIntent = new Intent(ProfessorActivity.this, ClassStatusActivity.class);
                                classStatusActivityIntent.putExtra("sessionName", mSessionName);
                                ProfessorActivity.this.startActivity(classStatusActivityIntent);
                            } else {
                                Toast.makeText(ProfessorActivity.this, "No Students Joined!", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //handle databaseError
                            Log.d(TAG, String.valueOf(databaseError.getCode()));
                        }
                    });
                }
            }
        });

        // Do Class Review i.e. review the students who have not responded to an alert
        // Prerequisites: 1. Active session 2. Presence of such students
        mClassReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsEngaged){
                    Toast.makeText(ProfessorActivity.this, "No active session running", Toast.LENGTH_LONG).show();
                } else {
                    mSessionDatabaseReference.child(mSessionName).child("isUserBlacklisted").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot isUserBlacklistedDataSnapshot) {
                            if (Boolean.valueOf(isUserBlacklistedDataSnapshot.getValue().toString())) {
                                // non-zero blacklisted student present
                                mSessionDatabaseReference.child(mSessionName).child("isUserJoined").removeEventListener(this);
                                // Launch a new activity (window) showing details of blacklisted students
                                // also pass session name
                                Intent classReviewActivityIntent = new Intent(ProfessorActivity.this, ClassReviewActivity.class);
                                classReviewActivityIntent.putExtra("sessionName", mSessionName);
                                ProfessorActivity.this.startActivity(classReviewActivityIntent);
                            } else {
                                Toast.makeText(ProfessorActivity.this, "No Students Blacklisted!", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //handle databaseError
                            Log.d(TAG, String.valueOf(databaseError.getCode()));
                        }
                    });
                }
            }
        });
    }
    // to show the Sign Out button in top-right menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    // Sign Out from Firebase auth if Signout button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                // revert to Login screen
                ProfessorActivity.this.startActivity(new Intent(ProfessorActivity.this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // This function iterates through the list of joined students,
    // disengaging from current session when "End Session is Pressed"
    private void disengageStudents(Map<String,Object> students) {
        for (Map.Entry<String, Object> entry : students.entrySet()){
            //Get joined student ID to update his/her database entry
            String uid = entry.getKey();
            mStudentDatabaseReference.child(uid).child("isEngaged").setValue("false");
            mStudentDatabaseReference.child(uid).child("currentSession").setValue("None");
        }

    }
    // This function initialises an empty session when a new session is created
    private void saveSessionDetails(final String uid, final String sessionName, final String sessionPassword){
        // Engage professor
        mProfessorDatabaseReference.child(uid).child("isEngaged").setValue("true");
        mProfessorDatabaseReference.child(uid).child("currentSession").setValue(sessionName);
        // Session Attributes
        DatabaseReference currentSessionReference = mSessionDatabaseReference.child(sessionName);
        currentSessionReference.child("isActive").setValue(true);
        currentSessionReference.child("isUserBlacklisted").setValue(false);
        currentSessionReference.child("isUserJoined").setValue(false);
        currentSessionReference.child("sessionPassword").setValue(sessionPassword);
        currentSessionReference.child("alerts").setValue("None");
        currentSessionReference.child("joinedUsers").setValue("None");
        Toast.makeText(ProfessorActivity.this, "Successfully created session!", Toast.LENGTH_LONG).show();
    }
}
