package rover2.rover2android.websocket;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import rover2.rover2android.Const;


public class WebSocketClient {
    
    private WebSocket socket;
    private IWebSocketReceiver receiver;

    private long lastTimeConnectionOk;

    public WebSocketClient(IWebSocketReceiver receiver)
    {
        this.receiver = receiver;
    }


    public void startSocketListener() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0,  TimeUnit.MILLISECONDS)
                .build();

        RoverWebSocketListener listener = new RoverWebSocketListener();

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



    private void log(String message){

        this.receiver.log(message);
    }

    public void sendText(String text) {

        if(this.socket != null) {
            this.socket.send(text);
        }
        else {
            this.log("Socket is null");
        }
    }

    public void checkConnection(){
        if(this.lastTimeConnectionOk > 0)
        {
            long tDelta = System.currentTimeMillis() - this.lastTimeConnectionOk;
            double elapsedSeconds = tDelta / 1000.0;
            if(elapsedSeconds > 20)
            {
                this.startSocketListener();
            }
        }
        try {
            this.sendText("CT");
        }
        catch (Exception exCheck)
        {
            Log.e(Const.TAG, exCheck.getMessage(), exCheck);
            try {
                this.startSocketListener();
            }
            catch (Exception exStart){
                Log.e(Const.TAG, exStart.getMessage(), exStart);
            }
        }
    }

    private final class RoverWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {

            webSocket.send("Init");
        }
        @Override
        public void onMessage(WebSocket webSocket, String text) {

            if("Init".equals(text))
            {
                log("Initializaton OK");
            }
            else if("CT".equals(text)){
                log("CT OK");
                lastTimeConnectionOk = System.currentTimeMillis();
            }
            else {
                log("got text: " + text);
                receiver.arduinoCommand(text);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            log("Receiving bytes : " + bytes.hex());
        }
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            log("Closing : " + code + " / " + reason);
        }
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            log("Error : " + t.getMessage());
        }
    }
}
