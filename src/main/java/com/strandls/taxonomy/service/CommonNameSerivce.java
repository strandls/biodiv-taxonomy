/**
 * 
 */
package com.strandls.taxonomy.service;

import java.util.List;
import java.util.Map;

import com.strandls.taxonomy.pojo.CommonName;

/**
 * 
 * @author vilay
 *
 */
public interface CommonNameSerivce {

	public CommonName fetchById(Long id);

	public CommonName save(CommonName commonName);

	public List<CommonName> getCommonName(Long languageId, Long taxonConceptId, String commonNameString);

	public CommonName updateIsPreffered(Long id);

	public List<CommonName> addCommonNames(Long taxonConceptId, Map<Long, String[]> languageIdToCommonNames, String source);

}
