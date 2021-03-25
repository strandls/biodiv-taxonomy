/**
 * 
 */
package com.strandls.taxonomy.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.taxonomy.dao.AcceptedSynonymDao;
import com.strandls.taxonomy.dao.CommonNamesDao;
import com.strandls.taxonomy.dao.SpeciesGroupDao;
import com.strandls.taxonomy.dao.SpeciesGroupMappingDao;
import com.strandls.taxonomy.dao.SpeciesPermissionDao;
import com.strandls.taxonomy.dao.TaxonomyDefinitionDao;
import com.strandls.taxonomy.dao.TaxonomyRegistryDao;
import com.strandls.taxonomy.pojo.AcceptedSynonym;
import com.strandls.taxonomy.pojo.BreadCrumb;
import com.strandls.taxonomy.pojo.CommonNames;
import com.strandls.taxonomy.pojo.CommonNamesData;
import com.strandls.taxonomy.pojo.SpeciesGroup;
import com.strandls.taxonomy.pojo.SpeciesGroupMapping;
import com.strandls.taxonomy.pojo.SpeciesPermission;
import com.strandls.taxonomy.pojo.TaxonTree;
import com.strandls.taxonomy.pojo.TaxonomicNames;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;
import com.strandls.taxonomy.pojo.TaxonomyRegistry;
import com.strandls.taxonomy.service.TaxonomySerivce;
import com.strandls.utility.controller.UtilityServiceApi;
import com.strandls.utility.pojo.Language;

/**
 * @author Abhishek Rudra
 *
 */
public class TaxonomyServiceImpl implements TaxonomySerivce {

	@Inject
	private TaxonomyDefinitionDao taxonomyDao;

	@Inject
	private TaxonomyRegistryDao taxonomyRegistryDao;

	@Inject
	private SpeciesGroupMappingDao speciesMappingDao;

	@Inject
	private SpeciesGroupDao speciesGroupDao;

	@Inject
	private AcceptedSynonymDao acceptedSynonymDao;

	@Inject
	private SpeciesPermissionDao speciesPermissionDao;

	@Inject
	private CommonNamesDao commonNamesDao;

	@Inject
	private UtilityServiceApi utilityService;

	private final Logger logger = LoggerFactory.getLogger(TaxonomyServiceImpl.class);

	@Override
	public TaxonomyDefinition fetchById(Long id) {
		TaxonomyDefinition taxonomy = taxonomyDao.findById(id);
		return taxonomy;
	}

	@Override
	public List<BreadCrumb> fetchByTaxonomyId(Long id) {
		TaxonomyRegistry taxoRegistry = taxonomyRegistryDao.findbyTaxonomyId(id);
		if (taxoRegistry == null) {
			AcceptedSynonym acceptedSynonym = acceptedSynonymDao.findAccpetedId(id);
			taxoRegistry = taxonomyRegistryDao.findbyTaxonomyId(acceptedSynonym.getAcceptedId());
		}

		String paths = taxoRegistry.getPath().replace("_", ",");
		List<BreadCrumb> breadCrumbs = new ArrayList<BreadCrumb>();
		List<TaxonomyDefinition> breadCrumbLists = taxonomyDao.breadCrumbSearch(paths);
		for (TaxonomyDefinition td : breadCrumbLists) {
			BreadCrumb breadCrumb = new BreadCrumb(td.getId(), td.getNormalizedForm());
			breadCrumbs.add(breadCrumb);
		}

		return breadCrumbs;
	}

