package ar.com.bahiavalley.nfc;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;


public class home extends AppCompatActivity{

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    Location location;
    String lat;
    String provider;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Context context = this.getBaseContext();

        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            // adapter exists and is enabled.
        }else{
            Toast.makeText(getApplicationContext(), "Activa el nfc y vuelve atras!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }

    }

    public void ubicarlo(View v){

    }

    public void tocreateTag(View v){
        Intent i = new Intent(this, crearTag.class );
        startActivity(i);
    }
    public void toreadTag(View v){
        Intent i = new Intent(this, leerTag.class );
        startActivity(i);
    }

    public void toreadTagMF(View v){
        Intent i = new Intent(this, leetTagMF.class );
        startActivity(i);
    }

    public void tocreateTagMF(View v){
        Intent i = new Intent(this, crearTagMF.class );
        startActivity(i);
    }

}
