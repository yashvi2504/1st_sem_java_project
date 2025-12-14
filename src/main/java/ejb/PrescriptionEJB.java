package ejb;

import entity.Prescription;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Stateless
public class PrescriptionEJB implements PrescriptionEJBLocal {

    @PersistenceContext(unitName = "pharmacyPU")
    private EntityManager em;

    @Override
    public Prescription findByOrderAndMedicine(Integer orderId, Integer medicineId) {
        try {
            return em.createQuery(
                "SELECT p FROM Prescription p " +
                "WHERE p.orderId.orderId = :orderId " +
                "AND p.medicineId.medicineId = :medicineId",
                Prescription.class
            )
            .setParameter("orderId", orderId)
            .setParameter("medicineId", medicineId)
            .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }
}
