package chanboss.liveauction.streaming;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.UUID;

import chanboss.liveauction.R;
import chanboss.liveauction.database.PhpConnectPost;
import chanboss.liveauction.database.SessionManager;
import io.antmedia.android.liveVideoBroadcaster.LiveVideoBroadcasterActivity;


public class StreamRoomAdd extends AppCompatActivity {
    EditText pdTitleET;
    EditText pdContentsET;
    EditText startPriceET;


    Button saveBtn;

    SessionManager sessionManager;

    String fileName;
    String param;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_streamroom_add);

        pdTitleET = findViewById(R.id.pdTitle);
        startPriceET = findViewById(R.id.startPrice);
        saveBtn = findViewById(R.id.saveBtn);


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhpConnectPost phpConnectPost = new PhpConnectPost();
                String liveId = UUID.randomUUID().toString();
                fileName = "liveroom_insert.php";
                param = new SessionManager(getApplicationContext()).getUserId()+"//"+pdTitleET.getText().toString()+"//"+startPriceET.getText().toString()+"//"+liveId;

                try{
                    String result = phpConnectPost.execute(fileName,param).get();
                    if(result.equals("success")){
                            Intent intent = new Intent(getApplicationContext(),LiveVideoBroadcasterActivity.class);
                            intent.putExtra("liveId",liveId);
                            intent.putExtra("liveName",pdTitleET.getText().toString());
                            intent.putExtra("nowPrice",startPriceET.getText().toString());
                            startActivity(intent);
                            finish();

                    }else{
                        Toast.makeText(getApplicationContext(),"잠시 후 다시 시도해주세요.",Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e){

                }
            }
        });




    }
}
