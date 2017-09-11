package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.clients.Client;
import server.clients.ClientManager;

import java.io.IOException;

public class BanchoWorker {
    public Logger logger;

    public Runnable runnable;
    public int id;

    private BanchoWorker () {

    }

    public static BanchoWorker Create(int id) {
        BanchoWorker banchoWorker = new BanchoWorker();
        banchoWorker.logger = LogManager.getLogger("Worker " + id);
        banchoWorker.runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    banchoWorker.DoWork(null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        return banchoWorker;
    }

    public void DoWork(Object state) throws InterruptedException, IOException {
        Client client = ClientManager.getClientForProcessing();
        if (client == null) {
            // logger.info("No processed client, sleep.");
            Thread.sleep(20);
            return;
        }

        long before = Bancho.getCurrentTime();
        client.CheckClient(this);
        ClientManager.addClientForProcessing(client);

        long after = Bancho.getCurrentTime();

    }
}
