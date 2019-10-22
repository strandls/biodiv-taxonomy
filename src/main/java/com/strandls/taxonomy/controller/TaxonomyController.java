/**
 * 
 */
package com.strandls.taxonomy.controller;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.inject.Inject;
import com.strandls.taxonomy.ApiConstants;
import com.strandls.taxonomy.pojo.BreadCrumb;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;
import com.strandls.taxonomy.service.TaxonomySerivce;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Abhishek Rudra
 *
 */
@Api("Taxonomy Services")
@Path(ApiConstants.V1 + ApiConstants.TAXONOMY)
public class TaxonomyController {

	@Inject
	private TaxonomySerivce taxonomyService;

	@GET
	@Path("/{taxonomyConceptId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find Taxonomy by ID", notes = "Returns Taxonomy details", response = TaxonomyDefinition.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Taxonomy not found", response = String.class) })

	public Response getTaxonomyConceptName(@PathParam("taxonomyConceptId") String taxonomyConceptId) {
		try {
			Long id = Long.parseLong(taxonomyConceptId);
			TaxonomyDefinition taxonomy = taxonomyService.fetchById(id);
			return Response.status(Status.OK).entity(taxonomy).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.BREADCRUMB + "/{taxonomyId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find Taxonomy Registry by ID", notes = "Returns Taxonomy Registry details", response = BreadCrumb.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Taxonomy not found", response = String.class) })

	public Response getTaxonomyBreadCrumb(@PathParam("taxonomyId") String taxonomyId) {
		try {

			Long id = Long.parseLong(taxonomyId);
			List<BreadCrumb> breadCrumbs = taxonomyService.fetchByTaxonomyId(id);
			return Response.status(Status.OK).entity(breadCrumbs).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.SPECIES + "/{speciesId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find taxonomy by SpeciesId", notes = "Return a List of Species Id", response = String.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Taxonomy not Found", response = String.class) })

	public Response getTaxonomyBySpecies(@PathParam("speciesId") String sGroup,
			@QueryParam("taxonomyList") List<String> taxonList) {
		try {
			Long speciesId = Long.parseLong(sGroup);
			List<String> taxonomyList = taxonomyService.fetchBySpeciesId(speciesId, taxonList);
			return Response.status(Status.OK).entity(taxonomyList).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
}
