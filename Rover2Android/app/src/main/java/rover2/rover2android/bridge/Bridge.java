package rover2.rover2android.bridge;


import java.nio.charset.StandardCharsets;

import rover2.rover2android.MainActivity;
import rover2.rover2android.serial.ISerialDataReceiver;
import rover2.rover2android.serial.UsbSerial;
import rover2.rover2android.websocket.IWebSocketReceiver;
import rover2.rover2android.websocket.WebSocketClient;

public class Bridge {

    private MainActivity mainActivity;
    private UsbSerial usbSerial = new UsbSerial();
    private WebSocketClient webSocketClient;

    public Bridge(){
    }

    public void onCreate(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        this.usbSerial.onCreate(mainActivity, new ISerialDataReceiver() {
            @Override
            public void onReceivedData(String change) {
                Bridge.this.mainActivity.output("Arduino: " + change);
            }
        });

        this.webSocketClient = new WebSocketClient(new IWebSocketReceiver() {
            @Override
            public void log(String message) {
                Bridge.this.mainActivity.output("WebSocket: " + message);
            }

            @Override
            public void arduinoCommand(String message) {
                Bridge.this.sendCommandToArduino(message);
            }
        });
    }

    public void start(){
        this.webSocketClient.startSocketListener();
    }

    public void sendTextToWebsocket(String message)
    {
        this.webSocketClient.sendText(message);
    }

    public void sendCommandToArduino(String message)
    {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        this.usbSerial.write(data);
    }

}
