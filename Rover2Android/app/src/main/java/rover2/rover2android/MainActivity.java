package rover2.rover2android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import rover2.rover2android.bridge.Bridge;

public class MainActivity extends AppCompatActivity {

    Bridge bridge = new Bridge();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bridge.onCreate(this);



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

        //Scrolling
        TextView tv = (TextView) findViewById(R.id.textViewLog);
        tv.setMovementMethod(new ScrollingMovementMethod());


    }

    private void clearLog(){
        TextView tv = (TextView) findViewById(R.id.textViewLog);
        tv.setText("");
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
        final TextView textView = (TextView) findViewById(R.id.textViewLog);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(textView.getText().toString() + "\n" + output);
            }
        });
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
