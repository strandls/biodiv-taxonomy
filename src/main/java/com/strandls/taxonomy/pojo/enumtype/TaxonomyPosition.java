package com.strandls.taxonomy.pojo.enumtype;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "taxonomyStatus")
@XmlEnum
public enum TaxonomyPosition {

	@XmlEnumValue("CLEAN")
	CLEAN("CLEAN"),
	@XmlEnumValue("RAW")
	RAW("RAW"),
	@XmlEnumValue("WORKING")
	WORKING("WORKING");

	private String value;
	
	private TaxonomyPosition(String value) {
		this.value = value;
	}
	
	public static TaxonomyPosition fromValue(String value) {
		for(TaxonomyPosition layerStatus : TaxonomyPosition.values()) {
			if(layerStatus.value.equals(value))
				return layerStatus;
		}
		throw new IllegalArgumentException(value);
	}
}
