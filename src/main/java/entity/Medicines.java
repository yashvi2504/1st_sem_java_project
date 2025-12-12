package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import jakarta.json.bind.annotation.JsonbDateFormat;
import java.math.RoundingMode;
import java.time.LocalDate;
@Entity
@Table(name = "medicines")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "Medicines.findByName",
        query = "SELECT m FROM Medicines m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))"
    ),
    @NamedQuery(
        name = "Medicines.findAll",
        query = "SELECT m FROM Medicines m"
    ),
    @NamedQuery(
    name = "Medicines.findByCategory",
    query = "SELECT m FROM Medicines m WHERE m.categoryId.categoryId = :categoryId"
),
@NamedQuery(
    name = "Medicines.findByManufacturer",
    query = "SELECT m FROM Medicines m WHERE m.manufacturerId.manufacturerId = :manufacturerId"
),
@NamedQuery(
    name = "Medicines.findLowStock",
    query = "SELECT m FROM Medicines m WHERE m.stock < :threshold"
)


})
public class Medicines implements Serializable {

    private static final long serialVersionUID = 1L;

  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Integer medicineId;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "name")
    private String name;

    @Size(max = 100)
    @Column(name = "brand")
    private String brand;

    @Basic(optional = false)
    @NotNull
    @Column(name = "price")
    private BigDecimal price;

    @Basic(optional = false)
    @NotNull
    @Column(name = "stock")
    private int stock;
    
 @JsonbDateFormat("yyyy-MM-dd") //
    @Column(name = "expiry_date")  
    private LocalDate expiryDate;

    // ===== New picture field =====
    @Size(max = 255)
    @Column(name = "picture")
    private String picture;
// ✅ ADD THIS
@Size(max = 500)
@Column(name = "description")
private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    @ManyToOne
    private Categories categoryId;

    @JoinColumn(name = "manufacturer_id", referencedColumnName = "manufacturer_id")
    @ManyToOne
    private Manufacturers manufacturerId;
@jakarta.json.bind.annotation.JsonbTransient
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicineId")
    private Collection<CartItems> cartItemsCollection;
@jakarta.json.bind.annotation.JsonbTransient
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicineId")
 
    private Collection<OrderItems> orderItemsCollection;
@Column(name = "pack_of")
private Integer packOf;
@Column(name = "prescription_required")
private Boolean prescriptionRequired;

    public Boolean getPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(Boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public Medicines() {
    }

    public Medicines(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public Medicines(Integer medicineId, String name, BigDecimal price, int stock) {
        this.medicineId = medicineId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // ===== Getters and Setters =====
    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

  

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Categories getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Categories categoryId) {
        this.categoryId = categoryId;
    }

    public Manufacturers getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(Manufacturers manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    @XmlTransient
    public Collection<CartItems> getCartItemsCollection() {
        return cartItemsCollection;
    }

    public void setCartItemsCollection(Collection<CartItems> cartItemsCollection) {
        this.cartItemsCollection = cartItemsCollection;
    }

  


    @XmlTransient
    public Collection<OrderItems> getOrderItemsCollection() {
        return orderItemsCollection;
    }

    public void setOrderItemsCollection(Collection<OrderItems> orderItemsCollection) {
        this.orderItemsCollection = orderItemsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (medicineId != null ? medicineId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Medicines)) {
            return false;
        }
        Medicines other = (Medicines) object;
        if ((this.medicineId == null && other.medicineId != null) || (this.medicineId != null && !this.medicineId.equals(other.medicineId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Medicines[ medicineId=" + medicineId + " ]";
    }

    public Integer getPackOf() {
        return packOf;
    }

    public void setPackOf(Integer packOf) {
        this.packOf = packOf;
    }

    


public void setCategoryIdInt(int id) {
    this.categoryId = new Categories();
    this.categoryId.setCategoryId(id);
}

public void setManufacturerIdInt(int id) {
    this.manufacturerId = new Manufacturers();
    this.manufacturerId.setManufacturerId(id);
}

}
