package com.strandls.taxonomy.pojo.response;

import java.util.List;
import java.util.Map;

import com.strandls.taxonomy.pojo.TaxonomyDefinition;

public class TaxonomySearch {

	private List<TaxonomyDefinition> matched;
	private Map<Long, List<TaxonomyRegistryResponse>> partiallyMatchedRegistry;

	public TaxonomySearch() {
		super();
	}

	public TaxonomySearch(List<TaxonomyDefinition> matched,
			Map<Long, List<TaxonomyRegistryResponse>> partiallyMatchedRegistry) {
		super();
		this.matched = matched;
		this.partiallyMatchedRegistry = partiallyMatchedRegistry;
	}

	public List<TaxonomyDefinition> getMatched() {
		return matched;
	}

	public void setMatched(List<TaxonomyDefinition> matched) {
		this.matched = matched;
	}

	public Map<Long, List<TaxonomyRegistryResponse>> getPartiallyMatchedRegistry() {
		return partiallyMatchedRegistry;
	}

	public void setPartiallyMatchedRegistry(Map<Long, List<TaxonomyRegistryResponse>> partiallyMatchedRegistry) {
		this.partiallyMatchedRegistry = partiallyMatchedRegistry;
	}

}
