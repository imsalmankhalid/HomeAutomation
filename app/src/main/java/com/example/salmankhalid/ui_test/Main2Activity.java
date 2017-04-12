package com.example.salmankhalid.ui_test;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.salmankhalid.ui_test.library.GaugeView;

import java.util.Random;

public class Main2Activity extends AppCompatActivity {

    Button btn;
    TextView txt;
    TextView txt2;

    int i=0;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        btn = (Button)findViewById(R.id.button3);
        final GaugeView temperture = (GaugeView) findViewById(R.id.gauge_view);
        txt = (TextView) findViewById(R.id.textView6);
        txt2 = (TextView) findViewById(R.id.txtVal);
        final Random random = new Random();
        temperture.setTargetValue(40);
        final CountDownTimer timer = new CountDownTimer(10000, 2) {
            @Override
            public void onTick(long millisUntilFinished) {
                txt.setText("Waiting...");
                txt2.setText("Value = "+temperture.getTargetValue());
                temperture.setTargetValue(i++);
                if(i>95)
                    i=0;

            }
            @Override
            public void onFinish() {
            }
        };
        timer.start();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent fp=new Intent(getApplicationContext(),MainActivity.class);
               Intent fp=new Intent(getApplicationContext(),MainActivity.class);
               startActivity(fp);

            }
        });
    }

    public void update_Text(String msg)
    {
        txt.setText(msg);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetooth_chat, menu);
        return true;
    }
}
