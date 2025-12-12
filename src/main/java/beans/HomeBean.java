package beans;

import ejb.AdminEJBLocal;
import entity.Categories;
import entity.Medicines;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@Named("homeBean")
@RequestScoped
public class HomeBean {

    @Inject
    private AdminEJBLocal adminEJB;

    private List<Categories> categories;
    private List<Medicines> medicines;

    @PostConstruct
    public void init() {
        categories = adminEJB.getAllCategories();
         medicines = (List<Medicines>) adminEJB.getAllMedicines();
    }

    public List<Categories> getCategories() {
        return categories;
    }
    
    
    public List<Medicines> getMedicines() {
        return medicines;
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
