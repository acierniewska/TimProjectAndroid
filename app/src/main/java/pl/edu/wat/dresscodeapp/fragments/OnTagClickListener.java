package pl.edu.wat.dresscodeapp.fragments;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public class OnTagClickListener implements AdapterView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArrayAdapter a = (ArrayAdapter) parent.getAdapter();
        String selectedItem = (String) a.getItem(position);
        if (a != null) {
            a.remove(selectedItem);
            a.notifyDataSetChanged();
        }
    }
}
