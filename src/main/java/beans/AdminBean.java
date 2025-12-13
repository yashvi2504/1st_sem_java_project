package beans;

import ejb.AdminEJB;
import ejb.AdminEJBLocal;
import entity.Categories;
import entity.Manufacturers;
import entity.Medicines;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
//import jakarta.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.stream.Collectors;   // <-- add this
import java.util.ArrayList;
import java.util.List;
//import org.primefaces.event.FileUploadEvent;
//import org.primefaces.model.file.UploadedFile;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;


@Named("adminBean")
@ViewScoped
public class AdminBean implements Serializable {

    @Inject
    private AdminEJBLocal adminEJB;
@Inject
private LoginBean loginBean;

    // ---------------- Users ----------------
    private List<Users> usersList;

    // ---------------- Categories ----------------
    private List<Categories> categoriesList;
    private Categories selectedCategory = new Categories();
    private boolean editMode = false;
    private String searchKeyword; // <-- ADD THIS
    private UploadedFile categoryImage;

// ---------------- Init ----------------
// ---------------- Manufacturers ----------------
private List<Manufacturers> manufacturersList;
private Manufacturers selectedManufacturer = new Manufacturers();
private boolean manufacturerEditMode = false;
private String manufacturerSearchKeyword;
// ---------------- Medicines ----------------
// ---------------- Medicines ----------------
private List<Medicines> medicinesList;
private Medicines selectedMedicine = new Medicines();
private boolean medicineEditMode = false;
private String medicineSearchKeyword;

// Dropdown values
private Integer selectedCategoryId;
private Integer selectedManufacturerId;
// Temporary editable fields
private String editUsername;
private String editEmail;
private String editPhone;
private String editPassword;

public void loadProfileForEdit() {
    Users u = loginBean.getLoggedUser();
    editUsername = u.getUsername();
    editEmail = u.getEmail();
    editPhone = u.getPhone();
    editPassword = u.getPassword();
}
private boolean lowStockAvailable;
private String lowStockMessage;

public boolean isLowStockAvailable() {
    return lowStockAvailable;
}

public String getLowStockMessage() {
    return lowStockMessage;
}
public void loadMedicines() {

    // adminEJB.getAllMedicines() returns Collection<Medicines>
    // convert to a List to avoid incompatible types
    medicinesList = new ArrayList<>(adminEJB.getAllMedicines());

    // use Collectors.toList() (Java 11 compatible)
    List<String> lowMedNames = medicinesList.stream()
            .filter(m -> m.getStock() < 7)
            .map(Medicines::getName)
            .collect(Collectors.toList());

    if (!lowMedNames.isEmpty()) {
        lowStockAvailable = true;
        lowStockMessage = "⚠ Low Stock: " + String.join(", ", lowMedNames);
    } else {
        lowStockAvailable = false;
        lowStockMessage = null;
    }
}

// File upload
private UploadedFile pictureFile;
String uploadPath = FacesContext.getCurrentInstance()
        .getExternalContext()
        .getRealPath("/medicine_images/");

 private final String ABSOLUTE_UPLOAD_PATH =
            "D:/java/yasi/src/main/webapp/medicine_images/";

private String uploadedFileName;   // NEW
//private Part picturePart;

  // --- Load medicines in init() ---
@PostConstruct
public void init() {
    usersList = adminEJB.getAllUsers();
    categoriesList = adminEJB.getAllCategories();
    manufacturersList = adminEJB.getAllManufacturers();
    medicinesList = (List<Medicines>) adminEJB.getAllMedicines(); 
    loadMedicines();
      loadLowStockMedicines();
}
public void loadLowStockMedicines() {

    List<Object[]> list = adminEJB.getLowStockMedicines();

    if (list == null || list.isEmpty()) {
        lowStockAvailable = false;
        return;
    }

    StringBuilder sb = new StringBuilder("⚠ LOW STOCK ALERT ⚠  ");

    for (Object[] row : list) {
        String name = (String) row[0];
        Integer stock = (Integer) row[1];
        sb.append(name)
          .append(" (")
          .append(stock)
          .append(" left) | ");
    }

    lowStockMessage = sb.toString();
    lowStockAvailable = true;
}

    // ---------------- Users ----------------
    public List<Users> getUsersList() {
        return usersList;
    }

    // ---------------- Categories ----------------
    public List<Categories> getCategoriesList() {
        return categoriesList;
    }

