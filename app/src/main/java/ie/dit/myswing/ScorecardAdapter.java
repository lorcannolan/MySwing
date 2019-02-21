package ie.dit.myswing;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.URI;
import java.util.List;

public class ScorecardAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private int mResourse;
    private int numberHeight;

    private Spinner holeParSpinner, holeIndexSpinner;

    public ScorecardAdapter(Context context, int resource, List<String> courseNumbers) {
        super(context, resource, courseNumbers);
        this.mContext = context;
        this.mResourse = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResourse, parent, false);

        numberHeight = parent.getHeight()/9;

        TextView holeNumber = (TextView)convertView.findViewById(R.id.hole_number);
        holeNumber.setText(getItem(position));
        holeNumber.setHeight(numberHeight);

        holeParSpinner = (Spinner)convertView.findViewById(R.id.choose_par);
        ArrayAdapter<CharSequence> parAdapter = ArrayAdapter.createFromResource(getContext(), R.array.pars, android.R.layout.simple_spinner_item);
        parAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holeParSpinner.setAdapter(parAdapter);

        holeIndexSpinner = (Spinner)convertView.findViewById(R.id.choose_index);
        ArrayAdapter<CharSequence> indexAdapter = ArrayAdapter.createFromResource(getContext(), R.array.indexes, android.R.layout.simple_spinner_item);
        indexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holeIndexSpinner.setAdapter(indexAdapter);

        return convertView;
    }

}
