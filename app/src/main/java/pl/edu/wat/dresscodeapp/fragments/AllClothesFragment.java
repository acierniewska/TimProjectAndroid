package pl.edu.wat.dresscodeapp.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

public class AllClothesFragment extends android.support.v4.app.Fragment implements View.OnTouchListener {
    final GestureDetector gestureDetector = new GestureDetector(new GestureListener());

    List<Bitmap> clothesPics = new ArrayList<>();
    ImageView imageView;
    int currentClothesPic = 0;

    public AllClothesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_all_clothes, container, false);

        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        imageView.setOnTouchListener(this);
        new HttpAsyncTask().execute("http://192.168.0.31:8080/timProject/rest/clothes/get");

        return rootView;
    }

    public static String GET(String url) {
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            // convert inputstream to string
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
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();

        return result;

    }

    public boolean onTouch(final View v, final MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
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
            boolean result = false;
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
                      //  imageView = (ImageView) getActivity().findViewById(R.id.imageView);
                        imageView.setImageBitmap(clothesPics.get(currentClothesPic));
                    }
                } else {
                    // onTouch(e);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onTouch(MotionEvent e) {
    }

    public void onSwipeRight() {
        if (currentClothesPic == 0) {
            return;
        }

        currentClothesPic--;

    }

    public void onSwipeLeft() {
        if (currentClothesPic == clothesPics.size() - 1) {
            return;
        }

       currentClothesPic++;

    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
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
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject myJson = jsonArray.getJSONObject(i);
                    byte[] decodedByte = Base64.decode(myJson.getString("clothesPic"), 0);
                    Bitmap clothesPic = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
                    if (clothesPic == null) {
                        Toast.makeText(getActivity(), "Bitmapa to null", Toast.LENGTH_LONG).show();
                    } else {
                        clothesPics.add(clothesPic);
                    }
                }

                imageView.setImageBitmap(clothesPics.get(currentClothesPic));

            } catch (JSONException e) {
                Toast.makeText(getActivity(), "dupa dupa", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
