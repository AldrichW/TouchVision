package com.teamsight.touchvision;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;


/**
 * Created by aldrichW on 15-11-22.
 */
public class HTTPBackendService {
    private HttpURLConnection mUrlConnection;
    private static final String DEFAULT_GROCERY_URL = "https://still-sierra-6295.herokuapp.com/product";
    private static final String DEFAULT_TTC_URL     = "http://webservices.nextbus.com/service/publicXMLFeed";

    public String createPOSTDataWithProductIdentifier(String productIdentifier){
        if(productIdentifier != null) {
            if (productIdentifier.isEmpty()) {
                return "";  //just return an empty string back.
            }
            JSONObject json = new JSONObject();
            try {
                json.put("upc", productIdentifier);     //Can't use an int here. Too large
                json.put("show_nutrition", "true");
            } catch (JSONException e) {
                System.err.println("[HTTPBackendService] Failed to create POST data using JSONObject");
                e.printStackTrace();
            }
            return json.toString();
        } else {
            return "";
        }
    }


    public Document sendGETRequest(URL url, final String stopId, final String routeTag){

        final String query = "command=predictions&a=ttc&stopId=" + stopId + "&routeTag=" + routeTag;

        try{
            //Set the Default URL is an empty string is passed in
            url = (url == null) ? new URL(DEFAULT_TTC_URL + "?" + query) : url;

            mUrlConnection = (HttpURLConnection) url.openConnection();
            Log.d("sendGetRequest", "Sending 'GET' request to URL : " + url);

            final int responseCode = mUrlConnection.getResponseCode();
            Log.d("sendGetRequest", "Response Code: " + responseCode);

            //Handle Server Response
            InputStream inputStream = mUrlConnection.getInputStream();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(inputStream);

            return document;
        }
        catch(Exception e){
            System.err.println("[HTTPBackendService] Failed to send GET Request.");
            e.printStackTrace();
        }
        finally{
            try{
                mUrlConnection.disconnect();
            }
            catch(Exception e){
                System.err.println("[HTTPBackendService] Failed to disconnect URL Connection.");
                e.printStackTrace();
            }
        }

        return null;
    }


    public String sendPOSTRequest(URL url, String jsonData){
        BufferedReader reader = null;
        try{
            //Set the Default URL is an empty string is passed in
            url = (url == null) ? new URL(DEFAULT_GROCERY_URL) : url;

            mUrlConnection = (HttpURLConnection) url.openConnection();

            //Send POST Request
            mUrlConnection.setDoOutput(true);
            mUrlConnection.setRequestMethod("POST");
            mUrlConnection.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter wr = new OutputStreamWriter(mUrlConnection.getOutputStream());
            wr.write(jsonData);
            wr.flush();

            //Handle Server Response

            reader = new BufferedReader(new InputStreamReader(mUrlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = reader.readLine()) != null){
                sb.append(line+"\n");
            }

            return sb.toString();

        }
        catch(Exception e){
            System.err.println("[HTTPBackendService] Failed to send POST Request.");
            e.printStackTrace();
        }
        finally{
            try{
                reader.close();
                mUrlConnection.disconnect();
            }
            catch(Exception e){
                System.err.println("[HTTPBackendService] Failed to disconnect URL Connection.");
                e.printStackTrace();
            }
        }

        return null;

    }


}
