package com.example.airquality_projandroid;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class CurrentAirQualityFragment extends Fragment {

    ImageView airQualityPicture;
    TextView cityName, countryName, cityTimeStamp, cityAQI, cityAQIRating;
    LinearLayout currentPanel;
    ProgressBar currentProgressBar;
    SwipeRefreshLayout pullToRefresh;
    RelativeLayout currentLayout;

    CityData cityData = new CityData();

    static CurrentAirQualityFragment instance;

    /**
     * This is just to make sure the CurrentAirQualityFragment is not null
     * when it is created.
     *
     * @return - the instance of the CurrentAirQualityFragment
     */
    public static CurrentAirQualityFragment getInstance() {
        if (instance == null) {
            instance = new CurrentAirQualityFragment();
        }
        return instance;
    }

    public CurrentAirQualityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_current_air_quality, container, false);

        currentLayout = itemView.findViewById(R.id.current_bg);

        cityName = itemView.findViewById(R.id.cityName);
        cityTimeStamp = itemView.findViewById(R.id.cityTimestamp);
        cityAQI = itemView.findViewById(R.id.cityAQI);
        cityAQIRating = itemView.findViewById(R.id.cityAQIRating);

        currentPanel = itemView.findViewById(R.id.current_panel);
        currentProgressBar = itemView.findViewById(R.id.current_progress_bar);
        pullToRefresh = itemView.findViewById(R.id.current_swipeRefresh);

        pullToRefresh.setOnRefreshListener(() -> {
            Toast.makeText(getContext(), "Refreshed", Toast.LENGTH_SHORT).show();
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
            } else {
                getCurrentAirQualityData();
                Log.d("Test", "Value is: " + cityData.getCityName());
                Log.d("Test", "Value is: " + cityData.getCityTimeStamp());
                Log.d("Test", "Value is: " + cityData.getCityAQI());
            }
            pullToRefresh.setRefreshing(false);
        });

        //If the Fragment is being created for the first time
        //savedInstanceState will be null
        if (savedInstanceState == null) {
            getCurrentAirQualityData();
            Log.d("Test", "first time");
        } else {
            //Call the loadCurrentData method to display the data
            loadCurrentDataTargetApi26(cityData);
            Log.d("Test", "another time");
        }

        return itemView;
    }

    /**
     * This method gets the current data from firebase
     */
    private void getCurrentAirQualityData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference cityLocation = database.getReference("Ho Chi Minh City");

        // Read from the database
        cityLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                cityData = dataSnapshot.getValue(CityData.class);
                Log.d("Test", "Success");
                loadCurrentDataTargetApi26(cityData);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Test", "Failed to read value.", error.toException());
            }
        });
    }

    /**
     * This function loads in the current air quality information based on the
     * data got from firebase. Only if API level is 26 or higher
     *
     * @param cityData - that contains information from firebase
     */
    @TargetApi(26)
    private void loadCurrentDataTargetApi26(CityData cityData) {
        cityName.setText(cityData.getCityName());
        cityTimeStamp.setText(decodeTimestamp(cityData.getCityTimeStamp()));
        cityAQI.setText("" + cityData.getCityAQI());
        cityAQIRating.setText(rankAQIUS(cityData.getCityAQI()));

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        Log.d("Time", "24 hours: " + hour);

        if (getContext() != null) {
            if (hour >= 0 && hour < 5) {
                currentLayout.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_night));
                Window window = Objects.requireNonNull(getActivity()).getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#041b20"));
            } else if (hour >= 5 && hour < 7) {
                currentLayout.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_sunrise));
                Window window = Objects.requireNonNull(getActivity()).getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#060f18"));
            } else if (hour >= 7 && hour < 17) {
                currentLayout.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_sunny));
                Window window = Objects.requireNonNull(getActivity()).getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#08253b"));
            } else if (hour >= 17 && hour < 20) {
                currentLayout.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_sunset));
                Window window = Objects.requireNonNull(getActivity()).getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#20244c"));
            } else if (hour >= 20 && hour < 24) {
                currentLayout.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_night));
                Window window = Objects.requireNonNull(getActivity()).getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#041b20"));
            }
        }

//        //Display the information after it's been loaded
//        //Hide the progress bar and show the info.
        currentPanel.setVisibility(View.VISIBLE);
        currentProgressBar.setVisibility(View.GONE);
    }

    /**
     * This method converts a ISO-8601 timestamp to a readable date.
     *
     * @param timestamp - a String of ISO-8601 compliant timestamp
     * @return - a String of the formatted date in words
     */
    @TargetApi(26)
    private String decodeTimestamp(String timestamp) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(timestamp, inputFormatter);
        String formattedDate = outputFormatter.format(date);
        return formattedDate;
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

    /**
     * This method converts a numerical AQI value to its severity ranking in words
     *
     * @param aqius - This is the air quality index by U.S. EPA standards
     * @return the rank of the air quality index, a String
     */
    private String rankAQIUS(int aqius) {
        if (aqius >= 0 && aqius <= 50) {
            return "Good";
        } else if (aqius >= 51 && aqius <= 100) {
            return "Moderate";
        } else if (aqius >= 101 && aqius <= 150) {
            return "Unhealthy for Sensitive Groups";
        } else if (aqius >= 151 && aqius <= 200) {
            return "Unhealthy";
        } else if (aqius >= 201 && aqius <= 300) {
            return "Very Unhealthy";
        } else if (aqius >= 301) {
            return "Hazardous";
        } else {
            return "ERROR";
        }
    }
}
