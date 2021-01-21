package com.strandls.taxonomy.pojo;

import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SqlResultSetMapping(name = "TaxonomyESDocumentMapping", entities = {
		@EntityResult(entityClass = TaxonomyESDocument.class, fields = { @FieldResult(name = "id", column = "id"),
				@FieldResult(name = "name", column = "name"),
				@FieldResult(name = "canonical_form", column = "canonicalForm"),
				@FieldResult(name = "rank", column = "rank"), @FieldResult(name = "status", column = "status"),
				@FieldResult(name = "position", column = "position"), @FieldResult(name = "path", column = "path"),
				@FieldResult(name = "hierarchy", column = "hierarchy"),
				@FieldResult(name = "accepted_ids", column = "accepted_ids"),
				@FieldResult(name = "accepted_names", column = "accepted_names"),
				@FieldResult(name = "common_names", column = "common_names") }) })
@Entity
@JsonIgnoreProperties
public class TaxonomyESDocument {

	@Id
	private Long id;
	private String name;
	private String canonical_form;
	private String rank;
	private String status;
	private String position;
	private String path;
	private String hierarchy;
	private String accepted_ids;
	private String accepted_names;
	private String common_names;

	public TaxonomyESDocument() {
		super();
	}

	public TaxonomyESDocument(Long id, String name, String canonical_form, String rank, String status, String position,
			String path, String hierarchy, String accepted_ids, String accepted_names, String common_names) {
		super();
		this.id = id;
		this.name = name;
		this.canonical_form = canonical_form;
		this.rank = rank;
		this.status = status;
		this.position = position;
		this.path = path;
		this.hierarchy = hierarchy;
		this.accepted_ids = accepted_ids;
		this.accepted_names = accepted_names;
		this.common_names = common_names;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCanonical_form() {
		return canonical_form;
	}

	public void setCanonical_form(String canonical_form) {
		this.canonical_form = canonical_form;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	public String getAccepted_ids() {
		return accepted_ids;
	}

	public void setAccepted_ids(String accepted_ids) {
		this.accepted_ids = accepted_ids;
	}

	public String getAccepted_names() {
		return accepted_names;
	}

	public void setAccepted_names(String accepted_names) {
		this.accepted_names = accepted_names;
	}

	public String getCommon_names() {
		return common_names;
	}

	public void setCommon_names(String common_names) {
		this.common_names = common_names;
	}
}
