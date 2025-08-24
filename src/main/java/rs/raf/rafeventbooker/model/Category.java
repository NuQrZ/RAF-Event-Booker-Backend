package rs.raf.rafeventbooker.model;

public class Category {
    private Integer categoryID;
    private String categoryname;
    private String categoryDescription;

    public Category() {

    }

    public Category(Integer categoryID, String categoryname, String categoryDescription) {
        this.categoryID = categoryID;
        this.categoryname = categoryname;
        this.categoryDescription = categoryDescription;
    }

    public Integer getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(Integer categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryname() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }
}
