package ar.com.bahiavalley.nfc;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class crearTag extends AppCompatActivity {
    SoundPool sp;
    int yourSound = 0;
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;
    TextView info;
    Button btnWrite;
    String gpsActual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tag);

        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        yourSound = sp.load(this, R.raw.beep, 1);


        btnWrite = (Button)findViewById(R.id.button);
        final EditText message = (EditText)findViewById(R.id.edit_message);
        info = (TextView)findViewById(R.id.txtInfo);

        //setOnCLickListener hará la acción que necesitamos
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    //Si no existe tag al que escribir, mostramos un mensaje de que no existe.
                    if(myTag == null){
                        Toast.makeText(context, context.getString(R.string.error_notag), Toast.LENGTH_LONG).show();
                    }else{
                        //Llamamos al método write que definimos más adelante donde le pasamos por
                        //parámetro el tag que hemos detectado y el mensaje a escribir.
                        GPSTracker gps = new GPSTracker(crearTag.this);

                        if(gps.canGetLocation()) {
                            double latitude = gps.getLatitude();
                            double longitude = gps.getLongitude();

                            gpsActual = ", 'LAT' : '"+ latitude +"','LON' : '"+ longitude +"'";
                        } else {

                        }

                        write(Encriptacion.encriptar("{ 'ID' : " + message.getText().toString() + gpsActual + "}"),myTag);

                        if(yourSound != 0)
                            sp.play(yourSound, 1, 1, 0,0, 1);


                    }
                }catch(IOException e){
                    Toast.makeText(context, context.getString(R.string.error_write),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }catch(FormatException e){
                    Toast.makeText(context, context.getString(R.string.error_write), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};



    }

    //El método write es el más importante, será el que se encargue de crear el mensaje
    //y escribirlo en nuestro tag.
    private void write(String text, Tag tag) throws IOException, FormatException{
        //Creamos un array de elementos NdefRecord. Este Objeto representa un registro del mensaje NDEF
        //Para crear el objeto NdefRecord usamos el método createRecord(String s)
        NdefRecord[] records = {createRecord(text)};
        //NdefMessage encapsula un mensaje Ndef(NFC Data Exchange Format). Estos mensajes están
        //compuestos por varios registros encapsulados por la clase NdefRecord
        NdefMessage message = new NdefMessage(records);
        //Obtenemos una instancia de Ndef del Tag
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }
    //Método createRecord será el que nos codifique el mensaje para crear un NdefRecord
    @SuppressLint("NewApi") private NdefRecord createRecord(String text) throws UnsupportedEncodingException, UnsupportedEncodingException {
        String lang = "us";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payLoad = new byte[1 + langLength + textLength];

        payLoad[0] = (byte) langLength;

        System.arraycopy(langBytes, 0, payLoad, 1, langLength);
        System.arraycopy(textBytes, 0, payLoad, 1+langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payLoad);

        return recordNFC;

    }
    //en onnewIntent manejamos el intent para encontrar el Tag
    @SuppressLint("NewApi") protected void onNewIntent(Intent intent){
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Toast.makeText(this, this.getString(R.string.ok_detected) + myTag.toString(), Toast.LENGTH_LONG).show();
            if(yourSound != 0)
                sp.play(yourSound, 1, 1, 0,0, 1);

            info.setText(myTag.toString() + " " +  myTag.getTechList());
            btnWrite.setEnabled(true);

        }
    }

    public void onPause(){
        super.onPause();
        WriteModeOff();
    }
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }




    @SuppressLint("NewApi") private void WriteModeOn(){
        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);

    }

    @SuppressLint("NewApi") private void WriteModeOff(){
        writeMode = false;
        adapter.disableForegroundDispatch(this);
    }





}
