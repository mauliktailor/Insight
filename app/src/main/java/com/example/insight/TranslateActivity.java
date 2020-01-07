package com.example.insight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.TranslationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class TranslateActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String message;
    HashMap<String,String> catagories1;
    String langModel=null; // en-$langCode

    private String langToTranslate;
    private String translatedOutput;
    private TextToSpeech tts;
    private TextToSpeech tts1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Set Clickable property of the button to false
        ImageButton b =  findViewById(R.id.button4);
        b.setClickable(false);
        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);
        TextView translatedTextView = findViewById(R.id.translatedTextView);
        translatedTextView.setVisibility(View.INVISIBLE);
        //Set spinner
        setSpinner();

        // Set text to speech
        tts  = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });

        // Set the translated speak button invisible
        Button speakButton1 = findViewById(R.id.speakButton1);
        speakButton1.setVisibility(View.GONE);

    }

    private void setSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Arabic");
        categories.add("Bulgarian");
        categories.add("Czech");
        categories.add("Danish");
        categories.add("German");
        categories.add("Greek");
        categories.add("Spanish");
        categories.add("Estonian");
        categories.add("Finnish");
        categories.add("French");
        categories.add("Irish");
        categories.add("Hebrew");
        categories.add("Hindi");
        categories.add("Croatian");
        categories.add("Indonesian");
        categories.add("Italian");
        categories.add("Japanese");
        categories.add("Korean");
        categories.add("Lithuanian");
        categories.add("Malay");
        categories.add("Norwegian Bokmal");
        categories.add("Dutch");
        categories.add("Polish");
        categories.add("Portuguese");
        categories.add("Romanian");
        categories.add("Russian");
        categories.add("Slovak");
        categories.add("Slovenian");
        categories.add("Swedish");
        categories.add("Thai");
        categories.add("Turkish");
        categories.add("Simplified Chinese");
        categories.add("Traditional Chinese");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        // adding to the language code HashMap
        catagories1 = new HashMap<>();
        catagories1.put("Arabic", "ar");
        catagories1.put("Bulgarian", "bg");
        catagories1.put("Czech", "cs");
        catagories1.put("Danish", "da");
        catagories1.put("German", "de");
        catagories1.put("Greek", "el");
        catagories1.put("Spanish", "es");
        catagories1.put("Estonian", "et");
        catagories1.put("Finnish", "fi");
        catagories1.put("French", "fr");
        catagories1.put("Irish", "ga");
        catagories1.put("Hebrew", "he");
        catagories1.put("Hindi", "hi");
        catagories1.put("Croatian", "hr");
        catagories1.put("Indonesian", "is");
        catagories1.put("Italian", "it");
        catagories1.put("Japanese", "ja");
        catagories1.put("Korean", "ko");
        catagories1.put("Lithuanian", "lt");
        catagories1.put("Malay", "ms");
        catagories1.put("Norwegian Bokmal", "nb");
        catagories1.put("Dutch", "nl");
        catagories1.put("Polish", "pl");
        catagories1.put("Portuguese","pt");
        catagories1.put("Romanian", "ro");
        catagories1.put("Russian","ru");
        catagories1.put("Slovak","sk");
        catagories1.put("Slovenian","sl");
        catagories1.put("Swedish", "sv");
        catagories1.put("Thai","th");
        catagories1.put("Turkish","tr");
        catagories1.put("Simplified Chinese", "zh");
        catagories1.put("Traditional Chinese", "zh-TW");
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        langToTranslate = parent.getItemAtPosition(position).toString();
        String code = catagories1.get(langToTranslate);
        langModel = "en-"+code;
        ImageButton b =  findViewById(R.id.button4);
        b.setClickable(true);

        // set text view and speak button invisible if user want to translate more then one time
        TextView t = findViewById(R.id.translatedTextView);
        t.setText(translatedOutput);
        t.setVisibility(View.GONE);
        // Set the translated speak button visible
        Button speakButton1 = findViewById(R.id.speakButton1);
        speakButton1.setVisibility(View.GONE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public void Translate(View view) throws ExecutionException, InterruptedException {
        String out = new DataAsync().execute().get();
        translatedOutput = JsonHandler.parseTranslate(out);
        TextView t = findViewById(R.id.translatedTextView);
        t.setText(translatedOutput);
        t.setVisibility(View.VISIBLE);
        // Set the translated speak button visible
        Button speakButton1 = findViewById(R.id.speakButton1);
        speakButton1.setVisibility(View.VISIBLE);
    }
    class DataAsync extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            IamAuthenticator authenticator = new IamAuthenticator("7pGO-xjJ4PXnLPUxVwN2XBfqGT7OUr1qGwKufVk0KDBP");
            LanguageTranslator lt = new LanguageTranslator("2018-05-01", authenticator);
            lt.setServiceUrl("https://gateway.watsonplatform.net/language-translator/api");
            String textToTranslate = message;
            TranslateOptions translateOptions = new TranslateOptions.Builder()
                    .addText(textToTranslate)
                    .modelId(langModel)
                    .build();

            TranslationResult result = lt.translate(translateOptions)
                    .execute().getResult();

//            System.out.println(result);
            return result.toString();
        }


    }


    // Handle First speak button
    public void speakButtonClicked(View view){
        tts.setLanguage(Locale.ENGLISH);
        speak(message);
    }

    @Override
    public void onPause(){
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void speakButton1Clicked(View view){
        Locale l = getLanguageLocal();
        if(l!=null){
            int id = view.getId();
            String strID = new Integer(id).toString();
            tts.setLanguage(l);
            speak(translatedOutput);
        }
        else{
            Toast.makeText(TranslateActivity.this,"Sorry speech not available",Toast.LENGTH_LONG).show();
        }
    }

    private Locale getLanguageLocal() {
        Locale l1;
//         Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN, Locale.JAPANESE, Locale.KOREAN, Locale.TAIWAN//trad chinese ,Locale.PRC// simp chinese
        switch (langToTranslate){
            case "French":
                l1= Locale.FRENCH;
                break;
            case "German":
                l1= Locale.GERMAN;
                break;
            case "Italian":
                l1= Locale.ITALIAN;
                break;
            case "Japanese":
                l1= Locale.JAPANESE;
                break;
            case "Korean":
                l1= Locale.KOREAN;
                break;
            case "Simplified Chinese":
                l1= Locale.CHINESE;
                break;
            case "Traditional Chinese":
                l1= Locale.TAIWAN;
                break;
            default:
                l1= null;
        }
        return l1;
    }
}
