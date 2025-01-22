package Mohammad.mustaqeem.crackadmin.Model;

public class AddPDFModel {
    String pdfName,pdfId,pdfSubName,pdfUrl;

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public AddPDFModel(String pdfName, String pdfId, String pdfSubName,String pdfUrl) {
        this.pdfName = pdfName;
        this.pdfId = pdfId;
        this.pdfSubName = pdfSubName;
        this.pdfUrl = pdfUrl;

    }
    public AddPDFModel(){

    }
    public String getPdfName() {
        return pdfName;
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
    }

    public String getPdfId() {
        return pdfId;
    }

    public void setPdfId(String pdfId) {
        this.pdfId = pdfId;
    }

    public String getPdfSubName() {
        return pdfSubName;
    }

    public void setPdfSubName(String pdfSubName) {
        this.pdfSubName = pdfSubName;
    }
}
