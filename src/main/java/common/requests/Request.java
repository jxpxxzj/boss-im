package common.requests;

import common.enums.RequestType;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.swing.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;

public class Request {
    public RequestType type;
    public String token;
    public long sendTime;
    public Map<String, Object> payload;
}
