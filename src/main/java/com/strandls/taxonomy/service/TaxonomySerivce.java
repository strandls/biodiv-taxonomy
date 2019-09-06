/**
 * 
 */
package com.strandls.taxonomy.service;

import java.util.List;

import com.strandls.taxonomy.pojo.TaxonomyDefinition;

/**
 * @author Abhishek Rudra
 *
 */
public interface TaxonomySerivce {

	public TaxonomyDefinition fetchById(Long id);
	
	public List<String> fetchByTaxonomyId(Long id);
}
