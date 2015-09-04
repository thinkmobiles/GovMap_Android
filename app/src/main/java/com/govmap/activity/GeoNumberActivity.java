package com.govmap.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.govmap.MainApplication;
import com.govmap.R;
import com.govmap.model.DataObject;
import com.govmap.model.GeocodeResponse;
import com.govmap.utils.GeocodeClient;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by MediumMG on 01.09.2015.
 */
public class GeoNumberActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final String NO_RESULT_FOUND_HE = "לא נמצאו תוצאות מתאימות";

    private EditText etBlock, etSmooth;
    private Button btnSearch;

    private GeoNumberReceiver mReceiver;

    private DataObject mDataObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geonumber);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etBlock = (EditText) findViewById(R.id.etBlock_AGN);
        etSmooth = (EditText) findViewById(R.id.etSmooth_AGN);
        btnSearch = (Button) findViewById(R.id.btnSearch_AGN);

        etSmooth.setOnEditorActionListener(GeoNumberActivity.this);
        btnSearch.setOnClickListener(GeoNumberActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mReceiver = new GeoNumberReceiver();
        IntentFilter intentFilter = new IntentFilter(MainApplication.ACTION_INNER_ADDRESS);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (checkData())
            callRequest();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (checkData())
            callRequest();
        return true;
    }

    private void goToMap() {
        Log.v(MainApplication.TAG, mDataObject.toString());

        Intent intent = new Intent(GeoNumberActivity.this, MapActivity.class);
        intent.putExtra(MainApplication.EXTRA_DATA_OBJECT, mDataObject);
        startActivity(intent);
        finish();
    }

    private boolean checkData() {
        if (TextUtils.isEmpty(etBlock.getText())) {
            etBlock.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etSmooth.getText())) {
            etSmooth.requestFocus();
            return false;
        }
        return true;
    }

    private void callRequest() {
        mDataObject = new DataObject();

        String block = String.valueOf(etBlock.getText());
        String smooth = String.valueOf(etSmooth.getText());

        String cadastralString = String.format(getString(R.string.req_for_nubmer_format1), block, smooth);

        Log.v(MainApplication.TAG, cadastralString);
        mDataObject.setCadastre(cadastralString);

        ((MainApplication) getApplication()).startCadastreSearch(cadastralString);
    }


    private class GeoNumberReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainApplication.ACTION_INNER_ADDRESS.equals(intent.getAction())) {
                ((MainApplication) getApplication()).clearResults();

                String address = intent.getStringExtra(MainApplication.EXTRA_DATA_ADDRESS);

                if (NO_RESULT_FOUND_HE.equals(address)) {
                    // no results found
                    Toast.makeText(GeoNumberActivity.this, NO_RESULT_FOUND_HE, Toast.LENGTH_LONG).show();
                }
                else {
                    // Get coordinates;
                    mDataObject.setAddress(address);

                    GeocodeClient.get().getGeocodeByAddress(address.replace(" ", "+"), new GeocodeCallback()) ;
                }
            }
        }
    }

    private class GeocodeCallback implements Callback<GeocodeResponse> {

        @Override
        public void success(GeocodeResponse geocodeResponse, Response response) {
            Log.v(MainApplication.TAG, geocodeResponse.toString());
            mDataObject.setLatitude(geocodeResponse.results.get(0).geometry.location.lat);
            mDataObject.setLongitude(geocodeResponse.results.get(0).geometry.location.lng);

            goToMap();
        }

        @Override
        public void failure(RetrofitError error) {
            Toast.makeText(GeoNumberActivity.this, NO_RESULT_FOUND_HE, Toast.LENGTH_LONG).show();
        }
    }

}
