import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public class WebInterface {
    private static final String globalUrl = "http://cakenet.net/applet/";
    private static final String managerUrl = globalUrl + "managercafebabe/";
    private static final Map<String, String> query = new HashMap<String, String>();
    private static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private static byte[] buf = new byte[5000];

    public static synchronized String query(String url, Map<String, String> query) {
        try {
            url = managerUrl + url + buildQuery(query);
            System.out.println(url);
            URL u = new URL(url);
            URLConnection con = u.openConnection();
            InputStream is = con.getInputStream();
            baos.reset();
            int read;
            while ((read = is.read(buf, 0, buf.length)) != -1)
                baos.write(buf, 0, read);

            is.close();
            return new String(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildQuery(Map<String, String> map) {
        if(map == null)
            map = new HashMap<>();
        map.putAll(query);
        StringBuilder sb = new StringBuilder();
        String part = "?";
        for(String key: map.keySet()) {
            try {
                sb.append(part).append(key).append("=").append(URLEncoder.encode(map.get(key), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            part = "&";
        }
        return sb.toString();
    }

    public static Map<String,String> create(Object... args) {
        if(args.length % 2 != 0)
            throw new InvalidParameterException();
        Map<String, String> map = new HashMap<>();
        for(int i = 0; i < args.length; i+= 2) {
            map.put(String.valueOf(args[i]), String.valueOf(args[i+1]));
        }
        return map;
    }

    private WebInterface() {}

    static {
        query.put("pass", "ka8s7dhsb._0");
    }
}