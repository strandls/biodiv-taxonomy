/**
 * 
 */
package com.strandls.taxonomy.service.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.strandls.taxonomy.service.CommonNameSerivce;
import com.strandls.taxonomy.service.RankSerivce;
import com.strandls.taxonomy.service.SpeciesGroupService;
import com.strandls.taxonomy.service.TaxonomyDefinitionSerivce;
import com.strandls.taxonomy.service.TaxonomyRegistryService;

/**
 * @author Abhishek Rudra
 *
 */
public class TaxonomyServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TaxonomyDefinitionSerivce.class).to(TaxonomyDefinitionServiceImpl.class).in(Scopes.SINGLETON);
		bind(RankSerivce.class).to(RankServiceImpl.class).in(Scopes.SINGLETON);
		bind(TaxonomyRegistryService.class).to(TaxonomyRegistryServiceImpl.class).in(Scopes.SINGLETON);
		bind(SpeciesGroupService.class).to(SpeciesGroupServiceImpl.class).in(Scopes.SINGLETON);
		bind(CommonNameSerivce.class).to(CommonNameServiceImpl.class).in(Scopes.SINGLETON);
		bind(TaxonomyESUpdate.class).in(Scopes.SINGLETON);
	}
}
