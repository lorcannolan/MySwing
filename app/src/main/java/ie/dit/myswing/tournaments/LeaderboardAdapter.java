package ie.dit.myswing.tournaments;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.dit.myswing.R;

public class LeaderboardAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int mResourse;
    private ArrayList<String> names;
    private ArrayList<Integer> scores;

    public LeaderboardAdapter(Context context, int resource, ArrayList<String> names, ArrayList<Integer> scores) {
        super(context, resource, names);
        this.mContext = context;
        this.mResourse = resource;
        this.names = names;
        this.scores = scores;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int mPosition = position;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResourse, parent, false);

        int rowHeight = parent.getHeight()/12;

        TextView playerName = (TextView)convertView.findViewById(R.id.player_name);
        playerName.setText(names.get(position));
        playerName.setHeight(rowHeight);

        TextView playerScore = (TextView)convertView.findViewById(R.id.player_score);
        playerScore.setText(scores.get(position) + "");
        if (scores.get(position) < 0) {
            playerScore.setTextColor(Color.RED);
        }

        return convertView;
    }

}
