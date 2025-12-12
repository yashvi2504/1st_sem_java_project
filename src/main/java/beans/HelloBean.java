// package beans;
package beans;

import entity.Medicines;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import record.KeepRecord;

@Named("helloBean")
@RequestScoped
public class HelloBean {
    @Inject
    KeepRecord keepRecord;

    public String getSecureHello() {
        if (keepRecord.getPrincipal() != null)
            return "Hello " + keepRecord.getPrincipal().getName();
        return "Hello guest";
    }

    public String getMessage() {
        return keepRecord.getErrorStatus();
    }
    private Medicines selectedMedicine;

public Medicines getSelectedMedicine() {
    return selectedMedicine;
}

public void setSelectedMedicine(Medicines selectedMedicine) {
    this.selectedMedicine = selectedMedicine;
}

public void openMedicine(Medicines m) {
    this.selectedMedicine = m;
}

}
