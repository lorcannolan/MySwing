package ie.dit.myswing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class TournamentsFragment extends Fragment {

    private TabLayout tabLayout;
    private TabPageAdapter mPageAdapter;
    private ViewPager mViewPager;

    private FloatingTextButton createTournament;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tournaments, container, false);

        mViewPager = (ViewPager) view.findViewById(R.id.tournament_tab_container);

        mPageAdapter = new TabPageAdapter(getChildFragmentManager());
        setupViewPager(mViewPager);

        tabLayout = (TabLayout) view.findViewById(R.id.tournament_tab_navigation);
        tabLayout.setupWithViewPager(mViewPager);

        createTournament = (FloatingTextButton) view.findViewById(R.id.new_tournament);
        createTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createTournamentIntent = new Intent(getActivity(), CreateTournament.class);
                createTournamentIntent.putExtra("tournamentFragment", "Tournament Fragment");
                startActivity(createTournamentIntent);
            }
        });

        return view;
    }

    public void setupViewPager (ViewPager viewPager) {
        TabPageAdapter adapter = new TabPageAdapter(getChildFragmentManager());
        adapter.addFragment(new PlayedTournamentsFragment(), "Played");
        adapter.addFragment(new InvitedTournamentsFragment(), "Invited");
        viewPager.setAdapter(adapter);
    }
}
