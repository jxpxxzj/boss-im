package server.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.deploy.panel.CacheSettingsDialog;
import common.database.FriendsRelation;
import common.database.GroupMember;
import common.database.Message;
import common.database.User;
import common.enums.RequestType;
import common.enums.EndpointType;
import common.helpers.JsonSerializer;
import common.requests.Request;
import common.helpers.PasswordAuthentication;
import server.services.GroupService;
import server.services.MessageService;
import server.services.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.Bancho;
import server.BanchoWorker;

import java.io.*;
import java.net.Socket;
import java.util.*;


public class Client {
    public static final int timeoutSeconds = 90;

    public User user;
    public boolean IsAuth;

    public boolean isKilled = false;

    public Socket socket;

    private LinkedList<Request> sendQueue;
    private boolean Pinging;
    private long lastPingTime;
    private long lastReceiveTime;

    private Logger logger;

    public Client(Socket socket) {
        this.socket = socket;
        sendQueue = new LinkedList<>();
        logger = LogManager.getLogger(Client.class);
        lastReceiveTime = Bancho.getCurrentTime();
    }

    private void receive() throws IOException {
        if (isKilled) {
            return;
        }
        InputStream stream = socket.getInputStream();
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader reader1 = new BufferedReader(reader);
        String totals;
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
                Kill("invalid data");
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
        if (isKilled) {
            return;
        }
        if (sendQueue.size() == 0) {
            return;
        }
        Request data = sendQueue.getFirst();
        String payloadJson = JsonSerializer.Serialize(data.payload);
        data.sendTime = Bancho.getCurrentTime();
        data.token = new PasswordAuthentication().hash(payloadJson + data.sendTime);
        String json = JsonSerializer.Serialize(data);
        logger.info(json);
        OutputStream outputStream = socket.getOutputStream();
        BufferedWriter printWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        if(!json.trim().isEmpty())
            printWriter.write(json + "\n");
        printWriter.flush();

        sendQueue.removeFirst();
    }

    private void work() {
        if (!IsAuth) return;
        List<Message> messages = MessageService.getUnreadMessage(user.id);
        for (Message m:
             messages) {
            SendRequest(RequestType.Message, MessageService.CreateMap(m));
        }
    }

    public void CheckClient(BanchoWorker worker) throws IOException {
        if (isKilled) {
            return;
        }

        long current = Bancho.getCurrentTime();
        long timeout = current - lastReceiveTime;

        if(timeout > timeoutSeconds * 1000) {
            Kill("timed out");
            return;
        }

        work();
        receive();
        Ping();
        send();

    }

