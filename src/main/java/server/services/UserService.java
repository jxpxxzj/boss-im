package server.services;

import common.database.FriendsRelation;
import common.database.User;
import common.helpers.HibernateUtil;
import common.helpers.PasswordAuthentication;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import server.Bancho;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserService {

    public static User GetUser(long uid) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from User where id=:uid");
        query.setParameter("uid", uid);
        User user = (User)query.uniqueResult();
        transaction.commit();
        session.close();
        user.password = "";
        return user;
    }

    public static User GetUserByTime(long time) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from User where createTime=:time");
        query.setParameter("time", time);
        User user = (User)query.uniqueResult();
        transaction.commit();
        session.close();
        return user;
    }

    public static String getToken(User user) {
        PasswordAuthentication authentication = new PasswordAuthentication();
        String token = authentication.hash(user.password + user.username + user.id);
        return token;
    }

    public static boolean Login(String uid, String password) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("select password from User where id = :uid");
        query.setParameter("uid", Long.valueOf(uid));
        String passwordDb = (String)query.uniqueResult();
        transaction.commit();
        session.close();
        return PasswordAuthentication.getInstance().authenticate(password, passwordDb);
    }

    public static long GetUserCount() {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("select count(*) from User");
        long data = (Long)query.uniqueResult();
        transaction.commit();
        session.close();
        return data;
    }

    public static long Register(String username, String password) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        User user = new User();
        user.username = username;
        user.password = PasswordAuthentication.getInstance().hash(password);
        user.createTime = Bancho.getCurrentTime();
        session.save(user);
        transaction.commit();
        session.close();
        return GetUserByTime(user.createTime).id;
    }

    public static void AddFriend(long user1, long user2) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        FriendsRelation relation = new FriendsRelation();
        relation.user1 = user1;
        relation.user2 = user2;
        session.save(relation);
        transaction.commit();
        session.close();
    }

    public static void RemoveFriend(long user1, long user2) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from FriendsRelation where user1=:uid1 and user2=:uid2");
        query.setParameter("uid1", user1);
        query.setParameter("uid2", user2);
        FriendsRelation relation = (FriendsRelation)query.uniqueResult();
        session.remove(relation);
        query = session.createQuery("from FriendsRelation  where user2=:uid1 and user1=:uid2");
        query.setParameter("uid1", user1);
        query.setParameter("uid2", user2);
        FriendsRelation relation1 = (FriendsRelation)query.uniqueResult();
        session.remove(relation1);

        transaction.commit();
        session.close();
    }

    public static List<User> GetFriendsList(long uid) {
        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("from FriendsRelation where user1 = :uid");
        query.setParameter("uid", uid);
        List<FriendsRelation> list1 = query.getResultList();
        query = session.createQuery("from FriendsRelation where user2 = :uid");
        query.setParameter("uid", uid);
        List<FriendsRelation> list2 = query.getResultList();
        transaction.commit();
        session.close();

        List<User> friends = new ArrayList<>();
        for (FriendsRelation fr:
             list1) {
            friends.add(GetUser(fr.user2));
        }
        for (FriendsRelation fr:
                list2) {
            friends.add(GetUser(fr.user1));
        }

        return friends;
    }
}
