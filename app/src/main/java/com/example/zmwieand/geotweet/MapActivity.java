package com.example.zmwieand.geotweet;

import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


public class MapActivity extends ActionBarActivity {

    private GoogleMap _map;
    private EditText _searchBar;
    private Marker _searchLocation;
    private Twitter _twitter;
    private Query _query;
    private static final double RADIUS = 50.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        _map  = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
        _searchBar = (EditText)findViewById(R.id.searchBar);

        setupTwitter();
        setupMap();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void setupMap(){

        _searchLocation = _map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .draggable(true));


        _map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {

                final String search = _searchBar.getText().toString();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        GeoLocation location = new GeoLocation(latLng.latitude, latLng.longitude);
                        _query.query(search);
                        _query.setGeoCode(location, RADIUS, _query.MILES);

                        try {
                            QueryResult result = _twitter.search(_query);
                            for(Status status : result.getTweets()){
                                Log.d("Tweet", "@" + status.getUser().getScreenName() + ":" + status.getText());
                            }
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }

                    }
                });
                t.start();

                _searchLocation.setPosition(latLng);
                _searchLocation.setTitle("'" + search + "'");
            }
        });
    }

    public void setupTwitter(){
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("WYijQ1OlMq0JSSuZdK7etBLSt")
          .setOAuthConsumerSecret("lYJ28HRv6R2BSOKrdGzelWIxvkd3zjwrsPHil0gIP9ju5Q02S6")
          .setOAuthAccessToken("365885253-KDWTVHqHRuXXznxuzJLeLTaaStNBUEGPrfQWeMVP")
          .setOAuthAccessTokenSecret("BMLSCwUbua8vC69LPcpUx2pKHiVPHMy6092p2pMVSSLNw");

        TwitterFactory tf = new TwitterFactory(cb.build());
        _twitter = tf.getInstance();

        _query = new Query("source:twitter4j yusukey");
        _query.setCount(10);
    }
}