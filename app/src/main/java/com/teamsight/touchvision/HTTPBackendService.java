package com.teamsight.touchvision;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by aldrichW on 15-11-22.
 */
public class HTTPBackendService {
    private HttpURLConnection mUrlConnection;
    private URL mUrl;

    public String createPOSTDataWithProductIdentifier(String productIdentifier){
        if(productIdentifier.isEmpty()){
            return "";  //just return an empty string back.
        }
        JSONObject json = new JSONObject();
        try {
            json.put("product_id", Integer.parseInt(productIdentifier));
        }
        catch(JSONException e){
            System.err.println("[HTTPBackendService] Failed to create POST data using JSONObject");
            e.printStackTrace();
        }
        return json.toString();
    }

    public String sendPOSTRequest(URL url, String jsonData){
        URL urlCopy;
        urlCopy = url;
        BufferedReader reader = null;
        try{
            //Set the Default URL is an empty string is passed in
            if(urlCopy == null){
                urlCopy = new URL("https://still-sierra-6295.herokuapp.com/product");
            }

            mUrlConnection = (HttpURLConnection) urlCopy.openConnection();

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
