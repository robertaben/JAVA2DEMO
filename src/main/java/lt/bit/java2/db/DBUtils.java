package lt.bit.java2.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lt.bit.java2.db.entities.Employee;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DBUtils {


    static private Properties properties() throws IOException {

        Properties properties = new Properties();

        properties.load(DBUtils.class.getClassLoader().getResourceAsStream("application.properties"));

        return properties;
    }

    static private EntityManagerFactory entityManagerFactory;

    static private ThreadLocal<EntityManager> threadLocal = new ThreadLocal<>();

    static private EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory("employee-persistence-unit");
        }
        return entityManagerFactory;
    }

    static public EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();
        if (em == null || !em.isOpen()) {
            em = getEntityManagerFactory().createEntityManager();
            threadLocal.set(em);
        }
        return em;
    }

    static public <E> E executeInTransaction(Function<EntityManager, E> action) {
        EntityManager em = DBUtils.getEntityManager();
        try {
            em.getTransaction().begin();
            //
            E e = action.apply(em);
            em.getTransaction().commit();
            return e;

        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new DBExeption(e);

        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }

    }

    static public class DBExeption extends RuntimeException {
        public DBExeption(Throwable cause) { super(cause);
        }

        public DBExeption() {
            super();
        }

        public DBExeption(String message, Throwable cause) {
            super(message, cause);
        }

        public DBExeption(String message) {
            super(message);
        }
    }
}
