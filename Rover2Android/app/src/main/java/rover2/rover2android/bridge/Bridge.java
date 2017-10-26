package rover2.rover2android.bridge;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;

import rover2.rover2android.MainActivity;
import rover2.rover2android.R;
import rover2.rover2android.camera.CameraPreview;
import rover2.rover2android.util.UiThreadTimer;
import rover2.rover2android.camera.IImageReceiver;
import rover2.rover2android.serial.ISerialDataReceiver;
import rover2.rover2android.serial.UsbSerial;
import rover2.rover2android.websocket.IWebSocketReceiver;
import rover2.rover2android.websocket.ImageUploader;
import rover2.rover2android.websocket.WebSocketClient;

public class Bridge implements IImageReceiver {

    private MainActivity mainActivity;
    private UsbSerial usbSerial = new UsbSerial();
    private WebSocketClient webSocketClient;
    private BroadcastReceiver broadcastReceiver;
    private CameraPreview cameraPreview;


    public Bridge(){
    }

    public void onPause() {
        this.mainActivity.unregisterReceiver(broadcastReceiver);
    }


    public void onResume(){
        this.setFilters();
    }


    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbSerial.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbSerial.ACTION_NO_USB);
        filter.addAction(UsbSerial.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbSerial.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbSerial.ACTION_USB_PERMISSION_NOT_GRANTED);
        this.mainActivity.registerReceiver(broadcastReceiver, filter);
    }

    public void onCreate(MainActivity mainActivity, Bundle savedInstanceState)
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

        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onReceiveBroadcast(context, intent);
            }
        };

        if (null == savedInstanceState) {
            this.setCameraPreview(CameraPreview.newInstance());
            this.mainActivity.getFragmentManager().beginTransaction()
                    .replace(R.id.cameraPlaceholder, getCameraPreview())
                    .commit();
        }
    }

    public void onReceiveBroadcast(Context context, Intent intent) {
        switch (intent.getAction()) {
            case UsbSerial.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                break;
            case UsbSerial.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                break;
            case UsbSerial.ACTION_NO_USB: // NO USB CONNECTED
                Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                break;
            case UsbSerial.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                break;
            case UsbSerial.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public CameraPreview getCameraPreview() {
        return cameraPreview;
    }

    public void setCameraPreview(CameraPreview cameraPreview) {
        this.cameraPreview = cameraPreview;
    }

    public void start(){


        this.webSocketClient.startSocketListener();

        //Send pictures to server if required
        if(this.mainActivity.isSendPictures()) {

            UiThreadTimer cameraTimer = new UiThreadTimer(new Runnable() {
                @Override
                public void run() {
                    getCameraPreview().takePicture();
                }
            });
            cameraTimer.start(2000);
        }

        //Reconnect if connection lost
        UiThreadTimer webSocketTimer = new UiThreadTimer(new Runnable() {
            @Override
            public void run() {
                Bridge.this.webSocketClient.checkConnection();
            }
        });
        webSocketTimer.start(10000);
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

    @Override
    public void binaryReceived(byte[] bytes) {
        ImageUploader imageUploader = new ImageUploader(this.mainActivity);
        imageUploader.addBinary("bitmap", "rover.jpeg", bytes);
        imageUploader.execute("http://rover2.azurewebsites.net/Rover/UploadImage");
    }
}
