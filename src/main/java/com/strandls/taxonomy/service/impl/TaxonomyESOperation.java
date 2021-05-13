package com.strandls.taxonomy.service.impl;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.esmodule.ApiException;
import com.strandls.esmodule.controllers.EsServicesApi;
import com.strandls.esmodule.pojo.MapQueryResponse;
import com.strandls.taxonomy.TaxonomyConfig;
import com.strandls.taxonomy.pojo.TaxonomyESDocument;

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
	public List<MapQueryResponse> pushToElastic(List<Long> taxonIds) {

		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		try {
			String qryString = TaxonomyConfig.fetchFileAsString(SQL_FILE);

			Query<TaxonomyESDocument> query = session.createNativeQuery(qryString, TaxonomyESDocument.class);
			query.setParameterList("taxonIds", taxonIds);
			List<TaxonomyESDocument> taxonomyESDocuments = query.getResultList();
			
			String index = TaxonomyConfig.getString(ES_TAXONOMY_INDEX);
			String type = TaxonomyConfig.getString(ES_TAXONOMY_TYPE);

			String taxonomyJsonData = objectMapper.writeValueAsString(taxonomyESDocuments);
			return esServicesApi.bulkUpload(index, type, taxonomyJsonData);

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ApiException e) {
			e.printStackTrace();
		}

		return null;
	}

}
