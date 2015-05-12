package malbec.fer.dao;

import static malbec.util.SqlUtil.findSqlException;
import static malbec.util.StringUtils.getLock;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import malbec.fer.Order;
import malbec.fer.OrderStatus;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle all of my database needs for Orders.
 * 
 */
public class OrderDao {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EntityManagerFactory emf;

    private static OrderDao instance = new OrderDao();

    private OrderDao() {
        emf = Persistence.createEntityManagerFactory("BADB");
    }

    public static OrderDao getInstance() {
        return instance;
    }

    public Order findOrderByClientOrderID(String clientOrderId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Query query = em.createQuery("from Order as orders where orders.clientOrderId = :clientOrderId");
        query.setParameter("clientOrderId", clientOrderId);

        Order dbOrder = null;

        // Put in our own logic as we may get no results
        List<?> results = query.getResultList();
        if (results.size() > 0) {
            dbOrder = (Order) results.get(0);
        }

        tx.commit();
        em.close();

        return dbOrder;
    }

    /**
     * Make it easier to use.
     * 
     * @param userOrderId
     * @param orderDate
     * @return
     */
    public Order findOrderByUserOrderId(String userOrderId, Date orderDate) {
        return findOrderByUserOrderId(userOrderId, new LocalDate(orderDate));
    }

    /**
     * We are assuming that we will not be using the message type to lookup orders.
     * 
     * @param userOrderId
     * @param orderDate
     * @return
     */
    public Order findOrderByUserOrderId(String userOrderId, LocalDate orderDate) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Query query = em.createQuery("from Order as orders where orders.userOrderId = :userOrderId "
            + "and orders.orderDate = :orderDate");
        query.setParameter("userOrderId", userOrderId);
        query.setParameter("orderDate", orderDate.toDateMidnight().toDate());

        Order dbOrder = null;

        // Put in our own logic as we may get no results
        List<?> results = query.getResultList();
        if (results.size() > 0) {
            dbOrder = (Order) results.get(0);
        }

        tx.commit();
        em.close();

