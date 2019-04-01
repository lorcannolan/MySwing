package ie.dit.myswing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlayScorecardAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int mResourse;
    private int numberHeight;
    private String courseFirebaseKey;
    private String mGender;
    private String roundID;

    private DatabaseReference holesRef, courseRef, roundRef;
    private FirebaseAuth mAuth;

    public PlayScorecardAdapter(Context context, int resource, List<String> courseNumbers, String courseFirebaseKey, String gender, String roundID) {
        super(context, resource, courseNumbers);
        this.mContext = context;
        this.mResourse = resource;
        this.courseFirebaseKey = courseFirebaseKey;
        this.mGender = gender;
        this.mAuth = FirebaseAuth.getInstance();
        this.roundID = roundID;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int mPosition = position;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResourse, parent, false);

        numberHeight = parent.getHeight()/9;

        TextView holeNumber = (TextView)convertView.findViewById(R.id.play_hole_number);
        holeNumber.setText(getItem(position));
        holeNumber.setHeight(numberHeight);

        final TextView holeParText = (TextView)convertView.findViewById(R.id.play_hole_par);
        final TextView holeIndexText = (TextView)convertView.findViewById(R.id.play_hole_index);
        final TextView holeScoreText = (TextView)convertView.findViewById(R.id.play_hole_score);

        holesRef = FirebaseDatabase.getInstance().getReference().child("courses").child(courseFirebaseKey).child("holes");
        courseRef = holesRef.child(getItem(mPosition));
        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mGender.equalsIgnoreCase("ladies")) {
                    holeIndexText.setText(dataSnapshot.child("ladies index").getValue().toString());
                    holeParText.setText(dataSnapshot.child("ladies par").getValue().toString());
                }
                else {
                    holeIndexText.setText(dataSnapshot.child("mens index").getValue().toString());
                    holeParText.setText(dataSnapshot.child("mens par").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


        roundRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).child("rounds").child(roundID);
        roundRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("holes")) {
                    holeScoreText.setText("0");
                }
                else {
                    String holeNumber = getItem(mPosition);
                    if (dataSnapshot.child("holes").child(holeNumber).exists()) {
                        int holeShotTotal;
                        String holeShots, holePutts;

                        // Count Shots
                        if (dataSnapshot.child("holes").child(holeNumber).hasChild("shots")) {
                            holeShots = Long.toString(dataSnapshot.child("holes").child(holeNumber).child("shots").getChildrenCount());
                        }
                        else {
                            holeShots = "0";
                        }
                        // Count Putts
                        if (dataSnapshot.child("holes").child(holeNumber).hasChild("putts")) {
                            holePutts = Long.toString(dataSnapshot.child("holes").child(holeNumber).child("putts").getChildrenCount());
                        }
                        else {
                            holePutts = "0";
                        }

                        holeScoreText.setText((Integer.parseInt(holeShots) + Integer.parseInt(holePutts)) + "");
                    }
                    else {
                        holeScoreText.setText("0");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

//        holeScoreEditText = (EditText)convertView.findViewById(R.id.choose_par);
//        holeScoreEditTextList.add(holeScoreEditText);

        return convertView;
    }

}
