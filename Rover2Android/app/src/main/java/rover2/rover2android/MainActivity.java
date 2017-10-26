package rover2.rover2android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;

import rover2.rover2android.bridge.Bridge;
import rover2.rover2android.camera.CameraPreview;
import rover2.rover2android.serial.UsbSerial;

public class MainActivity extends AppCompatActivity {

    private Bridge bridge = new Bridge();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bridge.onCreate(this, savedInstanceState);

        this.hookupButtons();

        //Scrolling
        TextView tv = (TextView) findViewById(R.id.textViewLog);
        tv.setMovementMethod(new ScrollingMovementMethod());

        //Keep Sceen alive
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private void hookupButtons(){

        //Send to websocket Button
        final Button buttonSend = (Button) findViewById(R.id.buttonSendToWebsocket);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendTextToWebSocket();
            }
        });

        //Send to arduino button
        final Button buttonSendToArduino = (Button) findViewById(R.id.buttonSendToArduino);
        buttonSendToArduino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToArduino();
            }
        });

        //Start button
        final Button buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                bridge.start();
            }
        });

        //Clear button
        final Button buttonClear = (Button) findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                clearLog();
            }
        });

    }

    public Bridge getBridge()
    {
        return this.bridge;
    }

    private void clearLog(){
        TextView tv = (TextView) findViewById(R.id.textViewLog);
        tv.setText("");
    }

    public boolean isSendPictures()
    {
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBoxSendPicture);
        return checkBox.isChecked();
    }

    public void sendTextToWebSocket() {
        TextView tv  = (TextView) this.findViewById(R.id.editTextToWebsocket);
        String text = tv.getText().toString();
        this.bridge.sendTextToWebsocket(text);
    }

    public void sendToArduino(){
        TextView tv  = (TextView) this.findViewById(R.id.editTextSendToArduino);
        String text = tv.getText().toString();
        this.bridge.sendCommandToArduino(text);
    }

    public void output(final String output) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String time = sdf.format(new java.util.Date());
        final String formattedOutput = time + " " + output;
        final TextView textView = (TextView) findViewById(R.id.textViewLog);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(formattedOutput +  "\n" + textView.getText().toString());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        this.bridge.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.bridge.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
