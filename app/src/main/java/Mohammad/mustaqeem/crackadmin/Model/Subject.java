package Mohammad.mustaqeem.crackadmin.Model;

public class Subject {
    String subjectName,subjectImage,subjectId;

    public Subject(String subjectName, String subjectImage, String subjectId) {
        this.subjectName = subjectName;
        this.subjectImage = subjectImage;
        this.subjectId = subjectId;
    }

    public Subject(){

    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectImage() {
        return subjectImage;
    }

    public void setSubjectImage(String subjectImage) {
        this.subjectImage = subjectImage;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
}
