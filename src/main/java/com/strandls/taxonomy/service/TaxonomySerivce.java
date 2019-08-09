/**
 * 
 */
package com.strandls.taxonomy.service;

import com.strandls.taxonomy.pojo.TaxonomyDefinition;

/**
 * @author Abhishek Rudra
 *
 */
public interface TaxonomySerivce {

	public TaxonomyDefinition fetchById(Long id);
}
