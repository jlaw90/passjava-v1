import java.util.*;

public class ChoiceQuestion extends Question {
    private List<ChoiceQuestionOption> options;
    private List<ChoiceQuestionOption> origOptions;
    private String originalCorrectText;
    private String correctText;

    public ChoiceQuestion(int id, String question, String correctText, ChoiceQuestionOption... options) {
        super(id, question);
        this.origOptions = new ArrayList<>();
        Collections.addAll(origOptions, options);
        this.options = new LinkedList<>();
        this.options.addAll(origOptions);
        this.originalCorrectText = this.correctText = correctText;
    }

    public String getCorrectText() {
        return correctText;
    }

    public void setCorrectText(String t) {
        this.correctText = t;
    }

    public List<ChoiceQuestionOption> getAddedOptions() {
        List<ChoiceQuestionOption> diff = new ArrayList<>();
        diff.addAll(options);
        diff.removeAll(origOptions);
        return Collections.unmodifiableList(diff);
    }

    public List<ChoiceQuestionOption> getRemovedOptions() {
        List<ChoiceQuestionOption> diff = new ArrayList<>();
        diff.addAll(origOptions);
        diff.removeAll(options);
        return Collections.unmodifiableList(diff);
    }

    public List<ChoiceQuestionOption> getModifiedOptions() {
        List<ChoiceQuestionOption> diff = new ArrayList<>();
        for(ChoiceQuestionOption cqo: origOptions)
            if(cqo.isModified())
                diff.add(cqo);
        return Collections.unmodifiableList(diff);
    }

    public List<ChoiceQuestionOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void addOption(ChoiceQuestionOption opt) {
        options.add(opt);
    }

    public void removeOption(ChoiceQuestionOption opt) {
        options.remove(opt);
    }

    public boolean isModified() {
        return super.isModified() || !originalCorrectText.equals(correctText);
    }

    public boolean optionsModified() {
        return origOptions.size() != options.size() || !options.containsAll(origOptions) || getModifiedOptions().size() != 0;
    }

    public void save() {
        super.save();
        this.originalCorrectText = correctText;
    }

    public void saveOptions() {
        this.origOptions.clear();
        this.origOptions.addAll(options);
    }
}