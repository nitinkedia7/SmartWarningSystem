/**
 <header>
 Class: AdditionalUserData
 Date of creation: 14-04-18
 Author: Nitin Kedia
 Modification history:  By Namit Kumar 16-04-18
 14-04-18: Created class with constructor, getter and setter functions for every variable
 16-04-18: Documented code.
 Synopsis:
 This is a class which contains the information(like name, device token etc.) for every user
 Global variables: None
 Functions:
 getFullName()
 setFullName()
 getCurrentSession()
 setCurrentSession()
 getToken()
 setToken()
 getIsEngaged()
 setIsEngaged()
 </header>
 **/

package io.github.nitinkedia7.smartwarningsystem;


public class AdditionalUserData{
    private String fullName;            //Name of user
    private String isEngaged;           //Status of user (Engaged or Not Engaged)
    private String token;               //Token of current device the user is using
    private String currentSession;      //Current session that the user is joined in

    //constructor, getter and setter functions
    public AdditionalUserData() {
    }

    public AdditionalUserData(String fullName, String isEngaged, String token, String currentSession) {
        this.fullName = fullName;
        this.currentSession = currentSession;
        this.isEngaged = isEngaged;
        this.token = token;
    }

    public String getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(String currentSession) {
        this.currentSession = currentSession;
    }

    public String getIsEngaged() {
        return isEngaged;
    }

    public void setIsEngaged(String isEngaged) {
        isEngaged = isEngaged;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}