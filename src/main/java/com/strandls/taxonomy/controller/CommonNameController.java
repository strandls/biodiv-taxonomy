package com.strandls.taxonomy.controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.strandls.authentication_utility.filter.ValidateUser;
import com.strandls.taxonomy.ApiConstants;
import com.strandls.taxonomy.pojo.CommonName;
import com.strandls.taxonomy.pojo.SpeciesGroup;
import com.strandls.taxonomy.service.CommonNameSerivce;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Common Name Services")
@Path(ApiConstants.V1 + ApiConstants.CNAME)
public class CommonNameController {

	@Inject
	private CommonNameSerivce commonNameService;

	@GET
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	
	@ApiOperation(value = "Get the common name", notes = "Get the common name", response = CommonName.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Could not find the common name", response = String.class) })
	public Response getCommonName(@Context HttpServletRequest request, @PathParam("id") Long id) {
		try {
			CommonName commonName = commonNameService.fetchById(id);
			return Response.ok().entity(commonName).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	
	@ValidateUser
	
	@ApiOperation(value = "Add the common name", notes = "Save the common name", response = SpeciesGroup.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Could not add common name", response = String.class) })
	public Response save(@Context HttpServletRequest request, @ApiParam("commonName") CommonName commonName) {
		try {
			commonName = commonNameService.save(commonName);
			return Response.ok().entity(commonName).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@PUT
	@Path("preffered")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	
	@ValidateUser
	
	@ApiOperation(value = "Update the preffered common name over all", notes = "Return the common name", response = SpeciesGroup.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Could not set the common name to preffered", response = String.class) })
	public Response updateIsPreffered(@QueryParam("commonNameId") Long id) {
		try {
			CommonName commonName = commonNameService.updateIsPreffered(id);
			return Response.ok().entity(commonName).build();
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
}
