/**
 * 
 */
package com.strandls.taxonomy.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.taxonomy.pojo.TaxonomyRegistry;
import com.strandls.taxonomy.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class TaxonomyRegistryDao extends AbstractDAO<TaxonomyRegistry, Long> {

	private final Logger logger = LoggerFactory.getLogger(TaxonomyRegistryDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected TaxonomyRegistryDao(SessionFactory sessionFactory) {
		super(sessionFactory);

	}

	@Override
	public TaxonomyRegistry findById(Long id) {
		Session session = sessionFactory.openSession();
		TaxonomyRegistry entity = null;
		try {
			entity = session.get(TaxonomyRegistry.class, id);

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public TaxonomyRegistry findbyTaxonomyId(Long taxonomyId) {

		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
		Properties properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		Long classificationId = Long.parseLong(properties.getProperty("classificationId"));

		String qry = "from TaxonomyRegistry tr where tr.taxonomyDefinationId = :taxonomyId "
				+ "and tr.classificationId = :classificationId";
		Session session = sessionFactory.openSession();
		TaxonomyRegistry result = null;
		try {
			Query<TaxonomyRegistry> query = session.createQuery(qry);
			query.setParameter("taxonomyId", taxonomyId);
			query.setParameter("classificationId", classificationId);
			result = query.getSingleResult();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			session.close();
		}

		return result;
	}

	public List<String> findByTaxonIdOnTraitList(List<Long> traitTaxonIds, Set<String> speciesGroupTaxonIds) {

		if (speciesGroupTaxonIds.isEmpty())
			return new ArrayList<String>();
		String speciesGroupTaxons = String.join("|", speciesGroupTaxonIds);
		speciesGroupTaxons = "*." + speciesGroupTaxons +".*";

		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
		Properties properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		Long classificationId = Long.parseLong(properties.getProperty("classificationId"));

		String queryString = "select taxon_definition_id from taxonomy_registry t where t.classification_id = :classificationId and "
				+ "t.path @> any(select tr.path from taxonomy_registry tr where tr.taxon_definition_id in (:traitTaxonIds) and tr.path ~ lquery(:speciesGroupTaxons))";

		Session session = sessionFactory.openSession();

		List<String> result = new ArrayList<String>();
		try {
			NativeQuery query = session.createNativeQuery(queryString);
			query.setParameter("classificationId", classificationId);
			query.setParameterList("traitTaxonIds", traitTaxonIds);
			query.setParameter("speciesGroupTaxons", speciesGroupTaxons);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			session.close();
		}
		return result;
	}

}
