package chanboss.liveauction.database;

import android.os.AsyncTask;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

public class PhpConnect extends AsyncTask<String,Void,String> {
    protected void onPreExecute(){

    }

    @Override
    protected String doInBackground(String... arg0) {
        try {
            String fileName = arg0[0];
            String param =  arg0[1];
            //ip 불러오기
            InformationVO informationVO = new InformationVO();
            String ip = informationVO.getMyIp();

            String link = ip+"/"+fileName+"?"+param;
            System.out.println(link);
            URL url = new URL(link);
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line = "";

            while ((line = in.readLine()) != null) {
                sb.append(line);
                break;
            }
            in.close();

            return sb.toString();
        } catch (Exception e1) {
            return e1.getMessage();
        }
    }
    @Override
    protected void onPostExecute(String result){

    }
}