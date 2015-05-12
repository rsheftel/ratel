package malbec.fer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

/**
 * Represent a Spread Trade.
 * 
 * There are two legs to a spread trade, leg 1 and leg 2.
 * 
 * This is based on working with Redi Plus and thus might be biased towards
 * their implementation and concepts.
 * 
 * 
 * Note: Orders may have 'ENTRYUSERID' of fp00* when released from the spread
 * trader
 * 
 * The generated orderId on the execution report is made up of these RediPlus
 * fields Branch+sequence number|OmsRefCorrId|OmsRefLineId|OmsRefLineSeq - the
 * line message line type is 'OrderEntry'
 * 
 */
@Entity
@Table(name = "SpreadTrade")
public class SpreadTrade
{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Id")
    private Long id; // DB Id

    @Column(name = "PairId")
    private String pairId;

    @Column(name = "Platform")
    private String platform;

    @Column(name = "SpreadCalculation")
    private String spreadCalculation;

    @Column(name = "HedgeType")
    private String hedgeType = "Quantity"; // default to Quantity

    /*
     * Calculated based on the number of shares sent for each leg
     */
    @Column(name = "HedgeRatio")
    private BigDecimal hedgeRatio; // multiply leg1 shares to get shares for
                                   // leg2

    @Column(name = "BalanceOrderType")
    private String balanceOrderType = "Market"; // probably market

    @Column(name = "BundleBalanceOrders")
    private boolean bundleBalanceOrders = true;

    // composite objects
    @Embedded
    @AttributeOverrides(
    {
        @AttributeOverride(name = "spreadRatio", column = @Column(name = "Leg1SpreadRatio")),
        @AttributeOverride(name = "roundLots", column = @Column(name = "Leg1RoundLots")),
        @AttributeOverride(name = "manageOddLots", column = @Column(name = "Leg1ManageOddLots")),
        @AttributeOverride(name = "symbol", column = @Column(name = "Leg1Symbol")),
        @AttributeOverride(name = "side", column = @Column(name = "Leg1Side")),
        @AttributeOverride(name = "securityType", column = @Column(name = "Leg1SecurityType")),
        @AttributeOverride(name = "strategy", column = @Column(name = "Leg1Strategy")),
        @AttributeOverride(name = "account", column = @Column(name = "Leg1Account")),
        @AttributeOverride(name = "destination", column = @Column(name = "Leg1Destination")),
        @AttributeOverride(name = "initiate", column = @Column(name = "Leg1Initiate")),
        @AttributeOverride(name = "quantity", column = @Column(name = "Leg1Quantity"))
    })
    private TradeLeg leg1;

    @Embedded
    @AttributeOverrides(
    {
        @AttributeOverride(name = "spreadRatio", column = @Column(name = "Leg2SpreadRatio")),
        @AttributeOverride(name = "roundLots", column = @Column(name = "Leg2RoundLots")),
        @AttributeOverride(name = "manageOddLots", column = @Column(name = "Leg2ManageOddLots")),
        @AttributeOverride(name = "symbol", column = @Column(name = "Leg2Symbol")),
        @AttributeOverride(name = "side", column = @Column(name = "Leg2Side")),
        @AttributeOverride(name = "securityType", column = @Column(name = "Leg2SecurityType")),
        @AttributeOverride(name = "strategy", column = @Column(name = "Leg2Strategy")),
        @AttributeOverride(name = "account", column = @Column(name = "Leg2Account")),
        @AttributeOverride(name = "destination", column = @Column(name = "Leg2Destination")),
        @AttributeOverride(name = "initiate", column = @Column(name = "Leg2Initiate")),
        @AttributeOverride(name = "quantity", column = @Column(name = "Leg2Quantity"))
    })
    private TradeLeg leg2;

    @Embedded
    @AttributeOverrides(
    {
        @AttributeOverride(name = "spreadTarget", column = @Column(name = "SetupSpreadTarget")),
        @AttributeOverride(name = "outsideLimit", column = @Column(name = "SetupOutsideLimit")),
        @AttributeOverride(name = "positionObjective", column = @Column(name = "SetupPositionObjective")),
        @AttributeOverride(name = "initiateQuantity", column = @Column(name = "SetupInitiateQuantity"))
    })
    private TradeTrigger setupTrigger;

