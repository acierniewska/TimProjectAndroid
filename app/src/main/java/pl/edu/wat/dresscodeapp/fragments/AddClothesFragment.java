package pl.edu.wat.dresscodeapp.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.edu.wat.dresscodeapp.MainActivity;
import pl.edu.wat.dresscodeapp.R;

public class AddClothesFragment extends android.support.v4.app.Fragment {
    Button picButton;
    Button addButton;

    ImageView imageView;
    EditText edit;

    Spinner clothesTypes;
    List<String> clothesTypesList = new ArrayList<>();
    ArrayAdapter<String> clothesTypesAdapter;

    Spinner colours;
    List<String> coloursList = new ArrayList<>();
    ArrayAdapter<String> colourAdapter;

    MultiAutoCompleteTextView tags;
    List<String> tagsList = new ArrayList<>();

    ProgressBar progressBar;

    TextView tv1;
    TextView tv2;
    TextView tv3;
    TextView tv4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_clothes, container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar3);


        new HttpAsyncTask().execute("http://192.168.0.31:8080/timProject/rest/clothesTypes/get");
        new HttpAsyncTask().execute("http://192.168.0.31:8080/timProject/rest/colour/get");
        new HttpAsyncTask().execute("http://192.168.0.31:8080/timProject/rest/tag/get");

        imageView = (ImageView) rootView.findViewById(R.id.addedPic);
        edit = (EditText) rootView.findViewById(R.id.editText);
        prepareAddButtons(rootView);
        preparePicButtons(rootView);

        prepareClothesTypeSpinner(rootView);
        prepareColoursSpinner(rootView);
        prepareTags(rootView);

        tv1 = (TextView) rootView.findViewById(R.id.textView);
        tv2 = (TextView) rootView.findViewById(R.id.textView2);
        tv3 = (TextView) rootView.findViewById(R.id.textView3);
        tv4 = (TextView) rootView.findViewById(R.id.textView4);

        setVisibility(View.INVISIBLE);

        return rootView;
    }

    private void setVisibility(int visibility) {
        tags.setVisibility(visibility);
        clothesTypes.setVisibility(visibility);
        imageView.setVisibility(visibility);
        colours.setVisibility(visibility);
        addButton.setVisibility(visibility);
        picButton.setVisibility(visibility);
        edit.setVisibility(visibility);
        tv1.setVisibility(visibility);
        tv2.setVisibility(visibility);
        tv3.setVisibility(visibility);
        tv4.setVisibility(visibility);
    }

    private void prepareTags(View rootView) {
        tags = (MultiAutoCompleteTextView) rootView.findViewById(R.id.tags);
        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, tagsList);
        tags.setAdapter(tagAdapter);
        tags.setThreshold(1);
        tags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        tags.setOnItemClickListener(new OnTagClickListener());
    }

    private void prepareColoursSpinner(View rootView) {
        colours = (Spinner) rootView.findViewById(R.id.colours);
        colourAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, coloursList);
        colourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colours.setAdapter(colourAdapter);
    }

    private void prepareClothesTypeSpinner(View rootView) {
        clothesTypes = (Spinner) rootView.findViewById(R.id.clothesTypes);
        clothesTypesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, clothesTypesList);
        clothesTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clothesTypes.setAdapter(clothesTypesAdapter);
    }

    private void preparePicButtons(View v) {
        picButton = (Button) v.findViewById(R.id.picButton);
        picButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity().getApplicationContext().getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_CAMERA)) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 100);

                } else {
                    Toast.makeText(getActivity(), "Aparat nie jest wspierany w tym urz¹dzeniu", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void prepareAddButtons(View v) {
        addButton = (Button) v.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String clothesDesc = edit.getText().toString();
                String colour = (String) colours.getSelectedItem();
                String typ = (String) clothesTypes.getSelectedItem();
                List<String> tagsList = parseTags(tags.getText().toString());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                ((BitmapDrawable) imageView.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] image = stream.toByteArray();
                String base64String = Base64.encodeToString(image, Base64.DEFAULT);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("colourName", colour);
                    jsonObject.put("clothesTypeName", typ);
                    jsonObject.put("clothesDesc", clothesDesc);
                    jsonObject.put("clothesTags", tagsList);
                    jsonObject.put("clothesPic", base64String);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.print(jsonObject.toString());

                new HttpAsyncPost().execute("http://192.168.0.31:8080/timProject/rest/clothes/post", jsonObject.toString());
                new HttpAsyncTask().execute("http://192.168.0.31:8080/timProject/rest/tag/get");
            }

            private List<String> parseTags(String text) {
                List<String> tagsList = new ArrayList<>();
                text = text.trim();
                String[] ts = text.trim().substring(0, text.length() - 1).split(",");
                Collections.addAll(tagsList, ts);

                return tagsList;
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == MainActivity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
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
            Toast.makeText(getActivity(), "Dodano nowe ubranie!", Toast.LENGTH_LONG).show();
            imageView.setImageBitmap(null);
            tags.setText(null);
            clothesTypes.setSelection(0);
            colours.setSelection(0);
            ((EditText) getActivity().findViewById(R.id.editText)).setText(null);
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
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject myJson = jsonArray.getJSONObject(i);
                    if (myJson.has("tagName")) {
                        tagsList.add(myJson.getString("tagName"));
                    } else if (myJson.has("clothesTypeName")) {
                        clothesTypesList.add(myJson.getString("clothesTypeName"));
                        if (clothesTypesAdapter != null) {
                            clothesTypesAdapter.notifyDataSetChanged();
                        }
                    } else if (myJson.has("colourName")) {
                        coloursList.add(myJson.getString("colourName"));
                        if (colourAdapter != null) {
                            colourAdapter.notifyDataSetChanged();
                        }
                    }
                    if (!coloursList.isEmpty() && !clothesTypesList.isEmpty() && !tagsList.isEmpty()) {
                        setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
