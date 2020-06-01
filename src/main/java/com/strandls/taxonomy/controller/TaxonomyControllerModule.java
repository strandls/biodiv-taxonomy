/**
 * 
 */
package com.strandls.taxonomy.controller;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Abhishek Rudra
 *
 */
public class TaxonomyControllerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TaxonomyController.class).in(Scopes.SINGLETON);
	}
}