    @Embedded
    @AttributeOverrides(
    {
        @AttributeOverride(name = "spreadTarget", column = @Column(name = "UnwindSpreadTarget")),
        @AttributeOverride(name = "outsideLimit", column = @Column(name = "UnwindOutsideLimit")),
        @AttributeOverride(name = "positionObjective", column = @Column(name = "UnwindPositionObjective")),
        @AttributeOverride(name = "initiateQuantity", column = @Column(name = "UnwindInitiateQuantity"))
    })
    private TradeTrigger unwindTrigger;

    // these are for security
    @Column(name = "ClientHostname")
    private String clientHostname;

    @Column(name = "ClientUserID")
    private String clientUserID;

    @Column(name = "ClientAppName")
    private String clientAppName;

    @Column(updatable = false, insertable = false, name = "CreatedAt")
    @Generated(GenerationTime.INSERT)
    private Date createdAt;

    @Column(updatable = false, insertable = false, name = "UpdatedAt")
    @Generated(GenerationTime.ALWAYS)
    private Date updatedAt;

    public SpreadTrade ()
    {
    }

    public Long getId() {
        return id;
    }
    
    void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the pairId
     */
    public String getPairId ()
    {
        return pairId;
    }

    /**
     * @param pairId
     *            the pairId to set
     */
    public void setPairId (String pairId)
    {
        this.pairId = pairId.toUpperCase();
    }

    /**
     * @return the spreadCalculation
     */
    public String getSpreadCalculation ()
    {
        return spreadCalculation;
    }

    /**
     * @param spreadCalculation
     *            the spreadCalculation to set
     */
    public void setSpreadCalculation (String spreadCalculation)
    {
        this.spreadCalculation = spreadCalculation;
    }

    /**
     * @return the hedgeType
     */
    public String getHedgeType ()
    {
        return hedgeType;
    }

    /**
     * @param hedgeType
     *            the hedgeType to set
     */
    public void setHedgeType (String hedgeType)
    {
        this.hedgeType = hedgeType;
    }

    /**
     * @return the balanceOrderType
     */
    public String getBalanceOrderType ()
    {
        return balanceOrderType;
    }

    /**
     * @param balanceOrderType
     *            the balanceOrderType to set
     */
    public void setBalanceOrderType (String balanceOrderType)
    {
        this.balanceOrderType = balanceOrderType;
    }

    /**
     * @return the bundleBalanceOrders
     */
    public boolean isBundleBalanceOrders ()
    {
        return bundleBalanceOrders;
    }

    /**
     * @param bundleBalanceOrders
     *            the bundleBalanceOrders to set
     */
    public void setBundleBalanceOrders (boolean bundleBalanceOrders)
    {
        this.bundleBalanceOrders = bundleBalanceOrders;
    }

    /**
     * @return the leg1
     */
    public TradeLeg getLeg1 ()
    {
        return leg1;
    }

    /**
     * @param leg1
     *            the leg1 to set
     */
    public void setLeg1 (TradeLeg leg1)
    {
        this.leg1 = leg1;
    }

    /**
     * @return the leg2
     */
    public TradeLeg getLeg2 ()
    {
        return leg2;
    }

    /**
     * @return the clientHostname
     */
    public String getClientHostname ()
    {
        return clientHostname;
    }

    /**
     * @param clientHostname
     *            the clientHostname to set
     */
    public void setClientHostname (String clientHostname)
    {
        this.clientHostname = clientHostname;
    }

    /**
     * @return the clientUserID
     */
    public String getClientUserID ()
    {
        return clientUserID;
    }

    /**
     * @param clientUserID
     *            the clientUserID to set
     */
    public void setClientUserID (String clientUserID)
    {
        this.clientUserID = clientUserID;
    }

    /**
     * @return the clientAppName
     */
    public String getClientAppName ()
    {
        return clientAppName;
    }

