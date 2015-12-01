import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CompilableQuestion extends Question {
    private List<CompilableQuestionSegment> origSegments;
    private List<CompilableQuestionSegment> segments;

    protected CompilableQuestion(int id, String question, CompilableQuestionSegment... segments) {
        super(id, question);
        this.segments = new ArrayList<>();
        Collections.addAll(this.segments, segments);
        this.origSegments = new ArrayList<>();
        origSegments.addAll(this.segments);
    }

    public boolean segmentsModified() {
        return origSegments.size() != segments.size() || !segments.containsAll(origSegments);
    }

    public List<CompilableQuestionSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public void setSegments(List<CompilableQuestionSegment> segs) {
        segments.clear();
        segments.addAll(segs);
    }

    public void saveSegments() {
        origSegments.clear();
        origSegments.addAll(segments);
    }
}