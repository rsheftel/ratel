package malbec.fer.mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Contains a host/client that can use the system.
 * 
 */
@Entity
@Table(name = "AllowedClients")
public class AllowedClient {

    private Long id;

    private String name;

    private Boolean allowedToSendOrders;

    private transient Map<String, AllowedPlatformForClient> platforms = new HashMap<String, AllowedPlatformForClient>();

    public AllowedClient() {}

    public AllowedClient(String name, Boolean canSendOrder) {
        super();
        this.allowedToSendOrders = canSendOrder;
        this.name = name;
    }

    @Column(name = "ClientName")
    public String getName() {
        return name;
    }

    /**
     * @return the id
     */
    @Id
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @param canSendOrder
     *            the canSendOrder to set
     */
    public void setCanSendOrder(Boolean canSendOrder) {
        this.allowedToSendOrders = canSendOrder;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the canSendOrder
     */
    @Column(name = "CanSendOrder")
    public Boolean getCanSendOrder() {
        return allowedToSendOrders;
    }

    /**
     * @return the platforms
     */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "client", fetch = FetchType.EAGER)
    public synchronized Set<AllowedPlatformForClient> getPlatforms() {
        Set<AllowedPlatformForClient> rt = new HashSet<AllowedPlatformForClient>();
        rt.addAll(platforms.values());

        return rt;
    }

    /**
     * @param platforms
     *            the platforms to set
     */

    public synchronized void setPlatforms(Set<AllowedPlatformForClient> platforms) {
        platforms.clear();
        for (AllowedPlatformForClient platform : platforms) {
            addPlatform(platform);
        }
    }

    public boolean canSendOrder() {
        return allowedToSendOrders;
    }

    public synchronized void addPlatform(AllowedPlatformForClient newPlatform) {
        newPlatform.setClient(this);
        platforms.put(newPlatform.getPlatform().toUpperCase(), newPlatform);
    }

    public boolean canSendOrderTo(String platform) {
        if (platform == null) {
            return false;
        }
        
        AllowedPlatformForClient clientPlatform = platforms.get(platform.toUpperCase());
        
        return clientPlatform != null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);

        sb.append("id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", canSendOrder=").append(allowedToSendOrders);

        return sb.toString();
    }

}
