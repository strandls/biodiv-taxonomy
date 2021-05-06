package com.strandls.taxonomy.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.esmodule.ApiException;
import com.strandls.esmodule.controllers.EsServicesApi;
import com.strandls.esmodule.pojo.MapQueryResponse;
import com.strandls.taxonomy.TaxonomyConfig;
import com.strandls.taxonomy.pojo.TaxonomyESDocument;
import com.strandls.taxonomy.pojo.enumtype.ElasticOperation;

public class TaxonomyESOperation {

	@Inject
	private SessionFactory sessionFactory;

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private EsServicesApi esServicesApi;

	private final String ES_TAXONOMY_INDEX = "es.taxonomy.index";
	private final String ES_TAXONOMY_TYPE = "es.taxonomy.type";

	private final String SQL_FILE = "extendedTaxonDefinition.sql";

	@Inject
	public TaxonomyESOperation() {
	}

	/**
	 * 
	 * @param taxonIds       - List of taxon Ids
	 * @param createOrUpdate - true - If the taxonIds need to be created in the
	 *                       elastic. false - If the taxonIds need to be updated in
	 *                       the elastic.
	 * @return - MapQueryResponse
	 */
	public List<MapQueryResponse> pushToElastic(List<Long> taxonIds, ElasticOperation elasticOperation) {

		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		try {
			String qryString = TaxonomyConfig.fetchFileAsString(SQL_FILE);

			Query<TaxonomyESDocument> query = session.createNativeQuery(qryString, TaxonomyESDocument.class);

			// Variable mentioned in the sql file
			query.setParameterList("taxonIds", taxonIds);
			List<TaxonomyESDocument> taxonomyESDocuments = query.getResultList();

			String index = TaxonomyConfig.getString(ES_TAXONOMY_INDEX);
			String type = TaxonomyConfig.getString(ES_TAXONOMY_TYPE);

			switch (elasticOperation) {
			case CREATE:
				String taxonomyJsonData = objectMapper.writeValueAsString(taxonomyESDocuments);
				return esServicesApi.bulkUpload(index, type, taxonomyJsonData);
			case UPDATE:
				List<Map<String, Object>> bulkUpdateData = new ArrayList<Map<String, Object>>();
				for (TaxonomyESDocument taxonomyESDocument : taxonomyESDocuments) {
					bulkUpdateData.add(
							objectMapper.convertValue(taxonomyESDocument, new TypeReference<Map<String, Object>>() {
							}));
				}
				return esServicesApi.bulkUpdate(index, type, bulkUpdateData);
			default:
				break;
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ApiException e) {
			e.printStackTrace();
		}

		return null;
	}

}
