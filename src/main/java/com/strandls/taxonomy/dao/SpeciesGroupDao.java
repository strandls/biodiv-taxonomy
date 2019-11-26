/**
 * 
 */
package com.strandls.taxonomy.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
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

}
