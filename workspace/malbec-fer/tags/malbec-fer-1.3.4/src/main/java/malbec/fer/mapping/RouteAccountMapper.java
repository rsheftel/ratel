package malbec.fer.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

class RouteAccountMapper {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("BADB");

    private Map<PlatformRoute, PlatformRouteAccount> praMap = new HashMap<PlatformRoute, PlatformRouteAccount>();

    public synchronized int initialize() {
        return initialize(praMap);
    }

    @SuppressWarnings("unchecked")
    private synchronized int initialize(Map<PlatformRoute, PlatformRouteAccount> map) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<PlatformRouteAccount> results;

        try {
            Query query = em.createQuery("from PlatformRouteAccount");
            tx.begin();
            results = query.getResultList();
            tx.commit();
        } finally {
            em.close();
        }

        if (results == null) {
            return -1;
        }

        for (PlatformRouteAccount pra : results) {
            // We may have bad data, ignore the rows as they are not used
            if (pra.platform != null && pra.route != null && pra.account != null) {
                map.put(new PlatformRoute(pra.platform, pra.route), pra);
            }
        }

        return results.size();
    }

    private static class PlatformRoute {
        private String platformId;
        private String route;

        public PlatformRoute(String platform, String route) {
            this.platformId = platform.toUpperCase();
            this.route = route.toUpperCase();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PlatformRoute)) {
                return false;
            }

            PlatformRoute other = (PlatformRoute) obj;
            return platformId.equals(other.platformId) && route.equals(other.route);
        }

        @Override
        public int hashCode() {
            return platformId.hashCode() * route.hashCode();
        }

    }

    public String lookupAccount(String platform, String route) {
        PlatformRouteAccount pra = praMap.get(new PlatformRoute(platform, route));

        if (pra != null) {
            return pra.account;
        }

        return null;
    }

    public void addMapping(String platform, String route, String account) {

        if (platform != null && route != null && account != null) {
            praMap.put(new PlatformRoute(platform, route), new PlatformRouteAccount(platform, route, account));
        }

    }

    public synchronized int reload() {
        Map<PlatformRoute, PlatformRouteAccount> map = new HashMap<PlatformRoute, PlatformRouteAccount>();

        int reloadCount = initialize(map);
        this.praMap = map;

        return reloadCount;
    }
}
