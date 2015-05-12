package malbec.fer.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class PlatformSecurity {
    // Start EntityManagerFactory
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

    private Map<String, AllowedClient> allowedPlatforms = new HashMap<String, AllowedClient>();

    public PlatformSecurity() {
        this(false);
    }

    public PlatformSecurity(boolean initalize) {
        if (initalize) {
            this.initialize(allowedPlatforms);
        }
    }

    public synchronized int initialize() {
        return initialize(allowedPlatforms);
    }

    @SuppressWarnings("unchecked")
    private synchronized int initialize(Map<String, AllowedClient> map) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<AllowedClient> results;

        try {
            Query query = em.createQuery("from AllowedClient");
            tx.begin();
            results = query.getResultList();
            tx.commit();
        } finally {
            em.close();
        }

        if (results == null) {
            return -1;
        }

        for (AllowedClient client : results) {
            map.put(client.getName().toUpperCase(), client);
        }

        return results.size();
    }

    public boolean canSendOrder(String client, String platform) {
        AllowedClient allowedClient = allowedPlatforms.get(client.toUpperCase());

        return allowedClient != null && allowedClient.canSendOrder()
                && allowedClient.canSendOrderTo(platform);
    }

    public synchronized int reload() {
        Map<String, AllowedClient> allowedPlatforms = new HashMap<String, AllowedClient>();

        int reloadCount = initialize(allowedPlatforms);
        this.allowedPlatforms = allowedPlatforms;

        return reloadCount;
    }

    public void addClient(String clientName) {
        allowedPlatforms.put(clientName.toUpperCase(), new AllowedClient(clientName, true));
    }

    public void addPlatformToClient(String clientName, String platform) {
        AllowedClient client = new AllowedClient(clientName, true);
        
        AllowedPlatformForClient newPlatform = new AllowedPlatformForClient(platform, client);
        client.addPlatform(newPlatform);
        
        allowedPlatforms.put(clientName.toUpperCase(), client);
    }
}
