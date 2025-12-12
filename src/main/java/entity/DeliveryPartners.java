/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author harsh
 */
@Entity
@Table(name = "delivery_partners")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DeliveryPartners.findAll", query = "SELECT d FROM DeliveryPartners d"),
    @NamedQuery(name = "DeliveryPartners.findByDeliveryPartnerId", query = "SELECT d FROM DeliveryPartners d WHERE d.deliveryPartnerId = :deliveryPartnerId"),
    @NamedQuery(name = "DeliveryPartners.findByVehicleNo", query = "SELECT d FROM DeliveryPartners d WHERE d.vehicleNo = :vehicleNo")})
public class DeliveryPartners implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "delivery_partner_id")
    private Integer deliveryPartnerId;
    @Size(max = 50)
    @Column(name = "vehicle_no")
    private String vehicleNo;
    
    @JsonbTransient   // 🚀 Add this line
    @OneToMany(mappedBy = "deliveryPartnerId")
    private Collection<Delivery> deliveryCollection;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private Users userId;

    public DeliveryPartners() {
    }

    public DeliveryPartners(Integer deliveryPartnerId) {
        this.deliveryPartnerId = deliveryPartnerId;
    }

    public Integer getDeliveryPartnerId() {
        return deliveryPartnerId;
    }

    public void setDeliveryPartnerId(Integer deliveryPartnerId) {
        this.deliveryPartnerId = deliveryPartnerId;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    @XmlTransient
    public Collection<Delivery> getDeliveryCollection() {
        return deliveryCollection;
    }

    public void setDeliveryCollection(Collection<Delivery> deliveryCollection) {
        this.deliveryCollection = deliveryCollection;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (deliveryPartnerId != null ? deliveryPartnerId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DeliveryPartners)) {
            return false;
        }
        DeliveryPartners other = (DeliveryPartners) object;
        if ((this.deliveryPartnerId == null && other.deliveryPartnerId != null) || (this.deliveryPartnerId != null && !this.deliveryPartnerId.equals(other.deliveryPartnerId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.DeliveryPartners[ deliveryPartnerId=" + deliveryPartnerId + " ]";
    }
    
}
