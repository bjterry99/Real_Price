package com.bjterry99gmail.realprice;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class changefrag extends Fragment {
    String one = "V1.0.0 - 2/27/18\n" + "-Calculate sales tax based on zip code. \n" + "-Copy result to clipboard. \n" + "-Display local sales tax rate and tax collected.";
    String title1 = "CHANGELOG";
    String two = "V1.0.1 - 2/28/18\n" + "-Fixed bug that caused app to close on back button press. \n" + "-Fixed bug that caused keyboard not to collapse.\n" + "-Fixed bug that caused UI elements to cover other elements.\n" + "-Changed the About icon.\n" + "-Removed Herobrine.\n";
    String three = "V1.1.0 - 3/4/18\n" + "-Added a Play Store link.\n" + "-Changed the Navigation header.\n" + "-Removed Herobrine.\n";

    public changefrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_changefrag, container, false);

        TextView title = v.findViewById(R.id.title);
        TextView V3 = v.findViewById(R.id.V3);
        TextView V2 = v.findViewById(R.id.V2);
        TextView V1 = v.findViewById(R.id.V1);
        title.setText(title1);
        V1.setText(one);
        V2.setText(two);
        V3.setText(three);

        return v;
    }

}
