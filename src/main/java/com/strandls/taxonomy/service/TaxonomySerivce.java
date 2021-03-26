/**
 * 
 */
package com.strandls.taxonomy.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.strandls.taxonomy.pojo.BreadCrumb;
import com.strandls.taxonomy.pojo.CommonNames;
import com.strandls.taxonomy.pojo.CommonNamesData;
import com.strandls.taxonomy.pojo.SpeciesGroup;
import com.strandls.taxonomy.pojo.SpeciesPermission;
import com.strandls.taxonomy.pojo.TaxonTree;
import com.strandls.taxonomy.pojo.TaxonomicNames;
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

	public SpeciesGroup fetchBySpeciesGroupName(String speciesGroupName);

	public List<TaxonTree> fetchTaxonTrees(List<Long> taxonList);

	public List<SpeciesPermission> getSpeciesPermissions(Long userId);

	public TaxonomicNames findSynonymCommonName(Long taxonId);

	public List<CommonNames> updateAddCommonName(HttpServletRequest request, CommonNamesData commonNamesData);

	public List<CommonNames> removeCommonName(HttpServletRequest request, Long commonNameId);

}
