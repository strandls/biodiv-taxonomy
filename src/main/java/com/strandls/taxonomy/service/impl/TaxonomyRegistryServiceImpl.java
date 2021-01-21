package com.strandls.taxonomy.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.strandls.taxonomy.dao.AcceptedSynonymDao;
import com.strandls.taxonomy.dao.TaxonomyDefinitionDao;
import com.strandls.taxonomy.dao.TaxonomyRegistryDao;
import com.strandls.taxonomy.pojo.AcceptedSynonym;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;
import com.strandls.taxonomy.pojo.TaxonomyRegistry;
import com.strandls.taxonomy.pojo.response.BreadCrumb;
import com.strandls.taxonomy.pojo.response.TaxonTree;
import com.strandls.taxonomy.service.TaxonomyRegistryService;
import com.strandls.taxonomy.util.AbstractService;

public class TaxonomyRegistryServiceImpl extends AbstractService<TaxonomyRegistry> implements TaxonomyRegistryService {

	@Inject
	private TaxonomyRegistryDao taxonomyRegistryDao;
	
	@Inject
	private AcceptedSynonymDao acceptedSynonymDao;
	
	@Inject
	private TaxonomyDefinitionDao taxonomyDefinitionDao;
	
	@Inject
	public TaxonomyRegistryServiceImpl(TaxonomyRegistryDao dao) {
		super(dao);
	}

	@Override
	public List<BreadCrumb> fetchByTaxonomyId(Long id) {
		TaxonomyRegistry taxoRegistry = taxonomyRegistryDao.findbyTaxonomyId(id);
		if (taxoRegistry == null) {
			AcceptedSynonym acceptedSynonym = acceptedSynonymDao.findAccpetedId(id);
			taxoRegistry = taxonomyRegistryDao.findbyTaxonomyId(acceptedSynonym.getAcceptedId());
		}

		String paths = taxoRegistry.getPath().replace(".", ",");
		List<BreadCrumb> breadCrumbs = new ArrayList<BreadCrumb>();
		List<TaxonomyDefinition> breadCrumbLists = taxonomyDefinitionDao.breadCrumbSearch(paths);
		for (TaxonomyDefinition td : breadCrumbLists) {
			BreadCrumb breadCrumb = new BreadCrumb(td.getId(), td.getNormalizedForm());
			breadCrumbs.add(breadCrumb);
		}

		return breadCrumbs;
	}

	@Override
	public List<TaxonTree> fetchTaxonTrees(List<Long> taxonList) {
		List<TaxonTree> taxonTree = new ArrayList<TaxonTree>();

		for (Long taxon : taxonList) {
			List<Long> taxonPath = new ArrayList<Long>();
			List<BreadCrumb> breadCrumbs = fetchByTaxonomyId(taxon);
			for (BreadCrumb breadCrumb : breadCrumbs) {
				taxonPath.add(breadCrumb.getId());
			}
			taxonTree.add(new TaxonTree(taxon, taxonPath));
		}

		return taxonTree;
	}

}
