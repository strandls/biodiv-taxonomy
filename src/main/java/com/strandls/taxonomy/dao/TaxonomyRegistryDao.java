/**
 * 
 */
package com.strandls.taxonomy.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
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
import com.strandls.taxonomy.pojo.response.TaxonomyRegistryResponse;
import com.strandls.taxonomy.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class TaxonomyRegistryDao extends AbstractDAO<TaxonomyRegistry, Long> {

	private final Logger logger = LoggerFactory.getLogger(TaxonomyRegistryDao.class);

	private static Long CLASSIFICATION_ID;

	static {
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
		Properties properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e) {
		}
		CLASSIFICATION_ID = Long.parseLong(properties.getProperty("classificationId"));
	}

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
		speciesGroupTaxons = "*." + speciesGroupTaxons + ".*";

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

	public List<Object[]> list(Long parent, List<Long> taxonIds, boolean expandTaxon) {
		Session session = sessionFactory.openSession();
		String queryString = "";
		Query query;
		if (parent == null && taxonIds == null) {
			queryString = "select cast(t.id as varchar),t.name, t.rank, ltree2text(tR.path) as path, cast(tR.classification_id as varchar), "
					+ " case when nlevel(tR.path) > 1 THEN ltree2text(subpath(tR.path,-2,1)) else null end as parent, t.position "
					+ " from taxonomy_definition as t, taxonomy_registry as tR"
					+ " where t.id=tR.taxon_definition_id and t.is_deleted=false  and tR.classification_id=:classification_id and "
					+ " nlevel(tR.path) = 2" 
					+ " order by nlevel(tR.path), t.name";
			query = session.createNativeQuery(queryString);
		} else {
			String parentCheck = " ";
			if (expandTaxon && taxonIds != null && !taxonIds.isEmpty()) {
				List<String> allTaxonIds = getPathToRoot(taxonIds);
				parentCheck = String.join("|", allTaxonIds);
				parentCheck = "*." + parentCheck + ".*{0,1}";
			} else if (parent != null) {
				parentCheck = "*." + parent + ".*{1}";
			}
			queryString = "select cast(t.id as varchar),t.name, t.rank, ltree2text(tR.path) as path, cast(tR.classification_id as varchar), "
					+ " case when nlevel(tR.path) > 1 THEN ltree2text(subpath(tR.path,-2,1)) else null end as parent, t.position "
					+ " from taxonomy_definition as t, taxonomy_registry as tR"
					+ " where t.id=tR.taxon_definition_id and t.is_deleted=false  and tR.classification_id=:classification_id and "
					+ " tR.path ~ lquery(:parentCheck) " 
					+ " order by nlevel(tR.path), t.name";
			query = session.createNativeQuery(queryString);
			query.setParameter("parentCheck", parentCheck);
		}
		query.setParameter("classification_id", CLASSIFICATION_ID);

		try {
			return query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return null;
	}
	
	public List<TaxonomyRegistryResponse> getPathToRoot(Long taxonId) {
		Session session = sessionFactory.openSession();
		try {
			String sqlString = "select cast(td.id as varchar), td.rank, td.name from (select * from taxonomy_registry where path @> "
					+ "(select path from taxonomy_registry where taxon_definition_id = :taxonId) and "
					+ "classification_id=:classificationId) tr "
					+ "left outer join taxonomy_definition td "
					+ "on td.id = tr.taxon_definition_id";
			Query query = session.createNativeQuery(sqlString);
			query.setParameter("taxonId", taxonId);
			query.setParameter("classificationId", CLASSIFICATION_ID);
			return getResultList(query, TaxonomyRegistryResponse.class);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return null;
	}

	public List<String> getPathToRoot(List<Long> taxonIds) {
		Session session = sessionFactory.openSession();
		try {
			String sqlString = "select cast(taxon_definition_id as varchar) from taxonomy_registry where path @> "
					+ "any(select path from taxonomy_registry where taxon_definition_id in (:taxonIds)) and "
					+ "classification_id=:classificationId";
			Query query = session.createNativeQuery(sqlString);
			query.setParameter("taxonIds", taxonIds);
			query.setParameter("classificationId", CLASSIFICATION_ID);
			return (List<String>) query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return null;
	}

	public List<Object[]> getHierarchy(Long taxonId) {
		Session session = sessionFactory.openSession();
		try {
			String sqlString = "select td.id, td.rank, td.canonical_form from " 
					+ " (select tr2.id, tr2.taxon_definition_id from taxonomy_registry tr1, taxonomy_registry tr2 "
					+ " where tr1.taxon_definition_id = :taxonId and tr1.classification_id=:classificationId and tr1.path <@ tr2.path) tr" 
					+ " inner join taxonomy_definition td on tr.taxon_definition_id = td.id";
			Query query = session.createNativeQuery(sqlString);
			query.setParameter("taxonId", taxonId);
			query.setParameter("classificationId", CLASSIFICATION_ID);
			return query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return null;
	}
}
