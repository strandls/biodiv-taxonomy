/**
 * 
 */
package com.strandls.taxonomy.service;

import java.util.List;

import com.strandls.taxonomy.pojo.BreadCrumb;
import com.strandls.taxonomy.pojo.SpeciesGroup;
import com.strandls.taxonomy.pojo.TaxonTree;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;

/**
 * @author Abhishek Rudra
 *
 */
public interface TaxonomySerivce {

	public TaxonomyDefinition fetchById(Long id);

	public List<BreadCrumb> fetchByTaxonomyId(Long id);

	public List<String> fetchBySpeciesId(Long id, List<String> taxonList);

	public List<SpeciesGroup> findAllSpecies();

	public List<TaxonTree> fetchTaxonTrees(List<Long> taxonList);

}
