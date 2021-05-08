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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import com.strandls.taxonomy.pojo.SynonymData;
import com.strandls.taxonomy.pojo.TaxonomicNames;
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
	@ApiOperation(value = "Get taxonomy based on the canonical name and rank", notes = "return the found taxonomy", response = TaxonomySearch.class)
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
	
	@PUT
	@Path("name")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	
	@ValidateUser
	
	@ApiOperation(value = "Update the name of taxonomy", notes="Update the name. input name should be scientific name", response = TaxonomyDefinition.class)
	@ApiResponses(value = {@ApiResponse(code = 400, message = "failed to update the name of taxonomy definition", response = String.class)})
	public Response updateName(@Context HttpServletRequest request, @QueryParam("taxonId") Long taxonId, @QueryParam("taxonName") String taxonName) {
		try {
			TaxonomyDefinition taxonomyDefinition = taxonomyService.updateName(taxonId, taxonName);
			return Response.status(Status.OK).entity(taxonomyDefinition).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path(ApiConstants.UPDATE + ApiConstants.SYNONYM + "/{speciesId}/{taxonId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "update and add synonyms", notes = "return synonyms based on taxonomyId", response = TaxonomyDefinition.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to add the names", response = String.class) })

	public Response updateAddSynonym(@Context HttpServletRequest request, @PathParam("speciesId") String speciesId,
			@PathParam("taxonId") String taxonId, @ApiParam(name = "synonymData") SynonymData synonymData) {
		try {
			Long sId = Long.parseLong(speciesId);
			Long tId = Long.parseLong(taxonId);
			List<TaxonomyDefinition> result = taxonomyService.updateAddSynonym(request, sId, tId, synonymData);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@DELETE
	@Path(ApiConstants.REMOVE + ApiConstants.SYNONYM + "/{speciesId}/{taxonId}/{synonymId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)

	@ValidateUser

	@ApiOperation(value = "delete synonyms", notes = "return list of avaible synonyms", response = TaxonomyDefinition.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "unable to delete the names", response = String.class) })

	public Response removeSynonyms(@Context HttpServletRequest request, @PathParam("speciesId") String speciesId,
			@PathParam("taxonId") String taxonId, @PathParam("synonymId") String synonymId) {
		try {
			Long sId = Long.parseLong(speciesId);
			Long tId = Long.parseLong(taxonId);
			Long synonId = Long.parseLong(synonymId);
			List<TaxonomyDefinition> result = taxonomyService.deleteSynonym(request, sId, tId, synonId);
			return Response.status(Status.OK).entity(result).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

}
