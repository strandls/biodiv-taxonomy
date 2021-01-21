package com.strandls.taxonomy.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.esmodule.ApiException;
import com.strandls.esmodule.controllers.EsServicesApi;
import com.strandls.esmodule.pojo.MapDocument;
import com.strandls.esmodule.pojo.MapQueryResponse;
import com.strandls.taxonomy.pojo.TaxonomyESDocument;

public class TaxonomyESUpdate {

	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private ObjectMapper objectMapper;
	
	@Inject
	private EsServicesApi esServicesApi;
	
	public static String LEFT_OUTER_JOIN = "left outer join ";
	
	private static String DB_NAME;
	
	static {
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
		Properties properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e) {
			
		}
		DB_NAME = properties.getProperty("language.db.name");
	}
	
	@Inject
	public TaxonomyESUpdate() {}
	
	public MapQueryResponse pushUpdateToElastic(List<Long> taxonIds) {
		try {
			TaxonomyESDocument esDocument = getESDocument(taxonIds);
			String esDocumentString = objectMapper.writeValueAsString(esDocument);
			MapDocument doc = new MapDocument();
			doc.setDocument(esDocumentString);
			return  esServicesApi.create("extended_taxon_definition", "_doc", taxonIds.toString(), doc);
		} catch (JsonProcessingException | ApiException e) {
			e.printStackTrace();
		}
		return null;
	}

	private TaxonomyESDocument getESDocument(List<Long> taxonIds) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		String qryString = "select id, name, canonical_form, rank, status, position, path, hierarchy, accepted_ids, accepted_names, common_names from " + 
								"(select id, name, canonical_form, rank, status, position " + 
								"from taxonomy_definition where is_deleted = false and " +
								"id in (:taxonIds) " + 
							") TD " + 
							LEFT_OUTER_JOIN + 
								"(select synonym_id, array_agg(accepted_id) as accepted_ids,array_agg(name) accepted_names from " + 
								"(select synonym_id, accepted_id from accepted_synonym) A " + 
								"inner join (select id, name from taxonomy_definition) TA on TA.id = A.accepted_id group by synonym_id " + 
								") A " + 
							"on TD.id = A.synonym_id " + 
							LEFT_OUTER_JOIN + 
								"(select taxon_definition_id as taxon_id, original_path as path, string_agg(taxon_name,',' order by nr) as hierarchy from " + 
									"(" + 
									"SELECT *, row_number() OVER(PARTITION by sid order by sid ) AS nr " + 
									"FROM  (SELECT id as sid, taxon_definition_id, p.split_path, path as original_path from taxonomy_registry T , " + 
									"unnest(cast(string_to_array(ltree2text(path), '.') as integer[])) AS p(split_path) where T.classification_id = 1) tr_split " + 
									") S " + 
									"inner join " + 
									"(select id taxon_id, name as taxon_name from taxonomy_definition where is_deleted = false ) TD " + 
									"on TD.taxon_id = S.split_path group by taxon_definition_id, original_path " + 
								") TR " + 
							"on TD.id = TR.taxon_id " + 
							LEFT_OUTER_JOIN + 
								"(select taxon_concept_id, (json_agg(row_to_json((SELECT t FROM (SELECT id ,name,language_id ,language_name,three_letter_code) t)))) AS common_names " + 
									"from (" + 
										"select id, name , taxon_concept_id,language_id,language_name,three_letter_code from " + 
										"(select id, name, language_id, taxon_concept_id from common_names where is_deleted = false and name !~ '^[0-9][0-9]|^[)`-]|^A$|^0$') CN " + 
										"left outer join dblink('dbname=" + DB_NAME +  "', 'select id l_id, name as language_name, three_letter_code from language') as L (l_id integer, language_name varchar, three_letter_code varchar) " + 
										"on CN.language_id = L.l_id " + 
									") CN GROUP BY taxon_concept_id " + 
								") CN " + 
							"on CN.taxon_concept_id = TD.id ";
		Query<TaxonomyESDocument> query = session.createNativeQuery(qryString, TaxonomyESDocument.class);
		query.setParameterList("taxonIds", taxonIds);
		return query.getSingleResult();
	}
}
