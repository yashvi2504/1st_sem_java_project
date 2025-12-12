package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;

/**
 * Entity class representing a Manufacturer in the pharmacy system.
 */
@Entity
@Table(name = "manufacturers")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Manufacturers.findAll", query = "SELECT m FROM Manufacturers m"),
    @NamedQuery(name = "Manufacturers.findByManufacturerId", query = "SELECT m FROM Manufacturers m WHERE m.manufacturerId = :manufacturerId"),
    @NamedQuery(name = "Manufacturers.findByName", query = "SELECT m FROM Manufacturers m WHERE m.name = :name"),
    @NamedQuery(name = "Manufacturers.findByContactInfo", query = "SELECT m FROM Manufacturers m WHERE m.contactInfo = :contactInfo")
})
public class Manufacturers implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "manufacturer_id")
    private Integer manufacturerId;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    @Column(name = "contact_info")
    private String contactInfo;

    @OneToMany(mappedBy = "manufacturerId")
   @jakarta.json.bind.annotation.JsonbTransient
private Collection<Medicines> medicinesCollection;

    // ===== Constructors =====
    public Manufacturers() { }

    public Manufacturers(Integer manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public Manufacturers(Integer manufacturerId, String name) {
        this.manufacturerId = manufacturerId;
        this.name = name;
    }

    // ===== Getters & Setters =====
    public Integer getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(Integer manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    @XmlTransient
    public Collection<Medicines> getMedicinesCollection() {
        return medicinesCollection;
    }

    public void setMedicinesCollection(Collection<Medicines> medicinesCollection) {
        this.medicinesCollection = medicinesCollection;
    }

    // ===== hashCode & equals =====
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (manufacturerId != null ? manufacturerId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Manufacturers)) {
            return false;
        }
        Manufacturers other = (Manufacturers) object;
        return (this.manufacturerId != null || other.manufacturerId == null) &&
               (this.manufacturerId == null || this.manufacturerId.equals(other.manufacturerId));
    }

    // ===== toString =====
    @Override
    public String toString() {
        return "entity.Manufacturers[ manufacturerId=" + manufacturerId + " ]";
    }
}
