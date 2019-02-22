package ie.dit.myswing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ScorecardAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int mResourse;
    private int numberHeight;
    private String courseFirebaseKey;
    private String mGender;

    private EditText holeParEditText, holeIndexEditText;
    // Array lists to store each edit text when they are being created to be able to access them later after being created
    private ArrayList<EditText> holeParEditTextList = new ArrayList<>();
    private ArrayList<EditText> holeIndexEditTextList = new ArrayList<>();

    private DatabaseReference holesRef, courseRef;

    public ScorecardAdapter(Context context, int resource, List<String> courseNumbers, String courseFirebaseKey, String gender) {
        super(context, resource, courseNumbers);
        this.mContext = context;
        this.mResourse = resource;
        this.courseFirebaseKey = courseFirebaseKey;
        this.mGender = gender;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int mPosition = position;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResourse, parent, false);

        numberHeight = parent.getHeight()/9;

        holesRef = FirebaseDatabase.getInstance().getReference().child("courses").child(courseFirebaseKey).child("holes");
        courseRef = holesRef.child(getItem(mPosition));
        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mGender.equalsIgnoreCase("ladies")) {
                    holeIndexEditTextList.get(mPosition).setText(dataSnapshot.child("ladies index").getValue().toString());
                    holeParEditTextList.get(mPosition).setText(dataSnapshot.child("ladies par").getValue().toString());
                }
                else {
                    holeIndexEditTextList.get(mPosition).setText(dataSnapshot.child("mens index").getValue().toString());
                    holeParEditTextList.get(mPosition).setText(dataSnapshot.child("mens par").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        TextView holeNumber = (TextView)convertView.findViewById(R.id.hole_number);
        holeNumber.setText(getItem(position));
        holeNumber.setHeight(numberHeight);

        holeParEditText = (EditText)convertView.findViewById(R.id.choose_par);
        holeParEditTextList.add(holeParEditText);
        holeParEditTextList.get(mPosition).setInputType(InputType.TYPE_NULL);
        holeParEditTextList.get(mPosition).setOnClickListener(new View.OnClickListener() {
            /*
            Source code on Dialogs found at:
                - https://developer.android.com/guide/topics/ui/dialogs
                - https://developer.android.com/reference/android/app/AlertDialog.Builder
            */
            @Override
            public void onClick(View v) {
                // Custom built dialog pop-up
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select the Par for this hole.");
                builder.setItems(R.array.pars, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Resources res = getContext().getResources();
                        String[] pars = res.getStringArray(R.array.pars);
                        // Sets the text of the edit text widget that was clicked by using an ArrayList to store all of the individual edit texts
                        holeParEditTextList.get(mPosition).setText(pars[which]);
                        int holeNumber = Integer.parseInt(getItem(mPosition));
                        if (mGender.equalsIgnoreCase("ladies")) {
                            holesRef.child(holeNumber + "").child("ladies par").setValue(Integer.parseInt(pars[which]));
                        }
                        else {
                            holesRef.child(holeNumber + "").child("mens par").setValue(Integer.parseInt(pars[which]));
                        }
                    }
                });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog chooseTeeBoxes = builder.create();
                chooseTeeBoxes.show();
            }
        });

        holeIndexEditText = (EditText)convertView.findViewById(R.id.choose_index);
        holeIndexEditTextList.add(holeIndexEditText);
        holeIndexEditTextList.get(mPosition).setInputType(InputType.TYPE_NULL);
        holeIndexEditTextList.get(mPosition).setOnClickListener(new View.OnClickListener() {
            /*
            Source code on Dialogs found at:
                - https://developer.android.com/guide/topics/ui/dialogs
                - https://developer.android.com/reference/android/app/AlertDialog.Builder
            */
            @Override
            public void onClick(View v) {
                // Custom built dialog pop-up
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select the Par for this hole.");
                builder.setItems(R.array.indexes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Resources res = getContext().getResources();
                        String[] indexes = res.getStringArray(R.array.indexes);
                        holeIndexEditTextList.get(mPosition).setText(indexes[which]);
                        int holeNumber = Integer.parseInt(getItem(mPosition));
                        if (mGender.equalsIgnoreCase("ladies")) {
                            holesRef.child(holeNumber + "").child("ladies index").setValue(Integer.parseInt(indexes[which]));
                        }
                        else {
                            holesRef.child(holeNumber + "").child("mens index").setValue(Integer.parseInt(indexes[which]));
                        }
                    }
                });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog chooseTeeBoxes = builder.create();
                chooseTeeBoxes.show();
            }
        });

        return convertView;
    }

}
