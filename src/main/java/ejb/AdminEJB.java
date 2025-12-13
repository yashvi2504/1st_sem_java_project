
package ejb;

import entity.Categories;
import entity.Manufacturers;
import entity.Medicines;
import entity.Orders;
import entity.Roles;
import entity.Users;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Stateless
@DeclareRoles({"Admin", "Customer", "Delivery"})
//@RolesAllowed("Admin")   // ⭐ ALL methods require ADMIN
public class AdminEJB implements AdminEJBLocal {

    @PersistenceContext(unitName = "pharmacyPU")
    private EntityManager em;

    private final String IMAGE_UPLOAD_DIR = "/path/to/upload/medicines/"; // set your path


@Override
public void addCategory(String name, String description, InputStream imageStream, String fileName) {

    Categories c = new Categories();
    c.setName(name);
    c.setDescription(description);

//    String srcFolder  = "D:/java/logic_pharmacy/src/main/webapp/category_images/";
String srcFolder  = "D:/java/yasi/src/main/webapp/resources/category_images/";
String targetFolder = "D:/java/yasi/target/logic_pharmacy-1.0-SNAPSHOT/resources/category_images/";

//    String targetFolder = "D:/java/logic_pharmacy/target/logic_pharmacy-1.0-SNAPSHOT/category_images/";

    if (imageStream != null && fileName != null) {
        try {
            String finalName = System.currentTimeMillis() + "_" + fileName;

            // Make sure folders exist
            new File(srcFolder).mkdirs();
            new File(targetFolder).mkdirs();

            // Save in src folder
            File srcFile = new File(srcFolder + finalName);
            Files.copy(imageStream, srcFile.toPath());

            // Copy to target folder
            File targetFile = new File(targetFolder + finalName);
            Files.copy(srcFile.toPath(), targetFile.toPath());

            c.setImage(finalName);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    em.persist(c);
}
public List<Object[]> getLowStockMedicines() {
    return em.createQuery(
        "SELECT m.name, m.stock FROM Medicines m WHERE m.stock < 3",
        Object[].class
    ).getResultList();
}



    @Override
    public List<Object[]> getOrderCountPerDay() {
        return em.createQuery(
            "SELECT FUNCTION('DAYNAME', o.orderDate), COUNT(o) " +
            "FROM Orders o " +
            "GROUP BY FUNCTION('DAYNAME', o.orderDate)",
            Object[].class
        ).getResultList();
    }

private final String CATEGORY_IMG_DIR =
        "D:/java/yasi/target/logic_pharmacy-1.0-SNAPSHOT/category_images/";

private String saveCategoryImage(InputStream uploadedStream, String fileName) {
    try {
        File folder = new File(CATEGORY_IMG_DIR);
        if (!folder.exists()) {
            folder.mkdirs(); // create folder if missing
        }

        String fullPath = CATEGORY_IMG_DIR + fileName;
        FileOutputStream fos = new FileOutputStream(fullPath);

        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = uploadedStream.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }

        fos.close();
        uploadedStream.close();

        return fileName;

    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}


    @Override
//    @RolesAllowed("Admin") 
    public void updateCategory(Integer categoryId, String name, String description) {
        Categories category = em.find(Categories.class, categoryId);
        if (category != null) {
            category.setName(name);
            category.setDescription(description);
            em.merge(category);
        }
    }

   @Override
//   @RolesAllowed("Admin") 
public void deleteCategory(Integer categoryId) {
    Categories category = em.find(Categories.class, categoryId);
    if (category == null) {
        return;
    }

    // Check if medicines exist
    if (category.getMedicinesCollection() != null && !category.getMedicinesCollection().isEmpty()) {
        throw new RuntimeException("Cannot delete category: medicines are still assigned!");
    }

    em.remove(category);
}

@Override
public List<Categories> getAllCategories() {
    // If the Categories.class is mapped incorrectly, this query fails silently or returns an empty list.
    List<Categories> cats = em.createQuery("SELECT c FROM Categories c", Categories.class).getResultList();
    System.out.println("DEBUG: Categories fetched from DB: " + cats.size()); // This is what we need to see!
    // ...
    return cats;
}


    // --------- Manufacturer CRUD ---------
    @Override
//    @RolesAllowed("Admin") 
    public void addManufacturer(String name, String contactInfo) {
        Manufacturers manufacturer = new Manufacturers();
        manufacturer.setName(name);
        manufacturer.setContactInfo(contactInfo);
        em.persist(manufacturer);
    }

    @Override
//    @RolesAllowed("Admin") 
    public void updateManufacturer(Integer manufacturerId, String name, String contactInfo) {
        Manufacturers manufacturer = em.find(Manufacturers.class, manufacturerId);
        if (manufacturer != null) {
            manufacturer.setName(name);
            manufacturer.setContactInfo(contactInfo);
            em.merge(manufacturer);
        }
    }

    @Override
//    @RolesAllowed("Admin") 
    public void deleteManufacturer(Integer manufacturerId) {
        Manufacturers manufacturer = em.find(Manufacturers.class, manufacturerId);
        if (manufacturer == null) return;
        Collection<Medicines> medicines = manufacturer.getMedicinesCollection();
        if (medicines != null) {
            for (Medicines m : medicines) {
                m.setManufacturerId(null); // Remove the foreign key
                em.merge(m);
            }
        }

        em.remove(manufacturer);
    }
@Override
public List<Categories> searchCategoriesByName(String name) {
    return em.createQuery("SELECT c FROM Categories c WHERE LOWER(c.name) LIKE :name", Categories.class)
             .setParameter("name", "%" + name.toLowerCase() + "%")
             .getResultList();
}
@Override
public List<Manufacturers> searchManufacturersByName(String name) {
    return em.createQuery("SELECT m FROM Manufacturers m WHERE LOWER(m.name) LIKE :name", Manufacturers.class)
             .setParameter("name", "%" + name.toLowerCase() + "%")
             .getResultList();
}

    @Override
//    @RolesAllowed("Admin") 
    public List<Manufacturers> getAllManufacturers() {
        return em.createNamedQuery("Manufacturers.findAll", Manufacturers.class)
                 .getResultList();
    }
    
    @Override
//    @RolesAllowed("Admin") 
public void addMedicine(String name, String brand, BigDecimal price, int stock, 
                        LocalDate expiryDate, Integer categoryId, Integer manufacturerId,
                        Integer packOf, String description, String picture) {

    
    Categories category = em.find(Categories.class, categoryId);
    Manufacturers manufacturer = em.find(Manufacturers.class, manufacturerId);

    Medicines m = new Medicines();
    m.setName(name);
    m.setBrand(brand);
    m.setPrice(price);          
    m.setStock(stock);
    m.setExpiryDate(expiryDate); 
    m.setPackOf(packOf);       
    m.setDescription(description);
    m.setPicture(picture);

    m.setCategoryId(category);
    m.setManufacturerId(manufacturer);

    Collection<Medicines> medicines = category.getMedicinesCollection();
    medicines.add(m);
    category.setMedicinesCollection(medicines);
    
Collection<Medicines> manufacturerMedicines = manufacturer.getMedicinesCollection();
manufacturerMedicines.add(m);
manufacturer.setMedicinesCollection(manufacturerMedicines);


    em.persist(m);
    em.merge(manufacturer);
    em.merge(category);
}

    @Override
//    @RolesAllowed("Admin") 
public void updateMedicine(Integer medicineId, String name, String brand, BigDecimal price, int stock,
                           LocalDate expiryDate, Integer categoryId, Integer manufacturerId,
                           Integer packOf, String description, String picture) {

    Medicines m = em.find(Medicines.class, medicineId);
    if (m == null) {
        throw new RuntimeException("Medicine not found with ID: " + medicineId);
    }

    m.setName(name);
    m.setBrand(brand);
    m.setPrice(price);
    m.setStock(stock);
    m.setExpiryDate(expiryDate);
    m.setPackOf(packOf);
    m.setDescription(description);
    
    if (picture != null && !picture.isEmpty()) {
        m.setPicture(picture);
    }

    if (categoryId != null) {
        Categories c = em.find(Categories.class, categoryId);
        m.setCategoryId(c);
    } else {
        m.setCategoryId(null);
    }

    if (manufacturerId != null) {
        Manufacturers manu = em.find(Manufacturers.class, manufacturerId);
        m.setManufacturerId(manu);
    } else {
        m.setManufacturerId(null);
    }

    em.merge(m);
}
    
   @Override
//@RolesAllowed("Admin")
public void deleteMedicine(Integer medicineId) {

    Medicines m = em.find(Medicines.class, medicineId);
    if (m == null) return;

    // Remove from Category
    if (m.getCategoryId() != null) {
        Categories c = m.getCategoryId();
        c.getMedicinesCollection().remove(m);
        m.setCategoryId(null);
        em.merge(c);
    }

    // Remove from Manufacturer
    if (m.getManufacturerId() != null) {
        Manufacturers manu = m.getManufacturerId();
        manu.getMedicinesCollection().remove(m);
        m.setManufacturerId(null);
        em.merge(manu);
    }

    // Now safe to delete
    em.remove(m);
}
@Override
public Users getUserByEmail(String email) {
    try {
        return em.createQuery(
                "SELECT u FROM Users u WHERE u.email = :email", Users.class)
                .setParameter("email", email)
                .getSingleResult();
    } catch (Exception e) {
        return null; // no user found
    }
}

  @Override
//  @RolesAllowed("Admin") 
public Collection<Medicines> getAllMedicines() {
    return em.createNamedQuery("Medicines.findAll").getResultList();
}

@Override
@RolesAllowed("Admin") 
public Medicines getMedicineById(Integer medicineId) {
    Medicines medicine = em.find(Medicines.class, medicineId);
    return medicine;
}

@Override
@RolesAllowed("Admin") 
public Collection<Medicines> getMedicineByName(String name) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByName")
                                        .setParameter("name", name)
                                        .getResultList();
    return medicines;
}
@Override@RolesAllowed("Admin") 
public Collection<Medicines> getMedicinesByCategory(Integer categoryId) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByCategory")
                                        .setParameter("categoryId", categoryId)
                                        .getResultList();
    return medicines;
}
@Override
@RolesAllowed("Admin") 
public Collection<Medicines> getMedicinesByManufacturer(Integer manufacturerId) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findByManufacturer")
                                        .setParameter("manufacturerId", manufacturerId)
                                        .getResultList();
    return medicines;
}

