package client.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import common.Callback;
import common.enums.RequestType;
import common.helpers.JsonSerializer;
import common.helpers.PasswordAuthentication;
import common.requests.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BanchoClient {

    private static BanchoClient client;
    private static int timeoutSeconds = 90;

    public static Runnable Create() throws IOException {
        if(client == null)
            client = new BanchoClient();

        return () -> {
            try {
                client.mainLoop();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    public static long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static final String endpoint = "127.0.0.1";
    public static final int port = 6163;

    private Socket socket;
    private Logger logger = LogManager.getLogger(BanchoClient.class);

    private boolean isConnected;
    private boolean isAuth;

    private long lastReceiveTime;

    private static ArrayList<Callback> callbacks = new ArrayList<Callback>();
    public static void addCallback(Callback callback) {
        callbacks.add(callback);
    }
    public static void removeCallback(Callback callback) {
        callbacks.remove(callback);
    }

    private static void notifyIncoming(Request request) {
        for (Callback c: callbacks) {
            c.callback(request);
        }
    }

    private LinkedList<Request> sendQueue;

    private BanchoClient() throws IOException {
        sendQueue = new LinkedList<Request>();
        lastReceiveTime = getCurrentTime();
        connect();
    }

    private void connect() throws IOException {
        socket = new Socket(endpoint, port);
        logger.info("connect success");
        isConnected = true;
    }
    private void disconnect(String reason) throws IOException {
        logger.error("disconnect for " + reason);
        sendRequest(RequestType.User_Logout,null);
        isConnected = false;
        socket.close();
    }

    private void mainLoop() throws IOException, InterruptedException {
        while(true) {

            long current = getCurrentTime();
            long timeout = current - lastReceiveTime;

            if(timeout > timeoutSeconds * 1000) {
                disconnect("timed out");
                return;
            }

            work();
            receive();
            send();
            Thread.sleep(200);
        }

    }

    private void work() {
        if (staticSendQueue.isEmpty()) return;
        Request request = staticSendQueue.peek();
        staticSendQueue.remove();
        sendRequest(request.type, request.payload);
    }

    private void receive() throws IOException {
        InputStream stream = socket.getInputStream();
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader reader1 = new BufferedReader(reader);
        String info = null;
        String totals = "";
        Request data = null;
        char[] buffer = new char[4096];
        int readResult;
        while (true)  {
            if (stream.available() > 0) {
                readResult = reader1.read(buffer);
            } else {
                break;
            }
            if(readResult == -1) {
                disconnect("invalid data");
                return;
            }

            if (Objects.equals(String.valueOf(buffer), "")) {
                break;
            }
            totals = String.valueOf(buffer).trim();
            if (!totals.trim().isEmpty())
                logger.info(totals);
            try {
                data = JsonSerializer.Deserialize(totals, Request.class);
                break;
            } catch (JsonProcessingException ex) {
                data = null;
            }

        }


        if (data != null) {
            HandleIncoming(data);
        }
    }

    private void send() throws IOException {
        if (!isConnected) {
            return;
        }
        if (sendQueue.size() == 0) {
            return;
        }
        Request data = sendQueue.getFirst();
        String payloadJson = JsonSerializer.Serialize(data.payload);
        data.sendTime = getCurrentTime();
        data.token = new PasswordAuthentication().hash(payloadJson + data.sendTime);
        String json = JsonSerializer.Serialize(data);

        OutputStream outputStream = socket.getOutputStream();
        BufferedWriter printWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        if (!json.trim().isEmpty())
            printWriter.write(json + "\n");
        printWriter.flush();

        sendQueue.removeFirst();
    }

    public void HandleIncoming(Request request) throws IOException {
        lastReceiveTime = getCurrentTime();
        if (!isAuth && request.type != RequestType.User_LoginResult) {
            logger.warn("Client not auth, ignore request");
            return;
        }
        switch(request.type) {
            case Ping:
                pong();
                break;
            case User_LoginResult:
                if(Long.parseLong(request.payload.get("result").toString()) > 0) {
                    logger.info("auth success");
                    isAuth = true;
                }
                break;
        }
        notifyIncoming(request);
    }

    private void pong() {
        sendRequest(RequestType.Pong, null);
    }


    public void sendRequest(RequestType type, Map<String, Object> o) {
        if ((!isAuth || !isConnected) && type != RequestType.User_Login && type != RequestType.User_Register) {
            return;
        }
        Request request = new Request();
        request.payload = o;
        request.type = type;
        sendQueue.push(request);
    }

    private static ConcurrentLinkedQueue<Request> staticSendQueue = new ConcurrentLinkedQueue<>();

    public static void SendRequest(RequestType type,  Map<String, Object> o) {
        Request request = new Request();
        request.payload = o;
        request.type = type;
        BanchoClient.staticSendQueue.add(request);
    }

}
