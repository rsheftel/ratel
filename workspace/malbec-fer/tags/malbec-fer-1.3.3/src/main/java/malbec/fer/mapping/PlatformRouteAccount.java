package malbec.fer.mapping;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "RouteAccountMapping")
class PlatformRouteAccount {

    public PlatformRouteAccount() {
        
    }

    public PlatformRouteAccount(String platform, String route, String account) {
        this.platform = platform;
        this.route = route;
        this.account = account;
    }

    @Id
    @Column(name = "ID")
    Long id;

    @Column(name = "PlatformId")
    String platform;

    @Column(name = "Route")
    String route;

    @Column(name = "Account")
    String account;
}
