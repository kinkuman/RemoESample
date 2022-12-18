package net.kinkuman.android.remoesample;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private  final static String ACCESS_URL = "https://api.nature.global/1/appliances";

    Handler handler = new Handler();

    // web　アクセス用スレッド
    Thread thread = null;


    // 接続リソース
    URL url = null;
    HttpURLConnection httpURLConnection = null;
    BufferedReader br = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v("hogehoge","create");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 自分の処理を始める
        initMyAction();

        Log.v("hogehoge","resume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 割り込んで止まってもらう
        thread.interrupt();

        Log.v("hogehoge","stop thread stop");
    }

    protected void initMyAction(){

        try {
            // URLの作成
            url = new URL(ACCESS_URL);
        } catch (MalformedURLException e) {
            Toast.makeText(MainActivity.this, "'URLERROR:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        Runnable run = new Runnable() {
            @Override
            public void run() {

                boolean loop = true;

                while(loop) {

                    // URL先の必要な内容取得処理
                    final HashMap<String,String> result = getURLContent();

                    String text = result.get("val");
                    String update_at = result.get("update_at");

                    Log.v("hogehoge",update_at+" "+text);

                    // メインスレッドで更新
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView tv = (TextView) findViewById(R.id.textview1);
                            tv.setText(text);
                            tv.setAlpha(0);
                            tv.animate().alpha(1).setDuration(1000);
                        }
                    });

                    // 300秒で30回(10秒に1回呼んでいいとされてるが、何回取得しても分単位でしか値がかわらん)
                    try {
                        Log.v("hogehoge","ちょっと寝る");
                        // 意味ないので20秒１回にする
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        // 割り込まれたら繰り返しをやめて終了する
                        loop = false;
                        //e.printStackTrace();
                        Log.v("hogehoge","woker thread interrupted");

                        thread = null;
                    }
                }

            }
        };

        thread = new Thread(run);
        thread.start();
    }

// デバッグ時にタップで更新できるようにしたもの
//    @Override
//    public void onClick(View view) {
//        Runnable run = new Runnable() {
//            @Override
//            public void run() {
//                final String text = getURLContent(ACCESS_URL);
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        TextView tv = (TextView)findViewById(R.id.textview1);
//                        tv.setText(text);
//                    }
//                });
//
//            }
//        };
//
//        Thread thread = new Thread(run);
//        thread.start();
//    }

    /**
     * HTTP読み込み スレッドから使う
     * @return
     */
    private HashMap<String,String> getURLContent() {

        // 戻り値をエラーでとりあえず作成
        HashMap<String,String> result = new HashMap<String,String>();
        result.put("val","error");
        result.put("update_at","error");

        // 接続がなければ
        if( httpURLConnection == null  ) {

            // 接続する

            try {

                httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Authorization","Bearer ここへNature Remoのサイトhome.nature.globalで取得したアクセストークンをいれてください");
                httpURLConnection.setRequestProperty("accept","application/json");

                // 接続
                httpURLConnection.connect();



            } catch (IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "IOERROR:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();

                if( httpURLConnection!=null) httpURLConnection.disconnect();
                httpURLConnection = null;

                return result;
            }

        }

        try {
            // Streamを得る
            InputStream in = httpURLConnection.getInputStream();

            // Readerにする
            br = new BufferedReader(new InputStreamReader(in,"UTF-8"));

            // 1行目を読む
            String jsonText = br.readLine();

            // 閉じる
            br.close();

            // JSONにする
            JSONArray top = new JSONArray(jsonText);

            // JSON解析
            JSONObject data = top.getJSONObject(0);
            //result = data.getString("id");
            JSONObject smart_meter = data.getJSONObject("smart_meter");
            JSONArray echonetlite_propertiesArray = smart_meter.getJSONArray("echonetlite_properties");
            JSONObject measured_instantaneous = echonetlite_propertiesArray.getJSONObject(5);
            String val = measured_instantaneous.getString("val");
            String update_at = measured_instantaneous.getString("updated_at");

            // 取得できた値で更新する
            result.put("val",val);
            result.put("update_at",update_at);

            return result;



        } catch (JSONException | IOException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "JSONERROR:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            Log.v("hogehoge",e.getMessage());
            e.printStackTrace(System.err);


        } finally {

            httpURLConnection.disconnect();
            httpURLConnection = null;

            try {
                if( br!=null) br.close();
            } catch (IOException e) {
                // closeの失敗はもう知らない
                e.printStackTrace();
            }

        }

        // 絶対errorだけど
        return result;
    }
}