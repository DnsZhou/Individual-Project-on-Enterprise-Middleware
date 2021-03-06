
/**
 *   
 * @author Tong Zhou b8027512@ncl.ac.uk
 * @created 01:13 16-11-2018
 */
package uk.ac.ncl.tongzhou.enterprisemiddleware.flight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.quickstarts.wfk.util.RestServiceException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import uk.ac.ncl.tongzhou.enterprisemiddleware.flight.Flight;

/**
 * <p>
 * This class produces a RESTful service exposing the functionality of
 * {@link FlightService}.
 * </p>
 *
 * <p>
 * The Path annotation defines this as a REST Web Service using JAX-RS.
 * </p>
 *
 * <p>
 * By placing the Consumes and Produces annotations at the class level the
 * methods all default to JSON. However, they can be overriden by adding the
 * Consumes or Produces annotations to the individual methods.
 * </p>
 *
 * <p>
 * It is Stateless to "inform the container that this RESTful web service should
 * also be treated as an EJB and allow transaction demarcation when accessing
 * the database." - Antonio Goncalves
 * </p>
 *
 * <p>
 * The full path for accessing endpoints defined herein is: api/flights/*
 * </p>
 * 
 * @author Tong Zhou
 * @see FlightService
 * @see javax.ws.rs.core.Response
 */
@Path("/flights")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/flights", description = "Operations about flights")
@Stateless
public class FlightRestService {
	@Inject
	private @Named("logger") Logger log;

	@Inject
	private FlightService service;

	/**
	 * <p>
	 * Return all the Flights. They are sorted alphabetically by number.
	 * </p>
	 *
	 * <p>
	 * Examples:
	 * 
	 * <pre>
	 * GET api/flights
	 * </pre>
	 * </p>
	 *
	 * @return A Response containing a list of Flights
	 */
	@GET
	@ApiOperation(value = "Fetch all Flights", notes = "Returns a JSON array of all stored Flight objects.")
	public Response retrieveAllFlights() {
		// Create an empty collection to contain the intersection of Flights to be
		// returned
		List<Flight> flights;

		flights = service.findAllOrderedByNumber();

		return Response.ok(flights).build();
	}

	/**
	 * <p>
	 * Creates a new Flight from the values provided. Performs validation and will
	 * return a JAX-RS response with either 201 (Resource created) or with a map of
	 * fields, and related errors.
	 * </p>
	 *
	 * @param flight
	 *            The Flight object, constructed automatically from JSON input, to
	 *            be <i>created</i> via {@link FlightService#create(Flight)}
	 * @return A Response indicating the outcome of the create operation
	 */
	@POST
	@ApiOperation(value = "Add a new Flight to the database")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Flight created successfully."),
			@ApiResponse(code = 400, message = "Invalid Flight supplied in request body"),
			@ApiResponse(code = 409, message = "Flight supplied in request body conflicts with an existing Flight"),
			@ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request") })
	public Response createFlight(
			@ApiParam(value = "JSON representation of Flight object to be added to the database", required = true) Flight flight) {

		if (flight == null) {
			throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
		}

		Response.ResponseBuilder builder;

		try {
			// Go add the new Flight.
			service.create(flight);

			// Create a "Resource Created" 201 Response and pass the Flight back in case
			// it is needed.
			builder = Response.status(Response.Status.CREATED).entity(flight);

		} catch (ConstraintViolationException ce) {
			// Handle bean validation issues
			Map<String, String> responseObj = new HashMap<>();

			for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
				responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
			}
			throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

		} catch (DestinationDuplicateWithDepartureException e) {
			// Handle the unique constraint violation
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("destination", "The destination is same with point of departure");
			throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);
		} catch (UniqueFlightNumberException e) {
			// Handle the unique constraint violation
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("number", "The Flight number already exists in system");
			throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
		} catch (Exception e) {
			// Handle generic exceptions
			log.log(Level.SEVERE, e.getMessage());
			throw new RestServiceException(e);
		}

		log.info("createFlight completed. Flight = " + flight.toString());
		return builder.build();
	}
	
	/**
	 * <p>
	 * Deletes a flight using the ID provided. If the ID is not present then
	 * nothing can be deleted.
	 * </p>
	 *
	 * <p>
	 * Will return a JAX-RS response with either 204 NO CONTENT or with a map of
	 * fields, and related errors.
	 * </p>
	 *
	 * @param id
	 *            The Long parameter value provided as the id of the Flight to be
	 *            deleted
	 * @return A Response indicating the outcome of the delete operation
	 */
	@DELETE
	@Path("/{id:[0-9]+}")
	@ApiOperation(value = "Delete a Flight from the database")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "The flight has been successfully deleted"),
			@ApiResponse(code = 400, message = "Invalid Flight id supplied"),
			@ApiResponse(code = 404, message = "Flight with id not found"),
			@ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request") })
	public Response deleteFlight(
			@ApiParam(value = "Id of Flight to be deleted", allowableValues = "range[0, infinity]", required = true) @PathParam("id") long id) {

		Response.ResponseBuilder builder;

		Flight flight = service.findById(id);
		if (flight == null) {
			// Verify that the flight exists. Return 404, if not present.
			throw new RestServiceException("No Flight with the id " + id + " was found!", Response.Status.NOT_FOUND);
		}

		try {
			service.delete(flight);

			builder = Response.noContent();

		} catch (Exception e) {
			// Handle generic exceptions
			throw new RestServiceException(e);
		}
		log.info("deleteFlight completed. Flight = " + flight.toString());
		return builder.build();
	}
}
