package ie.dit.myswing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Tournaments extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournaments);

        TextView title = (TextView)findViewById(R.id.tournamentsTitle);
        title.setText(R.string.title_tournaments);
    }
}
