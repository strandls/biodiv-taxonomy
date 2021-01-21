package com.strandls.taxonomy.pojo.enumtype;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "taxonomyStatus")
@XmlEnum
public enum TaxonomyStatus {

	@XmlEnumValue("ACCEPTED")
	ACCEPTED("ACCEPTED"),
	@XmlEnumValue("SYNONYM")
	SYNONYM("SYNONYM");

	private String value;
	
	private TaxonomyStatus(String value) {
		this.value = value;
	}
	
	public static TaxonomyStatus fromValue(String value) {
		for(TaxonomyStatus layerStatus : TaxonomyStatus.values()) {
			if(layerStatus.value.equals(value))
				return layerStatus;
		}
		throw new IllegalArgumentException(value);
	}
}