    public void HandleIncoming(Request request) throws IOException {
        lastReceiveTime = Bancho.getCurrentTime();
        if (!IsAuth && (request.type == RequestType.User_Login || request.type == RequestType.User_Register)) {
            switch (request.type) {
                case User_Register:
                    long uid = UserService.Register(request.payload.get("username").toString(), request.payload.get("password").toString());
                    Authenticate(String.valueOf(uid), request.payload.get("password").toString());
                    break;
                case User_Login:
                    Authenticate(request.payload.get("uid").toString(), request.payload.get("password").toString());
                    break;
            }
        }
        if (IsAuth) {
            switch(request.type) {
                case Message:
                    long targetId = Long.parseLong(request.payload.get("target").toString());
                    EndpointType targetType = Enum.valueOf(EndpointType.class, request.payload.get("targetType").toString());
                    switch(targetType) {
                        case User:
                            MessageService.SendMessage(user.id, targetId, Boolean.valueOf(request.payload.get("useEscape").toString()), request.payload.get("content").toString());
                            break;
                        case Channel:
                            break;
                        case Group:
                            GroupService.SendMessage(user.id, targetId, Boolean.valueOf(request.payload.get("useEscape").toString()), request.payload.get("content").toString());
                            break;
                    }
                    break;
                case Friend_Add:
                    long uid = Long.parseLong(request.payload.get("user").toString());
                    UserService.AddFriend(user.id, uid);
                    break;
                case Friend_GetList:
                    Map<String, Object> map = new HashMap<>();
                    map.put("friends", UserService.GetFriendsList(user.id));
                    SendRequest(RequestType.Friend_GetList, map);
                    break;
                case Friend_Delete:
                    long toRemove1 = Long.parseLong(request.payload.get("user1").toString());
                    long toRemove2 = Long.parseLong(request.payload.get("user2").toString());
                    UserService.RemoveFriend(toRemove1, toRemove2);
                    Map<String, Object> mapRemove = new HashMap<>();
                    mapRemove.put("user1", request.payload.get("user1").toString());
                    mapRemove.put("user2", request.payload.get("user2").toString());
                    mapRemove.put("result", String.valueOf(true));
                    SendRequest(RequestType.Friend_Delete, mapRemove);
                    Client targetClient = ClientManager.findClient(toRemove2);
                    targetClient.SendRequest(RequestType.Friend_Delete, mapRemove);
                    break;
                case Group_Create:
                    long gid = GroupService.CreateGroup(user.id, request.payload.get("name").toString());
                    Map<String, Object> map1 = new HashMap<>();
                    map1.put("gid", gid);
                    SendRequest(RequestType.Group_AcceptJoin, map1);
                    break;
                case Group_List:
                    Map<String, Object> groupListMap = new HashMap<>();
                    groupListMap.put("groups", GroupService.GetGroupUser(user.id));
                    SendRequest(RequestType.Group_List, groupListMap);
                    break;
                case Group_RequestJoin:
                    long gid2 = Long.parseLong(request.payload.get("id").toString());
                    GroupService.JoinGroup(gid2, user.id);
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("gid", gid2);
                    SendRequest(RequestType.Group_AcceptJoin, map2);
                    break;
                case Group_Exit:
                    long gid3 = Long.parseLong(request.payload.get("id").toString());
                    GroupService.LeaveGroup(gid3, user.id);
                    Map<String, Object> map3 = new HashMap<>();
                    map3.put("result", true);
                    SendRequest(RequestType.Group_Exit, map3);
                    break;
                case Group_Disband:
                    long gid4 = Long.parseLong(request.payload.get("id").toString());
                    GroupService.DisbandGroup(gid4);
                    Map<String, Object> map4 = new HashMap<>();
                    map4.put("result", true);
                    GroupService.SendRequestToGroup(gid4, RequestType.Group_Disband, map4);
                    break;
                case Group_MemberList:
                    long gid5 = Long.parseLong(request.payload.get("id").toString());
                    List<GroupMember> members = GroupService.GetGroupUser(gid5);
                    Map<String, Object> map5 = new HashMap<>();
                    map5.put("list", members);
                    SendRequest(RequestType.Group_MemberList, map5);
                    break;
                case User_Logout:
                    Kill("log out");
                    break;
                case Pong:
                    Pong();
                    break;
            }
        } else {
            logger.warn("Client not auth, ignore request");
        }
    }

    private void Authenticate(String uid, String password) {
        logger.info("uid: " + uid);
        boolean result = UserService.Login(uid, password);
        if (result) {
            IsAuth = true;
            user = UserService.GetUser(Long.valueOf(uid));
            Pong();
        }

        Map<String, Object> o = new HashMap<>();
        o.put("result", String.valueOf(IsAuth ? uid : 0));
        SendRequest(RequestType.User_LoginResult, o);
    }
    
    
    public void Ping() {
        long currentTime = Bancho.getCurrentTime();
        if (!Pinging && currentTime - lastPingTime > 30 * 1000) {
            sendPing();
        }
    }

    public void Pong() {
        Pinging = false;
    }

    private void sendPing() {
        Pinging = true;
        lastPingTime = Bancho.getCurrentTime();
        SendRequest(RequestType.Ping, null);
    }

    public void Kill(String reason) throws IOException {
        isKilled = true;
        SendRequest(RequestType.User_Logout, null);
        ClientManager.UnregisterClient(this);
        logger.info("Client killed for " + reason);
    }

    public void SendRequest(RequestType type, Map<String, Object> o) {
        if ((!IsAuth || isKilled) && type != RequestType.Ping && type != RequestType.User_LoginResult) {
            return;
        }
        Request request = new Request();

        request.payload = o;
        request.type = type;
        sendQueue.push(request);
    }
}
