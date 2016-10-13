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

public class leerTag extends AppCompatActivity {
    SoundPool sp;
    int yourSound = 0;
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;
    TextView info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leer_tag);

        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        yourSound = sp.load(this, R.raw.beep, 1);

        info = (TextView)findViewById(R.id.txtContenido);

        //setOnCLickListener hará la acción que necesitamos


        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};



    }

    //El método write es el más importante, será el que se encargue de crear el mensaje
    //y escribirlo en nuestro tag.

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

            Ndef ndef = Ndef.get(myTag);

            if (ndef == null) {
                // NDEF is not supported by this Tag.

            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        info.setText("Contenido: " + Encriptacion.desencriptar(readText(ndefRecord)));
                    } catch (UnsupportedEncodingException e) {
                        Log.e("error", "Unsupported Encoding", e);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

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


    public void onPostExecute(String result) {
        if (result != null) {
            info.setText("Read content: " + result);
        }
    }


    @SuppressLint("NewApi") private void WriteModeOn(){
        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);

    }

    @SuppressLint("NewApi") private void WriteModeOff(){
        writeMode = false;
        adapter.disableForegroundDispatch(this);
    }

    public String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        String go;
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);

    }



}
