/**
 * 
 */
package com.strandls.taxonomy.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.strandls.taxonomy.dao.TaxonomyDefinitionDao;
import com.strandls.taxonomy.dao.TaxonomyRegistryDao;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;
import com.strandls.taxonomy.pojo.TaxonomyRegistry;
import com.strandls.taxonomy.service.TaxonomySerivce;

/**
 * @author Abhishek Rudra
 *
 */
public class TaxonomyServiceImpl implements TaxonomySerivce {

	@Inject
	private TaxonomyDefinitionDao taxonomyDao;

	@Inject
	private TaxonomyRegistryDao taxonomyRegistryDao;

	@Override
	public TaxonomyDefinition fetchById(Long id) {
		TaxonomyDefinition taxonomy = taxonomyDao.findById(id);
		return taxonomy;
	}

	@Override
	public List<String> fetchByTaxonomyId(Long id) {
		TaxonomyRegistry taxoRegistry = taxonomyRegistryDao.findbyTaxonomyId(id);

		String paths = taxoRegistry.getPath().replace("_", ",");
		List<String> breadCrum = new ArrayList<String>();
		List<TaxonomyDefinition> breadCrumLists = taxonomyDao.breadCrumSearch(paths); 
		for(TaxonomyDefinition td:breadCrumLists) {
			breadCrum.add(td.getNormalizedForm());
		}

		return breadCrum;
	}

}
