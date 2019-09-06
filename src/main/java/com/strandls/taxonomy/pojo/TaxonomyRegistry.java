/**
 * 
 */
package com.strandls.taxonomy.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Abhishek Rudra
 *
 */

@Entity
@Table(name = "taxonomy_registry")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaxonomyRegistry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1891934272853024930L;
	private Long id;
	private Long classificationId;
	private String path;
	private Long taxonomyDefinationId;

	@Id
	@GeneratedValue
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "classification_id")
	public Long getClassificationId() {
		return classificationId;
	}

	public void setClassificationId(Long classificationId) {
		this.classificationId = classificationId;
	}

	@Column(name = "path")
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Column(name = "taxon_definition_id")
	public Long getTaxonomyDefinationId() {
		return taxonomyDefinationId;
	}

	public void setTaxonomyDefinationId(Long taxonomyDefinationId) {
		this.taxonomyDefinationId = taxonomyDefinationId;
	}

}
