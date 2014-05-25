package org.ap.android.sensor;

import android.hardware.Sensor;

/**
 * A simple data transfer object for a {@link Sensor}. Primarily, the {@link Sensor#toString()} method is provided to
 * display a human understandable, yet concise String that is suitable for use in lists and in other concise forms.
 * 
 * @author abhi
 * 
 */
public class SensorDTO {

	private Sensor sensor;
	private String shortStringRep;

	/**
	 * Default constructor.
	 * 
	 * @param sensor
	 *            the sensor object
	 */
	SensorDTO(Sensor sensor) {
		this.sensor = sensor;
		// since the sensor should be immutable, create the string here itself
		this.shortStringRep = getConciseStringRepresentation();
	}

	/**
	 * Returns the concise string representation as the id. This needs to be revisited.
	 * 
	 * @return the id
	 */
	String getId() {
		return getConciseStringRepresentation();
	}

	/**
	 * Returns the sensor this instance decorates.
	 * 
	 * @return the sensor
	 */
	public Sensor getSensor() {
		return sensor;
	}

	@Override
	public String toString() {
		return shortStringRep;
	}

	private String getConciseStringRepresentation() {
		StringBuilder builder = new StringBuilder();
		builder.append(sensor.getName());

		return builder.toString();
	}
}
