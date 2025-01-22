package Mohammad.mustaqeem.crackadmin.Model;

public class AddQuestionPaperModel {

    String qpImage, qpName, qpSubTitle, categoryName, qpId, subCategoryName, studyCategory, time, correctMarks, negativeMarks, totalQuestion, status,plan;

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public AddQuestionPaperModel(String qpImage, String qpName, String qpSubTitle, String categoryName, String qpId, String subCategoryName, String studyCategory, String time, String correctMarks, String negativeMarks, String totalQuestion, String status, String plan) {
        this.qpImage = qpImage;
        this.qpName = qpName;
        this.qpSubTitle = qpSubTitle;
        this.categoryName = categoryName;
        this.qpId = qpId;
        this.subCategoryName = subCategoryName;
        this.studyCategory = studyCategory;
        this.time = time;
        this.correctMarks = correctMarks;
        this.negativeMarks = negativeMarks;
        this.totalQuestion = totalQuestion;
        this.status = status;
        this.plan = plan;

    }

    public AddQuestionPaperModel() {

    }

    public String getTotalQuestion() {
        return totalQuestion;
    }

    public void setTotalQuestion(String totalQuestion) {
        this.totalQuestion = totalQuestion;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCorrectMarks() {
        return correctMarks;
    }

    public void setCorrectMarks(String correctMarks) {
        this.correctMarks = correctMarks;
    }

    public String getNegativeMarks() {
        return negativeMarks;
    }

    public void setNegativeMarks(String negativeMarks) {
        this.negativeMarks = negativeMarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQpImage() {
        return qpImage;
    }

    public void setQpImage(String qpImage) {
        this.qpImage = qpImage;
    }

    public String getQpName() {
        return qpName;
    }

    public void setQpName(String qpName) {
        this.qpName = qpName;
    }

    public String getQpSubTitle() {
        return qpSubTitle;
    }

    public void setQpSubTitle(String qpSubTitle) {
        this.qpSubTitle = qpSubTitle;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getQpId() {
        return qpId;
    }

    public void setQpId(String qpId) {
        this.qpId = qpId;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getStudyCategory() {
        return studyCategory;
    }

    public void setStudyCategory(String studyCategory) {
        this.studyCategory = studyCategory;
    }
}
