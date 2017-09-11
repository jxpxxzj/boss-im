package server;

import org.apache.logging.log4j.Logger;
import server.clients.Client;
import server.clients.ClientManager;

import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.*;

public class Bancho {

    public static Logger logger = LogManager.getLogger(Bancho.class);

    public static final int port = 6163;
    public static final int workerCount = 4;

    public static ExecutorService executorService;
    public static ExecutorService workerService;
    public static ServerSocket serverSocket;

    public static LinkedList<BanchoWorker> workers;
    private static long lastCntTime;
    private static long lastGCTime;

    public static long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static void init() throws IOException, InterruptedException {
        serverSocket = new ServerSocket(port);
        executorService = Executors.newSingleThreadExecutor();
        workerService = Executors.newCachedThreadPool();
        workers = new LinkedList<BanchoWorker>();
        ClientManager.clients = new LinkedList<Client>();

        for (int i=0;i<workerCount;i++) {
            workers.add(BanchoWorker.Create(i));
            logger.info("Add worker " + i);
        }
        acceptConnection();
        MainLoop();
    }

    private static void acceptConnection() {
        logger.info("Listening on " + port + " for connections...");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (socket != null) {
                        logger.info("accept socket");
                        Client client = new Client(socket);
                        ClientManager.RegisterClient(client);
                    }
                }
            }
        });
    }

    public static void MainLoop() throws IOException, InterruptedException {
        logger.info("Main loop is running...");
        long lastTime = 0;
        while(true) {
            long current = getCurrentTime();

            for (BanchoWorker wk: workers) {
                workerService.execute(wk.runnable);
            }

            if (current - lastCntTime > 10000) {
                logger.info("Client count:" + ClientManager.getClientCount());
                lastCntTime = getCurrentTime();
            }

            if (current - lastGCTime > 60000) {
                logger.info("gc called");
                System.gc();
                lastGCTime = getCurrentTime();
            }

            Thread.sleep(200);
        }


    }
}
