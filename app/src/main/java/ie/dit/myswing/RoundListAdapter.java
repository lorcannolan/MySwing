package ie.dit.myswing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/* In creating list view for tournaments, the following tutorial was followed:
    - https://www.youtube.com/watch?v=E6vE8fqQPTE
   This tutorial was then adapted to fit data from firebase database
*/
public class RoundListAdapter extends ArrayAdapter<Round> {

    private Context mContext;
    private int mResourse;

    public RoundListAdapter(Context context, int resource, ArrayList<Round> objects) {
        super(context, resource, objects);
        mContext = context;
        mResourse = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResourse, parent, false);

        TextView courseName = (TextView)convertView.findViewById(R.id.round_course_name);
        courseName.setText(getItem(position).getCourseName());

        TextView roundDate = (TextView)convertView.findViewById(R.id.round_date);
        roundDate.setText(getItem(position).getDate());

        TextView roundScore = (TextView)convertView.findViewById(R.id.round_row_score);
        roundScore.setText(getItem(position).getScore() + "");

        return convertView;
    }

}
