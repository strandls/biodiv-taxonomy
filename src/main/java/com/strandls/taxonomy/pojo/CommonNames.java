/**
 * 
 */
package com.strandls.taxonomy.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.strandls.utility.pojo.Language;

/**
 * @author Abhishek Rudra
 *
 * 
 */

@Entity
@Table(name = "common_names")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonNames implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4613116191423797252L;
	private Long id;
	private Long languageId;
	private String name;
	private Long taxonConceptId;
	private Date uploaderTime;
	private Long uploaderId;
	private String transliteration;
	private String status;
	private String position;
	private String authorYear;
	private String matchDataBaseName;
	private String matchId;
	private String ibpSource;
	private String viaDataSource;
	private String lowercaseName;
	private String nameSourceId;
	private Boolean isDeleted;
	private Language language;

	/**
	 * 
	 */
	public CommonNames() {
		super();
	}

	/**
	 * @param id
	 * @param languageId
	 * @param name
	 * @param taxonConceptId
	 * @param uploaderTime
	 * @param uploaderId
	 * @param transliteration
	 * @param status
	 * @param position
	 * @param authorYear
	 * @param matchDataBaseName
	 * @param matchId
	 * @param ibpSource
	 * @param viaDataSource
	 * @param lowercaseName
	 * @param nameSourceId
	 * @param isDeleted
	 */
	public CommonNames(Long id, Long languageId, String name, Long taxonConceptId, Date uploaderTime, Long uploaderId,
			String transliteration, String status, String position, String authorYear, String matchDataBaseName,
			String matchId, String ibpSource, String viaDataSource, String lowercaseName, String nameSourceId,
			Boolean isDeleted) {
		super();
		this.id = id;
		this.languageId = languageId;
		this.name = name;
		this.taxonConceptId = taxonConceptId;
		this.uploaderTime = uploaderTime;
		this.uploaderId = uploaderId;
		this.transliteration = transliteration;
		this.status = status;
		this.position = position;
		this.authorYear = authorYear;
		this.matchDataBaseName = matchDataBaseName;
		this.matchId = matchId;
		this.ibpSource = ibpSource;
		this.viaDataSource = viaDataSource;
		this.lowercaseName = lowercaseName;
		this.nameSourceId = nameSourceId;
		this.isDeleted = isDeleted;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "language_id")
	public Long getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "taxon_concept_id")
	public Long getTaxonConceptId() {
		return taxonConceptId;
	}

	public void setTaxonConceptId(Long taxonConceptId) {
		this.taxonConceptId = taxonConceptId;
	}

	@Column(name = "upload_time")
	public Date getUploaderTime() {
		return uploaderTime;
	}

	public void setUploaderTime(Date uploaderTime) {
		this.uploaderTime = uploaderTime;
	}

	@Column(name = "uploader_id")
	public Long getUploaderId() {
		return uploaderId;
	}

	public void setUploaderId(Long uploaderId) {
		this.uploaderId = uploaderId;
	}

	@Column(name = "transliteration")
	public String getTransliteration() {
		return transliteration;
	}

	public void setTransliteration(String transliteration) {
		this.transliteration = transliteration;
	}

	@Column(name = "status")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Column(name = "position")
	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	@Column(name = "author_year")
	public String getAuthorYear() {
		return authorYear;
	}

	public void setAuthorYear(String authorYear) {
		this.authorYear = authorYear;
	}

	@Column(name = "match_database_name")
	public String getMatchDataBaseName() {
		return matchDataBaseName;
	}

	public void setMatchDataBaseName(String matchDataBaseName) {
		this.matchDataBaseName = matchDataBaseName;
	}

	@Column(name = "match_id")
	public String getMatchId() {
		return matchId;
	}

	public void setMatchId(String matchId) {
		this.matchId = matchId;
	}

	@Column(name = "ibp_source")
	public String getIbpSource() {
		return ibpSource;
	}

	public void setIbpSource(String ibpSource) {
		this.ibpSource = ibpSource;
	}

	@Column(name = "via_datasource")
	public String getViaDataSource() {
		return viaDataSource;
	}

	public void setViaDataSource(String viaDataSource) {
		this.viaDataSource = viaDataSource;
	}

	@Column(name = "lowercase_name")
	public String getLowercaseName() {
		return lowercaseName;
	}

	public void setLowercaseName(String lowercaseName) {
		this.lowercaseName = lowercaseName;
	}

	@Column(name = "name_source_id")
	public String getNameSourceId() {
		return nameSourceId;
	}

	public void setNameSourceId(String nameSourceId) {
		this.nameSourceId = nameSourceId;
	}

	@Column(name = "is_deleted")
	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Transient
	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

}