    public Categories getSelectedCategory() {
        return selectedCategory;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void openNewCategory() {
        selectedCategory = new Categories();
        editMode = false;
    }
    public void searchCategory(String keyword) {
    categoriesList = adminEJB.searchCategoriesByName(keyword);
}


    public void openEditCategory(Categories c) {
        selectedCategory = c;
        editMode = true;
    }

  public void saveCategory() {

    try {
        InputStream imageStream = null;
        String fileName = null;

        if (categoryImage != null) {
            imageStream = categoryImage.getInputStream();
            fileName = categoryImage.getFileName();
        }

        if (!editMode) {
            adminEJB.addCategory(
                selectedCategory.getName(),
                selectedCategory.getDescription(),
                imageStream,
                fileName
            );
        }
        else {
            adminEJB.updateCategory(
                selectedCategory.getCategoryId(),
                selectedCategory.getName(),
                selectedCategory.getDescription()
            );
        }

        categoriesList = adminEJB.getAllCategories();
        showMessage("Category saved successfully!");
    }
    catch (Exception e) {
        showMessage("Error: " + e.getMessage());
    }
}
public void updateAdminProfile() {
    try {
        Users u = loginBean.getLoggedUser();
        if (u == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "No logged-in user.", null));
            return;
        }

        // Only overwrite fields user edited (temporary fields)
        if (editUsername != null && !editUsername.isBlank()) u.setUsername(editUsername);
        if (editEmail    != null && !editEmail.isBlank())    u.setEmail(editEmail);
        if (editPhone    != null && !editPhone.isBlank())    u.setPhone(editPhone);
        if (editPassword != null && !editPassword.isBlank()) u.setPassword(editPassword);

        // Persist changes (EJB handles partial updates)
        adminEJB.updateUser(u);

        // Refresh session user from DB to get merged state
        Users fresh = adminEJB.getUserById(u.getUserId());
        if (fresh != null) loginBean.setLoggedUser(fresh);

        // Clear temporary edit fields and refresh any lists
        editUsername = editEmail = editPhone = editPassword = null;
        usersList = adminEJB.getAllUsers();

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Profile updated successfully!", null));
    } catch (Exception e) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Update failed: " + e.getMessage(), null));
    }
}
// AdminBean.java (fields)
private String newAdminUsername;
private String newAdminEmail;
private String newAdminPhone;
private String newAdminPassword;
private Integer newAdminRoleId; // choose role id for Admin, or provide a dropdown

    public void deleteCategory(Categories c) {
        try {
            adminEJB.deleteCategory(c.getCategoryId());
            showMessage("Category deleted successfully!");
            categoriesList = adminEJB.getAllCategories(); // Reload
        } catch (Exception e) {
            showMessage("Cannot delete category: " + e.getMessage());
        }
    }

    private void showMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }
    // ... existing getters/setters ...
// ... (rest of Categories getters/setters and methods like getSelectedCategory(), isEditMode(), etc.) ...
    
    // --- New Search Keyword Getter/Setter ---
    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }
    
    // --- New Search Methods ---

    public void searchCategories() {
        try {
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                categoriesList = adminEJB.searchCategoriesByName(searchKeyword);
                showMessage("Found " + categoriesList.size() + " result(s).");
            } else {
                categoriesList = adminEJB.getAllCategories();
                showMessage("Search cleared. Showing all categories.");
            }
        } catch (Exception e) {
            showMessage("Error during search: " + e.getMessage());
        }
    }

    public void clearSearch() {
        searchKeyword = null;
        categoriesList = adminEJB.getAllCategories();
        showMessage("Search cleared. Showing all categories.");
    }

// ---------------- Manufacturers CRUD ----------------

// Get list
public List<Manufacturers> getManufacturersList() {
    return manufacturersList;
}

public Manufacturers getSelectedManufacturer() {
    return selectedManufacturer;
}

public boolean isManufacturerEditMode() {
    return manufacturerEditMode;
}

// Open empty dialog
public void openNewManufacturer() {
    selectedManufacturer = new Manufacturers();
    manufacturerEditMode = false;
}

// Open edit dialog
public void openEditManufacturer(Manufacturers m) {
    selectedManufacturer = m;
    manufacturerEditMode = true;
}

// Save or update
public void saveManufacturer() {
    try {
        if (manufacturerEditMode) {
            adminEJB.updateManufacturer(
                selectedManufacturer.getManufacturerId(),
                selectedManufacturer.getName(),
                selectedManufacturer.getContactInfo()
            );
            showMessage("Manufacturer updated successfully!");
        } else {
            adminEJB.addManufacturer(
                selectedManufacturer.getName(),
                selectedManufacturer.getContactInfo()
            );
            showMessage("Manufacturer added successfully!");
        }

        manufacturersList = adminEJB.getAllManufacturers(); // reload table

    } catch (Exception e) {
        showMessage("Error: " + e.getMessage());
    }
}

