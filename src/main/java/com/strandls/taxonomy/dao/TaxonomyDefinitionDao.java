/**
 * 
 */
package com.strandls.taxonomy.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.taxonomy.pojo.TaxonomyDefinition;
import com.strandls.taxonomy.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class TaxonomyDefinitionDao extends AbstractDAO<TaxonomyDefinition, Long> {

	private final Logger logger = LoggerFactory.getLogger(TaxonomyDefinitionDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected TaxonomyDefinitionDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public TaxonomyDefinition findById(Long id) {
		Session session = sessionFactory.openSession();
		TaxonomyDefinition entity = null;
		try {
			entity = session.get(TaxonomyDefinition.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<TaxonomyDefinition> breadCrumbSearch(String path) {
		Session session = sessionFactory.openSession();
		List<TaxonomyDefinition> result = null;

		String qry = "from " + daoType.getSimpleName() + " t where t.id in(" + path + ")";// + " order by t.rank";
		try {
			Query<TaxonomyDefinition> query = session.createQuery(qry);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}

	public List<TaxonomyDefinition> findByCanonicalForm(String canonicalForm, String rankName) {
		String queryStr = "" + "from " + daoType.getSimpleName() + " t "
				+ "where t.canonicalForm = :canonicalForm and t.rank = :rank and isDeleted = false";
		try (Session session = sessionFactory.openSession()) {
			Query<TaxonomyDefinition> query = session.createQuery(queryStr, TaxonomyDefinition.class);
			query.setParameter("canonicalForm", canonicalForm);
			query.setParameter("rank", rankName);
			try {
				List<TaxonomyDefinition> result = query.getResultList();
				if(result == null || result.isEmpty()) return new ArrayList<TaxonomyDefinition>();
				return result;
			} catch (NoResultException e) {
				return new ArrayList<TaxonomyDefinition>();
			}
		}
	}

	public List<Object[]> search(String term) {
		Session session = sessionFactory.openSession();
		try {
			String sqlString = "select t.name,t.status,t.position,t.id,t.rank from TaxonomyDefinition as t where lower(t.name) like :term order by t.name";
			Query query = session.createQuery(sqlString);
			query.setMaxResults(10);
			query.setParameter("term", term.toLowerCase().trim() + '%');
			return query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return null;
	}
	
	public List<String> specificSearch(String term, Long taxonId) {
		Session session = sessionFactory.openSession();
		try {
			String sqlString = "select distinct(cast(case when t.status = 'SYNONYM' then a.accepted_id else t.id end as varchar)) as id" + 
					" from (select * from taxonomy_definition where " + 
					(taxonId != null ? " id=:taxonId and ": "") + 
					" lower(name) like lower(:term)) t " + 
					" left outer join accepted_synonym a " + 
					" on t.id = a.synonym_id order by id ";
			Query query = session.createNativeQuery(sqlString);
			query.setParameter("term", term.toLowerCase().trim());
			if(taxonId != null)
				query.setParameter("taxonId", taxonId);
			return query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return null;
	}
}
