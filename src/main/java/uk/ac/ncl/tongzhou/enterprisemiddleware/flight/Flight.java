/**
 *   
 * @author Tong Zhou b8027512@ncl.ac.uk
 * @created 21:33 5-11-2018
 */
package uk.ac.ncl.tongzhou.enterprisemiddleware.flight;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.ncl.tongzhou.enterprisemiddleware.booking.Booking;

/**
 * <p>
 * This is a the Domain object. The Flight class represents how flight resources
 * are represented in the application database.
 * </p>
 *
 * <p>
 * The class also specifies how a flights are retrieved from the database
 * (with @NamedQueries), and acceptable values for Flight fields
 * (with @NotNull, @Pattern etc...)
 * </p>
 *
 * @author Tong Zhou
 */
/*
 * The @NamedQueries included here are for searching against the table that
 * reflects this object. This is the most efficient form of query in JPA though
 * is it more error prone due to the syntax being in a String. This makes it
 * harder to debug.
 */
@Entity
@NamedQueries({ @NamedQuery(name = Flight.FIND_ALL, query = "SELECT c FROM Flight c ORDER BY c.number ASC"), })
@XmlRootElement
@Table(name = "flight", uniqueConstraints = @UniqueConstraint(columnNames = "number"))
public class Flight implements Serializable {
	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	public static final String FIND_ALL = "Flight.findAll";

	@Id
	@JsonIgnore
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	// Flight number: a non-empty alpha-numerical string which is 5 characters in
	// length.
	@NotNull(message = "Flight Number could not be empty")
	@Pattern(regexp = "[A-Za-z0-9]{5}", message = "Please use a non-empty alpha-numerical string which is 5 characters in length")
	@Column(name = "number")
	private String number;

	// Flight point of departure: a non-empty alphabetical string, which is upper
	// case and 3 characters in length.
	@NotNull(message = "Point Of Departure could not be empty")
	@Pattern(regexp = "[A-Z]{3}", message = "Please use a non-empty alphabetical string, which is upper case and 3 characters in length")
	@Column(name = "point_of_departure")
	private String pointOfDeparture;

	// Flight destination: a non-empty alphabetical string, which is upper case, 3
	// characters in length and different from its point of departure.
	@NotNull(message = "Destination could not be empty")
	@Pattern(regexp = "[A-Z]{3}", message = "Please use a non-empty alphabetical string, which is upper case, 3 characters in length and different from its point of departure")
	@Column(name = "destination")
	private String destination;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "flight")
	private Set<Booking> bookings;

	@JsonProperty
	public Long getId() {
		return id;
	}

	@JsonIgnore
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Return the number.
	 *
	 * @return number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Set the value of flightNumber
	 *
	 * @param flightNumber:
	 *            flightNumber to be set.
	 */
	public void setNumber(String flightNumber) {
		this.number = flightNumber;
	}

	/**
	 * Return the flightPointOfDeparture.
	 *
	 * @return flightPointOfDeparture
	 */
	public String getPointOfDeparture() {
		return pointOfDeparture;
	}

	/**
	 * Set the value of flightPointOfDeparture
	 *
	 * @param flightPointOfDeparture:
	 *            flightPointOfDeparture to be set.
	 */
	public void setPointOfDeparture(String flightPointOfDeparture) {
		this.pointOfDeparture = flightPointOfDeparture;
	}

	/**
	 * Return the flightDestination.
	 *
	 * @return flightDestination
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * Set the value of flightDestination
	 *
	 * @param flightDestination:
	 *            flightDestination to be set.
	 */
	public void setDestination(String flightDestination) {
		this.destination = flightDestination;
	}

	/**
	 * Return the bookings.
	 *
	 * @return bookings
	 */
	public Set<Booking> getBookings() {
		return bookings;
	}

	/**
	 * Set the value of bookings
	 *
	 * @param bookings:
	 *            bookings to be set.
	 */
	public void setBookings(Set<Booking> bookings) {
		this.bookings = bookings;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Flight))
			return false;
		Flight flight = (Flight) o;
		if (!id.equals(flight.id))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
