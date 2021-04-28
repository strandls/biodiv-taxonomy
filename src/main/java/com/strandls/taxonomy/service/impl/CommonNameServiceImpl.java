package com.strandls.taxonomy.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.strandls.taxonomy.dao.CommonNameDao;
import com.strandls.taxonomy.pojo.CommonName;
import com.strandls.taxonomy.service.CommonNameSerivce;
import com.strandls.taxonomy.util.AbstractService;

public class CommonNameServiceImpl extends AbstractService<CommonName> implements CommonNameSerivce {
	
	@Inject
	private CommonNameDao commonNameDao;
	
	@Inject
	public CommonNameServiceImpl(CommonNameDao dao) {
		super(dao);
	}

	@Override
	public CommonName fetchById(Long id) {
		return commonNameDao.findById(id);
	}
	
	@Override
	public List<CommonName> fetchByTaxonId(Long taxonId) {
		return commonNameDao.getByPropertyWithCondtion("taxonConceptId", taxonId, "=", -1, -1);
	}

	@Override
	public List<CommonName> getCommonName(Long languageId, Long taxonConceptId, String commonNameString) {
		return commonNameDao.getCommonName(languageId, taxonConceptId, commonNameString);
	}
	
	@Override
	public CommonName getPrefferedCommonName(Long taxonId) {
		List<CommonName> commonNames = commonNameDao.getByPropertyWithCondtion("taxonConceptId", taxonId, "=", -1, -1);
		for(CommonName commonName : commonNames) {
			if(commonName.isPreffered()) 
				return commonName;
		}
		return null;
	}
	
	@Override
	public CommonName updateIsPreffered(Long id) {
		CommonName commonName = commonNameDao.findById(id);
		Long taxonConceptId = commonName.getTaxonConceptId();
		List<CommonName> commonNames = commonNameDao.getByPropertyWithCondtion("taxonConceptId", taxonConceptId, "=", -1, -1);
		for(CommonName c : commonNames) {
			c.setPreffered(false);
			update(c);
		}
		commonName.setPreffered(true);
		return update(commonName);
	}
	
	public List<CommonName> addCommonNames(Long taxonConceptId, Map<Long, String[]> languageIdToCommonNames, String source) {
		
		List<CommonName> commonNamesList = new ArrayList<CommonName>();
		
		for (Map.Entry<Long, String[]> entry : languageIdToCommonNames.entrySet()) {
			Long languageId = entry.getKey();
			String[] commonNameStrings = entry.getValue();
			for(String commonNameString : commonNameStrings) {
				List<CommonName> commonNames = getCommonName(languageId, taxonConceptId, commonNameString);
				if(commonNames.isEmpty()) {
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
}
