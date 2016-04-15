package com.gemeenterotterdam.bouwtrillingsmeter;

import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class Graph extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                String url = "https://www.rotterdam-vlg.com/mail_from_app.php";

                // Request a string response
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                // Result handling
                                Toast.makeText(Graph.this, "Mail verzonden", Toast.LENGTH_LONG).show();

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        // Error handling
                        System.out.println("Something went wrong!");
                        error.printStackTrace();

                    }
                });

                // Add the request to the queue
                Volley.newRequestQueue(Graph.this).add(stringRequest);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final int pointLimit = 2;
        private static Measurement measurement;
        private static int SensorUpdate = 0;
        private static Complex[] MeasurementArray = new Complex[128];

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public static double max(double[] ar) {
            double max = ar[0];

            for (int i = 0; i < ar.length; i++) {
                if (ar[i] > max) {
                    max = ar[i];
                }
            }

            return max;
        }

        public void onMeasurementUpdate(float x, float y, float z) {
            if (getView() != null) {

                TextView textView = (TextView) getView().findViewById(R.id.calc_result);
                LineChart chart = (LineChart) getView().findViewById(R.id.chart);
                YAxis leftAxis = chart.getAxisLeft();
                Toast alarm = new Toast(getActivity());

                ArrayList<Entry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<String>();

                float Fs = 100;
                float T = 1/Fs;
                float L = 128;

                float freq_step = 1/(T*L);

                MeasurementArray[SensorUpdate] = new Complex(x, 0);

                if (SensorUpdate == 127) {
                    double MeasurementAbsolute[] = new double[127];

                    Complex[] MeasurementArrayResult = FFT.fft(MeasurementArray);

                    double max = 0;
                    double maxFreq = 0;
                    for (int i = 1; i < (MeasurementArrayResult.length/2); i++) {
                        double rs = (float)MeasurementArrayResult[i].abs()/L;

                        double point = rs * 100;

                        if (point > pointLimit) {
                            Toast.makeText(getActivity(), "Alarm", Toast.LENGTH_SHORT).show();
                        }

                        entries.add(new Entry((float)point, i));
                        labels.add(Double.toString(i * freq_step));
                    }

                    LineDataSet dataset = new LineDataSet(entries, "# of Calls");
                    dataset.setDrawFilled(true);

                    LineData data = new LineData(labels, dataset);
                    chart.setData(data); // set the data and list of lables into chart
                    chart.animateY(50);
                    chart.invalidate();

                    SensorUpdate = 0;
                } else {
                    SensorUpdate++;
                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
            LineChart chart = (LineChart) rootView.findViewById(R.id.chart);
            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setAxisMaxValue(6);

            LimitLine ll = new LimitLine(2f, "Verhoogde kans op schade");
            ll.setLineColor(Color.RED);
            ll.setLineWidth(2f);
            ll.setTextColor(Color.BLACK);
            ll.setTextSize(12f);
            leftAxis.addLimitLine(ll);

            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setVisibility(View.INVISIBLE);
            // textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            textView = (TextView) rootView.findViewById(R.id.calc_result);

            if (measurement == null) {
                measurement = new Measurement(getActivity());
            }
            textView.setVisibility(View.INVISIBLE);
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                measurement.setCallback(this);
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                textView.setVisibility(View.INVISIBLE);
            }

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}

