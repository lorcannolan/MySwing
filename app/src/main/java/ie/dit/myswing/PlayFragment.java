package ie.dit.myswing;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

import static android.widget.ListPopupWindow.MATCH_PARENT;

public class PlayFragment extends Fragment {

    private CardView personal, tournament;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_play, container, false);

        personal = (CardView) view.findViewById(R.id.personal);
        personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectCourseIntent = new Intent(getActivity(), PlaySelectCourse.class);
                startActivity(selectCourseIntent);
            }
        });

        tournament = (CardView) view.findViewById(R.id.tournament);
        tournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectTournamentIntent = new Intent(getActivity(), PlaySelectTournament.class);
                startActivity(selectTournamentIntent);
            }
        });

        return view;
    }
}