    /**
     * @param clientAppName
     *            the clientAppName to set
     */
    public void setClientAppName (String clientAppName)
    {
        this.clientAppName = clientAppName;
    }

    /**
     * @param leg2
     *            the leg2 to set
     */
    public void setLeg2 (TradeLeg leg2)
    {
        this.leg2 = leg2;
    }

    /**
     * @return the setupTrigger
     */
    public TradeTrigger getSetupTrigger ()
    {
        return setupTrigger;
    }

    /**
     * @param setupTrigger
     *            the setupTrigger to set
     */
    public void setSetupTrigger (TradeTrigger setupTrigger)
    {
        this.setupTrigger = setupTrigger;
    }

    /**
     * @return the unwindTrigger
     */
    public TradeTrigger getUnwindTrigger ()
    {
        return unwindTrigger;
    }

    /**
     * @param unwindTrigger
     *            the unwindTrigger to set
     */
    public void setUnwindTrigger (TradeTrigger unwindTrigger)
    {
        this.unwindTrigger = unwindTrigger;
    }

    /**
     * @return the createdAt
     */
    public Date getCreatedAt ()
    {
        return createdAt;
    }

    /**
     * @param createdAt
     *            the createdAt to set
     */
    public void setCreatedAt (Date createdAt)
    {
        this.createdAt = createdAt;
    }

    /**
     * @return the updatedAt
     */
    public Date getUpdatedAt ()
    {
        return updatedAt;
    }

    /**
     * @param updatedAt
     *            the updatedAt to set
     */
    public void setUpdatedAt (Date updatedAt)
    {
        this.updatedAt = updatedAt;
    }

    /**
     * @param hedgeRatio
     *            the hedgeRatio to set
     */
    public void setHedgeRatio (BigDecimal hedgeRatio)
    {
        this.hedgeRatio = hedgeRatio;
    }

    public SpreadTrade (String pairId, String calculation)
    {
        this.pairId = pairId.toUpperCase();
        this.spreadCalculation = calculation;
    }

    public SpreadTrade (Map<String, String> asMap)
    {
        populateFromMap(this, asMap);
    }

    private static void populateFromMap (SpreadTrade st, Map<String, String> asMap)
    {

        // we must have a leg1 and leg2
        st.leg1 = new TradeLeg();
        st.leg2 = new TradeLeg();

        for (Map.Entry<String, String> entry : asMap.entrySet())
        {
            // extract the current field
            String uppercaseKey = entry.getKey().toUpperCase();

            if (uppercaseKey.equals("PAIRID"))
            {
                st.pairId = entry.getValue().toUpperCase();
                continue;
            }
            if (uppercaseKey.equals("PLATFORM"))
            {
                st.platform = entry.getValue().toUpperCase();
                continue;
            }
            if (uppercaseKey.equals("SPREADCALCULATION"))
            {
                st.spreadCalculation = entry.getValue().toUpperCase();
                continue;
            }
            if (uppercaseKey.equals("HEDGETYPE"))
            {
                st.hedgeType = entry.getValue().toUpperCase();
                continue;
            }
            if (uppercaseKey.equals("HEDGERATIO"))
            {
                st.hedgeRatio = new BigDecimal(entry.getValue());
                continue;
            }
            if (uppercaseKey.equals("BALANCEORDERTYPE"))
            {
                st.balanceOrderType = entry.getValue();
                continue;
            }
            if (uppercaseKey.equals("BUNDLEBALANCEORDERS"))
            {
                st.setBundleBalanceOrders(Boolean.valueOf(entry.getValue()));
                continue;
            }

            // check the composite objects
            if (addLegFromMap(st.leg1, 1, entry))
            {
                continue;
            }
            if (addLegFromMap(st.leg2, 2, entry))
            {
                continue;
            }
            if (addTriggerFromMap(st.setupTrigger, "SETUP", entry))
            {
                continue;
            }
            if (addTriggerFromMap(st.unwindTrigger, "UNWIND", entry))
            {
                continue;
            }
        }
    }

