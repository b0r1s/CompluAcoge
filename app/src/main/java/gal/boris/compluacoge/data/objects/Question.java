package gal.boris.compluacoge.data.objects;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gal.boris.compluacoge.data.pojos.DataQuestion;

public class Question {

    private final DataQuestion dataQuestion;
    private final List<Answer> answers;

    private PublicUser author;

    public Question(DataQuestion dataQuestion, Map<String,Answer> allAnswers) {
        this.dataQuestion = dataQuestion;
        this.answers = dataQuestion.getIdAnswers().stream().map(allAnswers::get).collect(Collectors.toList());
    }

    public void setAtTheEnd(Map<String,PublicUser> allUsers) {
        this.author = allUsers.get(dataQuestion.getPublicIdAuthor());
    }

    // --- Getters ---

    public String getIdQuestion() {
        return dataQuestion.getIdQuestion();
    }

    public String getText() {
        return dataQuestion.getText();
    }

    public PublicUser getAuthor() {
        return author;
    }

    public Long getCreated() {
        return dataQuestion.getCreated();
    }

    public Long getLastTimeAnswer() {
        return dataQuestion.getLastTimeAnswer();
    }

    public String getIdCorrectAnswer() {
        return dataQuestion.getIdCorrectAnswer();
    }

    public Boolean getOpen() {
        return dataQuestion.getOpen();
    }

    public Integer getAnswersOpenTo() {
        return dataQuestion.getAnswersOpenTo();
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return Objects.equals(dataQuestion, question.dataQuestion) &&
                Objects.equals(answers, question.answers) &&
                Objects.equals(author, question.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataQuestion, answers, author);
    }
}
