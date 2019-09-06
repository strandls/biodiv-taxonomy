/**
 * 
 */
package com.strandls.taxonomy.dao;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Abhishek Rudra
 *
 */
public class TaxonomyDaoModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TaxonomyDefinitionDao.class).in(Scopes.SINGLETON);
		bind(TaxonomyRegistryDao.class).in(Scopes.SINGLETON);

	}
}
