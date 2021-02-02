package com.strandls.taxonomy.service;

import java.util.List;

import com.strandls.taxonomy.pojo.response.BreadCrumb;
import com.strandls.taxonomy.pojo.response.TaxonTree;

public interface TaxonomyRegistryService {
	
	public List<BreadCrumb> fetchByTaxonomyId(Long id);
	
	public List<TaxonTree> fetchTaxonTrees(List<Long> taxonList);

	public List<BreadCrumb> getImmediateChildsForTaxon(String nodePath);

}
