package malbec.fer.mapping;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "AllowedPlatformsForClient")
public class AllowedPlatformForClient {
    
    @Id
    @Column(name = "ID")
    Long id;
    
    
    @Column(name = "PlatformID")
    String platformID;
    
    @ManyToOne(targetEntity = AllowedClient.class)
    @JoinColumn(name = "AllowedClientID", nullable = false)
    private AllowedClient client;

    public AllowedPlatformForClient() {}

    public void setClient(AllowedClient client) {
        this.client = client;
    }
    
    public AllowedClient getClient() {
        return client;
    }

    public String getPlatform() {
        return platformID;
    }

    public AllowedPlatformForClient(String platformID, AllowedClient client) {
        super();
        this.platformID = platformID;
        this.client = client;
    }

}
