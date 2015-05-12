package com.fftw.bfa.admin.seam.action;

import com.fftw.bfa.admin.seam.model.Platform;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.List;
import java.util.Arrays;

@Name("platformList")
public class PlatformList extends EntityQuery {

	private static final String[] RESTRICTIONS = {
			"lower(platform.platformId) like concat(lower(#{platformList.platform.platformId}),'%')",
			"lower(platform.description) like concat(lower(#{platformList.platform.description}),'%')",};

	private Platform platform = new Platform();

	@Override
	public String getEjbql() {
		return "select platform from Platform platform";
	}

	@Override
	public Integer getMaxResults() {
		return 25;
	}

	public Platform getPlatform() {
		return platform;
	}

	@Override
	public List<String> getRestrictions() {
		return Arrays.asList(RESTRICTIONS);
	}

}
