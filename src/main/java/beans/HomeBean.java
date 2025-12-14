package beans;

import ejb.AdminEJBLocal;
import entity.Categories;
import entity.Medicines;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("homeBean")
@ViewScoped
public class HomeBean implements Serializable{

    @Inject
    private AdminEJBLocal adminEJB;

    private List<Categories> categories;
    private List<Medicines> medicines;
//private List<Medicines> medicines;
private Integer selectedCategoryId;
private List<Medicines> allMedicines;   // ALL medicines

//    private List<Medicines> allMedicines;      // FULL list
    private List<Medicines> displayMedicines;  // UI list

@PostConstruct
public void init() {
    categories = adminEJB.getAllCategories();
  
    medicines = new ArrayList<>(adminEJB.getAllMedicines());
}
public List<Medicines> getAllMedicines() {
    return allMedicines;
}

    public List<Categories> getCategories() {
        return categories;
    }
    public void filterByCategory(Integer categoryId) {
    medicines = new ArrayList<>(adminEJB.getMedicinesByCategory(categoryId));
}

    
  public List<Medicines> getMedicines() {
    if (filteredMedicines != null) {
        return filteredMedicines;
    }

    // show latest 8 when no search is applied
    int size = medicines.size();
    int fromIndex = Math.max(0, size - 8);
    return medicines.subList(fromIndex, size);
}
public void searchMedicines() {
    if (searchText == null || searchText.trim().isEmpty()) {
        filteredMedicines = null; // reset to latest list
        return;
    }

    String keyword = searchText.toLowerCase();

    filteredMedicines = medicines.stream()
        .filter(m -> m.getName().toLowerCase().contains(keyword))
        .collect(java.util.stream.Collectors.toList());
}


public int getCurrentYear() {
    return java.time.Year.now().getValue();
}
private String searchText = "";
private List<Medicines> filteredMedicines;
public String getSearchText() {
    return searchText;
}

public void setSearchText(String searchText) {
    this.searchText = searchText;
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
