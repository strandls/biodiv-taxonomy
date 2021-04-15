/**
 * 
 */
package com.strandls.taxonomy.controller;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.taxonomy.ApiConstants;
import com.strandls.taxonomy.dao.TaxonomyDefinitionDao;
import com.strandls.taxonomy.pojo.TaxonomyDefinition;
import com.strandls.taxonomy.pojo.request.FileMetadata;
import com.strandls.taxonomy.pojo.request.TaxonomySave;
import com.strandls.taxonomy.pojo.response.TaxonomySearch;
import com.strandls.taxonomy.service.TaxonomyDefinitionSerivce;

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
public class TaxonomyDefinitionController {

	@Inject
	private TaxonomyDefinitionSerivce taxonomyService;

	@Inject
	private TaxonomyDefinitionDao taxonomyDefinitionDao;

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

	@Path("upload")
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Upload the file for taxon definition", notes = "Returns succuess failure", response = FileMetadata.class)
	@ApiResponses(value = { @ApiResponse(code = 400, message = "file not present", response = String.class),
			@ApiResponse(code = 500, message = "ERROR", response = String.class) })
	@ValidateUser
	public Response upload(@Context HttpServletRequest request, final FormDataMultiPart multiPart) {
		try {
			Map<String, Object> result = taxonomyService.uploadFile(request, multiPart);
			return Response.ok().entity(result).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@ApiOperation(value = "save the taxonomy list", notes = "return the saved taxonomy", response = TaxonomyDefinition.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "failed to save the taxon definition", response = String.class) })
	public Response saveTaxonomyList(@Context HttpServletRequest request,
			@ApiParam("taxonomyList") List<TaxonomySave> taxonomyList) {
		try {
			List<TaxonomyDefinition> taxonomyDefinition = taxonomyService.saveList(request, taxonomyList);
			return Response.status(Status.OK).entity(taxonomyDefinition).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@ValidateUser
	@ApiOperation(value = "save the taxonomy", notes = "return the saved taxonomy", response = TaxonomyDefinition.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "failed to save the taxon definition", response = String.class) })
	public Response saveTaxonomy(@Context HttpServletRequest request,
			@ApiParam("taxonSave") TaxonomySave taxonomySave) {
		try {
			TaxonomyDefinition taxonomyDefinition = taxonomyService.save(request, taxonomySave);
			return Response.status(Status.OK).entity(taxonomyDefinition).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@GET
	@Path("/nameSearch")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get taxonomy based on the canonical name and rank", notes = "return the found taxonomy", response = TaxonomyDefinition.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "failed to get the taxon definition", response = String.class) })
	public Response getByNameSearch(@QueryParam("scientificName") String scientificName,
			@QueryParam("rankName") String rankName) {
		try {
			TaxonomySearch taxonomySearch = taxonomyService.getByNameSearch(scientificName, rankName);
			return Response.status(Status.OK).entity(taxonomySearch).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	@ApiOperation(value = "Search taxonomy based on the name", notes = "return the found taxonomy", response = Object.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "failed to get the taxon definition", response = String.class) })
	public Response search(@QueryParam("term") String term) {
		try {
			Object name = taxonomyDefinitionDao.search(term);
			return Response.status(Status.OK).entity(name).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}

	}

	@GET
	@Path("/retrieve/specificSearch")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	@ApiOperation(value = "Search taxonomy based on the Ids", notes = "return the found taxonomy", response = Object.class)
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "failed to get the taxon definition", response = String.class) })
	public Response specificSearch(@QueryParam("term") String term, @QueryParam("classification") Long classificationId,
			@QueryParam("taxonid") Long taxonid) {
		try {
			List<String> resultTaxonIds = taxonomyDefinitionDao.specificSearch(term, taxonid);
			return Response.status(Status.OK).entity(resultTaxonIds).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
}
