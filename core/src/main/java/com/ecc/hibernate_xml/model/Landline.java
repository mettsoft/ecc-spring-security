package com.ecc.hibernate_xml.model;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;

import javax.persistence.Cacheable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@DiscriminatorValue("LANDLINE")
@Cacheable
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Landline extends Contact {}