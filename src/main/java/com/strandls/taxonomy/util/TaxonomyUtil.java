package com.strandls.taxonomy.util;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.pac4j.core.profile.CommonProfile;

import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.taxonomy.pojo.Rank;

import net.minidev.json.JSONArray;

public class TaxonomyUtil {
	
	private TaxonomyUtil() {
		
	}

	public static Double getHighestInputRank(List<Rank> ranks, Map<String, String> inputRanks) {
		Double highestRank = -1.0;
		for (Rank rank : ranks)
			if (inputRanks.containsKey(rank.getName()))
				highestRank = highestRank < rank.getRankValue() ? rank.getRankValue() : highestRank;

		return highestRank;
	}
	
	public static boolean isAdmin(HttpServletRequest request) {
		if(request == null) return false;
		
		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		if(profile == null) return false;
		
		JSONArray roles = (JSONArray) profile.getAttribute("roles");
		if (roles.contains("ROLE_ADMIN") )
			return true;
		
		return false;
	}
}
