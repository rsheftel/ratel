package com.malbec.tsdb.ivydb;

import com.malbec.tsdb.loader.*;

public class IvyDbLoader {
	public static void main(String[] args) {
		Loader.usage(args);
		new SecurityPriceLoader(args[0]).run(args, "ivydb");
		new StdOptionPriceLoader(args[0]).run(args, "ivydb");
		new OptionVolumeLoader(args[0]).run(args, "ivydb");
	}
}
