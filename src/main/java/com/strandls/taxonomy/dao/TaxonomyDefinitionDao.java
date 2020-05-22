/**
 * 
 */
package com.strandls.taxonomy.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

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
		
		String qry = "from TaxonomyDefinition td where td.id in("+path+") order by td.rank";
		try {
			Query<TaxonomyDefinition> query = session.createQuery(qry);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		finally {
			session.close();
		}
		
		return result;
	}

}
