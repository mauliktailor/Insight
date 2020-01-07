package com.example.insight;

//import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonHandler {

    public static String parseVisual(String op){

        String output = null;
        try {
            JSONObject reader = new JSONObject(op);
            // typecasting obj to JSONObject
            JSONArray jsonarr_1 = (JSONArray) reader.get("images");
            JSONObject classifiers = (JSONObject) jsonarr_1.get(0);

//    JSONArray classifiers = (JSONArray) jo.get("images");
            jsonarr_1 = (JSONArray) classifiers.get("classifiers");
            JSONObject temp = (JSONObject) jsonarr_1.get(0);

            // all the classes
            JSONArray classes = (JSONArray) temp.get("classes");

            ArrayList<String> classList = new ArrayList<>();
            ArrayList<Double> scoreList = new ArrayList<>();
            for (int i = 0; i < classes.length(); i++) {
                //Store the JSON objects in an array
                //Get the index of the JSON object and print the values as per the index
                JSONObject jsonobj_1 = (JSONObject) classes.get(i);
                classList.add((String) jsonobj_1.get("class"));
                scoreList.add((Double) jsonobj_1.get("score"));
            }


//            System.out.println("Classes: " + classList.toString());
//            System.out.println("Classes: " + scoreList.toString());
            JSONObject jsonobj_1 = (JSONObject) classes.get(0);
//            output  = classList.toString()+"\n"+scoreList+"\n"+ jsonobj_1.get("type_hierarchy");
//            System.out.println(jsonobj_1.get("type_hierarchy"));
            output = classList.get(0);
//            output = classList.toString();
            return output;
        }catch(JSONException e){
            return null;
        }
    }

    public static String parseTranslate(String op){
        String output = null;
        try {
            JSONObject reader = new JSONObject(op);
            // typecasting obj to JSONObject
            JSONArray jsonarr_1 = (JSONArray) reader.get("translations");
            JSONObject classifiers = (JSONObject) jsonarr_1.get(0);

            output = (String) classifiers.get("translation");

            return output;
        }catch(JSONException e){
            return null;
        }
    }
}
