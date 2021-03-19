/**
 * 
 */
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

import org.pac4j.core.profile.CommonProfile;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.authentication_utility.util.AuthUtil;
import com.strandls.taxonomy.ApiConstants;
import com.strandls.taxonomy.pojo.BreadCrumb;
import com.strandls.taxonomy.pojo.SpeciesGroup;
import com.strandls.taxonomy.pojo.SpeciesPermission;
import com.strandls.taxonomy.pojo.TaxonTree;
import com.strandls.taxonomy.pojo.TaxonomicNames;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;
import com.strandls.taxonomy.service.TaxonomySerivce;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

	@GET
	@Path(ApiConstants.SPECIES)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find all the SpeciesGroup", notes = "Returns all speciesGroup", response = SpeciesGroup.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Species Group not Found", response = String.class) })

	public Response getAllSpeciesGroup() {
		try {
			List<SpeciesGroup> result = taxonomyService.findAllSpecies();
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path(ApiConstants.SPECIES + "/name/{speciesName}")
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "Find SpeciesGroup by SpeciesGroup Name", notes = "Returns speciesGroup", response = SpeciesGroup.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Species Group not Found", response = String.class) })

	public Response getAllSpeciesGroupByName(@ApiParam("speciesName") @PathParam("speciesName") String speciesName) {
		try {
			SpeciesGroup result = taxonomyService.fetchBySpeciesGroupName(speciesName);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			e.printStackTrace();
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
			List<TaxonTree> result = taxonomyService.fetchTaxonTrees(tList);
			return Response.status(Status.OK).entity(result).build();

		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.SPECIES + ApiConstants.PERMISSION)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "get the speciesPermisison", notes = "return list of taxonomy id in which user can validate", response = SpeciesPermission.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to get the list", response = String.class) })

	public Response getSpeciesPermission(@Context HttpServletRequest request) {
		try {
			CommonProfile profile = AuthUtil.getProfileFromRequest(request);
			Long userId = Long.parseLong(profile.getId());
			List<SpeciesPermission> result = taxonomyService.getSpeciesPermissions(userId);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path(ApiConstants.NAMES + "/{taxonomyId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ApiOperation(value = "get the common name and synonyms", notes = "return taxonoicNames based on taxonomyId", response = TaxonomicNames.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to get the names", response = String.class) })

	public Response getNames(@PathParam("taxonomyId") String taxonomyId) {
		try {
			Long taxonId = Long.parseLong(taxonomyId);
			TaxonomicNames result = taxonomyService.findSynonymCommonName(taxonId);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_GATEWAY).entity(e.getMessage()).build();
		}
	}
}
