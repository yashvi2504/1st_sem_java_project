package ejb;

import entity.Categories;
import entity.Manufacturers;
import entity.Medicines;
import entity.Orders;
import entity.Users;
import jakarta.ejb.Local;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Local
public interface AdminEJBLocal {
        boolean login(String email, String password);
            List<Users> getAllUsers();
            // AdminEJBLocal.java (add this method declaration)
void addUser(String username, String email, String phone, String password, Integer roleId);
List<Orders> getAllOrders();

    BigDecimal getWeeklyIncome();   // ✅ THIS LINE IS REQUIRED
List<Object[]> getMostSoldMedicine();

    // Dashboard chart
    List<Object[]> getOrderCountPerDay();
    List<Object[]> getLowStockMedicines();

//    void addCategory(String name, String description);
//         void addCategory(String name, String description, InputStream imageStream, String fileName);
//   void addCategory(String name, String description);
void addCategory(String name, String description, InputStream imageStream, String fileName);

    void updateCategory(Integer categoryId, String name, String description);
    void deleteCategory(Integer categoryId);
    List<Categories> getAllCategories();
List<Categories> searchCategoriesByName(String name);

    void addManufacturer(String name, String contactInfo);
    void updateManufacturer(Integer manufacturerId, String name, String contactInfo);
    void deleteManufacturer(Integer manufacturerId);
    List<Manufacturers> getAllManufacturers();
    
    List<Manufacturers> searchManufacturersByName(String name);

    Users getUserByEmail(String email);

    
    Users getUserByUsername(String username);

 void addMedicine(String name, String brand, BigDecimal price, int stock,
                     LocalDate expiryDate, Integer categoryId, Integer manufacturerId,
                     Integer packOf, String description, String picture);

void updateMedicine(Integer medicineId, String name, String brand, BigDecimal price, int stock,
                        LocalDate expiryDate, Integer categoryId, Integer manufacturerId,
                        Integer packOf, String description, String picture);

void deleteMedicine(Integer medicineId);

Medicines getMedicineById(Integer medicineId);
Collection<Medicines> getAllMedicines();

  Collection<Medicines> getMedicineByName(String name);
  Collection<Medicines> getMedicinesByCategory(Integer categoryId);
Collection<Medicines> getMedicinesByManufacturer(Integer manufacturerId);
void updateMedicineStock(Integer medicineId, int newStock);
Collection<Medicines> getLowStockMedicines(int threshold);//all medicines whose stock < n
void updateUser(Users updatedUser);
Users getUserById(Integer userId);


}
