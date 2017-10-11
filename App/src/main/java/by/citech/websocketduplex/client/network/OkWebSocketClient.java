package by.citech.websocketduplex.client.network;

import java.util.concurrent.TimeUnit;
import okhttp3.WebSocket;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class OkWebSocketClient extends WebSocketListener {
    IOkWebSocketClientListener listener;
    private boolean retryOnConnectionFailure;
    private long readTimeout;
    private String url;

    public OkWebSocketClient(boolean retryOnConnectionFailure, long readTimeout, String url, IOkWebSocketClientListener listener) {
        this.listener = listener;
        this.retryOnConnectionFailure = retryOnConnectionFailure;
        this.readTimeout = readTimeout;
        this.url = url;
    }

    public void run() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(this.readTimeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(this.retryOnConnectionFailure)
                .build();
        Request request = new Request.Builder()
//              .url("ws://echo.network.org")
                .url(this.url)
                .build();
        client.newWebSocket(request, this);
        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
    }

    @Override public void onOpen(WebSocket webSocket, Response response) {
        listener.onOpen(webSocket);
//      System.out.println("Введите сообщение для отправки и нажмите Enter.");
//      String inString = "";
//      Scanner in = new Scanner(System.in);
//      while (!inString.equals("exit")) {
//          try {
//              inString = in.nextLine();
//          } catch (Exception e) {
//              e.printStackTrace();
//          }
//          webSocket.send(inString);
//      }
//      System.out.println("6");
//      webSocket.send("Hello...");
//      System.out.println("7");
//      webSocket.send("...World!");
//      System.out.println("8");
//      webSocket.send(ByteString.decodeHex("deadbeef"));
//      System.out.println("9");
//      System.out.println("10");
//      webSocket.close(1000, "Goodbye, World!");
//      int i = 0;
//      while (i++ < 10000) {
//          webSocket.send("...World!" + i);
//      }
        webSocket.send("...World!");
//      System.out.println("Client: onOpen");
//      webSocket.close(1000, "Goodbye, World!");
    }

    @Override public void onMessage(WebSocket webSocket, String text) {
        this.listener.onMessage(text);
    }

    @Override public void onMessage(WebSocket webSocket, ByteString bytes) {
        this.listener.onMessage(bytes);
    }

    @Override public void onClosing(WebSocket webSocket, int code, String reason) {
        this.listener.onClosing(code, reason);
        webSocket.close(1000, null);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        this.listener.onClosed(code, reason);
    }

    @Override public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        this.listener.onFailure();
        t.printStackTrace();
    }
}
