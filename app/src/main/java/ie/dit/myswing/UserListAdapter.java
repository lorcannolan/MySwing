package ie.dit.myswing;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/* In creating list view for courses, the following tutorial was followed:
    - https://www.youtube.com/watch?v=E6vE8fqQPTE
   This tutorial was then adapted to fit data from firebase database
*/
public class UserListAdapter extends ArrayAdapter<User> {

    DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference().child("courses");
    DatabaseReference societiesRef = FirebaseDatabase.getInstance().getReference().child("societies");

    private Context mContext;
    private int mResourse;

    public UserListAdapter(Context context, int resource, ArrayList<User> objects) {
        super(context, resource, objects);
        mContext = context;
        mResourse = resource;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResourse, parent, false);

        TextView firstName = (TextView)convertView.findViewById(R.id.user_first_name);
        firstName.setText(getItem(position).getFirstName());

        TextView lastName = (TextView)convertView.findViewById(R.id.user_last_name);
        lastName.setText(getItem(position).getLastName());

        TextView dob = (TextView)convertView.findViewById(R.id.user_dob);
        dob.setText(getItem(position).getDOB());

        final TextView club = (TextView)convertView.findViewById(R.id.user_club);
        if (getItem(position).getClub().equals("None")) {
            club.setText(getItem(position).getClub());
        }
        else {
            coursesRef.child(getItem(position).getClub()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    club.setText(dataSnapshot.child("name").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }

        final TextView society = (TextView)convertView.findViewById(R.id.user_society);
        if (getItem(position).getSociety().equals("None")) {
            society.setText(getItem(position).getSociety());
        }
        else {
            societiesRef.child(getItem(position).getSociety()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    society.setText(dataSnapshot.child("name").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }

        return convertView;
    }

}
