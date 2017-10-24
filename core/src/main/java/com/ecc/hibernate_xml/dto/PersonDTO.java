package com.ecc.hibernate_xml.dto;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Date;

import com.ecc.hibernate_xml.util.app.DateUtils;

public class PersonDTO {
	private Integer id;
	private NameDTO name;
	private AddressDTO address;
	private Date birthday;
	private BigDecimal GWA;
	private Boolean currentlyEmployed;
	private Date dateHired;
	private List<ContactDTO> contacts;
	private List<RoleDTO> roles;
	
	public PersonDTO() {
		currentlyEmployed = false;
		contacts = new ArrayList<>();
		roles = new ArrayList<>();
	}

	public Integer getId() {
		return id;
	}

	public NameDTO getName() {
		return name;
	}

	public AddressDTO getAddress() {
		return address;
	}

	public Date getBirthday() {
		return birthday;
	}

	public BigDecimal getGWA() {
		return GWA;
	}

	public Boolean getCurrentlyEmployed() {
		return currentlyEmployed;
	}

	public Date getDateHired() {
		return dateHired;
	}

	public List<ContactDTO> getContacts() {
		return contacts;
	}

	public List<RoleDTO> getRoles() {
		return roles;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(NameDTO name) {
		this.name = name;
	}

	public void setAddress(AddressDTO address) {
		this.address = address;
	}

	public void setBirthday(Date birthday) {
 		this.birthday = birthday;
	}

	public void setGWA(BigDecimal GWA) {
		this.GWA = GWA;
	}

	public void setCurrentlyEmployed(boolean currentlyEmployed) {
		this.currentlyEmployed = currentlyEmployed;
	}

	public void setDateHired(Date dateHired) {
		this.dateHired = dateHired;		
	}

	public void setContacts(List<ContactDTO> contacts) {
		this.contacts = contacts;
	}

	public void setRoles(List<RoleDTO> roles) {
		this.roles = roles;
	}

	public String getEmploymentStatus() {
		return currentlyEmployed? DateUtils.toString(dateHired): "No";
	}
}