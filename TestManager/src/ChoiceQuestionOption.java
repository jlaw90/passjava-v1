public class ChoiceQuestionOption {
    private static int maxId = -1;

    public final int id;

    private String origOption;
    private String origWrong;
    private boolean origCorrect;

    private String option;
    private String wrongText;
    private boolean correct;

    public ChoiceQuestionOption(int id, String option, boolean correct, String wrongText) {
        if(id > maxId)
            maxId = id;
        this.id = id;
        origOption = this.option = option;
        origCorrect = this.correct = correct;
        origWrong = this.wrongText = wrongText;
    }

    public String getOption() {
        return option;
    }

    public String getWrongText() {
        return wrongText;
    }

    public boolean correct() {
        return correct;
    }

    public boolean isModified() {
        return !origOption.equals(option) || origCorrect != correct || !origWrong.equals(wrongText);
    }

    public void setOption(String option) {
        this.option = option;
    }

    public void setWrongText(String wrongText) {
        this.wrongText = wrongText;
    }

    public void setCorrect(boolean b) {
        this.correct = b;
    }

    public String toString() {
        return option + (isModified()? "*" : "");
    }

    public void save() {
        this.origOption = option;
        this.origCorrect = correct;
        this.origWrong = wrongText;
    }

    public static int getId() {
        return ++maxId;
    }
}