package common.helpers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {
    static final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure("/hibernate.cfg.xml").build();
    public static SessionFactory factory;
    public static SessionFactory getFactory() {
        if (factory == null)
            factory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        return factory;
    }
    public static Session CreateSession() {
        Session session = getFactory().openSession();
        return session;
    }
}
