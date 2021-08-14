package gal.boris.compluacoge.data.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import gal.boris.compluacoge.data.pojos.DataStep;

public class Step {

    private final DataStep dataStep;
    private final List<Question> questions;

    public Step(DataStep dataStep, List<Question> questions) {
        this.dataStep = dataStep;
        this.questions = questions;
    }

    public Step(Step that) {
        this.dataStep = new DataStep(that.dataStep);
        this.questions = new ArrayList<>(that.questions); //? todo
    }

    public static Step createBased(Step that) {
        return new Step(DataStep.createBased(that.dataStep),new ArrayList<>(that.questions));
    }

    public static Step createEmpty(String name, String description) {
        return new Step(DataStep.createEmpty(name,description), new ArrayList<>());
    }

    // --- Getters ---

    public String getIdStep() {
        return dataStep.getIdStep();
    }

    public String getName() {
        return dataStep.getName();
    }

    public String getDescription() {
        return dataStep.getDescription();
    }

    public List<String> getImagesURL() {
        return dataStep.getImagesURL();
    }

    public List<Question> getQuestions() {
        return questions;
    }

    // --- Setters ---

    public void setName(String name) {
        dataStep.setName(name);
    }

    public void setDescription(String description) {
        dataStep.setDescription(description);
    }

    public void setImageList(List<String> imageList) {
        dataStep.setImagesURL(imageList);
    }

    public Map<String,Object> toMap() {
        return dataStep.toMap();
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Step step = (Step) o;
        return Objects.equals(dataStep, step.dataStep) &&
                Objects.equals(questions, step.questions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataStep, questions);
    }
}
