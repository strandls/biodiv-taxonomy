/**
 * 
 */
package com.strandls.taxonomy.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import com.strandls.taxonomy.pojo.AcceptedSynonym;
import com.strandls.taxonomy.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class AcceptedSynonymDao extends AbstractDAO<AcceptedSynonym, Long> {

	private final Logger logger = LoggerFactory.getLogger(AcceptedSynonymDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected AcceptedSynonymDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public AcceptedSynonym findById(Long id) {
		Session session = sessionFactory.openSession();
		AcceptedSynonym result = null;
		try {
			result = session.get(AcceptedSynonym.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public AcceptedSynonym findAccpetedId(Long synonymId) {
		Session session = sessionFactory.openSession();
		AcceptedSynonym result = null;
		String qry = "from AcceptedSynonym where synonymId = :synonymId";
		try {
			Query<AcceptedSynonym> query = session.createQuery(qry);
			query.setParameter("synonymId", synonymId);
			query.setMaxResults(1);
			result = query.getSingleResult();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

}
