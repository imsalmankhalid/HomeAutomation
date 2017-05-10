package com.SHProject.SmartHome.AppMain;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.macroyau.blue2serial.BluetoothDeviceListDialog;
import com.macroyau.blue2serial.BluetoothSerial;
import com.macroyau.blue2serial.BluetoothSerialListener;

import java.util.Timer;
import java.util.TimerTask;

public class Switch_Control extends AppCompatActivity
        implements BluetoothSerialListener, BluetoothDeviceListDialog.OnDeviceSelectedListener{

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private BluetoothSerial bluetoothSerial;

    private MenuItem actionConnect, actionDisconnect;

    private boolean crlf = false;
    private Timer timer;
    private String res;
    private String[] web_data = new String[20];
    private int count=0;

    private Switch switch1, switch2, switch3, switch4, switch5, switch6;
    private TextView tvTemp, tvHumidity, tvWater, tvPower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch__control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bluetoothSerial = new BluetoothSerial(this, this);

        switch1 = (Switch) findViewById(R.id.sw1);
        switch2 = (Switch) findViewById(R.id.sw2);
        switch3 = (Switch) findViewById(R.id.sw3);
        switch4 = (Switch) findViewById(R.id.sw4);
        switch5 = (Switch) findViewById(R.id.sw5);
        switch6 = (Switch) findViewById(R.id.sw6);

        tvTemp = (TextView) findViewById(R.id.temp);
        tvHumidity = (TextView) findViewById(R.id.humid);
        tvPower = (TextView) findViewById(R.id.power);
        tvWater = (TextView) findViewById(R.id.water);

        timer = new Timer();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Toast.makeText(Switch_Control.this, "Living Room Light : On", Toast.LENGTH_SHORT).show();
                    send_msg("L11");
                } else {
                    Toast.makeText(Switch_Control.this, "Living Room Light : Off", Toast.LENGTH_SHORT).show();
                    send_msg("L10");
                }
            }
        });

        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Toast.makeText(Switch_Control.this, "Living Room Fan : On ", Toast.LENGTH_SHORT).show();
                    send_msg("L21");
                } else {
                    Toast.makeText(Switch_Control.this, "Living Room Fan : Off", Toast.LENGTH_SHORT).show();
                    send_msg("L20");
                }
            }
        });


        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Toast.makeText(Switch_Control.this, "Bed Room Light: On ", Toast.LENGTH_SHORT).show();
                    send_msg("L31");
                } else {
                    Toast.makeText(Switch_Control.this, "Bed Room Light: Off", Toast.LENGTH_SHORT).show();
                    send_msg("L30");
                }
            }
        });

        switch4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Toast.makeText(Switch_Control.this, "Bed Room Fan: On", Toast.LENGTH_SHORT).show();
                    send_msg("L41");
                } else {
                    Toast.makeText(Switch_Control.this, "Bed Room Fan: Off", Toast.LENGTH_SHORT).show();
                    send_msg("L40");
                }
            }
        });

        switch5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Toast.makeText(Switch_Control.this, "Kitchen Light: On", Toast.LENGTH_SHORT).show();
                    send_msg("L51");
                } else {
                    Toast.makeText(Switch_Control.this, "Kitchen Light: Off", Toast.LENGTH_SHORT).show();
                    send_msg("L50");
                }
            }
        });

        switch6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Toast.makeText(Switch_Control.this, "Everythning: On ", Toast.LENGTH_SHORT).show();
                    send_msg("ALL1");
                } else {
                    Toast.makeText(Switch_Control.this, "Everything: Off", Toast.LENGTH_SHORT).show();
                    send_msg("ALL0");
                }
            }
        });
    }

    public void onStart() {
        super.onStart();
        // Check Bluetooth availability on the device and set up the Bluetooth adapter
        bluetoothSerial.setup();
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
    }
    public void onPause() {
        super.onPause();
        timer.cancel();
        bluetoothSerial.stop();
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
                            res = new ReadSwitchData(Switch_Control.this).execute().get();
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
           //     bluetoothSerial.connect("98:D3:31:30:74:62");
                break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
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
                    Toast.makeText(Switch_Control.this, "You are not connected to Home ", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        return result;
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
    StringBuilder msg_data = new StringBuilder();
    String values;
    public void onBluetoothSerialRead(String message) {
        for(int i=0; i<message.length();i++)
        {
            if(message.charAt(i) != '$') {
                if (message.charAt(i) != '\r' || message.charAt(i) != '\n')
                    msg_data.append(message.charAt(i));
            }
            else
            {
                if(msg_data.length() > 12)
                {
                    msg_data.setLength(0);
                }
                else {
                    if(msg_data.charAt(2) == ',' && msg_data.charAt(5) == ',' && msg_data.charAt(8) == ',' && msg_data.charAt(11) == '$') {
                        String[] data_val = msg_data.toString().split(",");
                        msg_data.setLength(0);
                        setValues(Integer.parseInt(data_val[0]),
                                Integer.parseInt(data_val[1]),
                                Integer.parseInt(data_val[2]),
                                Integer.parseInt(data_val[3]));
                    }
                }
            }
        }        //svTerminal.post(scrollTerminalToBottom);
    }

    @Override
    public void onBluetoothSerialWrite(String message) {
    }


    @Override
    public void onBluetoothDeviceSelected(BluetoothDevice device) {
        // Connect to the selected remote Bluetooth device
        bluetoothSerial.connect(device);
    }

    /* End of the implementation of listeners */

    private final Runnable scrollTerminalToBottom = new Runnable() {
        @Override
        public void run() {
            // Scroll the terminal screen to the bottom
            //      svTerminal.fullScroll(ScrollView.FOCUS_DOWN);
        }
    };




    public void send_msg(String message)
    {
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
                Toast.makeText(Switch_Control.this, "You are not connected to Home ", Toast.LENGTH_SHORT).show();
                break;
        }

    }
    private void setValues(int t, int h, int w, int p)
    {
        tvTemp.setText(""+t+" C");
        tvHumidity.setText(""+h+" %");
        tvPower.setText(""+p+" Watt");
        tvWater.setText(""+w+" %");
    }

}
