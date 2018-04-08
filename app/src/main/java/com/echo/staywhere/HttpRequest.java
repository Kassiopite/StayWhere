package com.echo.staywhere;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest extends AsyncTask<String, String, String> {

    private ProgressDialog dialog;

    public interface AsyncResponse {
        void onPostRequest(String output);
    }

    public AsyncResponse delegate = null;

    public HttpRequest(AsyncResponse delegate, Context context) {
        this.delegate = delegate;
        dialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Loading...");
        dialog.show();
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            URL urlToRequest = new URL(params[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String postParameters = formPostParam(params);
            urlConnection.setFixedLengthStreamingMode(postParameters.getBytes().length);
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(postParameters);
            out.close();

            BufferedReader br = new BufferedReader(new InputStreamReader((urlConnection.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        if(dialog.isShowing())
            dialog.dismiss();
        delegate.onPostRequest(result);
    }

    private String formPostParam(String[] params) {
        if (params.length > 1) {
            int size = params.length - 1;
            StringBuilder paramsBuilder = new StringBuilder();
            for (int i = 1; i <= size; i++) {
                String[] valuePair = params[i].split("\\|");
                String name = valuePair[0];
                String value = valuePair[1];
                paramsBuilder.append(name).append("=").append(value).append("&");
            }
            paramsBuilder.deleteCharAt(paramsBuilder.length() - 1);
            return paramsBuilder.toString();
        }
        return "";
    }

}
