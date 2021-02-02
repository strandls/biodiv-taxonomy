package com.strandls.taxonomy.controller;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.taxonomy.ApiConstants;
import com.strandls.taxonomy.pojo.response.BreadCrumb;
import com.strandls.taxonomy.pojo.response.TaxonTree;
import com.strandls.taxonomy.service.TaxonomyRegistryService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Taxonomy Tree Services")
@Path(ApiConstants.V1 + ApiConstants.TREE)
public class TaxonomyRegistryController {

	@Inject
	private TaxonomyRegistryService taxonomyRegistry;
	
	@GET
	@Path(ApiConstants.BREADCRUMB + "/{taxonomyId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find Taxonomy Registry by ID", notes = "Returns Taxonomy Registry details", response = BreadCrumb.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Taxonomy not found", response = String.class) })

	public Response getTaxonomyBreadCrumb(@PathParam("taxonomyId") String taxonomyId) {
		try {

			Long id = Long.parseLong(taxonomyId);
			List<BreadCrumb> breadCrumbs = taxonomyRegistry.fetchByTaxonomyId(id);
			return Response.status(Status.OK).entity(breadCrumbs).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.BREADCRUMB)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser
	@ApiOperation(value = "Find taxon Tree for a list of Taxons", notes = "Returns a List of Taxon Tree", response = TaxonTree.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "unable to fetch the taxon Tree", response = String.class) })

	public Response getTaxonTree(@Context HttpServletRequest request,
			@ApiParam(name = "taxonList") @QueryParam("taxonList") String taxonList) {
		try {

			String[] taxList = taxonList.split(",");
			List<Long> tList = new ArrayList<Long>();
			for (String s : taxList) {
				tList.add(Long.parseLong(s.trim()));
			}
			List<TaxonTree> result = taxonomyRegistry.fetchTaxonTrees(tList);
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@GET
	@Path(ApiConstants.CHILDREN)
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find taxon Tree for a list of Taxons", notes = "Returns a List of Taxon Tree", response = TaxonTree.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "unable to fetch the taxon Tree", response = String.class) })

	public Response getImmediateChildsForTaxon(@Context HttpServletRequest request,
			@ApiParam(name = "nodePath") @QueryParam("nodePath") String nodePath) {
		try {
			List<BreadCrumb> result = taxonomyRegistry.getImmediateChildsForTaxon(nodePath);
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
}
