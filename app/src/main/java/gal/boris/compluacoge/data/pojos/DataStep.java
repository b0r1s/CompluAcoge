package gal.boris.compluacoge.data.pojos;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gal.boris.compluacoge.extras.Box;
import gal.boris.compluacoge.logic.CloudDB;

public class DataStep {

    private String idStep;

    private static final String KEY_NAME = "name";
    private String name;
    private static final String KEY_DESCRIPTION = "description";
    private String description;
    private static final String KEY_IMAGES_URL = "imagesURL";
    private List<String> imagesURL;

    public static final String KEY_MAP_QUESTIONS = "mapQuestions";
    private Map<String,Object> mapQuestions;
    public static final String KEY_MAP_ANSWERS = "mapAnswers";
    private Map<String,Object> mapAnswers;

    private DataStep() {}

    public DataStep(DataStep that) {
        this.idStep = that.idStep;
        this.name = that.name;
        this.description = that.description;
        this.imagesURL = new ArrayList<>(that.imagesURL);
        this.mapQuestions = new HashMap<>(that.mapQuestions); //? todo
        this.mapAnswers = new HashMap<>(that.mapAnswers); //? todo
    }

    public static DataStep createBased(DataStep that) {
        DataStep dataStep = new DataStep(that);
        dataStep.idStep = CloudDB.autoId();
        return dataStep;
    }

    public static DataStep createEmpty(String name, String description) {
        DataStep step = new DataStep();
        step.idStep = CloudDB.autoId();
        step.name = name;
        step.description = description;
        step.imagesURL = new ArrayList<>();
        step.mapQuestions = new HashMap<>();
        step.mapAnswers = new HashMap<>();
        return step;
    }

    public static List<DataStep> parseMultiple(@NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        List<DataStep> list = new ArrayList<>();
        for(Map.Entry<String,Object> entry : data.entrySet()) {
            String idStep = entry.getKey();
            list.add(parse(idStep,(Map<String, Object>) entry.getValue(),dataMissing));
        }
        return list;
    }

    private static DataStep parse(String idStep, @NonNull Map<String, Object> data, @NonNull Box<Boolean> dataMissing) {
        DataStep step = new DataStep();
        step.setIdStep(idStep);

        String name = "Step X";
        if(data.containsKey(KEY_NAME)) {
            name = ((String) data.get(KEY_NAME));
        }
        else {
            dataMissing.set(true);
        }
        step.setName(name);

        String description = "This is the step's description";
        if(data.containsKey(KEY_DESCRIPTION)) {
            description = ((String) data.get(KEY_DESCRIPTION));
        }
        else {
            dataMissing.set(true);
        }
        step.setDescription(description);

        List<String> imagesURL = new ArrayList<>();
        if(data.containsKey(KEY_IMAGES_URL)) {
            imagesURL = ((List<String>) data.get(KEY_IMAGES_URL));
        }
        else {
            dataMissing.set(true);
        }
        step.setImagesURL(imagesURL);

        Map<String,Object> mapQuestions = new HashMap<>();
        if(data.containsKey(KEY_MAP_QUESTIONS)) {
            mapQuestions = ((Map<String,Object>) data.get(KEY_MAP_QUESTIONS));
        }
        else {
            dataMissing.set(true);
        }
        step.setMapQuestions(mapQuestions);

        Map<String,Object> mapAnswers = new HashMap<>();
        if(data.containsKey(KEY_MAP_ANSWERS)) {
            mapAnswers = ((Map<String,Object>) data.get(KEY_MAP_ANSWERS));
        }
        else {
            dataMissing.set(true);
        }
        step.setMapAnswers(mapAnswers);

        return step;
    }

    public static Map<String,Object> toMapMultiple(@NonNull List<DataStep> list) {
        return list.stream().collect(Collectors.toMap(DataStep::getIdStep,DataStep::toMap,(prev, next) -> prev));
    }

    public Map<String,Object> toMap() {
        Map<String,Object> data = new HashMap<>();
        data.put(KEY_NAME, name);
        data.put(KEY_DESCRIPTION, description);
        data.put(KEY_IMAGES_URL, imagesURL);
        data.put(KEY_MAP_QUESTIONS, mapQuestions);
        data.put(KEY_MAP_ANSWERS, mapAnswers);
        return data;
    }

    // --- Getters ---

    public String getIdStep() {
        return idStep;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getImagesURL() {
        return imagesURL;
    }

    public Map<String, Object> getMapQuestions() {
        return mapQuestions;
    }

    public Map<String, Object> getMapAnswers() {
        return mapAnswers;
    }

    // --- Setters ---

    private void setIdStep(String idStep) {
        this.idStep = idStep;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImagesURL(List<String> imagesURL) {
        this.imagesURL = imagesURL;
    }

    private void setMapQuestions(Map<String, Object> mapQuestions) {
        this.mapQuestions = mapQuestions;
    }

    private void setMapAnswers(Map<String, Object> mapAnswers) {
        this.mapAnswers = mapAnswers;
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataStep dataStep = (DataStep) o;
        return Objects.equals(idStep, dataStep.idStep) &&
                Objects.equals(name, dataStep.name) &&
                Objects.equals(description, dataStep.description) &&
                Objects.equals(imagesURL, dataStep.imagesURL) &&
                Objects.equals(mapQuestions, dataStep.mapQuestions) &&
                Objects.equals(mapAnswers, dataStep.mapAnswers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idStep, name, description, imagesURL, mapQuestions, mapAnswers);
    }
}
