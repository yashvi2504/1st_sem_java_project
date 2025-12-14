package ejb;

import entity.Prescription;
import jakarta.ejb.Local;

@Local
public interface PrescriptionEJBLocal {

    Prescription findByOrderAndMedicine(Integer orderId, Integer medicineId);
}
