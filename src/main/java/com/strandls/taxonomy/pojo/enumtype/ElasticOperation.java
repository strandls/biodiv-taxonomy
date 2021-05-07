package com.strandls.taxonomy.pojo.enumtype;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "elasticOperation")
@XmlEnum
public enum ElasticOperation {

	@XmlEnumValue("CREATE")
	CREATE("CREATE"),
	@XmlEnumValue("UPDATE")
	UPDATE("UPDATE");

	private String value;
	
	private ElasticOperation(String value) {
		this.value = value;
	}
	
	public static ElasticOperation fromValue(String value) {
		for(ElasticOperation layerStatus : ElasticOperation.values()) {
			if(layerStatus.value.equals(value))
				return layerStatus;
		}
		throw new IllegalArgumentException(value);
	}
}
