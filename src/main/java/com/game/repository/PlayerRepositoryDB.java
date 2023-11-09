package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;


@Repository(value = "db")

public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;
    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.HBM2DDL_AUTO, "update");

        sessionFactory = new Configuration()
                .addAnnotatedClass(Player.class)
                .addProperties(properties)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        int offset2 = pageNumber * pageSize;
        try(Session session = sessionFactory.openSession()) {
            NativeQuery nativeQuery = session.createNativeQuery("SELECT * FROM player", Player.class);
            nativeQuery.setFirstResult(offset2);
            nativeQuery.setMaxResults(pageSize);
            List<Player> list = nativeQuery.list();
            return list;
        }

    }

    @Override
    public int getAllCount() {

        try(Session session = sessionFactory.openSession()){
            Query<Long> playerGetAllCount = session.createNamedQuery("Player_GetAllCount", Long.class);
            return Math.toIntExact(playerGetAllCount.getSingleResult());
        }

    }

    @Override
    public Player save(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(player);
            transaction.commit();
            return player;
        }
    }

    @Override
    public Player update(Player player) {
        try(Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            Player playerUpdated = (Player) session.merge(player);
            transaction.commit();
            return playerUpdated;
        }

    }

    @Override
    public Optional<Player> findById(long id) {
        try(Session session = sessionFactory.openSession()) {
            String hql = "from Player where id =:id";
            Query<Player> query = session.createQuery(hql, Player.class);
            query.setParameter("id", id);
            Player player = query.uniqueResult();
            return Optional.ofNullable(player);
        }


    }

    @Override
    public void delete(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}