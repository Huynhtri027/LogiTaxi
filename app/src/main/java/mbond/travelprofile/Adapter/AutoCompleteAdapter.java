package mbond.travelprofile.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.StringTokenizer;

import mbond.travelprofile.DataModel.PlacePredictions;
import mbond.travelprofile.R;


public class AutoCompleteAdapter extends ArrayAdapter<PlacePredictions.PlaceAutoComplete> {
    ViewHolder holder;
    Context context;
    List<PlacePredictions.PlaceAutoComplete> Places;
    private Activity mActivity;

    public AutoCompleteAdapter(Context context, List<PlacePredictions.PlaceAutoComplete> modelsArrayList, Activity activity) {
        super(context, R.layout.autocomplete_row, modelsArrayList);
        this.context = context;
        this.Places = modelsArrayList;
        this.mActivity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.autocomplete_row, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) rowView.findViewById(R.id.place_name);
            holder.location = (TextView) rowView.findViewById(R.id.place_detail);
            rowView.setTag(holder);

        } else
            holder = (ViewHolder) rowView.getTag();
        /***** Get each Model object from ArrayList ********/
        holder.Place = Places.get(position);
        StringTokenizer st = new StringTokenizer(holder.Place.getPlaceDesc(), ",");
        /************  Set Model values in Holder elements ***********/

        holder.name.setText(st.nextToken());
        String desc_detail = "";
        for (int i = 1; i < st.countTokens(); i++) {
            if (i == st.countTokens() - 1) {
                desc_detail = desc_detail + st.nextToken();
            } else {
                desc_detail = desc_detail + st.nextToken() + ",";
            }
        }
        holder.location.setText(desc_detail);
        return rowView;
    }

    @Override
    public int getCount() {
        return Places.size();
    }

    class ViewHolder {
        PlacePredictions.PlaceAutoComplete Place;
        TextView name, location;
    }
}