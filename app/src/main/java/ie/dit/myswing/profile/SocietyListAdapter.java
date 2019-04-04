package ie.dit.myswing.profile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ie.dit.myswing.R;
import ie.dit.myswing.java_classes.Society;

/* In creating list view for courses, the following tutorial was followed:
    - https://www.youtube.com/watch?v=E6vE8fqQPTE
   This tutorial was then adapted to fit data from firebase database
*/
public class SocietyListAdapter extends ArrayAdapter<Society> {

    private Context mContext;
    private int mResourse;

    public SocietyListAdapter(Context context, int resource, ArrayList<Society> objects) {
        super(context, resource, objects);
        mContext = context;
        mResourse = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResourse, parent, false);

        TextView societyName = (TextView)convertView.findViewById(R.id.society_name);
        societyName.setText(getItem(position).getName());

        TextView societyOrg = (TextView)convertView.findViewById(R.id.organization_name);
        societyOrg.setText(getItem(position).getOrganization());

        TextView createdBy = (TextView)convertView.findViewById(R.id.created_by_user_name);
        createdBy.setText(getItem(position).getCreatedBy());

        return convertView;
    }

}
