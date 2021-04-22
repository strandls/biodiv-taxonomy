package com.strandls.taxonomy.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.strandls.taxonomy.dao.AcceptedSynonymDao;
import com.strandls.taxonomy.dao.SpeciesPermissionDao;
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

	private static final String ID = "id";
	private static final String taxonid = "taxonid";
	private static final String text = "text";
	private static final String rank = "rank";
	private static final String path = "path";
	private static final String classification = "classification";
	private static final String totalPath = "totalPath";
	private static final String parent = "parent";
	private static final String position = "position";

	@Inject
	private TaxonomyRegistryDao taxonomyRegistryDao;

	@Inject
	private AcceptedSynonymDao acceptedSynonymDao;

	@Inject
	private TaxonomyDefinitionDao taxonomyDefinitionDao;

	@Inject
	private SpeciesPermissionDao speciesPermissionDao;

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
			List<Object[]> taxonList = taxonomyRegistryDao.list(parent, taxonID, expandTaxon);
			List<String> data = null;

			if (taxonID != null) {
				data = taxonomyRegistryDao.getPathToRoot(taxonID);
			}

			List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();

			for (Object[] t : taxonList) {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put(ID, t[0]);
				m.put(taxonid, t[0]);
				m.put(text, t[1]);
				m.put(rank, t[2]);
				m.put(path, t[3]);
				m.put(classification, t[4]);
				m.put(position, t[6]);
				m.put(totalPath, data);

				if (t[5] != null) {
					m.put("parent", t[5]);
				} else {
					m.put("parent", "0");
				}
				res.add(m);
			}

			List<TaxonRelation> inputItems = createInputItems(res);

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
		List<TaxonRelation> result = new ArrayList<TaxonRelation>();
		Map<Long, TaxonRelation> idItemMap = prepareIdItemMap(items);

		for (TaxonRelation item : items) {
			Long parentId = item.getParent();

			if (parentId == 0) {
				result.add(item);
			} else {
				idItemMap.get(parentId).addChild(item);
			}
		}
		return result;
	}

	/**
	 * 
	 * @param items dummy
	 * @return dummy
	 */
	private Map<Long, TaxonRelation> prepareIdItemMap(List<TaxonRelation> items) {
		HashMap<Long, TaxonRelation> result = new HashMap<>();

		for (TaxonRelation eachItem : items) {
			result.put(Long.valueOf(eachItem.getId()), eachItem);
		}
		return result;

	}

	/**
	 * 
	 * @param res dummy
	 * @return dummy
	 */
	private List<TaxonRelation> createInputItems(List<Map<String, Object>> res) {
		List<TaxonRelation> result = new ArrayList<>();
		for (Map<String, Object> data : res) {
			result.add(new TaxonRelation(Long.parseLong((String) data.get(taxonid)), (String) data.get(path),
					(Long.parseLong((String) data.get(parent))), (String) data.get(text),
					Long.parseLong((String) data.get(classification)), (Long.parseLong((String) data.get(ID))),
					(String) data.get(rank), (String) data.get(position), (List<String>) data.get(totalPath)));
		}
		return result;
	}

}
