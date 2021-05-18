package com.strandls.taxonomy.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.strandls.taxonomy.dao.AcceptedSynonymDao;
import com.strandls.taxonomy.dao.TaxonomyDefinitionDao;
import com.strandls.taxonomy.dao.TaxonomyRegistryDao;
import com.strandls.taxonomy.pojo.AcceptedSynonym;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;
import com.strandls.taxonomy.pojo.TaxonomyRegistry;
import com.strandls.taxonomy.pojo.response.BreadCrumb;
import com.strandls.taxonomy.pojo.response.TaxonRelation;
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
			List<AcceptedSynonym> acceptedSynonyms = acceptedSynonymDao.findBySynonymId(id);
			if (acceptedSynonyms != null && !acceptedSynonyms.isEmpty())
				taxoRegistry = taxonomyRegistryDao.findbyTaxonomyId(acceptedSynonyms.get(0).getAcceptedId());
		}

		String paths = taxoRegistry.getPath().replace(".", ",");
		List<BreadCrumb> breadCrumbs = new ArrayList<BreadCrumb>();
		List<TaxonomyDefinition> breadCrumbLists = taxonomyDefinitionDao.breadCrumbSearch(paths);
		for (TaxonomyDefinition td : breadCrumbLists) {
			BreadCrumb breadCrumb = new BreadCrumb(td.getId(), td.getNormalizedForm(), td.getRank());
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

	@Override
	public List<TaxonRelation> list(Long parent, String taxonIds, boolean expandTaxon) {
		List<Long> taxonID;
		if (taxonIds == null || "".equals(taxonIds.trim())) {
			taxonID = null;
		} else {
			taxonID = new ArrayList<Long>();
			for (String taxon : taxonIds.split(","))
				taxonID.add(Long.parseLong(taxon));
		}

		try {
			List<TaxonRelation> inputItems = taxonomyRegistryDao.list(parent, taxonID, expandTaxon);

			if (expandTaxon) {
				if (taxonIds != null) {
					List<TaxonRelation> outputItems = buildHierarchy(inputItems);
					return outputItems;
				}
			}
			return inputItems;

		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 
	 * @param items dummy
	 * @return dummy
	 */
	private List<TaxonRelation> buildHierarchy(List<TaxonRelation> items) {
	
		Map<Long, TaxonRelation> idItemMap = new HashMap<Long, TaxonRelation>();
		for (TaxonRelation item : items) {
			idItemMap.put(item.getId(), item);
		}

		List<TaxonRelation> result = new ArrayList<TaxonRelation>();
		for (TaxonRelation item : items) {
			Long parentId = item.getParent();

			if (parentId == null || !idItemMap.containsKey(parentId)) {
				result.add(item);
			} else {
				TaxonRelation relation = idItemMap.get(parentId);
				relation.addChild(item);
			}
		}
		return result;
	}
}
