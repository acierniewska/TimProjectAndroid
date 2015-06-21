package pl.edu.wat.dresscodeapp.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import pl.edu.wat.dresscodeapp.MainActivity;
import pl.edu.wat.dresscodeapp.R;

public class AddClothesFragment extends android.support.v4.app.Fragment {
    Button picButton;
    ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_clothes, container, false);

        picButton = (Button) rootView.findViewById(R.id.picButton);
        imageView = (ImageView) rootView.findViewById(R.id.addedPic);

        preparePicButtons(rootView);

        return rootView;
    }

    private void preparePicButtons(View v) {
        picButton = (Button) v.findViewById(R.id.picButton);
        picButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickpic();
            }
        });
    }

    public void clickpic() {
        if (getActivity().getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 100);

        } else {
            Toast.makeText(getActivity(), "Aparat nie jest wspierany w tym urz¹dzeniu", Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == MainActivity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }
}
