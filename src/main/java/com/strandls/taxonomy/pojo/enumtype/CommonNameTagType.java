package com.strandls.taxonomy.pojo.enumtype;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "taxonomyStatus")
@XmlEnum
public enum CommonNameTagType {

	@XmlEnumValue("name")
	NAME("name"),
	@XmlEnumValue("threeLetterCode")
	THREE_LETTER_CODE("threeLetterCode"),
	@XmlEnumValue("twoLetterCode")
	TWO_LETTER_CODE("twoLetterCode");

	private String value;
	
	private CommonNameTagType(String value) {
		this.value = value;
	}
	
	public static CommonNameTagType fromValue(String value) {
		for(CommonNameTagType layerStatus : CommonNameTagType.values()) {
			if(layerStatus.value.equals(value))
				return layerStatus;
		}
		throw new IllegalArgumentException(value);
	}
}
