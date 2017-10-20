package rover2.rover2android.websocket;


public interface IWebSocketReceiver {
    public void log(String message);
    public void arduinoCommand(String message);
}
