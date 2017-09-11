package client.ui;

import client.network.BanchoClient;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import common.Callback;
import common.enums.RequestType;
import common.requests.Request;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

public class loginUIController implements Callback {

    @FXML
    public TextField uidField;
    @FXML
    public PasswordField passwordField;

    @FXML
    public Button loginButton;

    private boolean isLogging = false;
    @FXML
    public void onLoginClick() {
        if (isLogging) return;
        isLogging = true;
        loginButton.setDisable(true);
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uidField.getText());
        map.put("password", passwordField.getText());
        BanchoClient.SendRequest(RequestType.User_Login, map);
        BanchoClient.addCallback(this);

        Map<String, Object> map1 = new HashMap<>();
        map1.put("uid", 80);

        BanchoClient.SendRequest(RequestType.Friend_Add, map);
        BanchoClient.addCallback(this);
    }

    @Override
    public void callback(Request request) {
        Platform.runLater(() -> {
            if (request.type == RequestType.Friend_Add) {
                if(request.payload.get("result") == String.valueOf(true)) {
                    // ....
                }
            }
            if (request.type == RequestType.User_LoginResult) {
                loginButton.setDisable(true);
                loginButton.setText("Login success");
            }
            BanchoClient.removeCallback(this);
        });
    }
}
