/**
 * 
 */
package com.strandls.taxonomy.service.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.strandls.taxonomy.service.TaxonomySerivce;

/**
 * @author Abhishek Rudra
 *
 */
public class TaxonomyServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TaxonomySerivce.class).to(TaxonomyServiceImpl.class).in(Scopes.SINGLETON);
	}
}
