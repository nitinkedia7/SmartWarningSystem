
/**
 <header>
 Class: Notification
 Date of creation: 14-04-18
 Author: Namit Kumar
 Modification history:
 14-04-18: Created class with constructor, getter and setter functions for every variable
 16-04-18: Documented code.
 Synopsis:
 This is a class which contains the attributes of a notification for a student
 Global variables: None
 Functions:
 getState()
 setState()
 getComment()
 setComment()
 getTime()
 setTime()
 getStatus()
 setStatus()
 </header>
 **/

package io.github.nitinkedia7.smartwarningsystem;

public class Notification {

    private String state;       //Current State of user
    private String comment;     //Comment on the current state
    private String time;        //Time left to react to the notification
    private String status;      //Condition of the notification(Enabled/Disabled)

    //constructor, getter and setter functions
    public Notification() {
    }

    public Notification(String state, String comment, String time, String status) {
        this.state = state;
        this.comment = comment;
        this.time = time;
        this.status = status;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getState() {
        return state;
    }

    public void setState(String name) {
        this.state = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
