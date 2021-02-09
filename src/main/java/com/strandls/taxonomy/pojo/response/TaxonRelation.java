package com.strandls.taxonomy.pojo.response;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class TaxonRelation {
	
	private Long taxonId;
	private String path;
	private Long parent;
	private String text;
	private Long classification;
	private Long id;
	private String rank;
	private String position;
	List<String> ids=new ArrayList<String>();
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TaxonRelation> children = new ArrayList<TaxonRelation>();

	public TaxonRelation() {
		super();
	}

	public TaxonRelation(Long taxonId, String path, Long parent, String text, Long classification, Long id, String rank,
			String position, List<String> ids) {
		super();
		this.taxonId = taxonId;
		this.path = path;
		this.parent = parent;
		this.text = text;
		this.classification = classification;
		this.id = id;
		this.rank = rank;
		this.position = position;
		this.ids = ids;
	}
	
	public void addChild(TaxonRelation child) {
		children.add(child);
	}

	public Long getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(Long taxonId) {
		this.taxonId = taxonId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Long getParent() {
		return parent;
	}

	public void setParent(Long parent) {
		this.parent = parent;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Long getClassification() {
		return classification;
	}

	public void setClassification(Long classification) {
		this.classification = classification;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public List<TaxonRelation> getChildren() {
		return children;
	}

	public void setChildren(List<TaxonRelation> children) {
		this.children = children;
	}	
}