// Delete
public void deleteManufacturer(Manufacturers m) {
    try {
        adminEJB.deleteManufacturer(m.getManufacturerId());
        showMessage("Manufacturer deleted successfully!");
        manufacturersList = adminEJB.getAllManufacturers();
    } catch (Exception e) {
        showMessage("Cannot delete manufacturer: " + e.getMessage());
    }
}
public String getManufacturerSearchKeyword() {
    return manufacturerSearchKeyword;
}

public void setManufacturerSearchKeyword(String manufacturerSearchKeyword) {
    this.manufacturerSearchKeyword = manufacturerSearchKeyword;
}

public void searchManufacturers() {
    if (manufacturerSearchKeyword != null && !manufacturerSearchKeyword.trim().isEmpty()) {
        manufacturersList = adminEJB.searchManufacturersByName(manufacturerSearchKeyword.trim());
        showMessage(manufacturersList.size() + " result(s) found.");
    } else {
        manufacturersList = adminEJB.getAllManufacturers();
        showMessage("Showing all manufacturers.");
    }
}

public void clearManufacturerSearch() {
    manufacturerSearchKeyword = null;
    manufacturersList = adminEJB.getAllManufacturers();
    showMessage("Search cleared.");
}



public void openNewMedicine() {
    selectedMedicine = new Medicines();
    medicineEditMode = false;
    selectedCategoryId = null;
    selectedManufacturerId = null;
    pictureFile = null;
}
public void openEditMedicine(Medicines m) {
    selectedMedicine = m;
    medicineEditMode = true;
    selectedCategoryId = (m.getCategoryId() != null) ? m.getCategoryId().getCategoryId() : null;
    selectedManufacturerId = (m.getManufacturerId() != null) ? m.getManufacturerId().getManufacturerId() : null;
}
public void uploadPicture(FileUploadEvent event) {
    try {
        pictureFile = event.getFile();

        String fileName = System.currentTimeMillis() + "_" + pictureFile.getFileName();
        File saveFile = new File(uploadPath + fileName);

        Files.copy(pictureFile.getInputStream(), saveFile.toPath());

        selectedMedicine.setPicture(fileName);

        showMessage("Image uploaded successfully!");

    } catch (Exception e) {
        showMessage("Upload error: " + e.getMessage());
    }
}

 
public void saveMedicine() {
        try {
            // AUTO CREATE FOLDER IF NOT EXISTS
            File folder = new File(ABSOLUTE_UPLOAD_PATH);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // SAVE IMAGE FILE
            if (pictureFile != null) {
                String original = pictureFile.getFileName();
                uploadedFileName = System.currentTimeMillis() + "_" + original;

                File saveFile = new File(ABSOLUTE_UPLOAD_PATH + uploadedFileName);

                try (InputStream is = pictureFile.getInputStream()) {
                    Files.copy(is, saveFile.toPath());
                }
            }

            // NEW MEDICINE
            if (!medicineEditMode) {
                adminEJB.addMedicine(
                        selectedMedicine.getName(),
                        selectedMedicine.getBrand(),
                        selectedMedicine.getPrice(),
                        selectedMedicine.getStock(),
                        selectedMedicine.getExpiryDate(),
                        selectedCategoryId,
                        selectedManufacturerId,
                        selectedMedicine.getPackOf(),
                        selectedMedicine.getDescription(),
                        uploadedFileName
                );
            } else {
                // UPDATE MEDICINE
                adminEJB.updateMedicine(
                        selectedMedicine.getMedicineId(),
                        selectedMedicine.getName(),
                        selectedMedicine.getBrand(),
                        selectedMedicine.getPrice(),
                        selectedMedicine.getStock(),
                        selectedMedicine.getExpiryDate(),
                        selectedCategoryId,
                        selectedManufacturerId,
                        selectedMedicine.getPackOf(),
                        selectedMedicine.getDescription(),
                        uploadedFileName
                );
            }

            medicinesList = (List<Medicines>) adminEJB.getAllMedicines();
            showMessage("Medicine saved successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error saving medicine: " + e.getMessage());
        }
    }
// AdminBean.java (methods)
public void openAddAdmin() {
    // clear form fields
    newAdminUsername = null;
    newAdminEmail = null;
    newAdminPhone = null;
    newAdminPassword = null;

    // set default Admin role id if you like:
     newAdminRoleId = 1; // <-- set to your Admin role id
}