    @Embeddable
    public static class TradeLeg
    {
        private BigDecimal spreadRatio = BigDecimal.ONE; // used in calculating
                                                         // spread target by the
                                                         // Spread

        // Calculation
        private boolean roundLots = true;

        private boolean manageOddLots = true;

        private String symbol;

        private String side;

        private String securityType;

        private String strategy;

        private String account; // based on security type and strategy

        private String destination;

        private boolean initiate;

        private BigDecimal quantity;

        public TradeLeg ()
        {
        }

        /**
         * @return the spreadRatio
         */
        public BigDecimal getSpreadRatio ()
        {
            return spreadRatio;
        }

        /**
         * @param spreadRatio
         *            the spreadRatio to set
         */
        public void setSpreadRatio (BigDecimal spreadRatio)
        {
            this.spreadRatio = spreadRatio;
        }

        /**
         * @return the roundLots
         */
        public boolean isRoundLots ()
        {
            return roundLots;
        }

        /**
         * @param roundLots
         *            the roundLots to set
         */
        public void setRoundLots (boolean roundLots)
        {
            this.roundLots = roundLots;
        }

        /**
         * @return the manageOddLots
         */
        public boolean isManageOddLots ()
        {
            return manageOddLots;
        }

        /**
         * @param manageOddLots
         *            the manageOddLots to set
         */
        public void setManageOddLots (boolean manageOddLots)
        {
            this.manageOddLots = manageOddLots;
        }

        /**
         * @return the symbol
         */
        public String getSymbol ()
        {
            return symbol;
        }

        /**
         * @param symbol
         *            the symbol to set
         */
        public void setSymbol (String symbol)
        {
            this.symbol = symbol;
        }

        /**
         * @return the side
         */
        public String getSide ()
        {
            return side;
        }

        /**
         * @param side
         *            the side to set
         */
        public void setSide (String side)
        {
            this.side = side;
        }

        /**
         * @return the securityType
         */
        public String getSecurityType ()
        {
            return securityType;
        }

        /**
         * @param securityType
         *            the securityType to set
         */
        public void setSecurityType (String securityType)
        {
            this.securityType = securityType;
        }

        /**
         * @return the strategy
         */
        public String getStrategy ()
        {
            return strategy;
        }

        /**
         * @param strategy
         *            the strategy to set
         */
        public void setStrategy (String strategy)
        {
            this.strategy = strategy;
        }

        /**
         * @return the account
         */
        public String getAccount ()
        {
            return account;
        }

        /**
         * @param account
         *            the account to set
         */
        public void setAccount (String account)
        {
            this.account = account;
        }

        /**
         * @return the destination
         */
        public String getDestination ()
        {
            return destination;
        }

        /**
         * @param destination
         *            the destination to set
         */
        public void setDestination (String destination)
        {
            this.destination = destination;
        }

        /**
         * @return the initiate
         */
        public boolean isInitiate ()
        {
            return initiate;
        }

        /**
         * @param initiate
         *            the initiate to set
         */
        public void setInitiate (boolean initiate)
        {
            this.initiate = initiate;
        }

        /**
         * @return the quantity
         */
        public BigDecimal getQuantity ()
        {
            return quantity;
        }

        /**
         * @param quantity
         *            the quantity to set
         */
        public void setQuantity (BigDecimal quantity)
        {
            this.quantity = quantity;
        }

    }

    @Embeddable
    public static class TradeTrigger
    {
        // Trade initiation
        private BigDecimal spreadTarget;

        private BigDecimal outsideLimit;

        private BigDecimal positionObjective;

        private BigDecimal initiateQuantity;

        public TradeTrigger ()
        {

        }

        public TradeTrigger (BigDecimal spreadTarget, BigDecimal outsideLimit,
            BigDecimal initiateQuantity)
        {
            this();
            this.spreadTarget = spreadTarget;
            this.outsideLimit = outsideLimit;
            this.initiateQuantity = initiateQuantity;
        }

        /**
         * @return the spreadTarget
         */
        public BigDecimal getSpreadTarget ()
        {
            return spreadTarget;
        }

