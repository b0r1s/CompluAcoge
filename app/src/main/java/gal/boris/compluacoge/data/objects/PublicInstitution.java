package gal.boris.compluacoge.data.objects;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import gal.boris.compluacoge.data.Node;
import gal.boris.compluacoge.data.pojos.DataAnswer;
import gal.boris.compluacoge.data.pojos.DataProcedure;
import gal.boris.compluacoge.data.pojos.DataPublicInstitution;
import gal.boris.compluacoge.data.pojos.DataPublicUser;
import gal.boris.compluacoge.data.pojos.DataQuestion;
import gal.boris.compluacoge.data.pojos.DataStep;
import gal.boris.compluacoge.extras.Box;

public class PublicInstitution {

    protected final DataPublicInstitution dataPublicInstitution;
    private final Map<String,Map<String,Procedure>> procVisiblesMap;
    private final Map<String,Step> stepsVisibles;
    private final Map<Integer,List<Question>> openQuestions; //0=raso, 1=experto, 2=moderador
    private final Map<String,PublicUser> allPublicUsers;

    public static PublicInstitution createEmpty() {
        return new PublicInstitution(new HashMap<>(),new Box<>());
    }

    public PublicInstitution(Map<String,Object> data, Box<Boolean> dataMissing) {
        this.dataPublicInstitution = DataPublicInstitution.parse(data,dataMissing);

        Map<String, Map<String, DataProcedure>> dataVisibleProcedures = DataProcedure.parseMultiple(dataPublicInstitution.getProcVisibles(),dataMissing);
        List<DataStep> dataStepsVisibles = DataStep.parseMultiple(dataPublicInstitution.getStepsVisibles(),dataMissing);
        List<Step> allStepsVisibles = new ArrayList<>();
        for(DataStep dataStep : dataStepsVisibles) {
            List<DataQuestion> dataQuestions = DataQuestion.parseMultiple(dataStep.getMapQuestions(),dataMissing);
            List<DataAnswer> dataAnswers = DataAnswer.parseMultiple(dataStep.getMapAnswers(),dataMissing);
            Map<String,Answer> answers = dataAnswers.stream().collect(Collectors.toMap(DataAnswer::getIdAnswer, Answer::new, (prev, next) -> prev));
            List<Question> questions =  dataQuestions.stream().map(q -> new Question(q,answers)).collect(Collectors.toList());
            allStepsVisibles.add(new Step(dataStep,questions));
        }
        this.procVisiblesMap = new HashMap<>();
        for(Map.Entry<String,Map<String, DataProcedure>> entry : dataVisibleProcedures.entrySet()) {
            this.procVisiblesMap.put(entry.getKey(),entry.getValue().entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey,d -> new Procedure(d.getValue(), allStepsVisibles,true),(prev,next)->prev)));
        }

        this.openQuestions = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            this.openQuestions.put(i,new ArrayList<>());
        }
        List<Question> allQuestions = new ArrayList<>();
        for(Step step : allStepsVisibles) {
            allQuestions.addAll(step.getQuestions());
        }
        for(Question q : allQuestions) {
            if(q.getOpen()) {
                this.openQuestions.get(q.getAnswersOpenTo()).add(q);
            }
        }

        this.stepsVisibles = allStepsVisibles.stream().collect(Collectors.toMap(Step::getIdStep, Function.identity(),(prev,next)->prev));

        this.allPublicUsers = dataPublicInstitution.getPublicUsers().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(),e -> new PublicUser(DataPublicUser.parse((Map<String,Object>) e.getValue(),dataMissing)),(prev,next)->prev));
        List<Answer> allAnswers = new ArrayList<>();
        for(Question q : allQuestions) {
            q.setAtTheEnd(allPublicUsers);
            allAnswers.addAll(q.getAnswers());
        }
        for(Answer a : allAnswers) {
            a.setAtTheEnd(allPublicUsers);
        }
    }

    public Map<String,Object> toMap(){
        return dataPublicInstitution.toMap();
    }

    // --- Getters ---

    public String getName() {
        return dataPublicInstitution.getName();
    }

    public String getID() {
        return dataPublicInstitution.getID();
    }

    public String getDescription() {
        return dataPublicInstitution.getDescription();
    }

    public Long getCreated() {
        return dataPublicInstitution.getCreated();
    }

    public Long getLastModified() {
        return dataPublicInstitution.getLastModified();
    }

    public Procedure getProcVisible(String id, String version) {
        Map<String,Procedure> aux = procVisiblesMap.get(id);
        if(aux!=null) {
            return aux.get(version);
        }
        return null;
    }

    public List<Procedure> getCopyVersionsProcVisible(String id) {
        Map<String,Procedure> aux = procVisiblesMap.get(id);
        if(aux!=null) {
            return new ArrayList<>(aux.values());
        }
        return new ArrayList<>();
    }

    public Set<Procedure> getCopyMainProcVisibles() {
        Set<Procedure> result = new HashSet<>();
        for(Map.Entry<String,Map<String,Procedure>> entry : procVisiblesMap.entrySet()) {
            Optional<Long> biggestVersion = entry.getValue().keySet().stream().map(Long::parseLong).max(Long::compareTo);
            biggestVersion.ifPresent(version -> result.add(entry.getValue().get("" + version)));
        }
        return result;
    }

    public Map<Integer, List<Question>> getOpenQuestions() {
        return openQuestions;
    }

    public Map<String, Step> getStepsVisibles() {
        return stepsVisibles;
    }

    public Map<String, PublicUser> getAllPublicUsers() {
        return allPublicUsers;
    }

    // --- Setters ---

    public List<String> addNewProc(Procedure draft) {
        //Update this class
        if(!procVisiblesMap.containsKey(draft.getIdProcedure())) {
            procVisiblesMap.put(draft.getIdProcedure(), new HashMap<>());
        }
        procVisiblesMap.get(draft.getIdProcedure()).put(draft.getVersionProcedure(), draft);
        for(Node<Step> node : draft.getAllSteps()) {
            stepsVisibles.put(node.getNode().getIdStep(), node.getNode());
        }

        //Update DataPublicInstitution
        if(!dataPublicInstitution.getProcVisibles().containsKey(draft.getIdProcedure())) {
            dataPublicInstitution.getProcVisibles().put(draft.getIdProcedure(), new HashMap<>());
        }
        dataPublicInstitution.getProcVisibles().get(draft.getIdProcedure()).put(draft.getVersionProcedure(), draft.dataToMap());
        for(Node<Step> step : draft.getAllSteps()) {
            dataPublicInstitution.getStepsVisibles().put(step.getNode().getIdStep(), step.getNode().toMap());
        }

        //Which fields have to be updated on DB
        return Arrays.asList(DataPublicInstitution.KEY_PROC_VISIBLES,DataPublicInstitution.KEY_STEPS_VISIBLES);
    }

    public Pair<Boolean,List<String>> closeVisibleVersions(Procedure proc) {
        Collection<Procedure> versions = getCopyVersionsProcVisible(proc.getIdProcedure());
        boolean changed = false;
        for(Procedure p : versions) {
            if(!p.getClosed()) {
                p.setClosed(true);
                dataPublicInstitution.getProcVisibles().get(p.getIdProcedure()).put(p.getVersionProcedure(),p.dataToMap());
                changed = true;
            }
        }
        return new Pair<>(changed,Arrays.asList(DataPublicInstitution.KEY_PROC_VISIBLES));
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicInstitution that = (PublicInstitution) o;
        return Objects.equals(dataPublicInstitution, that.dataPublicInstitution) &&
                Objects.equals(procVisiblesMap, that.procVisiblesMap) &&
                Objects.equals(openQuestions, that.openQuestions) &&
                Objects.equals(stepsVisibles, that.stepsVisibles) &&
                Objects.equals(allPublicUsers, that.allPublicUsers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataPublicInstitution, procVisiblesMap, openQuestions, stepsVisibles, allPublicUsers);
    }
}
