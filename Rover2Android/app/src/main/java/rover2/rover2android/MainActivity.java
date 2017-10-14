package rover2.rover2android;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    private WebSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        //Send Button
        final Button buttonSend = (Button) findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendText();
            }
        });

        //Start button
        final Button buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startSocketListener();
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
        TextView tv = (TextView) findViewById(R.id.textViewReceived);
        tv.setMovementMethod(new ScrollingMovementMethod());

    }

    private void clearLog(){
        TextView tv = (TextView) findViewById(R.id.textViewReceived);
        tv.setText("");
    }

    private void sendCommandToArduino(final String message)
    {
        if(message == null || message.equals("Init") || message.equals(""))
        {
            this.output("message empty");
            return;
        }
        TextView tv  = (TextView) findViewById(R.id.editTextAdress);
        String server = tv.getText().toString();

        OkHttpClient client = new OkHttpClient();

        String url = "http://" + server + message;

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            output("sending command to arduino.");
            output("url: " + url);
            response = client.newCall(request).execute();
            String body = response.body().string();
            output("got answer: " + body);
        } catch (IOException e) {
            output("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            webSocket.send("Init");
        }
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            output("got text: " + text);
            sendCommandToArduino(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            output("Receiving bytes : " + bytes.hex());
        }
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            output("Closing : " + code + " / " + reason);
        }
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            output("Error : " + t.getMessage());
        }
    }

    public void startSocketListener() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0,  TimeUnit.MILLISECONDS)
                .build();

        EchoWebSocketListener listener = new EchoWebSocketListener();

        Request request = new Request.Builder()
                .url("ws://rover2.azurewebsites.net/ws")
                .build();

        //close pervious socket
        if(this.socket != null)
        {
            this.socket.cancel();
        }
        this.socket = client.newWebSocket(request, listener);

        client.dispatcher().executorService().shutdown();
    }

    public void sendText() {
        TextView tv  = (TextView) this.findViewById(R.id.textViewReceived);
        String text = tv.getText().toString();
        if(this.socket != null) {
            this.socket.send(text);
        }
        else {
            this.output("Socket is null");
        }
    }

    private void output(final String output) {
        final TextView textView = (TextView) findViewById(R.id.textViewReceived);
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
