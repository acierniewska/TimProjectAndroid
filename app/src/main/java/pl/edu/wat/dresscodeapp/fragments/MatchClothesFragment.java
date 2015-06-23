package pl.edu.wat.dresscodeapp.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import pl.edu.wat.dresscodeapp.R;

public class MatchClothesFragment extends android.support.v4.app.Fragment implements View.OnTouchListener, LocationListener {
    final GestureDetector gestureDetector = new GestureDetector(new GestureListener());
    LocationManager locationManager;
    Location location;
    int temp = 666;

    private int matchedId;

    Button findClothesButton;
    Button confirmButton;

    ImageView matchedClothes;
    int currentClothesPic = 0;
    List<Bitmap> clothesPics = new ArrayList<>();

    Spinner event;
    List<String> events = new ArrayList<>();
    ArrayAdapter<String> eventsAdapter;

    RatingBar ratingBar;

    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        new HttpAsyncTask().execute("http://192.168.0.31:8080/timProject/rest/event/get");

        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&units=metric";
        new HttpAsyncTask().execute(url);

        View rootView = inflater.inflate(R.layout.fragment_match_clothes, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        ratingBar.setVisibility(View.INVISIBLE);

        matchedClothes = (ImageView) rootView.findViewById(R.id.matchedClothes);
        matchedClothes.setOnTouchListener(this);
        matchedClothes.setVisibility(View.INVISIBLE);

        rootView.findViewById(R.id.noMatchFind).setVisibility(View.INVISIBLE);

        prepareConfirmButtons(rootView);
        prepareFindClothesButton(rootView);
        prepareEventSpinner(rootView);

        return rootView;
    }


    private void prepareEventSpinner(View rootView) {
        event = (Spinner) rootView.findViewById(R.id.events);
        eventsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, events);
        eventsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        event.setAdapter(eventsAdapter);
    }

    private void prepareFindClothesButton(View v) {
        findClothesButton = (Button) v.findViewById(R.id.findClothesButton);
        findClothesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.textView5).setVisibility(View.INVISIBLE);
                event.setVisibility(View.INVISIBLE);
                findClothesButton.setVisibility(View.INVISIBLE);

                String eventName = (String) event.getSelectedItem();
                eventName = eventName.replace(" ", "%20");
                new HttpAsyncTask().execute("http://192.168.0.31:8080/timProject/rest/clothes/match?eventName=" + eventName + "&temp=" + temp + "");
            }
        });
    }

    private void prepareConfirmButtons(View v) {
        confirmButton = (Button) v.findViewById(R.id.confirmButton);
        confirmButton.setVisibility(View.INVISIBLE);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rate = ratingBar.getNumStars();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("matchedId", matchedId);
                    jsonObject.put("rate", rate);
                    new HttpAsyncPost().execute("http://192.168.0.31:8080/timProject/rest/clothes/rate", jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        });
    }


    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            onTouch(e);
            return true;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        if (currentClothesPic < clothesPics.size()) {
                            matchedClothes.setImageBitmap(clothesPics.get(currentClothesPic));
                        } else if (matchedId != -1L){
                            matchedClothes.setVisibility(View.INVISIBLE);
                            ratingBar.setVisibility(View.VISIBLE);
                            confirmButton.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    // onTouch(e);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        }
    }

    public void onTouch(MotionEvent e) {
    }

    public void onSwipeRight() {
        if (currentClothesPic == 0 || currentClothesPic == clothesPics.size()) {
            return;
        }

        currentClothesPic--;

    }

    public void onSwipeLeft() {
        if (currentClothesPic == 0 || currentClothesPic == clothesPics.size()) {
            return;
        }

        currentClothesPic++;

    }

    public static String makeRequest(String uri, String json) {
        try {
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(json, "utf-8"));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Charset", "utf-8");
            HttpResponse response = new DefaultHttpClient().execute(httpPost);
            if (response != null)
                return response.getStatusLine().getReasonPhrase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    private class HttpAsyncPost extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return makeRequest(urls[0], urls[1]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getActivity(), "Zatwierdzono zmiany.", Toast.LENGTH_LONG).show();
        }
    }


    public static String GET(String url) {
        InputStream inputStream;
        String result = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }


    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();

        return result;

    }


    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                if (result.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(result);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject myJson = jsonArray.getJSONObject(i);
                        if (myJson.has("eventName")) {
                            events.add(myJson.getString("eventName"));
                            eventsAdapter.notifyDataSetChanged();
                        }
                    }
                } else if (result.startsWith("{")) {
                    JSONObject myJson = new JSONObject(result);
                    if (myJson.has("main")) {
                        myJson = myJson.getJSONObject("main");
                        Object t = myJson.get("temp");
                        if (t instanceof Integer) {
                            temp = (Integer) t;
                        } else if (t instanceof Double) {
                            temp = ((Double) t).intValue();
                        }
                    } else if (myJson.has("matchedId")) {
                        matchedId = Integer.valueOf(myJson.getString("matchedId"));
                        JSONArray jsonArray = myJson.getJSONArray("matchedClothes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            myJson = jsonArray.getJSONObject(i);
                            byte[] decodedByte = Base64.decode(myJson.getString("clothesPic"), 0);
                            Bitmap clothesPic = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
                            clothesPics.add(clothesPic);
                        }
                        if (clothesPics.size() > 0) {
                            matchedClothes.setImageBitmap(clothesPics.get(currentClothesPic));
                        } else {
                            getActivity().findViewById(R.id.noMatchFind).setVisibility(View.VISIBLE);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        matchedClothes.setVisibility(View.VISIBLE);

                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "Brak po³¹czenia z serwerem.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
