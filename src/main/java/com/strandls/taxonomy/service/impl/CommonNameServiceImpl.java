package com.strandls.taxonomy.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.taxonomy.dao.CommonNameDao;
import com.strandls.taxonomy.pojo.CommonName;
import com.strandls.taxonomy.pojo.CommonNamesData;
import com.strandls.taxonomy.service.CommonNameSerivce;
import com.strandls.taxonomy.util.AbstractService;
import com.strandls.utility.controller.LanguageServiceApi;
import com.strandls.utility.pojo.Language;

public class CommonNameServiceImpl extends AbstractService<CommonName> implements CommonNameSerivce {

	@Inject
	private CommonNameDao commonNameDao;

	@Inject
	private LanguageServiceApi languageService;

	@Inject
	public CommonNameServiceImpl(CommonNameDao dao) {
		super(dao);
	}

	private final Logger logger = LoggerFactory.getLogger(CommonNameServiceImpl.class);

	@Override
	public CommonName fetchById(Long id) {
		return commonNameDao.findById(id);
	}

	@Override
	public List<CommonName> getCommonName(Long languageId, Long taxonConceptId, String commonNameString) {
		return commonNameDao.getCommonName(languageId, taxonConceptId, commonNameString);
	}

	@Override
	public CommonName updateIsPreffered(Long id) {
		CommonName commonName = commonNameDao.findById(id);
		Long taxonConceptId = commonName.getTaxonConceptId();
		List<CommonName> commonNames = commonNameDao.getByPropertyWithCondtion("taxonConceptId",
				taxonConceptId.toString(), "=", -1, -1);
		for (CommonName c : commonNames) {
			c.setPreffered(false);
			update(c);
		}
		commonName.setPreffered(true);
		return update(commonName);
	}

	public List<CommonName> addCommonNames(Long taxonConceptId, Map<Long, String[]> languageIdToCommonNames,
			String source) {

		List<CommonName> commonNamesList = new ArrayList<CommonName>();

		for (Map.Entry<Long, String[]> entry : languageIdToCommonNames.entrySet()) {
			Long languageId = entry.getKey();
			String[] commonNameStrings = entry.getValue();
			for (String commonNameString : commonNameStrings) {
				List<CommonName> commonNames = getCommonName(languageId, taxonConceptId, commonNameString);
				if (commonNames.isEmpty()) {
					CommonName commonName = createCommonName(languageId, commonNameString, taxonConceptId, source);
					commonNamesList.add(commonName);
				}
			}
		}

		return commonNamesList;
	}

	private CommonName createCommonName(Long languageId, String commonNameString, Long taxonConceptId, String source) {
		Timestamp uploadTime = new Timestamp(new Date().getTime());
		Long uploaderId = TaxonomyDefinitionServiceImpl.UPLOADER_ID;
		String transliteration = "";

		CommonName commonName = new CommonName();
		commonName.setLanguageId(languageId);
		commonName.setName(commonNameString.trim());
		commonName.setTaxonConceptId(taxonConceptId);
		commonName.setUploadTime(uploadTime);
		commonName.setUploaderId(uploaderId);
		commonName.setTransliteration(transliteration);
		commonName.setViaDatasource(source);
		commonName.setIsDeleted(false);

		commonName = save(commonName);
		return commonName;
	}

	@Override
	public List<CommonName> updateAddCommonName(HttpServletRequest request, CommonNamesData commonNamesData) {

		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long uploaderId = Long.parseLong(profile.getId());
			if (commonNamesData.getName() == null || commonNamesData.getTaxonConceptId() == null)
				return null;
			if (commonNamesData.getId() == null) {
				CommonName commonNames = new CommonName(null, commonNamesData.getLanguageId(),
						commonNamesData.getName(), commonNamesData.getTaxonConceptId(), new Date(), uploaderId, null,
						false, null, false);

				commonNameDao.save(commonNames);
			} else {
				CommonName commonName = commonNameDao.findById(commonNamesData.getId());
				if (!commonName.getTaxonConceptId().equals(commonNamesData.getTaxonConceptId()))
					return null;
				commonName.setName(commonNamesData.getName());
				if (commonNamesData.getLanguageId() != null)
					commonName.setLanguageId(commonNamesData.getLanguageId());

				commonNameDao.update(commonName);
			}
			List<CommonName> result = fetchByTaxonId(commonNamesData.getTaxonConceptId());
			return result;

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;

	}

	@Override
	public List<CommonName> removeCommonName(HttpServletRequest request, Long commonNameId) {
		try {
			CommonName commonName = commonNameDao.findById(commonNameId);
			commonName = commonNameDao.delete(commonName);
			List<CommonName> result = fetchByTaxonId(commonName.getTaxonConceptId());
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public List<CommonName> fetchByTaxonId(Long taxonId) {

		try {
			List<CommonName> result = commonNameDao.findByTaxonId(taxonId);
			for (CommonName commonNames : result) {
				if (commonNames.getLanguageId() != null) {
					Language language = languageService.fetchLanguageById(commonNames.getLanguageId().toString());
					commonNames.setLanguage(language);
				}
			}
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}
}