public void addAnotherAdmin() {
    try {
        // basic validation
        if (newAdminUsername == null || newAdminUsername.isBlank()) {
            showMessage("Username required.");
            return;
        }
        if (newAdminEmail == null || newAdminEmail.isBlank()) {
            showMessage("Email required.");
            return;
        }
        if (newAdminPassword == null || newAdminPassword.isBlank()) {
            showMessage("Password required.");
            return;
        }

        // Supply role id; if left null we won't assign role
        adminEJB.addUser(newAdminUsername.trim(),
                         newAdminEmail.trim(),
                         (newAdminPhone == null ? "" : newAdminPhone.trim()),
                         newAdminPassword,
                         newAdminRoleId);

        // refresh users list so UI shows the new admin
        usersList = adminEJB.getAllUsers();

        // clear form
        openAddAdmin();

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Admin added successfully!", null));
    } catch (Exception ex) {
        ex.printStackTrace();
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error adding admin: " + ex.getMessage(), null));
    }
}


public void deleteMedicine(Medicines m) {
    try {
        adminEJB.deleteMedicine(m.getMedicineId());
        showMessage("Medicine deleted successfully!");
        medicinesList = (List<Medicines>) adminEJB.getAllMedicines();

    } catch (Exception e) {
        showMessage("Error: " + e.getMessage());
    }
}

public void searchMedicines() {
    if (medicineSearchKeyword != null && !medicineSearchKeyword.isEmpty()) {
        medicinesList = (List<Medicines>) adminEJB.getMedicineByName(medicineSearchKeyword);
        showMessage(medicinesList.size() + " result(s) found.");
    } else {
        medicinesList = (List<Medicines>) adminEJB.getAllMedicines();
        showMessage("Showing all medicines.");
    }
}

public void clearMedicineSearch() {
    medicineSearchKeyword = null;
    medicinesList = (List<Medicines>) adminEJB.getAllMedicines();
    showMessage("Search cleared.");
}
public List<Medicines> getMedicinesList() { return medicinesList; }
public Medicines getSelectedMedicine() { return selectedMedicine; }
public boolean isMedicineEditMode() { return medicineEditMode; }
public String getMedicineSearchKeyword() { return medicineSearchKeyword; }
public void setMedicineSearchKeyword(String keyword) { this.medicineSearchKeyword = keyword; }

public Integer getSelectedCategoryId() { return selectedCategoryId; }
public void setSelectedCategoryId(Integer id) { this.selectedCategoryId = id; }

public Integer getSelectedManufacturerId() { return selectedManufacturerId; }
public void setSelectedManufacturerId(Integer id) { this.selectedManufacturerId = id; }

public UploadedFile getPictureFile() { return pictureFile; }
public void setPictureFile(UploadedFile f) { this.pictureFile = f; }
public UploadedFile getCategoryImage() { return categoryImage; }
public void setCategoryImage(UploadedFile categoryImage) { this.categoryImage = categoryImage; }
private String previewImagePath;

public String getPreviewImagePath() {
    return previewImagePath;
}

public void setPreviewImagePath(String previewImagePath) {
    this.previewImagePath = previewImagePath;
}public String getEditUsername() { return editUsername; }
public void setEditUsername(String editUsername) { this.editUsername = editUsername; }

public String getEditEmail() { return editEmail; }
public void setEditEmail(String editEmail) { this.editEmail = editEmail; }

public String getEditPhone() { return editPhone; }
public void setEditPhone(String editPhone) { this.editPhone = editPhone; }

public String getEditPassword() { return editPassword; }
public void setEditPassword(String editPassword) { this.editPassword = editPassword; }

public String getNewAdminUsername() { return newAdminUsername; }
public void setNewAdminUsername(String newAdminUsername) { this.newAdminUsername = newAdminUsername; }

public String getNewAdminEmail() { return newAdminEmail; }
public void setNewAdminEmail(String newAdminEmail) { this.newAdminEmail = newAdminEmail; }

public String getNewAdminPhone() { return newAdminPhone; }
public void setNewAdminPhone(String newAdminPhone) { this.newAdminPhone = newAdminPhone; }

public String getNewAdminPassword() { return newAdminPassword; }
public void setNewAdminPassword(String newAdminPassword) { this.newAdminPassword = newAdminPassword; }

public Integer getNewAdminRoleId() { return newAdminRoleId; }
public void setNewAdminRoleId(Integer newAdminRoleId) { this.newAdminRoleId = newAdminRoleId; }

}   