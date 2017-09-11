package client;

import client.network.BanchoClient;
import common.database.User;
import common.helpers.JsonSerializer;
import common.requests.Request;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    public static Runnable client;
    public static ExecutorService executorService;
    public static void main(String[] args) throws IOException {
        client = BanchoClient.Create();
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(client);
        Scanner scanner = new Scanner(System.in);
        while(true) {
            String text = scanner.nextLine();
            try {
                Request request = JsonSerializer.Deserialize(text, Request.class);
                BanchoClient.SendRequest(request.type, request.payload);
            } catch (Exception e) {

            }

        }
    }
}
