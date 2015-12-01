import java.util.ArrayList;
import java.util.List;

public class CompilableQuestionProcessor {
    public static List<CompilableQuestionSegment> process(String src) {
        boolean visible = false;
        boolean editable = false;

        List<CompilableQuestionSegment> segs = new ArrayList<>();

        int start;
        int end = 0;
        while ((start = src.indexOf("/*", end)) != -1) {
            end = src.indexOf("*/", start + 2);
            if(end == -1)
                break;

            String part = src.substring(start + 2, end);
            if(part.length() != 2 || (part.charAt(0) != 'V' && part.charAt(0) != 'E') ||
                    (part.charAt(1) != '+' && part.charAt(1) != '-'))
                continue; // Just a comment, ignore

            if(start != 0) {
                segs.add(new CompilableQuestionSegment(src.substring(0, start), visible, editable));
            }

            if (part.startsWith("V"))
                visible = part.substring(1, 2).equals("+");
            else if (part.startsWith("E"))
                editable = part.substring(1, 2).equals("+");
            src = src.substring(end + 2);
            end = 0;
        }

        if(src.length() > 0)
            segs.add(new CompilableQuestionSegment(src, visible, editable));


        return segs;
    }

    public static String process(List<CompilableQuestionSegment> segments) {
        boolean visible = false;
        boolean editable = false;

        StringBuilder sb = new StringBuilder();

        for(CompilableQuestionSegment seg: segments) {
            if(seg.isVisible() != visible) {
                sb.append("/*V").append(seg.isVisible()? "+": "-").append("*/");
                visible = seg.isVisible();
            }
            if(seg.isEditable() != editable) {
                sb.append("/*E").append(seg.isEditable()? "+": "-").append("*/");
                editable = seg.isEditable();
            }
            sb.append(seg.getData());
        }

        return sb.toString();
    }
}