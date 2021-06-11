package com.strandls.taxonomy.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.strandls.taxonomy.dao.AcceptedSynonymDao;
import com.strandls.taxonomy.dao.RankDao;
import com.strandls.taxonomy.dao.TaxonomyDefinitionDao;
import com.strandls.taxonomy.dao.TaxonomyRegistryDao;
import com.strandls.taxonomy.pojo.AcceptedSynonym;
import com.strandls.taxonomy.pojo.Rank;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;
import com.strandls.taxonomy.pojo.TaxonomyRegistry;
import com.strandls.taxonomy.pojo.enumtype.TaxonomyPosition;
import com.strandls.taxonomy.pojo.enumtype.TaxonomyStatus;
import com.strandls.taxonomy.pojo.response.BreadCrumb;
import com.strandls.taxonomy.pojo.response.TaxonRelation;
import com.strandls.taxonomy.pojo.response.TaxonTree;
import com.strandls.taxonomy.pojo.response.TaxonomyRegistryResponse;
import com.strandls.taxonomy.service.TaxonomyRegistryService;
import com.strandls.taxonomy.util.AbstractService;
import com.strandls.taxonomy.util.TaxonomyUtil;

public class TaxonomyRegistryServiceImpl extends AbstractService<TaxonomyRegistry> implements TaxonomyRegistryService {

	@Inject
	private TaxonomyRegistryDao taxonomyRegistryDao;

	@Inject
	private AcceptedSynonymDao acceptedSynonymDao;

	@Inject
	private TaxonomyDefinitionDao taxonomyDefinitionDao;
	
	@Inject
	private RankDao rankDao;

	@Inject
	public TaxonomyRegistryServiceImpl(TaxonomyRegistryDao dao) {
		super(dao);
	}

