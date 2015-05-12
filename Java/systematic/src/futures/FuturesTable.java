package futures;

import static util.Errors.*;
import static util.Objects.*;
import static util.Strings.*;
import static db.clause.Clause.*;
import static futures.FuturesOptionTable.*;

import java.util.*;

import db.*;
import db.clause.*;
import db.tables.TSDB.*;

class FuturesTable extends FuturesDetailsBase {
    private static final long serialVersionUID = 1L;
    public static final FuturesTable FUTURES = new FuturesTable();
	
	public FuturesTable() {
		super("fut");
	}

	public ContractCurrent insert(String contract, int numQuarterly, int numMonthly, String exchange, String expiryType, String groupName) {
		insert(
			C_CONTRACT.with(contract),
			C_EXCHANGE.with(exchange),
			C_GROUP_NAME.with(groupName),
			C_BBG_YELLOW_KEY.with("Comdty"),
			C_NUM_QUARTERLY.with(numQuarterly),
			C_NUM_MONTHLY.with(numMonthly),
			C_EXPIRY_TYPE.with(expiryType)
		);
		return new ContractCurrent(contract, "Comdty");
	}

	public int quarters(String contractName) {
		Row row = row(contractMatches(contractName));
		bombUnless(row.value(C_NUM_MONTHLY) == 0, "serial/monthly contracts not implemented yet");
		return row.value(C_NUM_QUARTERLY);
	}

	public List<ContractCurrent> contracts() {
		List<Row> rows = rows();
		List<ContractCurrent> result = empty();
		for (Row row : rows)
			result.add(new ContractCurrent(row.value(C_CONTRACT), row.value(C_BBG_YELLOW_KEY)));
		return result ;
	}

	public BloombergJob job(String name, String prefix, String suffix) {
		Row row = row(contractMatches(name));
		return new BloombergJob(join("_", prefix, row.string(C_EXCHANGE), row.string(C_GROUP_NAME), suffix));
	}

	private Clause contractMatches(String contractName) {
		return C_CONTRACT.is(contractName);
	}

	public int id(String name) {
		return C_ID.value(contractMatches(name));
	}

	public String contract(Integer id) {
		return C_CONTRACT.value(C_ID.is(id));
	}

	@Deprecated
	public void deleteAll() {
		OPTIONS.deleteAll();
		deleteAll(TRUE);
	}

	public String expiry(String name) {
		return C_EXPIRY_TYPE.value(contractMatches(name));
	}
	
}