        /**
         * @param spreadTarget
         *            the spreadTarget to set
         */
        public void setSpreadTarget (BigDecimal spreadTarget)
        {
            this.spreadTarget = spreadTarget;
        }

        /**
         * @return the outsideLimit
         */
        public BigDecimal getOutsideLimit ()
        {
            return outsideLimit;
        }

        /**
         * @param outsideLimit
         *            the outsideLimit to set
         */
        public void setOutsideLimit (BigDecimal outsideLimit)
        {
            this.outsideLimit = outsideLimit;
        }

        /**
         * @return the positionObjective
         */
        public BigDecimal getPositionObjective ()
        {
            return positionObjective;
        }

        /**
         * @param positionObjective
         *            the positionObjective to set
         */
        public void setPositionObjective (BigDecimal positionObjective)
        {
            this.positionObjective = positionObjective;
        }

        /**
         * @return the initiateQuantity
         */
        public BigDecimal getInitiateQuantity ()
        {
            return initiateQuantity;
        }

        /**
         * @param initiateQuantity
         *            the initiateQuantity to set
         */
        public void setInitiateQuantity (BigDecimal initiateQuantity)
        {
            this.initiateQuantity = initiateQuantity;
        }

    }

    private TradeLeg createLeg (String side, String symbol, String securityType, String strategy,
        String destination, BigDecimal quantity)
    {
        TradeLeg leg = new TradeLeg();
        leg.side = side.toUpperCase();
        leg.symbol = symbol.toUpperCase();
        leg.securityType = securityType.toUpperCase();
        leg.strategy = strategy;
        leg.quantity = quantity;
        leg.destination = destination;

        return leg;
    }

    public void addLeg1 (String side, String symbol, String securityType, String strategy,
        String destination, int quantity)
    {
        addLeg1(side, symbol, securityType, strategy, destination, new BigDecimal(quantity));
    }

    public void addLeg1 (String side, String symbol, String securityType, String strategy,
        String destination, BigDecimal quantity)
    {
        leg1 = createLeg(side, symbol, securityType, strategy, destination, quantity);
        calculateFields();
    }

    public void addLeg2 (String side, String symbol, String securityType, String strategy,
        String destination, int quantity)
    {
        addLeg2(side, symbol, securityType, strategy, destination, new BigDecimal(quantity));
    }

    public void addLeg2 (String side, String symbol, String securityType, String strategy,
        String destination, BigDecimal quantity)
    {
        leg2 = createLeg(side, symbol, securityType, strategy, destination, quantity);

        calculateFields();
    }

    private void calculateFields ()
    {
        if (leg1 != null && leg2 != null)
        {
            hedgeRatio = leg1.quantity.divide(leg2.quantity, 3, RoundingMode.HALF_UP);
        }

        if (leg1 != null && setupTrigger != null)
        {
            setupTrigger.positionObjective = leg1.quantity;
        }
        if (leg1 != null && unwindTrigger != null)
        {
            unwindTrigger.positionObjective = leg1.quantity;
        }
    }

    public void setSetup (float spreadTarget, float outsideLimit, int initiateQuantity)
    {
        setSetup(new BigDecimal(spreadTarget), new BigDecimal(outsideLimit), new BigDecimal(
            initiateQuantity));
    }

    public void setSetup (BigDecimal spreadTarget, BigDecimal outsideLimit,
        BigDecimal initiateQuantity)
    {
        this.setupTrigger = new TradeTrigger(spreadTarget, outsideLimit, initiateQuantity);
        calculateFields();
    }

    public void setUnwind (float spreadTarget, float outsideLimit, int initiateQuantity)
    {
        setUnwind(new BigDecimal(spreadTarget), new BigDecimal(outsideLimit), new BigDecimal(
            initiateQuantity));
    }

    public void setUnwind (BigDecimal spreadTarget, BigDecimal outsideLimit,
        BigDecimal initiateQuantity)
    {
        this.unwindTrigger = new TradeTrigger(spreadTarget, outsideLimit, initiateQuantity);
        calculateFields();
    }

    public boolean isLeg1Set ()
    {
        return leg1 != null;
    }

