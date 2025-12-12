//package rest;
//
//import ejb.AdminEJBLocal;
//import entity.Categories;
//import entity.Manufacturers;
//import entity.Medicines;
//import jakarta.annotation.security.DeclareRoles;
//import jakarta.annotation.security.RolesAllowed;
//import jakarta.ejb.EJB;
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Collection;
//import java.util.List;
//
//@Path("/admin")
//@DeclareRoles({"Admin", "Customer", "Delivery"})
//@RolesAllowed("Admin")   // ⭐ ALL endpoints require ADMIN
//public class AdminREST {
//
//    @EJB
//    private AdminEJBLocal adminEJB;
//
//    // --------- Category Endpoints ---------
//    @POST
//    @Path("category/add/{name}/{description}")
//    public void addCategory(@PathParam("name") String name,
//                            @PathParam("description") String description) {
//        adminEJB.addCategory(name, description);
//    }
//
//    @PUT
//    @Path("category/update/{id}/{name}/{description}")
//    public void updateCategory(@PathParam("id") Integer id,
//                               @PathParam("name") String name,
//                               @PathParam("description") String description) {
//        adminEJB.updateCategory(id, name, description);
//    }
//
//   @DELETE
//@Path("deletecategory/{id}")
//public Response deleteCategory(@PathParam("id") Integer categoryId) {
//    try {
//        adminEJB.deleteCategory(categoryId);
//        return Response.ok().build(); // 200 OK
//    } catch (RuntimeException e) {
//        return Response.status(Response.Status.BAD_REQUEST)
//                       .entity("{\"error\":\"" + e.getMessage() + "\"}")
//                       .type(MediaType.APPLICATION_JSON)
//                       .build();
//    }
//}
//
//    @GET
//    @Path("category/all")
//    @Produces(MediaType.APPLICATION_JSON)
//    public List<Categories> getAllCategories() {
//        return adminEJB.getAllCategories();
//    }
//
//    // --------- Manufacturer Endpoints ---------
//    @POST
//    @Path("manufacturer/add/{name}/{contactInfo}")
//    public void addManufacturer(@PathParam("name") String name,
//                                @PathParam("contactInfo") String contactInfo) {
//        adminEJB.addManufacturer(name, contactInfo);
//    }
//
//    @PUT
//    @Path("manufacturer/update/{id}/{name}/{contactInfo}")
//    public void updateManufacturer(@PathParam("id") Integer id,
//                                   @PathParam("name") String name,
//                                   @PathParam("contactInfo") String contactInfo) {
//        adminEJB.updateManufacturer(id, name, contactInfo);
//    }
//
//    @DELETE
//    @Path("manufacturer/delete/{id}")
//    public void deleteManufacturer(@PathParam("id") Integer id) {
//        adminEJB.deleteManufacturer(id);
//    }
//
//    @GET
//    @Path("manufacturer/all")
//    @Produces(MediaType.APPLICATION_JSON)
//    public List<Manufacturers> getAllManufacturers() {
//        return adminEJB.getAllManufacturers();
//    }
//    @POST
//@Path("addmedicine/{name}/{brand}/{price}/{stock}/{expiryDate}/{categoryId}/{manufacturerId}/{packOf}/{description}/{picture}")
//public void addMedicine(
//        @PathParam("name") String name,
//        @PathParam("brand") String brand,
//        @PathParam("price") BigDecimal price,
//        @PathParam("stock") int stock,
//        @PathParam("expiryDate") String expiryDateStr, // will convert to LocalDate
//        @PathParam("categoryId") Integer categoryId,
//        @PathParam("manufacturerId") Integer manufacturerId,
//        @PathParam("packOf") Integer packOf,
//        @PathParam("description") String description,
//        @PathParam("picture") String picture)
//{
//
//    LocalDate expiryDate = LocalDate.parse(expiryDateStr); // convert String to LocalDate
//
//    adminEJB.addMedicine(
//            name, brand, price, stock, expiryDate, categoryId, manufacturerId, packOf, description, picture
//    );
//}
//@PUT
//@Path("updatemedicine/{id}/{name}/{brand}/{price}/{stock}/{expiryDate}/{categoryId}/{manufacturerId}/{packOf}/{description}/{picture}")
//public Response updateMedicine(
//        @PathParam("id") Integer medicineId,
//        @PathParam("name") String name,
//        @PathParam("brand") String brand,
//        @PathParam("price") BigDecimal price,
//        @PathParam("stock") int stock,
//        @PathParam("expiryDate") String expiryDateStr,
//        @PathParam("categoryId") Integer categoryId,
//        @PathParam("manufacturerId") Integer manufacturerId,
//        @PathParam("packOf") Integer packOf,
//        @PathParam("description") String description,
//        @PathParam("picture") String picture
//) {
//    try {
//        LocalDate expiryDate = LocalDate.parse(expiryDateStr);
//
//        adminEJB.updateMedicine(
//            medicineId, name, brand, price, stock, expiryDate,
//            categoryId, manufacturerId, packOf, description, picture
//        );
//
//        return Response.ok("{\"message\":\"Medicine updated successfully\"}").build();
//    } catch (Exception e) {
//        e.printStackTrace();
//        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                       .entity("{\"error\":\"" + e.getMessage() + "\"}")
//                       .build();
//    }
//}
//   @DELETE
//    @Path("deletemedicine/{id}")
//    public void deleteMedicine(@PathParam("id") Integer medicineId) {
//        adminEJB.deleteMedicine(medicineId);
//    }
//  @GET
//@Path("findallmedicines")
//@Produces(MediaType.APPLICATION_JSON)
//public Collection<Medicines> getAllMedicines() {
//    return adminEJB.getAllMedicines();
//}
//
//@GET
//@Path("findmedicinebyid/{medicineId}")
//@Produces(MediaType.APPLICATION_JSON)
//public Medicines findMedicineById(@PathParam("medicineId") Integer medicineId) {
//    return adminEJB.getMedicineById(medicineId);
//}
//
//@GET
//@Path("findmedicinebyname/{name}")
//@Produces(MediaType.APPLICATION_JSON)
//public Collection<Medicines> findMedicineByName(@PathParam("name") String name) {
//    return adminEJB.getMedicineByName(name);
//}
//@GET
//@Path("findmedicinesbycategory/{categoryId}")
//@Produces(MediaType.APPLICATION_JSON)
//public Collection<Medicines> findMedicinesByCategory(@PathParam("categoryId") Integer categoryId) {
//    return adminEJB.getMedicinesByCategory(categoryId);
//}
//@GET
//@Path("findmedicinesbymanufacturer/{manufacturerId}")
//@Produces(MediaType.APPLICATION_JSON)
//public Collection<Medicines> findMedicinesByManufacturer(@PathParam("manufacturerId") Integer manufacturerId) {
//    return adminEJB.getMedicinesByManufacturer(manufacturerId);
//}
//@PUT
//@Path("updatemedicinestock/{medicineId}/{newStock}")
//@Produces(MediaType.APPLICATION_JSON)
//public void updateMedicineStock(
//        @PathParam("medicineId") Integer medicineId,
//        @PathParam("newStock") int newStock) {
//    adminEJB.updateMedicineStock(medicineId, newStock);
//}
//
//@GET
//@Path("lowstockmedicines/{threshold}")
//@Produces(MediaType.APPLICATION_JSON)
//public Collection<Medicines> getLowStockMedicines(@PathParam("threshold") int threshold) {
//    return adminEJB.getLowStockMedicines(threshold);
//}   
//
//}
