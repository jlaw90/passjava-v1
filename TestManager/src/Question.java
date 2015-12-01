public abstract class Question {
    private static int maxId = -1;
    public final int id;
    private String origQuestion;
    private String question;

    protected Question(int id, String question) {
        this.id = id;
        if(id > maxId)
            maxId = id;
        this.origQuestion = this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public boolean isModified() {
        return !origQuestion.equals(question);
    }

    public void setQuestion(String q) {
        this.question = q;
    }

    public static int getNextId() {
        maxId++;
        return maxId;
    }

    public void save() {
        this.origQuestion = question;
    }

    public String toString() {
        return "Question " + id + (isModified()? "*": "");
    }
}