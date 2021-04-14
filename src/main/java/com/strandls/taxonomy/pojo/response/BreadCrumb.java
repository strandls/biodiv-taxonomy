/**
 * 
 */
package com.strandls.taxonomy.pojo.response;

/**
 * @author Abhishek Rudra
 *
 */
public class BreadCrumb {

	private Long id;
	private String name;

	/**
	 * @param id
	 * @param name
	 */
	public BreadCrumb(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
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

}
