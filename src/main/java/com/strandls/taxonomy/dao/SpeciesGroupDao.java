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

import com.strandls.taxonomy.pojo.SpeciesGroup;
import com.strandls.taxonomy.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class SpeciesGroupDao extends AbstractDAO<SpeciesGroup, Long> {

	private final Logger logger = LoggerFactory.getLogger(SpeciesGroupDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected SpeciesGroupDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public SpeciesGroup findById(Long id) {
		Session session = sessionFactory.openSession();
		SpeciesGroup entity = null;
		try {
			entity = session.get(SpeciesGroup.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<SpeciesGroup> findAllOrdered() {
		Session session = sessionFactory.openSession();
		List<SpeciesGroup> result = null;

		String qry = "from SpeciesGroup order by groupOrder asc";
		try {
			Query<SpeciesGroup> query = session.createQuery(qry);
			result = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

}
