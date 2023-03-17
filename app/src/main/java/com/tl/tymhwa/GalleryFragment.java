package com.tl.tymhwa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class GalleryFragment extends Fragment {
    private TextView textView;

    public GalleryFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
//        textView = root.findViewById(R.id.text_gallery);
//        textView.setText("This is GALLERY fragment!");

        return root;
    }

    public void setText(String text){textView.setText(text);}
}