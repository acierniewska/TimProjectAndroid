package pl.edu.wat.dresscodeapp.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.wat.dresscodeapp.R;

public class AllClothesFragment extends android.support.v4.app.Fragment implements View.OnTouchListener {
    final GestureDetector gestureDetector = new GestureDetector(new GestureListener());

    List<Bitmap> clothesPics = new ArrayList<>();
    Map<Bitmap, List<String>> map = new HashMap<>();
    ImageView imageView;
    int currentClothesPic = 0;

    ProgressBar progressBar;
    TextView msg;

    public AllClothesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_all_clothes, container, false);

        imageView = (ImageView) rootView.findViewById(R.id.addedPic);
        imageView.setOnTouchListener(this);
        imageView.setVisibility(View.INVISIBLE);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar2);
        msg = (TextView) rootView.findViewById(R.id.textView6);
        msg.setVisibility(View.INVISIBLE);

        new HttpAsyncTask().execute("http://192.168.0.31:8080/timProject/rest/clothes/get");

        return rootView;
    }


    @Override
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
                        imageView.setImageBitmap(clothesPics.get(currentClothesPic));
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return false;
        }
    }

    public void onTouch(MotionEvent e) {
        List<String> tags = map.get(((BitmapDrawable) imageView.getDrawable()).getBitmap());
        StringBuilder builder = new StringBuilder("Tagi: ");
        for (String t : tags){
            builder.append(t + " ");
        }
        Toast.makeText(getActivity(), builder.toString(), Toast.LENGTH_LONG).show();
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


    public static String GET(String url) {
        InputStream inputStream;
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
                    JSONArray newArray = myJson.getJSONArray("clothesTags");
                    List<String> tags = new ArrayList<>();
                    for (int j = 0; j < newArray.length(); j++){
                        myJson = newArray.getJSONObject(j);
                        tags.add(myJson.getString("tagName"));
                    }
                    clothesPics.add(clothesPic);
                    map.put(clothesPic, tags);
                }
                if (!clothesPics.isEmpty()) {
                    imageView.setImageBitmap(clothesPics.get(currentClothesPic));
                    imageView.setVisibility(View.VISIBLE);
                }

            } catch (JSONException e) {
                msg.setVisibility(View.VISIBLE);
                e.printStackTrace();
            } finally {
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
}
