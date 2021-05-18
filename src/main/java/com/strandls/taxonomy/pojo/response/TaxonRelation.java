package com.strandls.taxonomy.pojo.response;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class TaxonRelation {

	private Long id;
	private String name;
	private String rank;
	private String path;
	private Long classification;
	private Long parent;
	private String position;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<TaxonRelation> children = new ArrayList<TaxonRelation>();

	public TaxonRelation() {
		super();
	}

	public TaxonRelation(Long id, String name, String rank, String path, Long classification, Long parent,
			String position) {
		super();
		this.id = id;
		this.name = name;
		this.rank = rank;
		this.path = path;
		this.classification = classification;
		this.parent = parent;
		this.position = position;
	}

	public TaxonRelation(Long id, String name, String rank, String path, Long classification, Long parent,
			String position, List<TaxonRelation> children) {
		super();
		this.id = id;
		this.name = name;
		this.rank = rank;
		this.path = path;
		this.classification = classification;
		this.parent = parent;
		this.position = position;
		this.children = children;
	}

	public void addChild(TaxonRelation child) {
		children.add(child);
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

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Long getClassification() {
		return classification;
	}

	public void setClassification(Long classification) {
		this.classification = classification;
	}

	public Long getParent() {
		return parent;
	}

	public void setParent(Long parent) {
		this.parent = parent;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public List<TaxonRelation> getChildren() {
		return children;
	}

	public void setChildren(List<TaxonRelation> children) {
		this.children = children;
	}
}
