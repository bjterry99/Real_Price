package com.bjterry99gmail.realprice;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.app.FragmentTransaction;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {
    FragmentTransaction fragmentTransaction;

    String dis = "Disclaimers: \n" + "-Real Price only works in the USA.\n" + "-Real Price does not guarantee 100% accurate tax rates.\n" + "-Real Price will pause to find sales tax rates in large states. \n" + "-Real Price is developed and maintained with publicly available information.\n" + "-Some products (like groceries) may not have any sales tax.\n" + "-Some products may add an additional tax.\n" + "-'Real Price' is the intellectual property and copyright of Desert Delta Technologies.";
    String text1 = "Thank you for installing Real Price!\n" + "\n" + "Real Price was conceived when one of our developers tried to find an app to calculate the sales tax of a purchase based on specific location, but was unable to find such app. We decided to take initiative and create our own!\n" + "\n" + "Real Price will continue to be updated with more features in the future.\n" + "\n" + "To report any bugs, please send an email to realpriceapp@gmail.com";
    private ConstraintLayout layout;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_about, container, false);

        TextView disclaim = v.findViewById(R.id.disclaimer);
        Button change = v.findViewById(R.id.change);
        TextView text = v.findViewById(R.id.text);
        layout = v.findViewById(R.id.layout);
        disclaim.setText(dis);
        text.setText(text1);

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new changefrag());
                fragmentTransaction.commit();

            }
        });

        return v;
    }

}
