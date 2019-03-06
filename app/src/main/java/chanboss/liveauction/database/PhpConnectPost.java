package chanboss.liveauction.database;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class PhpConnectPost extends AsyncTask<String,Void,String> {
    protected void onPreExecute(){

    }

    @Override
    protected String doInBackground(String... arg0) {
        try {
            String fileName = arg0[0];
            String param =  arg0[1];
            String[] paramSplit = param.split("//");
            //ip 불러오기
            InformationVO informationVO = new InformationVO();
            String ip = informationVO.getMyIp();


            String link = ip+"/"+fileName;
            System.out.println(ip+"/"+fileName);
            URL url = new URL(link);
            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost();
            httpPost.setURI(new URI(link));
            /*HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            HttpResponse response = client.execute(request);*/
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
            nameValuePairs.add(new BasicNameValuePair("userId",paramSplit[0]));
            nameValuePairs.add(new BasicNameValuePair("pdTitle",paramSplit[1]));
            nameValuePairs.add(new BasicNameValuePair("startPrice",paramSplit[2]));
            nameValuePairs.add(new BasicNameValuePair("liveId",paramSplit[3]));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));



            HttpResponse response = client.execute(httpPost);
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