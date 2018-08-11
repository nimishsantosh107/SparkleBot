package com.example.nimish.bot;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity {

    private final int REQ_ACCESS_CODE=100;
    Button listen,send;
    TextView txtIn,txtOut;
    EditText editTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listen=findViewById(R.id.listenButton);
        send=findViewById(R.id.sendButton);
        txtIn=findViewById(R.id.textIn);
        txtOut=findViewById(R.id.textOut);
        editTxt=findViewById(R.id.editText);


        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promtSpeechInput();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTxt.getEditableText().toString()!=""){
                    String userMessage=editTxt.getEditableText().toString();
                    txtIn.setText(userMessage);
                    RetrieveFeedTask task=new RetrieveFeedTask();
                    task.execute(userMessage);
                    editTxt.setText("");}
            }
        });


    }

    private void promtSpeechInput(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"say something");
        try {
            startActivityForResult(intent,REQ_ACCESS_CODE);
        }
        catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "sorry! Your device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_ACCESS_CODE: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String userQuery=result.get(0);
                    txtIn.setText(userQuery);
                    RetrieveFeedTask task=new RetrieveFeedTask();
                    task.execute(userQuery);
                }
                break;
            }
        }
    }

    public String GetText(String query) throws UnsupportedEncodingException{

        String text = " ";
        BufferedReader reader =null;

        //send data
        try {
            //def where to send data
            URL url = new URL("https://api.dialogflow.com/v1/query?v=20150910");

            //send post data request

            URLConnection connection=url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setRequestProperty("Authorization","Bearer ab94467493b24a999b48ba849a1194bc");
            connection.setRequestProperty("Content-Type","application/json");

            //create new json object
            JSONObject jsonParam = new JSONObject();
            JSONArray queryArray = new JSONArray();
            queryArray.put(query);
            jsonParam.put("query", queryArray);
            //jsonParam.put("name", "order a medium pizza");
            jsonParam.put("lang", "en");
            jsonParam.put("sessionId", "1234567890");

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            Log.d("karma", "after conversion is " + jsonParam.toString());
            wr.write(jsonParam.toString());
            wr.flush();
            Log.d("karma", "json is " + jsonParam);

            //Get server response

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;


            // Read Server Response
            while ((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line + "\n");
            }


            text = sb.toString();

            JSONObject object1 = new JSONObject(text);
            JSONObject object = object1.getJSONObject("result");
            JSONObject fulfillment = null;
            String speech = null;
//            if (object.has("fulfillment")) {
            fulfillment = object.getJSONObject("fulfillment");
//                if (fulfillment.has("speech")) {
            speech = fulfillment.optString("speech");
//                }
//            }

            Log.d("karma ", "response is " + text);
            return speech;


        }
        catch (Exception ex){
            Log.d("karma", "exception at last " + ex);
        }
         finally {
             try {
                 reader.close();
             }
             catch (Exception ex) {
             }
        }
        return null;
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... voids) {
            String s = null;
            try {
                s = GetText(voids[0]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.d("karma", "Exception occurred " + e);
            }

            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            txtOut.setText(s);

        }
    }
}


