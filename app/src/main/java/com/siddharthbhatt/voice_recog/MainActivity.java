package com.siddharthbhatt.voice_recog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;


public class MainActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{


    LocationClient mLocationClient;
    //voice recognition and general variables

    //variable for checking Voice Recognition support on user device
    private static final int VR_REQUEST = 999;

    //ListView for displaying suggested words
    private ListView wordList;

    //Log tag for output information
    private final String LOG_TAG = "SpeechRepeatActivity";
    private static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;

    //TTS variables

    //variable for checking TTS engine data on user device
    private int MY_DATA_CHECK_CODE = 0;

    //Text To Speech instance
    private TextToSpeech repeatTTS;

    //For Audio Capture
    private MediaRecorder myAudioRecorder;
    private String outputFile = null;

    //On-Screen Assests
    private Button settingButton;
    private Button voice_button;
    private Button aboutButton;
    private Button insButton;

    private void listenToSpeech() {

        //start the speech recognition intent passing required data
        Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //indicate package
        listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        //message to display while listening
        listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a word!");
        //set speech model
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //specify number of results to retrieve
        listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        //start listening
        startActivityForResult(listenIntent, VR_REQUEST);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stop recording if it was enabled
        if(Boolean.parseBoolean(sharedpreferences.getString("recordAudioBoolean","true"))) {
            stopAudioRecording();
        }
        if(Boolean.parseBoolean(sharedpreferences.getString("sendLocation","true"))) {
            disconnectToLocationService();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check speech recognition result
        if (requestCode == VR_REQUEST && resultCode == RESULT_OK)
        {
            //store the returned word list as an ArrayList
            ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //set the retrieved list to display in the ListView using an ArrayAdapter
            wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.word, suggestedWords));
            //call the function to process words
            process(suggestedWords);
        }

        //tss code here

        //call superclass method
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        insButton = (Button) findViewById(R.id.insButton);
        aboutButton = (Button) findViewById(R.id.aboutButton);
        settingButton = (Button) findViewById(R.id.settingsButton);
        voice_button = (Button) findViewById(R.id.voice_button);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/emergency.3gp";
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);

        insButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Instructions.class);
                startActivity(intent);
            }
        });

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), About.class);
                startActivity(intent);
            }
        });

        mLocationClient = new LocationClient(this, this, this);
        if(Boolean.parseBoolean(sharedpreferences.getString("sendLocation","true"))) {
            connectToLocationService();
        }

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
            }
        });

        //find out whether speech recognition is supported
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> intActivities = packManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (intActivities.size() != 0) {
            //speech recognition is supported - detect user button clicks
            voice_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //listen for results
                    listenToSpeech();
                }
            });
        }
        else
        {
            //speech recognition not supported, disable button and output message
            voice_button.setEnabled(false);
            Toast.makeText(this, "Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
        }
    }

    public void process(ArrayList suggestedWords){

        Uri callring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), callring);

        for (int i = 0; i < suggestedWords.size(); i++) {

            if(suggestedWords.get(i).toString().contains("hello")) {
                Toast.makeText(this, "Hello to you too !", Toast.LENGTH_LONG).show();
            }else if(suggestedWords.get(i).toString().contains(sharedpreferences.getString("helpWord","help"))){

                makeCall();

                //send SMS if it is enabled
                if(Boolean.parseBoolean(sharedpreferences.getString("sendSMSBoolean","true"))) {
                    if(Boolean.parseBoolean(sharedpreferences.getString("sendLocation","true"))) {
                        setSMSContent();
                    }
                    sendSMSMessage();
                }

                //start audio recording if it is enabled
                if(Boolean.parseBoolean(sharedpreferences.getString("recordAudioBoolean", "true"))) {
                    startAudioRecording();
                }

            }else if(suggestedWords.get(i).toString().contains("ring")){
                r.play();
            }else if(suggestedWords.get(i).toString().contains("stop")){
                r.stop();
            }

        }

    }//method over

    private void setSMSContent() {
        String a = sharedpreferences.getString("smsContent","I Need help urgently. This is an automated message.");
        a = a + getCurrentLocation();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("smsContent", a);
        editor.commit();
    }

    private void connectToLocationService(){
        mLocationClient.connect();
    }

    private void disconnectToLocationService(){
        mLocationClient.disconnect();
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Location Service Connected", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Location Service Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Display the error code on failure
        Toast.makeText(this, "Location Service Connection Failure : " +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    public String getCurrentLocation() {
        // Get the current location's latitude & longitude
        Location currentLocation = mLocationClient.getLastLocation();
        String msg = "Current Location: " +
                Double.toString(currentLocation.getLatitude()) + "," +
                Double.toString(currentLocation.getLongitude());

       return msg;
    }

    protected void makeCall() {
        Log.i("Make call", "");

        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        String no = "tel:"+sharedpreferences.getString("localEmergency","");
        phoneIntent.setData(Uri.parse(no));
        try {
            startActivity(phoneIntent);
            //finish();
            Log.i("Finished making a call.", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "Call failed, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }//makeCall() over

    protected void sendSMSMessage() {
        Log.i("Send SMS", "");

        String phoneNo1 = sharedpreferences.getString("contactNumber1", "");
        String name1 = sharedpreferences.getString("ContactName1","");
        String phoneNo2 = sharedpreferences.getString("contactNumber2", "");
        String name2 = sharedpreferences.getString("ContactName2","");
        String str = "SMS sent to "+name1+" and "+name2;
        String message = sharedpreferences.getString("smsContent","I Need help urgently. This is an automated message.");

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo1, null, message, null, null);
            smsManager.sendTextMessage(phoneNo2, null, message, null, null);
            Toast.makeText(getApplicationContext(),str,
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS failed, please try again.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }//sendSMSMessage() over

    public void startAudioRecording(){
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();

    }//startAudioRecording() over

    public void stopAudioRecording(){
        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder  = null;
        Toast.makeText(getApplicationContext(), "Audio recorded successfully",
                Toast.LENGTH_LONG).show();
    }//stopAudioRecording() over


}//class over
