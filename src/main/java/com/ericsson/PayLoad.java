package com.ericsson;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PayLoad implements Serializable {

	@Override
	public String toString() {
		return "PayLoad [frontLeftTirePressure=" + frontLeftTirePressure + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String frontLeftTirePressure;

	public String getFrontLeftTirePressure() {
		return frontLeftTirePressure;
	}

	public void setFrontLeftTirePressure(String frontLeftTirePressure) {
		this.frontLeftTirePressure = frontLeftTirePressure;
	}
	
	
}
