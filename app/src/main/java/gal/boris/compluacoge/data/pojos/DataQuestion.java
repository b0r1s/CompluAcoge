package gal.boris.compluacoge.data.pojos;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gal.boris.compluacoge.extras.Box;

public class DataQuestion {

    private String idQuestion;

    private static final String KEY_TEXT= "text";
    private String text;
    private static final String KEY_PUBLIC_ID_AUTHOR = "publicIdAuthor";
    private String publicIdAuthor;

    private static final String KEY_CREATED = "created";
    private Long created;
    private static final String KEY_LAST_TIME_ANSWER = "lastTimeAnswer";
    private Long lastTimeAnswer;

    private static final String KEY_ID_ASNWERS = "idAnswers";
    private List<String> idAnswers;
    private static final String KEY_ID_CORRECT_ANSWER = "idCorrectAnswer";
    private String idCorrectAnswer;
    private static final String KEY_OPEN = "open";
    private Boolean open;
    private static final String KEY_ANSWERS_OPEN_TO = "answersOpenTo";
    private Integer answersOpenTo;

    private DataQuestion() {}

    public static List<DataQuestion> parseMultiple(@NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        List<DataQuestion> list = new ArrayList<>();
        for(Map.Entry<String,Object> entry : data.entrySet()) {
            String idQuestion = entry.getKey();
            list.add(parse(idQuestion,(Map<String, Object>) entry.getValue(),dataMissing));
        }
        return list;
    }

    private static DataQuestion parse(String idQuestion, @NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        DataQuestion question = new DataQuestion();
        question.setIdQuestion(idQuestion);

        String text = "text";
        if(data.containsKey(KEY_TEXT)) {
            text = ((String) data.get(KEY_TEXT));
        }
        else {
            dataMissing.set(true);
        }
        question.setText(text);

        String publicIdAuthor = "";
        if(data.containsKey(KEY_PUBLIC_ID_AUTHOR)) {
            publicIdAuthor = ((String) data.get(KEY_PUBLIC_ID_AUTHOR));
        }
        else {
            dataMissing.set(true);
        }
        question.setPublicIdAuthor(publicIdAuthor);

        Long created = 0L;
        if(data.containsKey(KEY_CREATED)) {
            created = ((Long) data.get(KEY_CREATED));
        }
        else {
            dataMissing.set(true);
        }
        question.setCreated(created);

        Long lastTimeAnswer = 0L;
        if(data.containsKey(KEY_LAST_TIME_ANSWER)) {
            lastTimeAnswer = ((Long) data.get(KEY_LAST_TIME_ANSWER));
        }
        else {
            dataMissing.set(true);
        }
        question.setLastTimeAnswer(lastTimeAnswer);

        List<String> idAnswers = new ArrayList<>();
        if(data.containsKey(KEY_ID_ASNWERS)) {
            idAnswers = ((List<String>) data.get(KEY_ID_ASNWERS));
        }
        else {
            dataMissing.set(true);
        }
        question.setIdAnswers(idAnswers);

        String idCorrectAnswer = "";
        if(data.containsKey(KEY_ID_CORRECT_ANSWER)) {
            idCorrectAnswer = ((String) data.get(KEY_ID_CORRECT_ANSWER));
        }
        else {
            dataMissing.set(true);
        }
        question.setIdCorrectAnswer(idCorrectAnswer);

        Boolean open = true;
        if(data.containsKey(KEY_OPEN)) {
            open = ((Boolean) data.get(KEY_OPEN));
        }
        else {
            dataMissing.set(true);
        }
        question.setOpen(open);

        Integer answersOpenTo = 0;
        if(data.containsKey(KEY_ANSWERS_OPEN_TO)) {
            answersOpenTo = ((Integer) data.get(KEY_ANSWERS_OPEN_TO));
        }
        else {
            dataMissing.set(true);
        }
        question.setAnswersOpenTo(answersOpenTo);

        return question;
    }

    public static Map<String,Object> toMapMultiple(@NonNull List<DataQuestion> list) {
        return list.stream().collect(Collectors.toMap(DataQuestion::getIdQuestion,DataQuestion::toMap,(prev, next) -> prev));
    }

    private Map<String,Object> toMap() {
        Map<String,Object> data = new HashMap<>();
        data.put(KEY_TEXT, text);
        data.put(KEY_PUBLIC_ID_AUTHOR, publicIdAuthor);
        data.put(KEY_CREATED, created);
        data.put(KEY_LAST_TIME_ANSWER, lastTimeAnswer);
        data.put(KEY_ID_ASNWERS, idAnswers);
        data.put(KEY_ID_CORRECT_ANSWER, idCorrectAnswer);
        data.put(KEY_OPEN, open);
        data.put(KEY_ANSWERS_OPEN_TO, answersOpenTo);
        return data;
    }

    // --- Getters ---

    public String getIdQuestion() {
        return idQuestion;
    }

    public String getText() {
        return text;
    }

    public String getPublicIdAuthor() {
        return publicIdAuthor;
    }

    public Long getCreated() {
        return created;
    }

    public Long getLastTimeAnswer() {
        return lastTimeAnswer;
    }

    public List<String> getIdAnswers() {
        return idAnswers;
    }

    public String getIdCorrectAnswer() {
        return idCorrectAnswer;
    }

    public Boolean getOpen() {
        return open;
    }

    public Integer getAnswersOpenTo() {
        return answersOpenTo;
    }

    // --- Setters ---


    private void setIdQuestion(String idQuestion) {
        this.idQuestion = idQuestion;
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setPublicIdAuthor(String publicIdAuthor) {
        this.publicIdAuthor = publicIdAuthor;
    }

    private void setCreated(Long created) {
        this.created = created;
    }

    private void setLastTimeAnswer(Long lastTimeAnswer) {
        this.lastTimeAnswer = lastTimeAnswer;
    }

    private void setIdAnswers(List<String> idAnswers) {
        this.idAnswers = idAnswers;
    }

    private void setIdCorrectAnswer(String idCorrectAnswer) {
        this.idCorrectAnswer = idCorrectAnswer;
    }

    private void setOpen(Boolean open) {
        this.open = open;
    }

    private void setAnswersOpenTo(Integer answersOpenTo) {
        this.answersOpenTo = answersOpenTo;
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataQuestion that = (DataQuestion) o;
        return Objects.equals(idQuestion, that.idQuestion) &&
                Objects.equals(text, that.text) &&
                Objects.equals(publicIdAuthor, that.publicIdAuthor) &&
                Objects.equals(created, that.created) &&
                Objects.equals(lastTimeAnswer, that.lastTimeAnswer) &&
                Objects.equals(idAnswers, that.idAnswers) &&
                Objects.equals(idCorrectAnswer, that.idCorrectAnswer) &&
                Objects.equals(open, that.open) &&
                Objects.equals(answersOpenTo, that.answersOpenTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idQuestion, text, publicIdAuthor, created, lastTimeAnswer, idAnswers, idCorrectAnswer, open, answersOpenTo);
    }
}
