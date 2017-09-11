package server.services;

import common.database.Message;
import common.enums.EndpointType;
import common.helpers.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import server.Bancho;
import sun.util.resources.cldr.yav.CalendarData_yav_CM;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageService {

    public static List<Message> peekUnreadMessage(long target) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Message where target=:target and hasRead=false");
        query.setParameter("target", target);
        List<Message> messages = query.getResultList();
        transaction.commit();
        session.close();
        return messages;
    }

    public static List<Message> getUnreadMessage(long target) {
        List<Message> messages = peekUnreadMessage(target);
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        for (Message m: messages) {
            m.hasRead = true;
            session.update(m);
        }
        transaction.commit();
        session.close();
        return messages;
    }

    public static void SendMessage(long sender, long target, boolean useEscape, String content) {
        SendMessage(sender, 0, null, target, EndpointType.User, useEscape, content);
    }
    public static void SendMessage(long sender, long container, EndpointType containerType, long target, EndpointType targetType, boolean useEscape, String content) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Message message = new Message();
        message.sender = sender;
        message.target = target;
        message.container = container;
        message.containerType = containerType;
        message.timestamp = Bancho.getCurrentTime();
        message.useEscape = useEscape;
        message.content = content;
        session.save(message);
        transaction.commit();
        session.close();
    }

    public static Map CreateMap(Message message) {
        Map<String, String> map = new HashMap<>();
        map.put("sender", String.valueOf(message.sender));
        map.put("target", String.valueOf(message.target));
        map.put("container", String.valueOf(message.container));
        map.put("timestamp", String.valueOf(message.timestamp));
        map.put("containerType", String.valueOf(message.containerType));
        map.put("useEscape", String.valueOf(message.useEscape));
        map.put("content", message.content);
        return map;
    }
}
