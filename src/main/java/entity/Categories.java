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
 * Entity class representing a Category in the pharmacy system.
 */
@Entity
@Table(name = "categories")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Categories.findAll", query = "SELECT c FROM Categories c"),
    @NamedQuery(name = "Categories.findByCategoryId", query = "SELECT c FROM Categories c WHERE c.categoryId = :categoryId"),
    @NamedQuery(name = "Categories.findByName", query = "SELECT c FROM Categories c WHERE c.name = :name"),
    @NamedQuery(name = "Categories.findByDescription", query = "SELECT c FROM Categories c WHERE c.description = :description")
})
public class Categories implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "category_id")
    private Integer categoryId;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    @Column(name = "description")
    private String description;
   // ⭐ NEW IMAGE FIELD ⭐
    @Column(name = "image")
    private String image;

 @OneToMany(mappedBy = "categoryId")
@jakarta.json.bind.annotation.JsonbTransient
private Collection<Medicines> medicinesCollection;

    // ===== Constructors =====
    public Categories() { }

    public Categories(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Categories(Integer categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    // ===== Getters & Setters =====
    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
   // ⭐ NEW IMAGE GETTER / SETTER ⭐
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

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
        hash += (categoryId != null ? categoryId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Categories)) {
            return false;
        }
        Categories other = (Categories) object;
        return (this.categoryId != null || other.categoryId == null) &&
               (this.categoryId == null || this.categoryId.equals(other.categoryId));
    }

    // ===== toString =====
    @Override
    public String toString() {
        return "entity.Categories[ categoryId=" + categoryId + " ]";
    }
}
