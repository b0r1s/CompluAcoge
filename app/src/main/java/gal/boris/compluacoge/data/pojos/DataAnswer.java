package gal.boris.compluacoge.data.pojos;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gal.boris.compluacoge.extras.Box;

public class DataAnswer {

    private String idAnswer;

    private static final String KEY_TEXT = "text";
    private String text;
    private static final String KEY_PUBLIC_ID_AUTHOR = "publicIdAuthor";
    private String publicIdAuthor;

    private static final String KEY_ID_QUESTION = "idQuestion";
    private String idQuestion;

    private static final String KEY_CREATED = "created";
    private Long created;

    private static final String KEY_PUBLIC_ID_USERS_LIKED = "publicIdUsersLiked";
    private List<String> publicIdUsersLiked;

    private DataAnswer() {}

    public static List<DataAnswer> parseMultiple(@NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        List<DataAnswer> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String idAnswer = entry.getKey();
            list.add(parse(idAnswer, (Map<String, Object>) entry.getValue(), dataMissing));
        }
        return list;
    }

    private static DataAnswer parse(String idAnswer, @NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        DataAnswer step = new DataAnswer();
        step.setIdAnswer(idAnswer);

        String text = "text";
        if (data.containsKey(KEY_TEXT)) {
            text = ((String) data.get(KEY_TEXT));
        } else {
            dataMissing.set(true);
        }
        step.setText(text);

        String publicIdAuthor = "";
        if (data.containsKey(KEY_PUBLIC_ID_AUTHOR)) {
            publicIdAuthor = ((String) data.get(KEY_PUBLIC_ID_AUTHOR));
        } else {
            dataMissing.set(true);
        }
        step.setPublicIdAuthor(publicIdAuthor);

        String idQuestion = "id_question";
        if (data.containsKey(KEY_ID_QUESTION)) {
            idQuestion = ((String) data.get(KEY_ID_QUESTION));
        } else {
            dataMissing.set(true);
        }
        step.setIdQuestion(idQuestion);

        Long created = 0L;
        if (data.containsKey(KEY_CREATED)) {
            created = ((Long) data.get(KEY_CREATED));
        } else {
            dataMissing.set(true);
        }
        step.setCreated(created);

        List<String> publicIdUsersLiked = new ArrayList<>();
        if (data.containsKey(KEY_PUBLIC_ID_USERS_LIKED)) {
            publicIdUsersLiked = ((List<String>) data.get(KEY_PUBLIC_ID_USERS_LIKED));
        } else {
            dataMissing.set(true);
        }
        step.setPublicIdUsersLiked(publicIdUsersLiked);

        return step;
    }

    public static Map<String,Object> toMapMultiple(@NonNull List<DataAnswer> list) {
        return list.stream().collect(Collectors.toMap(DataAnswer::getIdAnswer,DataAnswer::toMap,(prev,next) -> prev));
    }

    private Map<String,Object> toMap() {
        Map<String,Object> data = new HashMap<>();
        data.put(KEY_TEXT, text);
        data.put(KEY_PUBLIC_ID_AUTHOR, publicIdAuthor);
        data.put(KEY_ID_QUESTION, idQuestion);
        data.put(KEY_CREATED, created);
        data.put(KEY_PUBLIC_ID_USERS_LIKED, publicIdUsersLiked);
        return data;
    }

    // --- Getters ---

    public String getIdAnswer() {
        return idAnswer;
    }

    public String getText() {
        return text;
    }

    public String getPublicAuthor() {
        return publicIdAuthor;
    }

    public String getIdQuestion() {
        return idQuestion;
    }

    public Long getCreated() {
        return created;
    }

    public List<String> getPublicUsersLiked() {
        return publicIdUsersLiked;
    }

    // --- Setters ---

    private void setIdAnswer(String idAnswer) {
        this.idAnswer = idAnswer;
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setPublicIdAuthor(String publicIdAuthor) {
        this.publicIdAuthor = publicIdAuthor;
    }

    private void setIdQuestion(String idQuestion) {
        this.idQuestion = idQuestion;
    }

    private void setCreated(Long created) {
        this.created = created;
    }

    private void setPublicIdUsersLiked(List<String> publicIdUsersLiked) {
        this.publicIdUsersLiked = publicIdUsersLiked;
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataAnswer that = (DataAnswer) o;
        return Objects.equals(idAnswer, that.idAnswer) &&
                Objects.equals(text, that.text) &&
                Objects.equals(publicIdAuthor, that.publicIdAuthor) &&
                Objects.equals(idQuestion, that.idQuestion) &&
                Objects.equals(created, that.created) &&
                Objects.equals(publicIdUsersLiked, that.publicIdUsersLiked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAnswer, text, publicIdAuthor, idQuestion, created, publicIdUsersLiked);
    }
}
