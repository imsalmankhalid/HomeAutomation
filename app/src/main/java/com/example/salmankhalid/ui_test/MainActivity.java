package com.example.salmankhalid.ui_test;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.salmankhalid.ui_test.library.GaugeView;
import com.macroyau.blue2serial.BluetoothDeviceListDialog;
import com.macroyau.blue2serial.BluetoothSerial;
import com.macroyau.blue2serial.BluetoothSerialListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements BluetoothSerialListener, BluetoothDeviceListDialog.OnDeviceSelectedListener{

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private BluetoothSerial bluetoothSerial;

    private MenuItem actionConnect, actionDisconnect;

    private boolean crlf = false;
    private Timer timer;
    private String res;
    private String[] web_data = new String[20];
    private int count=0;

    Button BtnSwitch;
    Button btnBack;
    TextView tvTerminal;
    GaugeView temperture;
    GaugeView humidity;
    GaugeView waterlevel;
    GaugeView power;
    TextView temp, humid, water, pow;

    int i=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothSerial = new BluetoothSerial(this, this);

        /* Define Gauges to show current values */
        temperture = (GaugeView) findViewById(R.id.g_temp);
        humidity = (GaugeView) findViewById(R.id.g_humid);
        waterlevel = (GaugeView) findViewById(R.id.g_water);
        power = (GaugeView) findViewById(R.id.g_power);

        /* define text views to display values */
        temp = (TextView)findViewById(R.id.txtTemp);
        humid = (TextView)findViewById(R.id.txtHumid);
        water = (TextView)findViewById(R.id.txtWater);
        pow = (TextView)findViewById(R.id.txtPower);
        timer = new Timer();

        /* Set a timer for polling the values */

        // If the adapter is null, then Bluetooth is not supported
        BtnSwitch = (Button) findViewById(R.id.button);
        tvTerminal = (TextView)findViewById(R.id.msg) ;
        btnBack = (Button) findViewById(R.id.button2);

        setValues(0,0,0,0);
        BtnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send_msg("VAL");
                try {
                    res = new ReadSwitchData(MainActivity.this).execute().get();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "Data "+res, Toast.LENGTH_SHORT).show();

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //   Intent fp=new Intent(getApplicationContext(),Main2Activity.class);
           //     startActivity(fp);
                   Intent fp=new Intent(getApplicationContext(),Switch_Control.class);
                     startActivity(fp);
            }
        });
    }

    public void onStart() {
        super.onStart();
        // Check Bluetooth availability on the device and set up the Bluetooth adapter
        bluetoothSerial.setup();
    }
    public void onPause() {
        super.onPause();
        timer.cancel();
        bluetoothSerial.stop();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();

        // Open a Bluetooth serial port and get ready to establish a connection
        if (bluetoothSerial.checkBluetooth() && bluetoothSerial.isBluetoothEnabled()) {
            if (!bluetoothSerial.isConnected()) {
                bluetoothSerial.start();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from the remote device and close the serial port
        bluetoothSerial.stop();
        timer.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth_chat, menu);

        actionConnect = menu.findItem(R.id.action_connect);
        actionDisconnect = menu.findItem(R.id.action_disconnect);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect) {
            showDeviceListDialog();
            return true;
        } else if (id == R.id.action_disconnect) {
            bluetoothSerial.stop();
            return true;
        } else if (id == R.id.action_crlf) {
            crlf = !item.isChecked();
            item.setChecked(crlf);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void invalidateOptionsMenu() {
        if (bluetoothSerial == null)
            return;

        // Show or hide the "Connect" and "Disconnect" buttons on the app bar
        if (bluetoothSerial.isConnected()) {
            if (actionConnect != null)
                actionConnect.setVisible(false);
            if (actionDisconnect != null)
                actionDisconnect.setVisible(true);
        } else {
            if (actionConnect != null)
                actionConnect.setVisible(true);
            if (actionDisconnect != null)
                actionDisconnect.setVisible(false);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                // Set up Bluetooth serial port when Bluetooth adapter is turned on
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothSerial.setup();
                }
                break;
        }
    }

    private void updateBluetoothState() {
        // Get the current Bluetooth state
        final int state;
        if (bluetoothSerial != null)
            state = bluetoothSerial.getState();
        else
            state = BluetoothSerial.STATE_DISCONNECTED;

        // Display the current state on the app bar as the subtitle
        String subtitle;
        switch (state) {
            case BluetoothSerial.STATE_CONNECTING:
                subtitle = getString(R.string.status_connecting);
                break;
            case BluetoothSerial.STATE_CONNECTED:
                subtitle = getString(R.string.status_connected, bluetoothSerial.getConnectedDeviceName());
                send_msg("VAL");
                timer.scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        try {
                            res = new ReadSwitchData(MainActivity.this).execute().get();
                            mHandler.obtainMessage(1).sendToTarget();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, 2000, 4000);
                break;
            default:
                subtitle = getString(R.string.status_disconnected);
            //    bluetoothSerial.connect("98:D3:31:30:74:62");
                break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            tvTerminal.setText(res.toString());
            web_data[count++] = res;
            Log.e("Web_data_get","Data" + res + "count " + count);
            process_web_data();
        }
    };

    void process_web_data() {
        if(count >= 0){
            for(int i= 0; i <= count; i++) {
                if(send_msg_chk(web_data[count-1])) {
                    Log.e("sending_to_bluetooth","Data" + web_data[count-1] + "count " + count);

                    web_data[count-1] = "";
                    count--;
                }
            }
        }

    }

    private void showDeviceListDialog() {
        // Display dialog for selecting a remote Bluetooth device
        BluetoothDeviceListDialog dialog = new BluetoothDeviceListDialog(this);
        dialog.setOnDeviceSelectedListener(this);
        dialog.setTitle(R.string.paired_devices);
        dialog.setDevices(bluetoothSerial.getPairedDevices());
        dialog.showAddress(true);
        dialog.show();
    }

    /* Implementation of BluetoothSerialListener */

    @Override
    public void onBluetoothNotSupported() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.no_bluetooth)
                .setPositiveButton(R.string.action_quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBluetoothDisabled() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
    }

    @Override
    public void onBluetoothDeviceDisconnected() {
        invalidateOptionsMenu();
        updateBluetoothState();
    }

    @Override
    public void onConnectingBluetoothDevice() {
        updateBluetoothState();
    }

    @Override
    public void onBluetoothDeviceConnected(String name, String address) {
        invalidateOptionsMenu();
        updateBluetoothState();
    }

    char[] msg_data = new char[20];
    String values;
    int j=0;
    public void onBluetoothSerialRead(String message) {
        // Print the incoming message on the terminal screen
//        tvTerminal.append(getString(R.string.terminal_message_template,
//                bluetoothSerial.getConnectedDeviceName(),
//                message));
//
        for(int i=0; i<message.length();i++)
        {
            if(message.charAt(i) != '$') {
                if (message.charAt(i) != '\r' || message.charAt(i) != '\n')
                    msg_data[j++] = message.charAt(i);
            }
            else
            {
                String[] data_val = new String(msg_data, 0, j).split(",");
                j = 0;
                for (int k = 0; k < 20; k++)
                    msg_data[k] = '0';
                setValues(Integer.parseInt(data_val[0]),
                        Integer.parseInt(data_val[1]),
                        Integer.parseInt(data_val[2]),
                        Integer.parseInt(data_val[3]));
            }
        }
        tvTerminal.setText(values);
        //svTerminal.post(scrollTerminalToBottom);
    }

    @Override
    public void onBluetoothSerialWrite(String message) {
        // Print the outgoing message on the terminal screen
        tvTerminal.append(getString(R.string.terminal_message_template,
                bluetoothSerial.getLocalAdapterName(),
                message));
        //svTerminal.post(scrollTerminalToBottom);
    }

    /* Implementation of BluetoothDeviceListDialog.OnDeviceSelectedListener */

    @Override
    public void onBluetoothDeviceSelected(BluetoothDevice device) {
        // Connect to the selected remote Bluetooth device
        bluetoothSerial.connect(device);
        //98:D3:31:30:74:62
    }

    /* End of the implementation of listeners */

    private final Runnable scrollTerminalToBottom = new Runnable() {
        @Override
        public void run() {
            // Scroll the terminal screen to the bottom
      //      svTerminal.fullScroll(ScrollView.FOCUS_DOWN);
        }
    };

    boolean send_msg_chk(String message){
        final int state;
        boolean result = false;
        if(!message.isEmpty()) {
            if (bluetoothSerial != null)
                state = bluetoothSerial.getState();
            else
                state = BluetoothSerial.STATE_DISCONNECTED;

            switch (state) {
                case BluetoothSerial.STATE_CONNECTED:
                    bluetoothSerial.write(message.toString().trim(), crlf);
                    Log.e("Sent message", message.toString().trim());
                    result = true;
                    break;
                default:
                    Toast.makeText(MainActivity.this, "You are not connected to Home ", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        return result;
    }
    public void send_msg(String message) {
        final int state;
        if (bluetoothSerial != null)
            state = bluetoothSerial.getState();
        else
            state = BluetoothSerial.STATE_DISCONNECTED;

        switch (state) {
            case BluetoothSerial.STATE_CONNECTED:
                bluetoothSerial.write(message.toString().trim(), crlf);
                break;
            default:
                Toast.makeText(MainActivity.this, "You are not connected to Home ", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setValues(int t, int h, int w, int p)
    {
        temperture.setTargetValue((float)t);
        temp.setText("Temperature = "+t);
        humidity.setTargetValue((float)h);
        humid.setText("Humidity = "+h);
        waterlevel.setTargetValue((float)w);
        water.setText("Water level = "+w);
        power.setTargetValue((float)p);
        pow.setText("Power = "+p);

        if(t !=0 && h != 0 && w !=0 && p != 0)
            new SendRequest(this,t,h,w,p).execute();
    }
}