        return dbOrder;
    }

    public long persistOrder(Order order) {
        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            em.persist(order);
            tx.commit();

            return order.getId();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            Throwable sql = findSqlException(e);
            if (sql != null) {
                order.setStatus("FAILED");
                order.setMessage(sql.getMessage());
                log.error("Unable to save new " + getPersistedClassName(order) + ":" + order.toString(), sql);
                SQLException sqlE = (SQLException) sql;
                if (sqlE.getErrorCode() == 2601) {
                    // don't send an email
                    return -1;
                }
            }
            order.setStatus("FAILEDINSERT");
            log.error("Unable to save new" + getPersistedClassName(order) + ":" + order.toString(), e);
            // sendPersistenceError(order, "Unable to persist order - will not be sending.", e);
            throw e;
        } finally {
            em.close();
        }
    }

    private String getPersistedClassName(Order order) {
        return order.getClass().getSimpleName();
    }

    /**
     * 1
     * 
     * @param clientOrderId
     * @param orderId
     * @return
     */
    public Order updateOrderWithOrderId(String clientOrderId, OrderStatus status, String orderId) {
        Order dbOrder = null;

        synchronized (getLock(clientOrderId)) {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Query query = em.createQuery("from Order as orders where orders.clientOrderId = :clientOrderId");
            query.setParameter("clientOrderId", clientOrderId);

            dbOrder = (Order) query.getSingleResult();

            dbOrder.setStatus(status);
            dbOrder.setOrderID(orderId);
            try {
                em.persist(dbOrder);
                tx.commit();
            } finally {
                em.close();
            }
        }
        return dbOrder;
    }

    /**
     * 2
     * 
     * @param orderID
     * @param status
     * @param executedQuantity
     * @return
     */
    public Order updateExecutedQuantity(String clientOrderID, OrderStatus status, double executedQuantity) {
        Order dbOrder = null;

        synchronized (getLock(clientOrderID)) {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Query query = em.createQuery("from Order as orders where orders.clientOrderId = :clientOrderId");
            query.setParameter("clientOrderId", clientOrderID);

            dbOrder = (Order) query.getSingleResult();

            dbOrder.setExecutedQuantity(executedQuantity);
            dbOrder.setStatus(status);

            try {
                em.persist(dbOrder);
                tx.commit();
            } finally {
                em.close();
            }
        }

        return dbOrder;
    }

    /**
     * 3
     * 
     * @param order
     * @param status
     * @return
     */
    public void updateOrder(Order order, OrderStatus status) {
        EntityManager em = null;
        try {
            synchronized (getLock(order.getClientOrderId())) {
                em = emf.createEntityManager();
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                Order dbOrder = em.getReference(Order.class, order.getId());
                dbOrder.setStatus(status);
                dbOrder.setExchange(order.getExchange());
                em.persist(dbOrder);
                tx.commit();
            }
        } finally {
            em.close();
        }
    }

    /**
     * 4
     * 
     * @param order
     * @param status
     * @param message
     * @return
     */
    public void updateOrder(Order order, OrderStatus status, String message) {
        EntityManager em = null;
        try {
            synchronized (getLock(order.getClientOrderId())) {
                em = emf.createEntityManager();
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                Order dbOrder = em.getReference(Order.class, order.getId());
                dbOrder.setStatus(status);
                // Limit the error message to the column size
                dbOrder.setMessage(message.substring(0, Math.min(message.length(), 255)));
                em.persist(dbOrder);
                tx.commit();
            }
        } finally {
            em.close();
        }
    }

    /**
     * 5
     * 
     * @param clientOrderID
     * @param status
     * @return
     */
    public Order updateOrder(String clientOrderID, OrderStatus status) {
        Order dbOrder = null;

        synchronized (getLock(clientOrderID)) {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Query query = em.createQuery("from Order as orders where orders.clientOrderId = :clientOrderId");
            query.setParameter("clientOrderId", clientOrderID);

            dbOrder = (Order) query.getSingleResult();

            dbOrder.setStatus(status);
            try {
                em.persist(dbOrder);
                tx.commit();
            } finally {
                em.close();
            }
        }
        return dbOrder;
    }

    /**
     * 6
     * 
     * @param clientOrderID
     * @param status
     * @param updateTime
     * @return
     */
    public Order updateOrder(String clientOrderID, OrderStatus status, DateTime updateTime) {
        synchronized (getLock(clientOrderID)) {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Query query = em.createQuery("from Order as orders where orders.clientOrderId = :clientOrderId");
            query.setParameter("clientOrderId", clientOrderID);

            Order dbOrder = (Order) query.getSingleResult();

            long dbTime = dbOrder.getUpdatedAt().getTime() / 1000 * 1000; // drop off the mills
            dbTime = dbTime - 1000; // subtract 1 second
            long eventTime = updateTime.getMillis();

            if (dbTime <= eventTime) {
                dbOrder.setStatus(status);
                try {
                    em.persist(dbOrder);
                    tx.commit();
                } finally {
                    em.close();
                }
                return dbOrder;
            } else {
                log.warn("Not updating order " + clientOrderID + " to status: " + status
                    + ", late update. LastUpdatedAt: " + dbOrder.getUpdatedAt() + ", messageTime: "
                    + updateTime);
                tx.commit();
                em.close();
                return null;
            }
        }
    }

    /**
     * 7
     * 
     * @param clientOrderID
     * @param status
     * @param message
     * @return
     */
    public Order updateOrder(String clientOrderID, OrderStatus status, String message) {
        Order dbOrder = null;

        synchronized (getLock(clientOrderID)) {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            Query query = em.createQuery("from Order as orders where orders.clientOrderId = :clientOrderId");
            query.setParameter("clientOrderId", clientOrderID);

            dbOrder = (Order) query.getSingleResult();

            dbOrder.setStatus(status);
            dbOrder.setMessage(message);
            try {
                em.persist(dbOrder);
                tx.commit();
            } finally {
                em.close();
            }
        }
        return dbOrder;
    }

    public Long maxIdBeforeToday() {
        LocalDate today = new LocalDate();

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Query query = em.createQuery("select max(o.id) from Order as o where o.createdAt < :createdAt");
        query.setParameter("createdAt", today.toDateMidnight().toDate());

        Long maxId = (Long) query.getSingleResult();
        tx.commit();
        em.close();

        return maxId;
    }

    @SuppressWarnings("unchecked")
    public List<Order> newOrdersForClientAppAfter(long greaterThanMe, String appName) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        StringBuilder sb = new StringBuilder(256);
        sb.append("from Order as orders where orders.id > :id and orders.clientAppName = :appName");
        //sb.append(" and orders.fixMessageType='D'");
        Query query = em.createQuery(sb.toString());
        query.setParameter("id", greaterThanMe);
        query.setParameter("appName", appName);
        

        List<?> results = query.getResultList();
        List<Order> newOrders = new ArrayList<Order>(results.size());
        
        for (Order order : (List<Order>)results) {
            if (order.reallyIsOrder()) {
                newOrders.add(order);
            }
        }
      
        tx.commit();
        em.close();

        return newOrders;
    }

    public void updateOrder(Order order) {
        EntityManager em = null;
        try {
            synchronized (getLock(order.getClientOrderId())) {
                em = emf.createEntityManager();
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                em.merge(order);
                tx.commit();
            }
        } finally {
            em.close();
        }
    }
}
