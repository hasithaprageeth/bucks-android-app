package uom.prageeth.hasitha.bucks;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationDrawerFragment extends Fragment implements NavAdapter.ClickListner{

    private RecyclerView recyclerView;
    public static final String PREF_FILE_NAME = "check";
    public static final String KEY_USER_LEARNED_DRAWER = "user_learned_drawer";
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private View mView;
    private NavAdapter adapter;

    public NavigationDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readFromPreferences(getActivity(), KEY_USER_LEARNED_DRAWER, "false"));

        if(savedInstanceState != null){
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        recyclerView = (RecyclerView)layout.findViewById(R.id.drawer_list);
        recyclerView = (RecyclerView)layout.findViewById(R.id.drawer_list);
        adapter = new NavAdapter(getActivity(),getNavDrawerItems());
        adapter.setClickListner(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return layout;
    }

    public static List<NavigationDrawerData> getNavDrawerItems(){
        List<NavigationDrawerData> data = new ArrayList<>();
        int[] icon = {R.drawable.ic_trending_up,
                      R.drawable.ic_check_all,
                      R.drawable.ic_contrast_box,
                      R.drawable.ic_history};
        String[] title ={"Latest Rates",
                         "Choose Currency",
                         "Converter",
                         "History"};

        for(int i=0;((i<title.length)&&(i<icon.length)); i++){

            NavigationDrawerData current = new NavigationDrawerData();
            current.iconId = icon[i];
            current.title = title[i];
            data.add(current);
        }
        return data;
    }

    public void setUp(int fragmentId,DrawerLayout drawerLayout, final Toolbar toolbar) {

        mView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(),drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close){

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onDrawerOpened(View drawerView) {

                super.onDrawerOpened(drawerView);
                if(!mUserLearnedDrawer){
                    mUserLearnedDrawer = true;
                    saveToPreferences(getActivity(),KEY_USER_LEARNED_DRAWER,mUserLearnedDrawer+"");
                }

                getActivity().invalidateOptionsMenu();
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                if(slideOffset<0.5){
                toolbar.setAlpha(1-slideOffset);
                }
            }
        };

        if(!mUserLearnedDrawer && !mFromSavedInstanceState){
            mDrawerLayout.openDrawer(mView);
        }
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    public void saveToPreferences(Context context, String preferenceName, String preferenceValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName,preferenceValue);
        editor.apply();
    }

    public String readFromPreferences(Context context, String preferenceName, String defaultValue){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName,defaultValue);
    }

    @Override
    public void itemClicked(View view, int position) {
        switch (position) {
            case 0:
                startActivity(new Intent(getActivity(), MainActivity.class));
                break;
            case 1:
                startActivity(new Intent(getActivity(), ChooseCurrencyActivity.class));
                break;
            case 2:
                startActivity(new Intent(getActivity(), ConverterActivity.class));
                //startActivity(new Intent(getActivity(),ConverterActivity.class));
                break;
            case 3:
                startActivity(new Intent(getActivity(), HistoryActivity.class));
                //startActivity(new Intent(getActivity(),HistoryActivity.class));
                break;
        }
    }
}
