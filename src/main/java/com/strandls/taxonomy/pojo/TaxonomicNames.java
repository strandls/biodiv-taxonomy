/**
 * 
 */
package com.strandls.taxonomy.pojo;

import java.util.List;

/**
 * @author Abhishek Rudra
 *
 * 
 */
public class TaxonomicNames {

	private List<CommonNames> commonNames;
	private List<TaxonomyDefinition> synonyms;

	/**
	 * 
	 */
	public TaxonomicNames() {
		super();
	}

	/**
	 * @param commonNames
	 * @param synonyms
	 */
	public TaxonomicNames(List<CommonNames> commonNames, List<TaxonomyDefinition> synonyms) {
		super();
		this.commonNames = commonNames;
		this.synonyms = synonyms;
	}

	public List<CommonNames> getCommonNames() {
		return commonNames;
	}

	public void setCommonNames(List<CommonNames> commonNames) {
		this.commonNames = commonNames;
	}

	public List<TaxonomyDefinition> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(List<TaxonomyDefinition> synonyms) {
		this.synonyms = synonyms;
	}

}
