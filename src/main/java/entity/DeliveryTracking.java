/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

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
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author harsh
 */
@Entity
@Table(name = "delivery_tracking")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DeliveryTracking.findAll", query = "SELECT d FROM DeliveryTracking d"),
    @NamedQuery(name = "DeliveryTracking.findByTrackingId", query = "SELECT d FROM DeliveryTracking d WHERE d.trackingId = :trackingId"),
    @NamedQuery(name = "DeliveryTracking.findByStatus", query = "SELECT d FROM DeliveryTracking d WHERE d.status = :status"),
    @NamedQuery(name = "DeliveryTracking.findByUpdateTime", query = "SELECT d FROM DeliveryTracking d WHERE d.updateTime = :updateTime")})
public class DeliveryTracking implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "tracking_id")
    private Integer trackingId;
    @Size(max = 100)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @NotNull
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
    @JoinColumn(name = "delivery_id", referencedColumnName = "delivery_id")
    @ManyToOne(optional = false)
    private Delivery deliveryId;

    public DeliveryTracking() {
    }

    public DeliveryTracking(Integer trackingId) {
        this.trackingId = trackingId;
    }

    public DeliveryTracking(Integer trackingId, Date updateTime) {
        this.trackingId = trackingId;
        this.updateTime = updateTime;
    }

    public Integer getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(Integer trackingId) {
        this.trackingId = trackingId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Delivery getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Delivery deliveryId) {
        this.deliveryId = deliveryId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (trackingId != null ? trackingId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DeliveryTracking)) {
            return false;
        }
        DeliveryTracking other = (DeliveryTracking) object;
        if ((this.trackingId == null && other.trackingId != null) || (this.trackingId != null && !this.trackingId.equals(other.trackingId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.DeliveryTracking[ trackingId=" + trackingId + " ]";
    }
    
}
