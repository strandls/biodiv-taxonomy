/**
 * 
 */
package com.strandls.taxonomy.dao;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.taxonomy.pojo.CommonNames;
import com.strandls.taxonomy.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 * 
 */
public class CommonNamesDao extends AbstractDAO<CommonNames, Long> {

	private final Logger logger = LoggerFactory.getLogger(CommonNamesDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected CommonNamesDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public CommonNames findById(Long id) {
		CommonNames commonName = null;
		Session session = sessionFactory.openSession();
		try {
			commonName = session.get(CommonNames.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return commonName;
	}

	@SuppressWarnings("unchecked")
	public List<CommonNames> findByTaxonId(Long taxonId) {
		String qry = "from CommonNames where taxonConceptId = :taxonId ";
		Session session = sessionFactory.openSession();
		List<CommonNames> result = null;
		try {
			Query<CommonNames> query = session.createQuery(qry);
			query.setParameter("taxonId", taxonId);
			result = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

}