@Override
@RolesAllowed("Admin") 
public void updateMedicineStock(Integer medicineId, int newStock) {
    Medicines medicine = em.find(Medicines.class, medicineId);
        medicine.setStock(newStock);
        em.merge(medicine);
}

@Override
@RolesAllowed("Admin") 
public Collection<Medicines> getLowStockMedicines(int threshold) {
    Collection<Medicines> medicines = em.createNamedQuery("Medicines.findLowStock", Medicines.class)
                                        .setParameter("threshold", threshold)
                                        .getResultList();
    return medicines;
}
    @Override
    @RolesAllowed("Admin") 
    public boolean login(String email, String password) {
        try {
            Long count = em.createQuery(
                "SELECT COUNT(a) FROM Users a WHERE a.email = :e AND a.password = :p",
                Long.class
            )
            .setParameter("e", email)
            .setParameter("p", password)
            .getSingleResult();

            return count == 1;
        } catch (Exception e) {
            return false;
        }
    }
  
    @Override
//    @RolesAllowed("Admin") 
    public List<Users> getAllUsers() {
        return em.createNamedQuery("Users.findAll", Users.class)
                .getResultList();
    }
    @Override
public Users getUserByUsername(String username) {
    try {
        return em.createQuery(
                "SELECT u FROM Users u WHERE u.username = :uname", Users.class)
                .setParameter("uname", username)
                .getSingleResult();
    } catch (Exception e) {
        return null;
    }
}

 public void updateOrderStatus(Orders o) {
    em.merge(o);
}
@Override
public void updateUser(Users updatedUser) {
    Users u = em.find(Users.class, updatedUser.getUserId());
    if (u == null) throw new RuntimeException("User not found!");

    if (updatedUser.getUsername() != null && !updatedUser.getUsername().isBlank()) {
        u.setUsername(updatedUser.getUsername());
    }
    if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
        u.setEmail(updatedUser.getEmail());
    }
    if (updatedUser.getPhone() != null && !updatedUser.getPhone().isBlank()) {
        u.setPhone(updatedUser.getPhone());
    }
    if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
        u.setPassword(updatedUser.getPassword());
    }

    em.merge(u);
}
// AdminEJB.java (inside class AdminEJB)
@Override
public void addUser(String username, String email, String phone, String password, Integer roleId) {
    Users u = new Users();
    u.setUsername(username);
    u.setEmail(email);
    u.setPhone(phone);
    u.setPassword(password);

    Roles r = em.find(Roles.class, roleId);
    if (r == null) {
        throw new RuntimeException("Role not found for ID = " + roleId);
    }

    u.setRoleId(r);

    em.persist(u);
}


@Override
public Users getUserById(Integer userId) {
    return em.find(Users.class, userId);
}




    }