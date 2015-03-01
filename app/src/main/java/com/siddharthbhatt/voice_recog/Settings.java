package com.siddharthbhatt.voice_recog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class Settings extends Activity {

    private EditText localEmergency;
    private EditText smsbox;
    private CheckBox recordAudio;
    private CheckBox sendSMScheckBox;
    private CheckBox locationCheckbox;
    private TextView contactName1;
    private TextView contactName2;
    private TextView serviceStatus;
    private TextView helpWordTextView;
    private Button contactPicker1;
    private Button contactPicker2;
    private Button serviceToggle;
    private Button helpWordButton;
    private Button saveButton;

    private Boolean recordAudioBoolean = true;
    private Boolean sendSMSBoolean = true;
    private Boolean sendLocation = true;
    private String LocalEmergency="100";
    private String smsContent="I Need help urgently. This is an automated message.";
    private String ContactName1="Not Selected";
    private String ContactName2="Not Selected";
    private String contactNumber1="";
    private String contactNumber2="";
    private String servicestatus="Stopped";
    private String helpWord="help";

    private String one;
    private String name;
    private String phoneNumber;
    private String contactId;
    private static final String MyPREFERENCES = "MyPrefs" ;
    public static final int PICK_CONTACT = 1;
    SharedPreferences sharedpreferences;

    //voice recognition and general variables
    //variable for checking Voice Recognition support on user device
    private static final int VR_REQUEST = 999;
    //ListView for displaying suggested words
    private ListView wordList;

    //TTS variables
    //variable for checking TTS engine data on user device
    private int MY_DATA_CHECK_CODE = 0;
    //Text To Speech instance
    private TextToSpeech repeatTTS;


    protected void setAssests(){

        //locate and fix assets
        localEmergency = (EditText) findViewById(R.id.localEmergencyNumber);
        smsbox = (EditText) findViewById(R.id.smsBox);
        recordAudio = (CheckBox) findViewById(R.id.recordAudiocheckBox);
        sendSMScheckBox = (CheckBox) findViewById(R.id.sendSMScheckBox);
        locationCheckbox = (CheckBox) findViewById(R.id.locationCheckbox);
        contactName1 = (TextView) findViewById(R.id.ContactName1);
        contactName2 = (TextView) findViewById(R.id.ContactName2);
        serviceStatus = (TextView) findViewById(R.id.serviceStatus);
        helpWordTextView = (TextView) findViewById(R.id.HelpWordTextView);
        contactPicker1 = (Button) findViewById(R.id.contactPicker1);
        contactPicker2 = (Button) findViewById(R.id.contactPicker2);
        serviceToggle = (Button) findViewById(R.id.toggleServiceButton);
        helpWordButton = (Button) findViewById(R.id.setHelpWordButton);
        saveButton = (Button) findViewById(R.id.saveButton);
    }

    public void writeToPref(){

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("localEmergency", LocalEmergency);
        editor.putString("contactName1", ContactName1);
        editor.putString("contactName2", ContactName2);
        editor.putString("contactNumber1", contactNumber1);
        editor.putString("contactNumber2", contactNumber2);
        editor.putString("smsContent", smsContent);
        editor.putString("serviceStatus", servicestatus);
        editor.putString("recordAudioBoolean", Boolean.toString(recordAudioBoolean));
        editor.putString("sendSMSBoolean", Boolean.toString(sendSMSBoolean));
        editor.putString("sendLocation", Boolean.toString(sendLocation));
        editor.putString("helpWord", helpWord);
        editor.commit();
    }

    protected void loadPref(){
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if(sharedpreferences.contains("localEmergency")){
            this.LocalEmergency = sharedpreferences.getString("localEmergency","100");
        }

        if(sharedpreferences.contains("contactName1")){
            this.ContactName1 = sharedpreferences.getString("contactName1","Not Selected");
        }

        if(sharedpreferences.contains("contactName2")){
            this.ContactName2 = sharedpreferences.getString("contactName2","Not Selected");
        }

        if(sharedpreferences.contains("contactNumber1")){
            this.contactNumber1 = sharedpreferences.getString("contactNumber1","");
        }

        if(sharedpreferences.contains("contactNumber2")){
            this.contactNumber2 = sharedpreferences.getString("contactNumber2","");
        }

        if(sharedpreferences.contains("smsContent")){
            this.smsContent = sharedpreferences.getString("smsContent","I Need help urgently. This is an automated message.");
        }

        if(sharedpreferences.contains("serviceStatus")){
            this.servicestatus = sharedpreferences.getString("serviceStatus","Stopped");
        }

        if(sharedpreferences.contains("recordAudioBoolean")){
            this.recordAudioBoolean = Boolean.parseBoolean(sharedpreferences.getString("recordAudioBoolean","true"));
        }

        if(sharedpreferences.contains("sendSMSBoolean")){
            this.sendSMSBoolean = Boolean.parseBoolean(sharedpreferences.getString("sendSMSBoolean","true"));
        }

        if(sharedpreferences.contains("sendLocation")){
            this.sendLocation = Boolean.parseBoolean(sharedpreferences.getString("sendLocation","true"));
        }

        if(sharedpreferences.contains("helpWord")){
            this.helpWord = sharedpreferences.getString("helpWord","help");
        }

    }

    protected void loadDatatoView(){

        if (!LocalEmergency.equals("")){

            localEmergency.setText(LocalEmergency);
        }

        if (!ContactName1.equals("")){

            contactName1.setText(ContactName1);
        }

        if (!ContactName2.equals("")){

            contactName2.setText(ContactName2);
        }

        if(!smsContent.equals("")){
            smsbox.setText(smsContent);
        }

        if(!servicestatus.equals("")){
            serviceStatus.setText(servicestatus);
        }

        if(!recordAudioBoolean.equals("")){
            recordAudio.setChecked(recordAudioBoolean);
        }

        if(!sendSMSBoolean.equals("")){
            sendSMScheckBox.setChecked(sendSMSBoolean);
        }

        if(!sendLocation.equals("")){
            locationCheckbox.setChecked(sendLocation);
        }

        if(!helpWord.equals("")){
            helpWordTextView.setText(helpWord);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setAssests();
        loadPref();
        loadDatatoView();

        //find out whether speech recognition is supported
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> intActivities = packManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (intActivities.size() != 0) {
            //speech recognition is supported - detect user button clicks
            helpWordButton.setOnClickListener(new View.OnClickListener() {
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
            helpWordButton.setEnabled(false);
            Toast.makeText(this, "Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
        }

        localEmergency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                LocalEmergency = localEmergency.getText().toString();
            }
        });

        smsbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                smsContent = smsbox.getText().toString();
            }
        });

        serviceToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start the service and then set the variable
                if (servicestatus.equals("Stopped")) {
                    startService();
                    servicestatus = "Running";
                } else {
                    stopService();
                    servicestatus = "Stopped";
                }
                //refresh the UI components to update the status
                loadDatatoView();
            }
        });

        recordAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    recordAudioBoolean = true;
                    Toast.makeText(getApplicationContext(), "App will record audio when listening \"help\". Please click 'save' button", Toast.LENGTH_LONG).show();
                } else {
                    recordAudioBoolean = false;
                    Toast.makeText(getApplicationContext(), "App will NOT record audio when listening \"help\".  Please click 'save' button", Toast.LENGTH_LONG).show();
                }
            }
        });

        sendSMScheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sendSMSBoolean = true;
                    Toast.makeText(getApplicationContext(),"App will send SMS when listening \"help\". Please click 'save' button", Toast.LENGTH_LONG).show();
                }else{
                    sendSMSBoolean = false;
                    Toast.makeText(getApplicationContext(),"App will NOT send SMS when listening \"help\".  Please click 'save' button", Toast.LENGTH_LONG).show();
                }
            }
        });

        locationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sendLocation = true;
                    Toast.makeText(getApplicationContext(),"App will send append location in SMS. Please click 'save' button", Toast.LENGTH_LONG).show();
                }else{
                    sendLocation = false;
                    Toast.makeText(getApplicationContext(),"App will NOT send append location in SMS.  Please click 'save' button", Toast.LENGTH_LONG).show();
                }
            }
        });

        contactPicker1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                one="one";

                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);

            }
        });

        contactPicker2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                one="two";

                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                writeToPref();
                Toast.makeText(getApplicationContext(), getString(R.string.Saved), Toast.LENGTH_SHORT).show();
                loadDatatoView();
                finish();
            }
        });


    }//onCreate over

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == PICK_CONTACT)
        {
            getContactInfo(data);
            //class variables now have the data

            if(one.equals("one")){
                ContactName1 = name;
                contactNumber1 = phoneNumber;
            }else if (one.equals("two")){
                ContactName2 = name;
                contactNumber2 = phoneNumber;
            }

            String a = "You picked : "+name+", "+phoneNumber+".";
            Toast.makeText(getApplicationContext(),a, Toast.LENGTH_LONG).show();
        }

        //check speech recognition result
        if (reqCode == VR_REQUEST && resultCode == RESULT_OK)
        {
            //store the returned word list as an ArrayList
            ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //set the retrieved list to display in the ListView using an ArrayAdapter
            //wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.word, suggestedWords));
            //call the function to process words
            process(suggestedWords);
        }


    }//onActivityResult over


    protected void getContactInfo(Intent intent)
    {

        Cursor cursor =  getContentResolver().query(intent.getData(), null, null, null, null);
        while (cursor.moveToNext())
        {
            contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);
            while (phones.moveToNext())
            {
                this.name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                this.phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            phones.close();

        }  //while (cursor.moveToNext())
        cursor.close();
    }//getContactInfo

    public void startService() {
        startService(new Intent(getBaseContext(), MyService.class));
    }

    // Method to stop the service
    public void stopService() {
        stopService(new Intent(getBaseContext(), MyService.class));
    }

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
    }//listenToSpeech() over

    public void process(ArrayList suggestedWords){
        this.helpWord = suggestedWords.get(0).toString();
        Toast.makeText(getApplicationContext(),"Help Phrase : "+helpWord, Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(),"Please click 'save' button", Toast.LENGTH_LONG).show();
    }//process() over

}//class ove