    public boolean isLeg2Set ()
    {
        return leg2 != null;
    }

    public boolean haveInitiator ()
    {
        // we must have a setup or unwind and one leg set to initiator
        boolean setupUnwing = (setupTrigger != null || unwindTrigger != null);

        if (leg1 != null && leg2 != null)
        {
            return setupUnwing && (leg1.initiate || leg2.initiate);
        }

        return false;
    }

    public BigDecimal getHedgeRatio ()
    {
        return hedgeRatio;
    }

    public BigDecimal getSetupPositionObjective ()
    {
        if (setupTrigger != null)
        {
            return setupTrigger.positionObjective;
        }

        return null;
    }

    public BigDecimal getUnwindPositionObjective ()
    {
        if (unwindTrigger != null)
        {
            return unwindTrigger.positionObjective;
        }

        return null;
    }

    void setLeg1Account (String account)
    {
        leg1.account = account;
    }

    void setLeg2Account (String account)
    {
        leg2.account = account;
    }

    public void setLeg1Initiate (boolean initiate)
    {
        leg1.initiate = initiate;
    }

    public void setLeg2Initiate (boolean initiate)
    {
        leg2.initiate = initiate;
    }

    public Map<String, String> toMap ()
    {
        Map<String, String> asMap = new HashMap<String, String>();

        asMap.put("PAIRID", pairId);
        asMap.put("PLATFORM", platform);
        asMap.put("SPREADCALCULATION", spreadCalculation);
        asMap.put("HEDGETYPE", hedgeType);
        asMap.put("HEDGERATIO", hedgeRatio.toPlainString());
        asMap.put("BALANCEORDERTYPE", balanceOrderType);
        asMap.put("BUNDLEBALANCEORDERS", Boolean.toString(bundleBalanceOrders));
        addLegToMap(asMap, 1, leg1);
        addLegToMap(asMap, 2, leg2);

        if (setupTrigger != null)
        {
            addSetupUnwind(asMap, "SETUP", setupTrigger);
        }

        if (unwindTrigger != null)
        {
            addSetupUnwind(asMap, "UNWIND", unwindTrigger);
        }

        return asMap;
    }

    private void addSetupUnwind (Map<String, String> asMap, String setupUnwind, TradeTrigger trigger)
    {
        asMap.put(buildFieldName(setupUnwind, "SpreadTarget"), trigger.spreadTarget.toPlainString());
        asMap.put(buildFieldName(setupUnwind, "OutsideLimit"), trigger.outsideLimit.toPlainString());
        asMap.put(buildFieldName(setupUnwind, "PositionObjective"), trigger.positionObjective
            .toPlainString());
        asMap.put(buildFieldName(setupUnwind, "InitiateQuantity"), trigger.initiateQuantity
            .toPlainString());
    }

    private static boolean addTriggerFromMap (TradeTrigger trigger, String triggerType,
        Entry<String, String> entry)
    {
        String start = triggerType.toUpperCase();

        String uppercaseKey = entry.getKey().toUpperCase();

        if (uppercaseKey.startsWith(start))
        {
            if (buildFieldName(start, "SpreadTarget").equalsIgnoreCase(uppercaseKey))
            {
                trigger.setSpreadTarget(new BigDecimal(entry.getValue()));
                return true;
            }
            if (buildFieldName(start, "OutsideLimit").equalsIgnoreCase(uppercaseKey))
            {
                trigger.setOutsideLimit(new BigDecimal(entry.getValue()));
                return true;
            }
            if (buildFieldName(start, "PositionObjective").equalsIgnoreCase(uppercaseKey))
            {
                trigger.setPositionObjective(new BigDecimal(entry.getValue()));
                return true;
            }
            if (buildFieldName(start, "InitiateQuantity").equalsIgnoreCase(uppercaseKey))
            {
                trigger.setInitiateQuantity(new BigDecimal(entry.getValue()));
                return true;
            }
        }

        return false;
    }

