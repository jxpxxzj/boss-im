package server.clients;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientManager {

    public static ConcurrentLinkedQueue<Client> clientsForProcessing = new ConcurrentLinkedQueue<Client>();

    public static List<Client> getClients() {
        synchronized (lockUserList) {
            return clients;
        }
    }

    public static int getClientCount() {
        synchronized (lockUserList) {
            return getClients().size();
        }
    }

    private static Object lockUserList = new Object();
    public static List<Client> clients;
    private static Logger logger = LogManager.getLogger(ClientManager.class);

    public static Client findClient(long uid) {
        List<Client> clients = getClients();

        for (int i = 0; i < getClients().size(); i++) {
            if (clients.get(i).user.id == uid) {
                return clients.get(i);
            }
        }
        return null;
    }

    public static Client getClientForProcessing() {
        synchronized (lockUserList) {
            if (clientsForProcessing.peek() == null) {
                return null;
            }
            Client client = clientsForProcessing.peek();
            clientsForProcessing.remove();
            return client;
        }
    }

    public static void addClientForProcessing(Client c) {
        clientsForProcessing.add(c);
    }

    public static void RegisterClient(Client c) {
        synchronized (lockUserList) {
            // if (FindClient(c.user.uid) != null) {
                clients.add(c);
                logger.info("Client registered");
                addClientForProcessing(c);

            // } else { // client exist
            //     logger.warn("same client is existed: " + c.user.uid);
            //     UnregisterClient(c);
            // RegisterClient(c);
            // }
        }
    }

    public static void UnregisterClient(Client c) throws IOException {
        synchronized (lockUserList) {
            c.socket.close();
            clients.remove(c);
            clientsForProcessing.remove(c);
            logger.info("Client unregistered");
        }
    }
}
