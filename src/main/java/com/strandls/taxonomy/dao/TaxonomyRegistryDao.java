/**
 * 
 */
package com.strandls.taxonomy.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
import com.strandls.taxonomy.pojo.response.TaxonRelation;
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
	
	public static Long getDefaultClassificationId() {
		return CLASSIFICATION_ID;
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

	public TaxonomyRegistry findbyTaxonomyId(Long taxonomyId, Long classificationId) {

		classificationId = classificationId == null ? CLASSIFICATION_ID : classificationId;

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
			session.close();
		}

		return result;
	}

	public TaxonomyRegistry createRegistry(Long classificationId, String path, String rank, Long taxonDefinitionId,
			Long uploaderId) {
		Timestamp uploadTime = new Timestamp(new Date().getTime());

		classificationId = classificationId == null ? getDefaultClassificationId() : classificationId;
		TaxonomyRegistry registry = new TaxonomyRegistry();
		registry.setClassificationId(classificationId);
		registry.setPath(path.toString());
		registry.setTaxonomyDefinationId(taxonDefinitionId);
		registry.setRank(rank);
		registry.setUploaderId(uploaderId);
		registry.setUploadTime(uploadTime);

		return save(registry);
	}

	public List<String> findByTaxonIdOnTraitList(List<Long> traitTaxonIds, Set<String> speciesGroupTaxonIds) {
		return findByTaxonIdOnTraitList(traitTaxonIds, speciesGroupTaxonIds, CLASSIFICATION_ID);
	}

	public List<String> findByTaxonIdOnTraitList(List<Long> traitTaxonIds, Set<String> speciesGroupTaxonIds,
			Long classificationId) {

		if (speciesGroupTaxonIds.isEmpty())
			return new ArrayList<String>();
		String speciesGroupTaxons = String.join("|", speciesGroupTaxonIds);
		speciesGroupTaxons = "*." + speciesGroupTaxons + ".*";

		classificationId = classificationId == null ? CLASSIFICATION_ID : classificationId;

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
			session.close();
		}
		return result;
	}

	public List<TaxonRelation> list(Long parent, List<Long> taxonIds, boolean expandTaxon, Long classificationId) {
		Session session = sessionFactory.openSession();
		String queryString = "";
		Query query;
		if (parent == null && taxonIds == null) {
			queryString = "select t.id,t.name, t.rank, ltree2text(tR.path) as path, tR.classification_id as classification, "
					+ " case when nlevel(tR.path) > 1 THEN ltree2text(subpath(tR.path,-2,1)) else null end as parent, t.position "
					+ " from taxonomy_definition as t, taxonomy_registry as tR"
					+ " where t.id=tR.taxon_definition_id and t.is_deleted=false  and tR.classification_id=:classification_id and "
					+ " nlevel(tR.path) = 2" + " order by nlevel(tR.path), t.name";
			query = session.createNativeQuery(queryString).setResultSetMapping("TaxonomyRelation");
		} else {
			String parentCheck = " ";
			if (expandTaxon && taxonIds != null && !taxonIds.isEmpty()) {
				List<String> allTaxonIds = getPathToRoot(taxonIds, classificationId);
				parentCheck = String.join("|", allTaxonIds);
				parentCheck = "*." + parentCheck + ".*{0,1}";
			} else if (parent != null) {
				parentCheck = "*." + parent + ".*{1}";
			} else {
				return new ArrayList<TaxonRelation>();
			}
			queryString = "select t.id, t.name, t.rank, ltree2text(tR.path) as path, tR.classification_id as classification, "
					+ " case when nlevel(tR.path) > 1 THEN ltree2text(subpath(tR.path,-2,1)) else null end as parent, t.position "
					+ " from taxonomy_definition as t, taxonomy_registry as tR"
					+ " where t.id=tR.taxon_definition_id and t.is_deleted=false  and tR.classification_id=:classification_id and "
					+ " tR.path ~ lquery(:parentCheck) and nlevel(tR.path) > 1 " + " order by nlevel(tR.path), t.name";
			query = session.createNativeQuery(queryString).setResultSetMapping("TaxonomyRelation");
			query.setParameter("parentCheck", parentCheck);
		}
		classificationId = classificationId == null ? CLASSIFICATION_ID : classificationId;
		query.setParameter("classification_id", classificationId);

		try {
			return query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return null;
	}

	public List<String> getPathToRoot(List<Long> taxonIds, Long classificationId) {
		Session session = sessionFactory.openSession();
		try {
			String sqlString = "select cast(taxon_definition_id as varchar) from taxonomy_registry where path @> "
					+ "any(select path from taxonomy_registry where taxon_definition_id in (:taxonIds)) and "
					+ "classification_id=:classificationId";
			Query query = session.createNativeQuery(sqlString);
			query.setParameter("taxonIds", taxonIds);

			classificationId = classificationId == null ? CLASSIFICATION_ID : classificationId;

			query.setParameter("classificationId", classificationId);
			return (List<String>) query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return null;
	}

	public List<TaxonomyRegistryResponse> getPathToRoot(Long taxonId, Long classificationId) {
		Session session = sessionFactory.openSession();
		try {
			String sqlString = "select cast(td.id as varchar), td.rank, td.name, td.canonical_form from (select * from taxonomy_registry where path @> "
					+ "(select path from taxonomy_registry where taxon_definition_id = :taxonId) and "
					+ "classification_id=:classificationId) tr " + "left outer join taxonomy_definition td "
					+ "on td.id = tr.taxon_definition_id order by tr.path";
			Query query = session.createNativeQuery(sqlString);
			query.setParameter("taxonId", taxonId);
			classificationId = classificationId == null ? CLASSIFICATION_ID : classificationId;
			query.setParameter("classificationId", classificationId);
			return getResultList(query, TaxonomyRegistryResponse.class);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return null;
	}
	
	/**
	 * Code below this point is only for the migration purpose.
	 * @param taxonId
	 * @param classificationIds
	 * @return
	 */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<TaxonomyRegistry> getOldHierarchy(Long taxonId, List<Long> classificationIds) {
		Session session = sessionFactory.openSession();
		try {
			String sqlString = "select id, classification_id, ltree2text(path) path, rank, taxon_definition_id, upload_time, uploader_id from taxonomy_registry_backup "
					+ " where taxon_definition_id = :taxonId"
					+ " and classification_id in (:classificationIds) order by classification_id desc";
			Query query = session.createNativeQuery(sqlString, TaxonomyRegistry.class);
			query.setParameter("taxonId", taxonId);
			query.setParameter("classificationIds", classificationIds);
			return query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		} finally {
			session.close();
		}
	}
	
	public List<TaxonomyRegistryResponse> getPathToRootForOldHierarchy(Long taxonId, Long classificationId) {
		Session session = sessionFactory.openSession();
		try {
			String sqlString = "select cast(td.id as varchar), td.rank, td.name, td.canonical_form from (select * from taxonomy_registry_backup where path @> "
					+ "(select path from taxonomy_registry_backup where taxon_definition_id = :taxonId and "
					+ "classification_id=:classificationId) and classification_id=:classificationId) tr " + "left outer join taxonomy_definition td "
					+ "on td.id = tr.taxon_definition_id order by tr.path";
			Query query = session.createNativeQuery(sqlString);
			query.setParameter("taxonId", taxonId);
			classificationId = classificationId == null ? CLASSIFICATION_ID : classificationId;
			query.setParameter("classificationId", classificationId);
			return getResultList(query, TaxonomyRegistryResponse.class);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return null;
	}
	
	public List<TaxonomyRegistryResponse> getNameFromPath(String path) {
		Session session = sessionFactory.openSession();
		try {
			List<Long> taxonIds = new ArrayList<Long>();
			for(String s : path.split("\\."))
				taxonIds.add(Long.parseLong(s));
			
			String sqlString = "select cast(td.id as varchar), td.rank, td.name, td.canonical_form from taxonomy_definition td "
					+ " left join taxonomy_rank r on td.rank = r.name "
					+ " where td.id in (:taxonIds) order by r.rankvalue";
			Query<String> query = session.createNativeQuery(sqlString);
			query.setParameterList("taxonIds", taxonIds);
			return getResultList(query, TaxonomyRegistryResponse.class);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return null;
	}

}
