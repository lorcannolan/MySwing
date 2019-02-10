package ie.dit.myswing;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/* In creating list view for courses, the following tutorial was followed:
    - https://www.youtube.com/watch?v=E6vE8fqQPTE
   This tutorial was then adapted to fit data from firebase database
*/
public class CourseListAdapter extends ArrayAdapter<Course> {

    private Context mContext;
    private int mResourse;

    public CourseListAdapter(Context context, int resource, ArrayList<Course> objects) {
        super(context, resource, objects);
        mContext = context;
        mResourse = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResourse, parent, false);

        TextView courseName = (TextView)convertView.findViewById(R.id.course_name);
        courseName.setText(getItem(position).getName());

        TextView courseAddress = (TextView)convertView.findViewById(R.id.course_address);
        courseAddress.setText(getItem(position).getAddress());

        TextView courseWebsite = (TextView)convertView.findViewById(R.id.course_website);
        courseWebsite.setText(getItem(position).getWebsiteURI());

        return convertView;
    }

}
