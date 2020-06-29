package com.example.airquality_projandroid;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

import in.galaxyofandroid.spinerdialog.SpinnerDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment implements ListAdapter.OnItemClickListener {

    RecyclerView recyclerView;
    SpinnerDialog spinnerDialog;
    ProgressBar currentProgressBar;
    SwipeRefreshLayout pullToRefresh;
    ArrayList<String> spinnerList;
    ListAdapter listAdapter;
    Toolbar toolbar;
    DatabaseHelper databaseHelper;
    private ArrayList<CityData> cityList;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    private static ListFragment instance;

    /**
     * This is just to make sure the ListFragment is not null
     * when it is created.
     *
     * @return - the instance of the ListFragment
     */
    public static ListFragment getInstance() {
        if (instance == null) {
            instance = new ListFragment();
        }
        return instance;
    }

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        databaseHelper = new DatabaseHelper(getActivity());

        //Check if user is connected to the internet
        //If the user is connected, don't show it
        if (!isNetworkAvailable()) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }
            builder.setTitle("No internet connection")
                    .setMessage("You must have an internet connection to receive the latest air quality information.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.cancel())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        //Initialize the Spinner
        spinnerList = new ArrayList<>();
        loadSpinnerListItems();
        spinnerDialog = new SpinnerDialog(getActivity(), spinnerList, "Select city");
        spinnerDialog.bindOnSpinerListener((s, i) -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
            loadSelectedDataToRecyclerView(s);
            listAdapter.notifyDataSetChanged();
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_list, container, false);

        cityList = new ArrayList<>();

        toolbar = itemView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        currentProgressBar = itemView.findViewById(R.id.list_progress_bar);
        pullToRefresh = itemView.findViewById(R.id.list_swipeRefresh);

        pullToRefresh.setOnRefreshListener(() -> {
            Toast.makeText(getContext(), "Refreshed", Toast.LENGTH_SHORT).show();
//            reloadAirQualityData();
            pullToRefresh.setRefreshing(false);
        });

        currentProgressBar.setVisibility(View.GONE);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //Initialize the recyclerView so it can display the data in the ArrayList
        recyclerView = itemView.findViewById(R.id.recyclerView);
        //Setting some properties of the recyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        listAdapter = new ListAdapter(getActivity(), cityList);
        listAdapter.setOnItemClickListener(this);

        recyclerView.setAdapter(listAdapter);

        return itemView;
    }

    @Override
    public void onStart() {
        super.onStart();
        cityList.clear();
        loadDataFromSQLiteDatabase();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.item_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextChange(String query) {
                    filter(query.toLowerCase());
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Switch and case on the MenuItem object's id
        switch (item.getItemId()) {
            case R.id.sort_best_aqi:
                sortAQIAscending();
                break;
            case R.id.sort_worst_aqi:
                sortAQIDescending();
                break;
            case R.id.item_add:
                spinnerDialog.showSpinerDialog();
        }
        listAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method checks if the user is connected to the internet.
     *
     * @return - true, if the user is connected.
     * false, if the user is not connected.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /*
     * This function gets data for the selected city from the API
     * and adds it to the RecyclerView list
     */
    private void loadSelectedDataToRecyclerView(String selectedStation) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference cityLocation = database.getReference(selectedStation);

        // Read from the database
        cityLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                CityData value = dataSnapshot.getValue(CityData.class);
                cityList.add(value);
                listAdapter.notifyDataSetChanged();
                addData(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    private void loadSpinnerListItems() {
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            spinnerList.add(snapshot.getKey());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
//        spinnerList.add("Hanoi, Vietnam");
//        spinnerList.add("Ho Chi Minh City, Vietnam");
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(getActivity(), "Now leaving list screen", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the query text is changed by the user.
     * This function uses a Linear Search algorithm to
     * only display items that contains the String user inputted.
     *
     * @param text the new content of the query text field.
     */
    private void filter(@NotNull String text) {
        if (text.length() == 0) {
            listAdapter.filterList(cityList);
        } else {
            ArrayList<CityData> filteredList = new ArrayList<>();

            for (CityData item : cityList) {
                if (item.getCityName().toLowerCase().contains(text)) {
                    filteredList.add(item);
                }
            }
            listAdapter.filterList(filteredList);
        }
    }

    private void sortAQIAscending() {
        //Call a Collections.sort function on the cityList
        //and implement a Comparator
        //@return - Positive integer if AQI of one city is greater than another
        //          Negative integer if AQI of one city is less than another
        //          Zero if both city's AQI are the same
        //This is using the built-in sort function
        Collections.sort(cityList, (city1, city2) -> {
            if (city1.getCityAQI() > city2.getCityAQI()) {
                return 1;
            } else if (city1.getCityAQI() < city2.getCityAQI()) {
                return -1;
            } else {
                return 0;
            }
        });
    }

    private void sortAQIDescending() {
        //Call a Collections.sort function on the cityList
        //and implement a Comparator
        //@return - Positive integer if AQI of one city is less than another
        //          Negative integer if AQI of one city is greater than another
        //          Zero if both city's AQI are the same
        //This is using the built-in sort function
        Collections.sort(cityList, (city1, city2) -> {
            if (city1.getCityAQI() > city2.getCityAQI()) {
                return -1;
            } else if (city1.getCityAQI() < city2.getCityAQI()) {
                return 1;
            } else {
                return 0;
            }
        });
    }

    public void addData(CityData data) {
        databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.saveCityRecord(data);
    }

    /**
     * This function loads a CityData object's data from the SQLITE database
     */
    private void loadDataFromSQLiteDatabase() {
        Cursor data = databaseHelper.getData();
        while (data.moveToNext()) {
            String cityName = data.getString(0);
            int cityAQI = data.getInt(1);
            String cityTimeStamp = data.getString(2);
            cityList.add(new CityData(cityName, cityAQI, cityTimeStamp));
        }
    }
}
