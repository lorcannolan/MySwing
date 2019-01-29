package ie.dit.myswing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Map extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        TextView title = (TextView)findViewById(R.id.mapTitle);
        title.setText(R.string.title_map);
    }
}