    private static boolean addLegFromMap (TradeLeg leg, int legNumber,
        Map.Entry<String, String> entry)
    {
        String legStart = "LEG" + legNumber;

        String uppercaseKey = entry.getKey().toUpperCase();

        if (uppercaseKey.startsWith(legStart))
        {
            if (buildLegName(legNumber, "SPREADRATIO").equals(uppercaseKey))
            {
                leg.setSpreadRatio(new BigDecimal(entry.getValue()));
                return true;
            }
            if (buildLegName(legNumber, "ROUNDLOTS").equals(uppercaseKey))
            {
                leg.setRoundLots(Boolean.valueOf(entry.getValue()));
                return true;
            }
            if (buildLegName(legNumber, "MANAGEODDLOTS").equals(uppercaseKey))
            {
                leg.setManageOddLots(Boolean.valueOf(entry.getValue()));
                return true;
            }
            if (buildLegName(legNumber, "SYMBOL").equals(uppercaseKey))
            {
                leg.setSymbol(entry.getValue());
                return true;
            }
            if (buildLegName(legNumber, "SIDE").equals(uppercaseKey))
            {
                leg.setSide(entry.getValue());
                return true;
            }
            if (buildLegName(legNumber, "SECURITYTYPE").equals(uppercaseKey))
            {
                leg.setSecurityType(entry.getValue());
                return true;
            }
            if (buildLegName(legNumber, "STRATEGY").equals(uppercaseKey))
            {
                leg.setStrategy(entry.getValue());
                return true;
            }
            if (buildLegName(legNumber, "ACCOUNT").equals(uppercaseKey))
            {
                leg.setAccount(entry.getValue());
                return true;
            }
            if (buildLegName(legNumber, "DESTINATION").equals(uppercaseKey))
            {
                leg.setDestination(entry.getValue());
                return true;
            }
            if (buildLegName(legNumber, "INITIATE").equals(uppercaseKey))
            {
                leg.setInitiate(Boolean.valueOf(entry.getValue()));
                return true;
            }
            if (buildLegName(legNumber, "QUANTITY").equals(uppercaseKey))
            {
                leg.setQuantity(new BigDecimal(entry.getValue()));
                return true;
            }
        }

        return false;
    }

    private void addLegToMap (Map<String, String> asMap, int legNumber, TradeLeg leg)
    {
        asMap.put(buildLegName(legNumber, "SpreadRatio"), leg.spreadRatio.toPlainString());
        asMap.put(buildLegName(legNumber, "RoundLots"), Boolean.toString(leg.roundLots));
        asMap.put(buildLegName(legNumber, "ManageOddLots"), Boolean.toString(leg.manageOddLots));
        asMap.put(buildLegName(legNumber, "Symbol"), emptyStringIfNull(leg.symbol));
        asMap.put(buildLegName(legNumber, "Side"), emptyStringIfNull(leg.side));
        asMap.put(buildLegName(legNumber, "SecurityType"), leg.securityType);
        asMap.put(buildLegName(legNumber, "Strategy"), emptyStringIfNull(leg.strategy));
        asMap.put(buildLegName(legNumber, "Account"), emptyStringIfNull(leg.account));
        asMap.put(buildLegName(legNumber, "Destination"), emptyStringIfNull(leg.destination));
        asMap.put(buildLegName(legNumber, "Initiate"), Boolean.toString(leg.initiate));
        asMap.put(buildLegName(legNumber, "Quantity"), leg.quantity.toPlainString());
    }

    private String emptyStringIfNull (String str)
    {
        return str == null ? "" : str;
    }

    private static String buildLegName (int leg, String field)
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append("LEG").append(leg).append(field.toUpperCase());

        return sb.toString();
    }

    private static String buildFieldName (String prefix, String field)
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append(prefix.toUpperCase()).append(field.toUpperCase());

        return sb.toString();
    }

    public void setPlatform (String platform)
    {
        this.platform = platform;
    }

    public String getPlatform ()
    {
        return platform;
    }

    /**
     * Set the strategy on both legs
     * 
     * @param strategy
     */
    public void setStrategy (String strategy)
    {
        leg1.strategy = strategy;
        leg2.strategy = strategy;
    }

}
