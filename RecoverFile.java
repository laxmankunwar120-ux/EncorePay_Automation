import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class RecoverFile {
    public static void main(String[] args) throws Exception {
        String logDir = "C:\\Users\\PRO BOOK\\.gemini\\antigravity\\brain\\2a3452b4-049a-405c-b5aa-2d75f601149d\\.system_generated\\logs\\transcript_full.jsonl";
        List<String> lines = Files.readAllLines(Paths.get(logDir));
        
        Map<Integer, String> recoveredLines = new TreeMap<>();
        Pattern p = Pattern.compile("^(\\d+): (.*)");
        
        for (String line : lines) {
            // Find view_file outputs
            if (line.contains("The following code has been modified to include a line number")) {
                // Parse the JSON string (simplified, just extract \n separated parts if possible, but it's JSONL)
                // A better approach is to unescape the JSON string, but we can just use regex on the raw line.
                // Actually, the log line is a JSON object.
                // Let's just find all occurrences of "1: package com.encorepay.pages;" up to "1192: }"
                // Since it's JSON, it has \n and \r escaped.
                String content = line;
                // replace \\n with actual newline
                content = content.replace("\\n", "\n").replace("\\r", "\r");
                
                String[] parts = content.split("\n");
                for (String part : parts) {
                    Matcher m = p.matcher(part);
                    if (m.find()) {
                        int lineNum = Integer.parseInt(m.group(1));
                        String code = m.group(2);
                        // unescape json quotes if any? no, java file doesn't have many \" at the start of lines, 
                        // but wait, if it's JSON, the whole string was quoted, so \" would be present.
                        code = code.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\t", "\t");
                        if (!recoveredLines.containsKey(lineNum)) {
                            recoveredLines.put(lineNum, code);
                        }
                    }
                }
            }
        }
        
        System.out.println("Recovered " + recoveredLines.size() + " lines.");
        
        List<String> out = new ArrayList<>();
        int maxLine = 0;
        for (int k : recoveredLines.keySet()) {
            if (k > maxLine) maxLine = k;
        }
        
        for (int i = 1; i <= maxLine; i++) {
            if (recoveredLines.containsKey(i)) {
                out.add(recoveredLines.get(i));
            } else {
                out.add("// MISSING LINE " + i);
            }
        }
        
        Files.write(Paths.get("src/main/java/com/encorepay/pages/AdminJobsPage.java"), out);
    }
}
