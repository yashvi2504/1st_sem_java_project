package beans;

import ejb.CustomerEJBLocal;
import ejb.OfferEJB;
import entity.Medicines;
import entity.Offers;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class OfferBean implements Serializable {

    private Offers offer;
    private List<Offers> offerList;       // NEW
    private List<Medicines> medicineList;
    private Integer medicineId;

    @Inject
    private CustomerEJBLocal customerEJB;

    @Inject
    private OfferEJB offerEJB;

    @PostConstruct
    public void init() {
        offer = new Offers();
        medicineList = (List<Medicines>) customerEJB.getAllMedicines();
        offerList = offerEJB.getAllOffers();     // LOAD ALL OFFERS
        medicineId = 0;
    }

    // 🎯 Refresh table after changes
    public void reloadList() {
        offerList = offerEJB.getAllOffers();
    }

    // 🎯 Save / Update Offer
    public String saveOffer() {

        // Set Medicine if selected
        if (medicineId != null && medicineId != 0) {
            Medicines m = customerEJB.getAllMedicines()
                    .stream()
                    .filter(x -> x.getMedicineId().equals(medicineId))
                    .findFirst()
                    .orElse(null);
            offer.setMedicineId(m);
        } else {
            offer.setMedicineId(null);
        }

        offerEJB.save(offer);

        reloadList();          // Refresh table
        offer = new Offers();  // Reset form
        medicineId = 0;

        return null;           // Stay on same page
    }

    // 🎯 Edit an existing offer
    public void editOffer(Offers o) {
        this.offer = o;
        if (o.getMedicineId() != null) {
            this.medicineId = o.getMedicineId().getMedicineId();
        } else {
            this.medicineId = 0;
        }
    }

    // 🎯 Delete an offer
    public void deleteOffer(Integer offerId) {
        offerEJB.deleteOffer(offerId);
        reloadList();      // Refresh
    }

public void loadForEdit(Offers o) {
    this.offer = o;
    this.medicineId = (o.getMedicineId() != null) ? o.getMedicineId().getMedicineId() : 0;
}

public void updateOffer() {
    // set medicine relationship if selected
    if (medicineId != null && medicineId != 0) {
        Medicines m = medicineList.stream()
                .filter(x -> x.getMedicineId().equals(medicineId))
                .findFirst()
                .orElse(null);
        offer.setMedicineId(m);
    } else {
        offer.setMedicineId(null);
    }

    // persist changes
    offerEJB.save(offer);   // merge when id != null
    reloadList();           // refresh offerList used by table
}


    // -----------------------
    // GETTERS & SETTERS
    // -----------------------

    public Offers getOffer() { return offer; }
public List<Offers> getOfferList() {
    if (offerList == null) {
        reloadList();
    }
    return offerList;
}

    public List<Medicines> getMedicineList() { return medicineList; }

    public Integer getMedicineId() { return medicineId; }
    public void setMedicineId(Integer medicineId) { this.medicineId = medicineId; }

}
