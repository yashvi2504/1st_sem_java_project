package entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "cart_items")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CartItems.findAll", query = "SELECT c FROM CartItems c"),
    @NamedQuery(name = "CartItems.findByCartItemId", query = "SELECT c FROM CartItems c WHERE c.cartItemId = :cartItemId"),
    @NamedQuery(name = "CartItems.findByQuantity", query = "SELECT c FROM CartItems c WHERE c.quantity = :quantity"),
    @NamedQuery(name = "CartItems.findByAddedDate", query = "SELECT c FROM CartItems c WHERE c.addedDate = :addedDate")
})
public class CartItems implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "cart_item_id")
    private Integer cartItemId;

    @Basic(optional = false)
    @NotNull
    @Column(name = "quantity")
    private int quantity;

    @Basic(optional = false)
    @NotNull
    @Column(name = "added_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date addedDate;

    @JsonbTransient
@ManyToOne
@JoinColumn(name = "cart_id", referencedColumnName = "cart_id")
private Cart cartId;
// ... inside CartItems.java, near other fields ...
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "price_per_unit")
    private Double pricePerUnit; // Or use BigDecimal

    // <-- IMPORTANT: target must be the entity class (Medicines), not int
    @ManyToOne(optional = false)
    @JoinColumn(name = "medicine_id", referencedColumnName = "medicine_id")
    private Medicines medicineId;   // <-- entity type

    public Medicines getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Medicines medicineId) {
        this.medicineId = medicineId;
    }

    
   
    public CartItems() {
    }

    public CartItems(Integer cartItemId, int quantity, Date addedDate) {
        this.cartItemId = cartItemId;
        this.quantity = quantity;
        this.addedDate = addedDate;
    }

    public Integer getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Integer cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public Cart getCartId() {
        return cartId;
    }

    public void setCartId(Cart cartId) {
        this.cartId = cartId;
    }

   

   
// ... inside CartItems.java, with other getters/setters ...

    public Double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(Double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cartItemId != null ? cartItemId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CartItems)) {
            return false;
        }
        CartItems other = (CartItems) object;
        return (this.cartItemId != null || other.cartItemId == null)
                && (this.cartItemId == null || this.cartItemId.equals(other.cartItemId));
    }

    @Override
    public String toString() {
        return "entity.CartItems[ cartItemId=" + cartItemId + " ]";
    }
}
