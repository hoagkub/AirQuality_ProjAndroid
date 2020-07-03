package com.example.airquality_projandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    Context context;
    ArrayList<CityData> cityDataArrayList;
    OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ListAdapter(Context context, ArrayList<CityData> cityDataArrayList) {
        this.context = context;
        this.cityDataArrayList = cityDataArrayList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_listitem, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapter.ViewHolder holder, int position) {
        CityData cityData = cityDataArrayList.get(position);
        holder.textViewTitle.setText(cityData.getCityName());
        holder.textViewShortDesc.setText("");
        int aqius = cityData.getCityAQI();
        setIconAQIUS(holder, aqius);
        holder.textViewRating.setText(aqius + "");
        setColorAQIUS(holder, aqius);
        holder.textViewPrice.setText(rankAQIUS(aqius));
    }

    @Override
    public int getItemCount() {
        return cityDataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewTitle, textViewShortDesc, textViewRating, textViewPrice;
        RelativeLayout listViewForeground, listViewBackground;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.listCityImage);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewShortDesc = itemView.findViewById(R.id.textViewShortDesc);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            listViewForeground = itemView.findViewById(R.id.listItemForeground);
            listViewBackground = itemView.findViewById(R.id.listItemBackground);

            itemView.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });
        }
    }

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

    /**
     * This function sets the icon corresponding to the AQI
     * @param holder - A ViewHolder object
     * @param aqius - This is the air quality index by U.S. EPA standards
     */
    private void setIconAQIUS(ViewHolder holder, int aqius) {
        if (aqius >= 0 && aqius <= 50) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_good));
        } else if (aqius >= 51 && aqius <= 100) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_moderate));
        } else if (aqius >= 101 && aqius <= 150) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_unhealthy_for_sensitive_groups));
        } else if (aqius >= 151 && aqius <= 200) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_unhealthy));
        } else if (aqius >= 201 && aqius <= 300) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_very_unhealthy));
        } else if (aqius >= 301) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_hazardous));
        } else {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_map_marker_error));
        }
    }

    /**
     * This function sets the color corresponding to the AQI
     * @param holder - A ViewHolder object
     * @param aqius - This is the air quality index by U.S. EPA standards
     */
    private void setColorAQIUS(ViewHolder holder, int aqius) {
        if (aqius >= 0 && aqius <= 50) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_good));
        } else if (aqius >= 51 && aqius <= 100) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_moderate));
        } else if (aqius >= 101 && aqius <= 150) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_unhealthyforsensitivegroups));
        } else if (aqius >= 151 && aqius <= 200) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_unhealthy));
        } else if (aqius >= 201 && aqius <= 300) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_veryunhealthy));
        } else if (aqius >= 301) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_hazardous));
        } else {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.colorError));
        }
    }

    /**
     * This function sets the cityDataArrayList to the new filteredList.
     * @param filteredList - an ArrayList of CityData objects.
     */
    public void filterList(ArrayList<CityData> filteredList) {
        cityDataArrayList = filteredList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        CityData cityData = cityDataArrayList.get(position);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        dbHelper.deleteCityRecord(cityData.getCityName(), context);
        cityDataArrayList.remove(position);
        // notify item added by position
        notifyItemRemoved(position);
        //notifyItemRangeChanged(position, stationList.size());
    }
}