	@Override
	public List<String> fetchBySpeciesId(Long id, List<String> taxonList) {
		List<SpeciesGroupMapping> traitList = speciesMappingDao.getTaxonomyId(id);
		for (SpeciesGroupMapping speciesGroup : traitList) {
			if (speciesGroup.getTaxonConceptId() != null)
				taxonList.add(speciesGroup.getTaxonConceptId().toString());
		}
		List<String> allTaxonomyList = new ArrayList<String>();
		for (String taxonId : taxonList) {
			TaxonomyRegistry taxoRegistry = taxonomyRegistryDao.findbyTaxonomyId(Long.parseLong(taxonId));
			String path[] = taxoRegistry.getPath().split("_");
			for (int i = 0; i < path.length; i++) {
				for (SpeciesGroupMapping speciesGroup : traitList) {
					if (speciesGroup.getTaxonConceptId() != null
							&& speciesGroup.getTaxonConceptId().toString().equals(path[i])) {
						i = 0;
						while (i < path.length) {
							allTaxonomyList.add(path[i]);
							i++;
						}
						break;
					}
				}
			}
		}
		return allTaxonomyList;
	}

	@Override
	public List<SpeciesGroup> findAllSpecies() {
		List<SpeciesGroup> result = speciesGroupDao.findAllOrdered();
		return result;
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
	public SpeciesGroup fetchBySpeciesGroupName(String speciesName) {
		SpeciesGroup group = speciesGroupDao.findBySpeciesGroupName(speciesName);
		return group;
	}

	@Override
	public List<SpeciesPermission> getSpeciesPermissions(Long userId) {
		List<SpeciesPermission> allowedTaxonList = speciesPermissionDao.findByUserId(userId);
		return allowedTaxonList;
	}

	@Override
	public TaxonomicNames findSynonymCommonName(Long taxonId) {

		try {
			List<CommonNames> commonNames = commonNamesDao.findByTaxonId(taxonId);

			for (CommonNames commonName : commonNames) {
				if (commonName.getLanguageId() != null) {
					Language language = utilityService.fetchLanguageById(commonName.getLanguageId().toString());
					commonName.setLanguage(language);
				}
			}

			List<AcceptedSynonym> acceptedSynonymsList = acceptedSynonymDao.findByAccepetdId(taxonId);
			List<TaxonomyDefinition> synonymList = new ArrayList<TaxonomyDefinition>();
			if (acceptedSynonymsList != null && !acceptedSynonymsList.isEmpty()) {
				for (AcceptedSynonym synonym : acceptedSynonymsList) {
					TaxonomyDefinition taxonomy = taxonomyDao.findById(synonym.getSynonymId());
					synonymList.add(taxonomy);
				}
			}

			TaxonomicNames result = new TaxonomicNames(commonNames, synonymList);
			return result;

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;

	}

	@Override
	public List<CommonNames> updateAddCommonName(HttpServletRequest request, CommonNamesData commonNamesData) {

		CommonProfile profile = AuthUtil.getProfileFromRequest(request);
		Long uploaderId = Long.parseLong(profile.getId());
		if (commonNamesData.getName() == null || commonNamesData.getTaxonConceptId() == null)
			return null;
		if (commonNamesData.getId() == null) {
			CommonNames commonNames = new CommonNames(null, commonNamesData.getLanguageId(), commonNamesData.getName(),
					commonNamesData.getTaxonConceptId(), new Date(), uploaderId, null, "COMMON", "RAW", null, null,
					null, null, null, commonNamesData.getName().toLowerCase(), null, false);
			commonNamesDao.save(commonNames);
		} else {
			CommonNames commonName = commonNamesDao.findById(commonNamesData.getId());
			if (commonName.getTaxonConceptId().equals(commonNamesData.getTaxonConceptId()))
				return null;
			commonName.setName(commonNamesData.getName());
			commonName.setLowercaseName(commonNamesData.getName().toLowerCase());
			if (commonNamesData.getLanguageId() != null)
				commonName.setLanguageId(commonNamesData.getLanguageId());

			commonNamesDao.update(commonName);
		}
		List<CommonNames> result = commonNamesDao.findByTaxonId(commonNamesData.getTaxonConceptId());
		return result;
	}

	@Override
	public Boolean removeCommonName(HttpServletRequest request, Long commonNameId) {
		CommonNames commonName = commonNamesDao.findById(commonNameId);
		commonName = commonNamesDao.delete(commonName);
		if (commonName != null)
			return true;
		return false;
	}

}
