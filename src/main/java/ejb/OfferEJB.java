package ejb;

import entity.Offers;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class OfferEJB {

    @PersistenceContext
    private EntityManager em;

    public List<Offers> getAllOffers() {
        return em.createQuery("SELECT o FROM Offers o", Offers.class)
                 .getResultList();
    }

    public void save(Offers offer) {
        if (offer.getOfferId() == null) {
            em.persist(offer);
        } else {
            em.merge(offer);
        }
    }

    public Offers find(Integer id) {
        return em.find(Offers.class, id);
    }

    public void deleteOffer(Integer id) {
        Offers o = em.find(Offers.class, id);
        if (o != null) {
            em.remove(o);
        }
    }
}