	@Override
	public List<BreadCrumb> fetchByTaxonomyId(Long id) {
		TaxonomyRegistry taxoRegistry = taxonomyRegistryDao.findbyTaxonomyId(id, null);
		if (taxoRegistry == null) {
			List<AcceptedSynonym> acceptedSynonyms = acceptedSynonymDao.findBySynonymId(id);
			if (acceptedSynonyms != null && !acceptedSynonyms.isEmpty())
				taxoRegistry = taxonomyRegistryDao.findbyTaxonomyId(acceptedSynonyms.get(0).getAcceptedId(), null);
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
	public List<TaxonRelation> list(Long parent, String taxonIds, boolean expandTaxon, Long classificationId) {
		List<Long> taxonID;
		if (taxonIds == null || "".equals(taxonIds.trim())) {
			taxonID = null;
		} else {
			taxonID = new ArrayList<Long>();
			for (String taxon : taxonIds.split(","))
				taxonID.add(Long.parseLong(taxon));
		}
		List<String> ids = taxonomyRegistryDao.getPathToRoot(taxonID, classificationId);
		try {
			List<TaxonRelation> inputItems = taxonomyRegistryDao.list(parent, taxonID, expandTaxon, classificationId);

			if (expandTaxon) {
				if (taxonIds != null && inputItems != null) {
					List<TaxonRelation> outputItems = buildHierarchy(inputItems, ids);
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
	 * @param ids
	 * @return dummy
	 */
	private List<TaxonRelation> buildHierarchy(List<TaxonRelation> items, List<String> ids) {

		Map<Long, TaxonRelation> idItemMap = new HashMap<Long, TaxonRelation>();
		for (TaxonRelation item : items) {
			item.setIds(ids);
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

	
	/**
	 * Code below from here on is only for the migration purpose
	 */
	@Override
	public Map<String, Object> migrate() throws CloneNotSupportedException {
		String countQueryString = "select id from taxonomy_definition where position = :position and status = :status";
		String queryString = "from TaxonomyDefinition td where td.position = :position and td.status = :status order by id";

		Map<String, Object> response = new HashMap<String, Object>();

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("status", TaxonomyStatus.ACCEPTED.name());

		// migrate the clean name on to the taxonomy tree.
		parameters.put("position", TaxonomyPosition.CLEAN.name());
		Long cleanNameCount = taxonomyDefinitionDao.getRowCount(countQueryString, parameters);
		response.putAll(migrateCleanName(queryString, cleanNameCount, parameters));

		// Snap the Working name to existing hierarchy
		parameters.put("position", TaxonomyPosition.WORKING.name());
		Long nameCount = taxonomyDefinitionDao.getRowCount(countQueryString, parameters);
		snapName(queryString, nameCount, parameters);

		// Snap the Raw name to existing hierarchy
		parameters.put("position", TaxonomyPosition.RAW.name());
		nameCount = taxonomyDefinitionDao.getRowCount(countQueryString, parameters);
		snapName(queryString, nameCount, parameters);

		Long count = taxonomyDefinitionDao.getRowCount();
		response.put("totalCount", count);

		return response;
	}

	private void snapName(String queryString, Long nameCount, Map<String, Object> parameters) {

		System.out.println(nameCount);
	}

	private Map<String, Object> migrateCleanName(String queryString, Long cleanNameCount,
			Map<String, Object> parameters) throws CloneNotSupportedException {

		List<Long> classificationIds = new ArrayList<Long>();
		classificationIds.add(TaxonomyRegistryDao.getDefaultClassificationId());
		Map<String, Object> missingNameInRegistry = new HashMap<String, Object>();
		Map<String, Object> missingNameInDefinition = new HashMap<String, Object>();
		Map<String, Object> pathAndHierarchyMissmatch = new HashMap<String, Object>();
		Map<String, Object> requiredRankMissing = new HashMap<String, Object>();
		
		int missingNameInRegistryCount = 0;
		int missingNameInDefinitionCount = 0;
		int pathAndHierarchyMissmatchCount = 0;
		int requiredRankMissingCount = 0;
		
		List<Rank> ranks = rankDao.getAllRank();
		/*
		 * classificationIds.add(821L); classificationIds.add(819L);
		 * classificationIds.add(818L); classificationIds.add(817L);
		 */

		final int BATCH_SIZE = 1000;

		// migrate in the batch of 1000
		for (int i = 0; i < cleanNameCount; i += BATCH_SIZE) {
			int offset = i;
			int limit = BATCH_SIZE;
			limit = offset + limit > cleanNameCount.intValue() ? cleanNameCount.intValue() : limit;
			List<TaxonomyDefinition> definitions = taxonomyDefinitionDao.getByQueryString(queryString, parameters,
					limit, offset);

			for (TaxonomyDefinition definition : definitions) {

				List<TaxonomyRegistry> taxonomyRegistrys = taxonomyRegistryDao.getOldHierarchy(definition.getId(),
						classificationIds);

				if (taxonomyRegistrys.isEmpty()) {
					if (definition.getIsDeleted())
						continue;
					Map<String, Object> r = new HashMap<String, Object>();
					r.put("status", "Name present but missing in the registry");
					r.put(definition.getId().toString(), definition.getName());
					missingNameInRegistry.put(definition.getId().toString(), r);
					missingNameInRegistryCount++;
					continue;
				} else {
					List<TaxonomyRegistryResponse> generatedHierarchyName = taxonomyRegistryDao.getPathToRootForOldHierarchy(
							definition.getId(), TaxonomyRegistryDao.getDefaultClassificationId());

					String pathToRootIds = "";

					for (TaxonomyRegistryResponse r : generatedHierarchyName)
						pathToRootIds += "." + r.getId();
					pathToRootIds = pathToRootIds.substring(1);

					String path = taxonomyRegistrys.get(0).getPath();
					List<TaxonomyRegistryResponse> actualHierarchyName = taxonomyRegistryDao.getNameFromPath(path);

					// Check for the valid hierarchy - See if all the required ranks are present.
					Set<String> rankNames = new HashSet<String>();
					for(TaxonomyRegistryResponse r : actualHierarchyName)
						rankNames.add(r.getRank());
					
					if (!pathToRootIds.equals(path)) {
						Map<String, Object> r = new HashMap<String, Object>();
						if(definition.getIsDeleted()) {
							r.put("status", "Name deleted but entry in the registry");
							r.put("actualPath", path);
							r.put("generatedPath", pathToRootIds);
							
							r.put("actualHierarchy", actualHierarchyName);
							r.put("generatedHierarchy", generatedHierarchyName);
							missingNameInDefinition.put(definition.getId().toString(), r);
							missingNameInDefinitionCount++;
						}
						else {
							r.put("status", "Missmatch between registry path and generated path");
							r.put("actualPath", path);
							r.put("generatedPath", pathToRootIds);
							
							r.put("actualHierarchy", actualHierarchyName);
							r.put("generatedHierarchy", generatedHierarchyName);
							pathAndHierarchyMissmatch.put(definition.getId().toString(), r);
							pathAndHierarchyMissmatchCount++;
						}
					} 
					if(!TaxonomyUtil.validateHierarchy(ranks, rankNames)) {
						Map<String, Object> r = new HashMap<String, Object>();
						r.put("status", "Required rank missing in the hierarchy");
						r.put("actualPath", path);
						r.put("actualHierarchy", actualHierarchyName);
						requiredRankMissing.put(definition.getId().toString(), r);
						requiredRankMissingCount++;
					}
				}

				TaxonomyRegistry ibpHierarchy = taxonomyRegistrys.get(0).clone();
				save(ibpHierarchy);
			}

		}
		
		missingNameInDefinition.put("missingNameInDefinitionCount", missingNameInDefinitionCount);
		missingNameInRegistry.put("missingNameInRegistryCount", missingNameInRegistryCount);
		pathAndHierarchyMissmatch.put("pathAndHierarchyMissmatchCount", pathAndHierarchyMissmatchCount);
		requiredRankMissing.put("requiredRankMissingCount", requiredRankMissingCount);

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("missingNameInRegistry", missingNameInRegistry);
		response.put("missingNameInDefinition", missingNameInDefinition);
		response.put("pathAndHierarchyMissmatch", pathAndHierarchyMissmatch);
		response.put("requiredRankMissing", requiredRankMissing);

		return response;

	}

}
