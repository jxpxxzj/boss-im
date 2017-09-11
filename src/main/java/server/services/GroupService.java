package server.services;

import common.database.Groups;
import common.database.GroupMember;
import common.enums.EndpointType;
import common.enums.GroupMemberType;
import common.enums.RequestType;
import common.helpers.HibernateUtil;
import common.requests.Request;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import server.Bancho;
import server.clients.Client;
import server.clients.ClientManager;

import java.security.acl.Group;
import java.util.List;
import java.util.Map;

public class GroupService {
    public static long CreateGroup(long uid, String name) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Groups group = new Groups();
        group.name = name;
        group.createTime = Bancho.getCurrentTime();
        session.save(group);
        transaction.commit();

        Groups group1 = GetGroupByTime(group.createTime);

        transaction = session.beginTransaction();
        GroupMember member = new GroupMember();
        member.groupId = group1.id;
        member.userId = uid;
        member.type = GroupMemberType.Creator;
        session.save(member);
        transaction.commit();
        session.close();
        return group1.id;
    }

    public static Groups GetGroup(long gid) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Groups where id = :id");
        query.setParameter("id", gid);
        Groups result = (Groups)query.uniqueResult();
        transaction.commit();
        session.close();
        return result;
    }

    public static Groups GetGroupByTime(long time) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from Groups where createTime=:time");
        query.setParameter("time", time);
        Groups result = (Groups)query.uniqueResult();
        transaction.commit();
        session.close();
        return result;
    }
    public static void JoinGroup(long gid, long uid) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        GroupMember member = new GroupMember();
        member.groupId = gid;
        member.userId = uid;
        member.type = GroupMemberType.Member;
        session.save(member);
        transaction.commit();
        session.close();
    }
    public static void LeaveGroup(long gid, long uid) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from GroupMember where groupId=:gid and userId = :uid");
        query.setParameter("gid", gid);
        query.setParameter("uid", uid);
        GroupMember member = (GroupMember)query.uniqueResult();
        session.delete(member);
        transaction.commit();
        session.close();
    }

    public static List<GroupMember> GetGroupUser(long gid) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from GroupMember where groupId=:id");
        query.setParameter("id", gid);
        List<GroupMember> result = query.getResultList();
        transaction.commit();
        session.close();
        return result;
    }

    public static void DisbandGroup(long gid) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from GroupMember where groupId=:gid");
        query.setParameter("gid", gid);
        List<GroupMember> members = query.getResultList();
        session.delete(members);
        transaction.commit();

        transaction = session.beginTransaction();
        query = session.createQuery("from Groups where id=:gid");
        query.setParameter("gid", gid);
        Groups groups = (Groups)query.uniqueResult();
        session.delete(groups);
        transaction.commit();
        session.close();
    }
    public static List<GroupMember> GetMember(long gid) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from GroupMember where groupId=:id");
        List<GroupMember> members = query.getResultList();
        transaction.commit();
        session.close();
        return members;
    }
    public static void SendMessage(long sender, long gid, boolean useEscape, String content) {
        List<GroupMember> members = GetGroupUser(gid);
        for (GroupMember m:
             members) {
            MessageService.SendMessage(sender, gid, EndpointType.Group, m.userId, EndpointType.User, useEscape, content);
        }
    }

    public static void SendRequestToGroup(long gid, RequestType type, Map<String, Object> payload) {
        List<GroupMember> members = GetGroupUser(gid);
        for (GroupMember m:
                members) {
            Client client = ClientManager.findClient(m.userId);
            client.SendRequest(type, payload);
        }
    }
}
