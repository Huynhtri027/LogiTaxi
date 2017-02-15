package mbond.travelprofile.DataModel;

import java.util.ArrayList;

/**
 * Created by Kyra on 1/11/2016.
 */
public class PlacePredictions {

    private ArrayList<PlaceAutoComplete> predictions;

    public ArrayList<PlaceAutoComplete> getPlaces() {
        return predictions;
    }

    public void setPlaces(ArrayList<PlaceAutoComplete> places) {
        this.predictions = places;
    }

    /**
     * Created by Kyra on 1/11/2016.
     */
    public static class PlaceAutoComplete {

        private String place_id;
        private String description;
        private ArrayList<PlaceTerm> terms;

        public ArrayList<PlaceTerm> getTerms() {
            return terms;
        }

        public void setTerms(ArrayList<PlaceTerm> terms) {
            this.terms = terms;
        }

        public String getPlaceDesc() {
            return description;
        }

        public void setPlaceDesc(String placeDesc) {
            description = placeDesc;
        }

        public String getPlaceID() {
            return place_id;
        }

        public void setPlaceID(String placeID) {
            place_id = placeID;
        }

    }
}
