package server;

import common.database.User;
import common.helpers.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import java.io.IOException;


public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        Bancho.init();
        // Test1();
        return;
    }

    public static void Test1() {

        Session session = HibernateUtil.CreateSession();
        Transaction transaction = session.beginTransaction();
        User user = new User();
        user.password = "Abc";
        user.username = "Def";
        session.save(user);
        Query query = session.createQuery("from User where id = 1");
        User user1 = (User)query.uniqueResult();
        System.out.println(user1.username);
        transaction.commit();
        session.close();
        return;
    }
}
