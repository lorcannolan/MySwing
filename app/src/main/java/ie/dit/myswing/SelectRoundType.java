package ie.dit.myswing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SelectRoundType extends AppCompatActivity {

    private CardView personal, tournament;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_round_type);

        getSupportActionBar().setTitle("Select Round Type");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_background));

        personal = (CardView) findViewById(R.id.personal);
        personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectCourseIntent = new Intent(SelectRoundType.this, PlaySelectCourse.class);
                startActivity(selectCourseIntent);
            }
        });

        tournament = (CardView) findViewById(R.id.tournament);
        tournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectTournamentIntent = new Intent(SelectRoundType.this, PlaySelectTournament.class);
                startActivity(selectTournamentIntent);
            }
        });
    }
}
