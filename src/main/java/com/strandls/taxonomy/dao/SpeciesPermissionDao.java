/**
 * 
 */
package com.strandls.taxonomy.dao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.taxonomy.pojo.SpeciesPermission;
import com.strandls.taxonomy.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class SpeciesPermissionDao extends AbstractDAO<SpeciesPermission, Long> {

	private final Logger logger = LoggerFactory.getLogger(SpeciesPermissionDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected SpeciesPermissionDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public SpeciesPermission findById(Long id) {
		Session session = sessionFactory.openSession();
		SpeciesPermission entity = null;
		try {
			entity = session.get(SpeciesPermission.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<SpeciesPermission> findByUserId(Long userId) {

		String qry = "from SpeciesPermission where authorId = :userId";
		Session session = sessionFactory.openSession();
		List<SpeciesPermission> allowedTaxonList = new ArrayList<>();
		try {
			Query<SpeciesPermission> query = session.createQuery(qry);
			query.setParameter("userId", userId);
			allowedTaxonList = query.getResultList();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return allowedTaxonList;

	}

}
