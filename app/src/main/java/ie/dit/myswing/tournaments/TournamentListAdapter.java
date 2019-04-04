package ie.dit.myswing.tournaments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ie.dit.myswing.R;
import ie.dit.myswing.java_classes.Tournament;

/* In creating list view for tournaments, the following tutorial was followed:
    - https://www.youtube.com/watch?v=E6vE8fqQPTE
   This tutorial was then adapted to fit data from firebase database
*/
public class TournamentListAdapter extends ArrayAdapter<Tournament> {

    private Context mContext;
    private int mResourse;

    public TournamentListAdapter(Context context, int resource, ArrayList<Tournament> objects) {
        super(context, resource, objects);
        mContext = context;
        mResourse = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResourse, parent, false);

        TextView tournamentName = (TextView)convertView.findViewById(R.id.course_name);
        tournamentName.setText(getItem(position).getName());

        TextView tournamentCourse = (TextView)convertView.findViewById(R.id.course_address);
        tournamentCourse.setText(getItem(position).getCourseName());
        ColorStateList defaultColour =  tournamentCourse.getTextColors();

        TextView tournamentDate = (TextView)convertView.findViewById(R.id.course_website);
        tournamentDate.setText(getItem(position).getDate());
        tournamentDate.setTextColor(defaultColour);

        return convertView;
    }

}
