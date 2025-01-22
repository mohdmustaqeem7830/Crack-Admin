package Mohammad.mustaqeem.crackadmin.Model;

public class AddCategoryModel {
    private String categoryName,categoryImage,catId,categorySubCategory;

    public String getCategorySubCategory() {
        return categorySubCategory;
    }

    public void setCategorySubCategory(String categorySubCategory) {
        this.categorySubCategory = categorySubCategory;
    }

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public AddCategoryModel(String categoryName, String categoryImage) {
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
    }


    public AddCategoryModel(String categoryId, String categoryName, String categoryImage ,String categorySubCategory) {
        this.catId = categoryId;
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
        this.categorySubCategory=categorySubCategory;
    }

    public AddCategoryModel(){

    }

    public String getcategoryName() {
        return categoryName;
    }

    public void setcategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getcategoryImage() {
        return categoryImage;
    }

    public void setcategoryImage(String categoryImage) {
        this.categoryImage = categoryImage;
    }
}
