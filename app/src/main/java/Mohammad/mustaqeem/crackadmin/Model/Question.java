package Mohammad.mustaqeem.crackadmin.Model;

public class Question {

    private String question, option1, option2, option3 ,option4, answer,qImage,qtype,qId,solution,solutionImage;


    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getSolutionImage() {
        return solutionImage;
    }

    public void setSolutionImage(String solutionImage) {
        this.solutionImage = solutionImage;
    }

    public String getQtype() {
        return qtype;
    }

    public String getqId() {
        return qId;
    }

    public void setqId(String qId) {
        this.qId = qId;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }


    public String getqImage() {
        return qImage;
    }

    public void setqImage(String qImage) {
        this.qImage = qImage;
    }

    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Question(){

    }
    //MCQ
    public Question(String question, String option1, String option2, String option3, String option4, String answer, int index, String qImage, String qtype,String solution,String solutionImage) {
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.answer = answer;
        this.index = index;
        this.qImage = qImage;
        this.qtype = qtype;
        this.solution = solution;
        this.solutionImage = solutionImage;
    }
    public Question(String question, String answer, int index, String qImage, String qtype,String solution,String solutionImage) {
        this.question = question;
        this.answer = answer;
        this.index = index;
        this.qImage = qImage;
        this.qtype = qtype;
        this.solution = solution;
        this.solutionImage = solutionImage;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
