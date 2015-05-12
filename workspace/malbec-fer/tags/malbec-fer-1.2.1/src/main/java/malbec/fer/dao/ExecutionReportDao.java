package malbec.fer.dao;

import static malbec.util.SqlUtil.findSqlException;

import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import malbec.fer.ExecutionReport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionReportDao {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EntityManagerFactory emf;

    private static ExecutionReportDao instance = new ExecutionReportDao();

    private ExecutionReportDao() {
        emf = Persistence.createEntityManagerFactory("BADB");
    }

    public static ExecutionReportDao getInstance() {
        return instance;
    }

    public long persistExecutionReport(ExecutionReport executionReport) {
        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();
            em.persist(executionReport);
            tx.commit();

            return executionReport.getId();
        } catch (RuntimeException e) {
            tx.rollback();
            Throwable sql = findSqlException(e);
            if (sql != null) {
                log.error("Unable to save new order:" + executionReport.toString(), sql);
                SQLException sqlE = (SQLException) sql;
                if (sqlE.getErrorCode() == 2601) {
                    // don't send an email
                    return -1;
                }
            }
            log.error("Unable to save new execution report:" + executionReport.toString(), e);
            throw e;
        } finally {
            em.close();
        }
    }

}
