package Mohammad.mustaqeem.crackadmin.Model;

public class AddSubCategoryModel {
    String subCategoryName,subCategoryId;

    public AddSubCategoryModel(String subCategoryName, String subCategoryId) {
        this.subCategoryName = subCategoryName;
        this.subCategoryId = subCategoryId;
    }

    public AddSubCategoryModel(){

    }
    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(String subCategoryId) {
        this.subCategoryId = subCategoryId;
    }
}
