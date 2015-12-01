import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Test {
    private static int maxId;

    public final int id;
    private String origName;
    private String name;
    private int origNumQuestions = 0;
    private int numQuestions;
    private List<Question> origQuestions = new ArrayList<>();
    private List<Question> questions = new ArrayList<>();

    public Test(int id) {
        this.id = id;
        if(id > maxId)
            maxId = id;
        this.name = "Test " + id;
        this.numQuestions = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuestionCount() {
        return numQuestions;
    }

    public void setQuestions(int questions) {
        numQuestions = questions;
    }

    public void addQuestion(Question q) {
        questions.add(q);
    }

    public void removeQuestion(Question q) {
        questions.remove(q);
    }

    public boolean isModified() {
        return !name.equals(origName) || numQuestions != origNumQuestions;
    }

    public boolean questionsModified() {
        return origQuestions.size() != questions.size() || !questions.containsAll(origQuestions) || getModifiedQuestions().size() != 0;
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public List<Question> getAddedQuestions() {
        List<Question> diff = new ArrayList<>();
        diff.addAll(questions);
        diff.removeAll(origQuestions);
        return Collections.unmodifiableList(diff);
    }

    public List<Question> getRemovedQuestions() {
        List<Question> diff = new ArrayList<>();
        diff.addAll(origQuestions);
        diff.removeAll(questions);
        return Collections.unmodifiableList(diff);
    }

    public List<Question> getModifiedQuestions() {
        List<Question> diff = new ArrayList<>();
        for (Question q : origQuestions)
            if (q.isModified())
                diff.add(q);
        return Collections.unmodifiableList(diff);
    }

    public static int getId() {
        return ++maxId;
    }

    public void save() {
        this.origName = name;
        this.origNumQuestions = numQuestions;
    }

    public void saveQuestions() {
        this.origQuestions.clear();
        this.origQuestions.addAll(questions);
    }

    public void loadTest() {
        // Load the test
        String data = WebInterface.query("loadtest.php", WebInterface.create("id", id));
        if (data.startsWith("ERROR"))
            throw new RuntimeException("failed to load test with specified ID");

        String[] parts = data.split(Character.toString((char) 0x00));

        origName = name = parts[0];
        origNumQuestions = numQuestions = Integer.parseInt(parts[1]);

        int q = Integer.parseInt(parts[2]);

        questions = new ArrayList<>(Integer.parseInt(parts[2]));

        int off = 3;
        for (int i = 0; i < q; i++) {
            int qId = Integer.parseInt(parts[off++]);
            String qtype = parts[off++];
            String qq = parts[off++];

            if (qtype.equals("singlechoice") || qtype.equals("multichoice")) {
                String correctMessage = parts[off++];
                int opts = Integer.parseInt(parts[off++]);

                ChoiceQuestionOption[] options = new ChoiceQuestionOption[opts];

                for (int j = 0; j < opts; j++) {
                    int id = Integer.parseInt(parts[off]);
                    boolean correct = Boolean.parseBoolean(parts[off + 2]);
                    options[j] = new ChoiceQuestionOption(id, parts[off + 1], correct, correct ? "" : parts[off + 3]);
                    off += 3;
                    if (!correct)
                        off++;
                }

                questions.add(new ChoiceQuestion(qId, qq, correctMessage, options));
            } else if(qtype.equals("compilable")) {
                int segCount = Integer.parseInt(parts[off++]);
                CompilableQuestionSegment[] segments = new CompilableQuestionSegment[segCount];

                for(int j = 0; j < segCount; j++) {
                    String segData = parts[off++];
                    boolean vis = Boolean.parseBoolean(parts[off++]);
                    boolean edi = Boolean.parseBoolean(parts[off++]);
                    segments[j] = new CompilableQuestionSegment(segData, vis, edi);
                }
                questions.add(new CompilableQuestion(qId, qq, segments));
            }
        }

        origQuestions.addAll(questions);
    }
}