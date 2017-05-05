package com.example.salmankhalid.ui_test;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.salmankhalid.ui_test.library.GaugeView;

import java.util.Random;

public class Main2Activity extends AppCompatActivity {

    Button btn;
    EditText username, password;
    String res;
    Lgin_Request request;
    TextView tvLogin;
    ProgressDialog progress;
    int i=0;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        btn = (Button)findViewById(R.id.button3);
        username = (EditText)findViewById(R.id.user);
        password = (EditText)findViewById(R.id.pass);
        tvLogin = (TextView)findViewById(R.id.textView4);

        final Random random = new Random();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    res = new Lgin_Request(Main2Activity.this, username.getText().toString(), password.getText().toString()).execute().get();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                if(res.matches("true"))
                {
                    Intent fp=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(fp);
                }
                else
                {
                    tvLogin.setText("Please enter valid credentials");
                    tvLogin.setTextColor(Color.RED);
                }
            }
        });
    }
//
//    public void update_Text(String msg)
//    {
//        txt.setText(msg);
//    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetooth_chat, menu);
        return true;
    }
}
