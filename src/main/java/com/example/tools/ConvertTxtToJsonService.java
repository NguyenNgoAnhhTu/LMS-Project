package com.example.tools;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ConvertTxtToJsonService {
    public String extractFileName(String content) {
        // Regular expression to find "CODE: " followed by the file name
        Pattern pattern = Pattern.compile("CODE:\\s*([\\w.\\-]+)");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            // Return the group containing the file name
            return matcher.group(1);
        }

        // Return null if no match is found
        return null;
    }

    public static List<Object> processFileContent(String fileContent) {
        // This list will store all multiple-choice questions after processing
        List<Object> mcQuestions = new ArrayList<>();

        // Split file content into lines
        String[] lines = fileContent.split("\n");

        // Flag to detect the very first question
        boolean firstQuestionDetected = false;
        // Track the current question number
        int currentQuestionNumber = 0;

        // Temporary buffer for the question text
        StringBuilder questionText = new StringBuilder();
        // Temporary list of answers for the current question
        List<String> answers = new ArrayList<>();

        // Iterate from the 11th line onward (skipping the first 10 lines)
        for (int i = 10; i < lines.length; i++) {
            try {
                // Remove "\r" just in case it appears
                String lineContent = lines[i].replace("\r", "");

                // For debugging or inspection
//             System.out.println("Line " + (i + 1) + ": " + lineContent.replace("\t", "\\t"));

                // If we have not detected the very first question yet
                if (!firstQuestionDetected) {
                    // Try to detect something like "1.\t" or "1\t" or "1.  \t"
                    if (lineContent.matches("^\\s*1\\.?\\s*\\t+(.*)")) {
                        firstQuestionDetected = true;
                    } else {
                        // Skip lines until we detect the first question
                        continue;
                    }
                }

                // 1) Check if this line starts a new question (e.g., "2.\tQuestion text")
                if (isNewQuestionLine(lineContent, currentQuestionNumber)) {
//                System.out.println("New question line: " + lineContent);
                    handleQuestionLine(
                            lineContent,
                            mcQuestions,
                            questionText,
                            answers,
                            /* currentQuestionNumber */ currentQuestionNumber
                    );

                    // If handleQuestionLine(...) confirmed or updated the question number,
                    // update our local variable. We get the new question number back from it.
                    currentQuestionNumber = getQuestionNumber(lineContent, currentQuestionNumber);

                    // 2) Check if the line is an answer choice (e.g. "A. ", "B.\t", "C. <text>")
                } else if (isAnswerLine(lineContent)) {
                    handleAnswerLine(lineContent, answers, questionText);

                    // 3) Otherwise, treat it as a continuation of the previous text
                } else {
                    lineContent = lineContent.replaceFirst("^\t", "");

                    // If we encounter a termination mark, we break
                    if (Objects.equals(lineContent.trim(), "-- THE END --")) {
                        break;
                    }

                    if (Objects.equals(lineContent.trim(), "--- End of Exam ---")) {
                        break;
                    }

                    appendContinuationLine(lineContent, questionText, answers);
                }
            } catch ( Exception e ) {
                throw new RuntimeException("Error while processing file near question " + currentQuestionNumber + ": " + e.getMessage());
            }
        }

        // After finishing all lines, if we have a final question with answers, add it to the list
        if (!questionText.isEmpty() && !answers.isEmpty()) {
            mcQuestions.add(createQuestionAndAnswerObject(questionText.toString(), answers));
        }

        return mcQuestions;
    }

    /**
     * Checks if a line matches the pattern for starting a new question,
     * e.g., "2.\t..." or "10\t..." etc.
     */
    private static boolean isNewQuestionLine(String lineContent, int currentQuestionNumber) {
        // e.g. " ^\\s*\\d+\\.?\\s*\\t+ " means:
        // - optional leading whitespace
        // - one or more digits
        // - optional period
        // - optional whitespace
        // - at least one tab

        return ( lineContent.matches("^\\s*\\d+\\.?\\s*\\t+.*") && (currentQuestionNumber + 1) == getQuestionNumber(lineContent, currentQuestionNumber) );
    }

    /**
     * Extracts and returns the question number from a line that matches new-question format,
     * or returns 'defaultValue' if something goes wrong.
     */
    private static int getQuestionNumber(String lineContent, int defaultValue) {
        Matcher questionMatcher = Pattern.compile("^\\s*(\\d+)\\.?\\s*\\t+(.*)").matcher(lineContent);
        if (questionMatcher.find()) {
            try {
                return Integer.parseInt(questionMatcher.group(1));
            } catch (NumberFormatException e) {
                // Fallback if there's any parse error
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Handles the logic when we detect a new question line:
     * - If it is truly a "next question" (e.g., # = currentQuestionNumber + 1),
     *   then we finalize the old question & start a new one.
     * - Otherwise, we treat the content as continuation text.
     */
    private static void handleQuestionLine(
            String lineContent,
            List<Object> mcQuestions,
            StringBuilder questionText,
            List<String> answers,
            int currentQuestionNumber
    ) {
        // Prepare a regex matcher
        Matcher questionMatcher = Pattern.compile("^\\s*(\\d+)\\.?\\s*\\t+(.*)").matcher(lineContent);
        if (questionMatcher.find()) {
            int questionNumber = Integer.parseInt(questionMatcher.group(1));
            String questionRawText = questionMatcher.group(2);

            // Check if questionNumber is exactly next in sequence
            if ((questionNumber == currentQuestionNumber + 1)
                    && ((!questionText.isEmpty() && !answers.isEmpty()) || currentQuestionNumber == 0)) {

                // If we already had a valid question and its answers, finalize it
                if (currentQuestionNumber != 0) {
                    mcQuestions.add(createQuestionAndAnswerObject(questionText.toString(), answers));
                }

                // Start building a new question
                questionText.setLength(0); // Clear old data
                questionText.append(questionRawText); // Set new question text
                answers.clear(); // Clear old answers

            } else {
                // Otherwise, treat it as a continuation line
                // Remove leading tab if any
                lineContent = lineContent.replaceFirst("^\t", "");
                appendContinuationLine(lineContent, questionText, answers);
            }
        }
    }

    /**
     * Checks if this line is an answer choice (e.g. "A. <text>", with at least
     * one space or tab after the dot).
     */
    private static boolean isAnswerLine(String lineContent) {
        // Example: "A. <space or tab> text"
        // - ^\\s*[A-Ma-m]\\.[ \\t] means optional leading spaces,
        //   single letter [A-M] or [a-m], then a dot, then either space or tab.
        return lineContent.matches("^\\s*[A-Ma-m]\\.[ \\t]\\s*\\t*.*");
    }

    /**
     * Handles the logic when the line is recognized as an answer:
     * - Extracts the answer label (e.g. A, B, C).
     * - Checks if it's the "continuous" next answer in sequence (index = answers.size()).
     * - If yes, add new answer. If not, treat as continuation text.
     */
    private static void handleAnswerLine(String lineContent, List<String> answers, StringBuilder questionText) {
        Matcher matcher = Pattern.compile("^\\s*([A-Ma-m])\\.[ \\t]\\s*\\t*(.*)").matcher(lineContent);

        if (matcher.find()) {
            char currentAnswer = Character.toUpperCase(matcher.group(1).charAt(0));
            String remainder = matcher.group(2).trim();

            if (currentAnswer - 'A' == answers.size()) {
                answers.add(remainder);

                char nextAnswer = (char) (currentAnswer + 1);

                Matcher checkContinuousMatch = Pattern.compile(
                        "^(.*?)\\t*(" + nextAnswer + ")\\.[ \\t](.*)"
                ).matcher(remainder);

                if (checkContinuousMatch.find()) {
                    String firstPart = checkContinuousMatch.group(1).trim();
                    String foundNextChar = checkContinuousMatch.group(2);
                    String nextPart = checkContinuousMatch.group(3).trim();

                    answers.set(answers.size() - 1, firstPart);

                    char foundNext = Character.toUpperCase(foundNextChar.charAt(0));
                    if (foundNext - 'A' == answers.size()) {
                        answers.add(nextPart);
                    } else {
                        appendContinuationLine(nextPart, questionText, answers);
                    }
                }
            } else {
                lineContent = lineContent.replaceFirst("^\\t", "");
                appendContinuationLine(lineContent, questionText, answers);
            }
        } else {
            lineContent = lineContent.replaceFirst("^\\t", "");
            appendContinuationLine(lineContent, questionText, answers);
        }
    }

    /**
     * Appends 'lineContent' to either the last answer (if answers is not empty)
     * or to the question text (if answers is empty).
     */
    private static void appendContinuationLine(String lineContent, StringBuilder questionText, List<String> answers) {
        Pattern nextAnswerPattern = Pattern.compile("([A-Ma-m])\\.[ \\t]");
        Matcher matcher = nextAnswerPattern.matcher(lineContent);

        if (matcher.find()) {
            int startPos = matcher.start();
            String beforeNextAnswer  = lineContent.substring(0, startPos).trim();

            if (!beforeNextAnswer .isEmpty()) {
                if (!answers.isEmpty()) {
                    int lastIndex = answers.size() - 1;
                    answers.set(lastIndex, answers.get(lastIndex) + "<br>" + beforeNextAnswer );
                } else {
                    questionText.append("<br>").append(beforeNextAnswer );
                }
            }

            String nextAnswerPart = lineContent.substring(startPos).trim();

            Pattern answerPattern = Pattern.compile("^\\s*([A-Ma-m])\\.[ \\t]+(.*)");
            Matcher answerMatcher = answerPattern.matcher(nextAnswerPart);

            if (answerMatcher.find()) {
                char currentAnswer = Character.toUpperCase(answerMatcher.group(1).charAt(0));
                String remainder = answerMatcher.group(2).trim();

                if (currentAnswer - 'A' == answers.size()) {
                    answers.add(remainder);
                } else {
                    if (!answers.isEmpty()) {
                        int lastIndex = answers.size() - 1;
                        answers.set(lastIndex, answers.get(lastIndex) + "<br>" + nextAnswerPart);
                    } else {
                        questionText.append("<br>").append(nextAnswerPart);
                    }
                }
            } else {
                if (!answers.isEmpty()) {
                    int lastIndex = answers.size() - 1;
                    answers.set(lastIndex, answers.get(lastIndex) + "<br>" + nextAnswerPart);
                } else {
                    questionText.append("<br>").append(nextAnswerPart);
                }
            }
        } else {
            // No next answer found in this line => treat as normal continuation
            if (!answers.isEmpty()) {
                int lastIndex = answers.size() - 1;
                answers.set(lastIndex, answers.get(lastIndex) + "<br>" + lineContent);
            } else {
                questionText.append("<br>").append(lineContent);
            }
        }
    }

    /**
     * Simulates creating the question & answer object from text and answers list.
     */
    private static Object createQuestionAndAnswerObject(String ques, List<String> ans) {
        // Using Map instead of HashMap (adhering to the interface)
        Map<String, Object> questionData = new HashMap<>();

        // Early return if there are no answers
        if (ans.isEmpty()) {
            questionData.put("question", cleanText(ques));
            questionData.put("answers", Collections.emptyList());
            questionData.put("total_correct", 0);
            return questionData;
        }

        // Get the last answer in the list
        String lastAns = ans.get(ans.size() - 1);
        // Split by "<br>"
        String[] rowInLastAns = lastAns.split("<br>");

        // Extract the correct answer (if any) from the last line and remove it
        String correctAns = extractCorrectAnswerFromLastLine(rowInLastAns, ans);

        // Prepare variables for correct answer processing
        List<String> correctAnswers = new ArrayList<>();
        int cnt_correct = 0;
        int max_index_answer = -1;

        if (!correctAns.isEmpty()) {
            // Split correctAns by comma to handle multiple correct answers
            String[] correctSplit = correctAns.split(",");
            for (String correctAnswer : correctSplit) {
                correctAnswer = correctAnswer.trim();
                if (!correctAnswer.isEmpty()) {
                    max_index_answer = Math.max(max_index_answer, correctAnswer.charAt(0) - 'A');
                }
            }

            // Remove empty answer lines behind max_index_answer
            removeEmptyAnswersBehind(ans, max_index_answer);

            // Collect valid correct answers into correctAnswers
            for (String ca : correctSplit) {
                ca = ca.trim();
                if (ca.isEmpty()) continue; // Skip if empty after trimming
                try {
                    int index = ca.charAt(0) - 'A';
                    if (index >= 0 && index < ans.size()) {
                        correctAnswers.add(ans.get(index));
                        cnt_correct++;
                    }
                } catch (Exception e) {
                    // Skip if format is invalid
                }
            }
        } else {
            // If correctAns is empty, still remove empty lines behind if any
            removeEmptyAnswersBehind(ans, max_index_answer);
        }

        // Remove correct answers from Ans list to avoid duplicates
        ans.removeAll(correctAnswers);
        // Place correct answers at the beginning
        correctAnswers.addAll(ans);

        // Build the final data object
        questionData.put("question", cleanText(ques));
        questionData.put("answers", cleanAnswers(correctAnswers));
        questionData.put("total_correct", cnt_correct);

        return questionData;
    }

    /**
     * This method extracts the correct answer from the last line (if any).
     * It also updates the last element in 'ans' by removing the correct answer section.
     */
    private static String extractCorrectAnswerFromLastLine(String[] rowInLastAns, List<String> ans) {
        // rowInLastAns is the array of lines split by "<br>"
        // The last element may contain the candidate for correctAns
        String lastLine = rowInLastAns[rowInLastAns.length - 1];

        // Case 1: The line contains at least one tab -> correctAns is after the last tab
        if (lastLine.matches(".*\\t+.*")) {
            String[] parts = lastLine.split("\\t+");
            // parts[parts.length - 1] is the substring after the last tab
            if (parts.length >= 2) {
                String candidate = parts[parts.length - 1].trim();

                // candidate might contain multiple answers (e.g., "A, B, C")
                String[] splitted = candidate.split(",");
                boolean isValid = isValidAnswers(splitted, ans);

                if (isValid && splitted.length > 0) {
                    // Cut correctAns from the last line
                    rowInLastAns[rowInLastAns.length - 1] =
                            lastLine.substring(0, lastLine.lastIndexOf("\t")).trim();

                    // Reconstruct the updated last answer with "<br>"
                    ans.set(ans.size() - 1, joinWithBr(rowInLastAns));
                    return candidate; // Return the correctAns
                }
            }
            // If invalid or parts.length < 2, treat as no correctAns
            return "";
        }

        // Case 2: No tab found -> possibly multiple answers (A,B,...) or empty
        String possibleCorrectAns = lastLine.replace("\t", "").trim();
        String[] splitted = possibleCorrectAns.split(",");
        boolean isValid = isValidAnswers(splitted, ans);

        if (isValid && splitted.length > 0) {
            // Remove the line containing correctAns from rowInLastAns
            if (rowInLastAns.length > 1) {
                // Truncate the last element
                String[] withoutLast = Arrays.copyOf(rowInLastAns, rowInLastAns.length - 1);
                ans.set(ans.size() - 1, joinWithBr(withoutLast));
            } else {
                // If there's only one line, set it to empty after removal
                ans.set(ans.size() - 1, "");
            }
            return possibleCorrectAns;
        }

        // By default, return empty if invalid
        return "";
    }

    /**
     * Checks whether the array of strings (e.g., ["A", "b", "C"])
     * matches the required format: each element is a single character
     * within [A-M] or [a-m]. You can add extra checks if needed
     * (for example, ensuring the answer doesn't exceed ans.size()).
     */
    private static boolean isValidAnswers(String[] answers, List<String> ans) {
        for (String s : answers) {
            s = s.trim();

            // Check format: must be a single character A–M or a–m
            if (!s.matches("^[A-Ma-m]$")) {
                return false;
            }

            // Additional check: index must not exceed ans.size()
             int index = Character.toUpperCase(s.charAt(0)) - 'A';
             if (index < 0 || index >= ans.size()) {
                 return false;
             }
        }
        return true;
    }

    /**
     * Joins lines (after removing the correctAns part) with "<br>".
     * Avoids adding extra "<br>" at the beginning.
     */
    private static String joinWithBr(String[] lines) {
        if (lines.length == 0) return "";

        StringBuilder sb = new StringBuilder(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            sb.append("<br>").append(lines[i]);
        }
        return sb.toString();
    }

    /**
     * Removes empty answers behind a given max_index in the list 'ans'.
     */
    private static void removeEmptyAnswersBehind(List<String> ans, int maxIndex) {
        for (int i = ans.size() - 1; i > maxIndex; i--) {
            if (ans.get(i).isEmpty()) {
                ans.remove(i);
            }
        }
    }

    private static String cleanText(String text) {
        String formatString = StringEscapeUtils.escapeHtml4(text);

        // Convert new line into <br>
        formatString = formatString.replace("\\r", "").replaceAll("\\r?\\n", "<br>");
        formatString = formatString.replace("&lt;br&gt;", "<br>");

        formatString = formatString.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp");

        formatString = formatString.replace("    ", "&nbsp;&nbsp;&nbsp;&nbsp;");

        return formatString;
    }

    private static List<String> cleanAnswers(List<String> answers) {
        List<String> cleanedAnswers = new ArrayList<>();
        for (String answer : answers) {
            cleanedAnswers.add(cleanText(answer).trim());
        }
        return cleanedAnswers;
    }

    private Object createObjectMcQuestion(List<Object> listQues) {
        if (listQues.isEmpty()) {
            return Collections.emptyMap(); // Return empty JSON object if no questions exist
        }

        HashMap<String, Object> questionData = new HashMap<>();
        questionData.put("mc_questions", listQues);
        questionData.put("total_questions", listQues.size());

        return questionData;
    }

    public Map<String, Object> convertTxtFilesToJson(Map<String, String> files) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> errorMap = new HashMap<>();

        // Iterate through each file in array
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String fileName = entry.getKey();
            String fileContent = entry.getValue();

            try {
                // Process file into list of question objects
                List<Object> processedFile = processFileContent(fileContent);

                // Convert question object and calculate the number of questions in the main object
                Object fileData = createObjectMcQuestion(processedFile);

                // Put it in the map
                result.put(fileName, fileData);
            } catch ( Exception e ) {
                errorMap.put(fileName, e.getMessage());
            }
        }

        if (!errorMap.isEmpty()) {
            result.put("errors", errorMap);
        }
        return result;
    }
}
