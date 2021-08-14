package gal.boris.compluacoge.data.objects;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gal.boris.compluacoge.data.pojos.DataAnswer;

public class Answer {

    private final DataAnswer dataAnswer;

    private PublicUser author;
    private List<PublicUser> usersLiked;

    public Answer(DataAnswer dataAnswer) {
        this.dataAnswer = dataAnswer;
    }

    public void setAtTheEnd(Map<String,PublicUser> allUsers) {
        this.author = allUsers.get(dataAnswer.getPublicAuthor());
        this.usersLiked = dataAnswer.getPublicUsersLiked().stream().map(allUsers::get).collect(Collectors.toList());
    }

    // --- Getters ---

    public String getText() {
        return dataAnswer.getText();
    }

    public Long getCreated() {
        return dataAnswer.getCreated();
    }

    public List<PublicUser> getUsersLiked() {
        return usersLiked;
    }

    public PublicUser getAuthor() {
        return author;
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Answer answer = (Answer) o;
        return Objects.equals(dataAnswer, answer.dataAnswer) &&
                Objects.equals(author, answer.author) &&
                Objects.equals(usersLiked, answer.usersLiked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataAnswer, author, usersLiked);
    }
}
