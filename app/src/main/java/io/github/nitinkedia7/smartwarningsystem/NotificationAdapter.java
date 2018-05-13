
/**
 <header>
 Module: NotificationAdapter
 Date of creation: 15-04-18
 Author: Namit Kumar
 Modification history:  By Nitin Kedia 16-04-18
 15-04-18: Receiving the notificationlist and binding it to layout file
 16-04-18: Documented code.
 Synopsis:
 This module receives the notificationlist and binds to layout file to display the list.
 Global variables: None
 Functions:
 NotificationAdapter()
 onCreateViewHolder()
 onBindViewHolder()
 getItemCount()
 </header>
 **/


package io.github.nitinkedia7.smartwarningsystem;

// import android, java libraries
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    //List of notifications
    private List<Notification> notificationsList;

    //All the views in a notification
    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView state, time, comment, status;

        public MyViewHolder(View view) {
            super(view);
            status = (TextView) view.findViewById(R.id.status);
            state = (TextView) view.findViewById(R.id.state);
            comment = (TextView) view.findViewById(R.id.comment);
            time = (TextView) view.findViewById(R.id.time);
        }
    }


    public NotificationAdapter(List<Notification> notificationsList) {
        this.notificationsList = notificationsList;
    }

    //Set view
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alert_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    //Set text in all the text views of the notification
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Notification notification = notificationsList.get(position);
        holder.status.setText(notification.getStatus());
        holder.state.setText("State : " + notification.getState());
        holder.comment.setText(notification.getComment());
        holder.time.setText(notification.getTime());
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }
}