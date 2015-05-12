/**
 * 
 */
package malbec.fer.mapping;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TRADING_STRATEGY")
class PlatformStrategyAccount {

    public PlatformStrategyAccount() {}

    public PlatformStrategyAccount(String platform, String strategy, String account, String accountType) {
        this.platform = platform;
        this.strategy = strategy;
        this.account = account;
        this.accountType = accountType;
    }

    @Id
    @Column(name = "ID")
    Long id;

    @Column(name = "PLATFORM_ID")
    String platform;

    @Column(name = "BloombergStrategy")
    String strategy;

    @Column(name = "TagValue")
    String account;

    @Column(name = "AccountType")
    String accountType;

    
    public String getPlatform() {
        return platform;
    }
 
    public String getStrategy() {
        return strategy;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getAccount() {
        return account;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        
        sb.append("id=").append(id);
        sb.append(", platform=").append(platform);
        sb.append(", strategy=").append(strategy);
        sb.append(", account=").append(account);
        sb.append(", accountType=").append(accountType);
        
        return sb.toString();
    }

}