package gal.boris.compluacoge.data.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gal.boris.compluacoge.data.Node;
import gal.boris.compluacoge.data.pojos.DataAnswer;
import gal.boris.compluacoge.data.pojos.DataPrivateInstitution;
import gal.boris.compluacoge.data.pojos.DataProcedure;
import gal.boris.compluacoge.data.pojos.DataQuestion;
import gal.boris.compluacoge.data.pojos.DataStep;
import gal.boris.compluacoge.extras.Box;

public class PrivateInstitution {

    private final DataPrivateInstitution dataPrivateInstitution;
    private final Map<String,Map<String,Procedure>> procHiddenMap;
    private final Map<String,Step> stepsHidden; //sobra? todo

    public PrivateInstitution(Map<String,Object> data, Box<Boolean> dataMissing) {
        this.dataPrivateInstitution = DataPrivateInstitution.parse(data,dataMissing);

        List<DataStep> dataStepsHidden = DataStep.parseMultiple(dataPrivateInstitution.getStepsHidden(),dataMissing);
        this.stepsHidden = new HashMap<>();
        for(DataStep dataStep : dataStepsHidden) {
            List<DataQuestion> dataQuestions = DataQuestion.parseMultiple(dataStep.getMapQuestions(),dataMissing);
            List<DataAnswer> dataAnswers = DataAnswer.parseMultiple(dataStep.getMapAnswers(),dataMissing);
            Map<String,Answer> answers = dataAnswers.stream().collect(Collectors.toMap(DataAnswer::getIdAnswer, Answer::new, (prev, next) -> prev));
            List<Question> questions =  dataQuestions.stream().map(q -> new Question(q,answers)).collect(Collectors.toList());
            stepsHidden.put(dataStep.getIdStep(), new Step(dataStep,questions));
        }

        List<Step> stepsHiddenList = new ArrayList<>(stepsHidden.values());
        this.procHiddenMap = new HashMap<>();
        Map<String, Map<String, DataProcedure>> dataHiddenProcedures = DataProcedure.parseMultiple(dataPrivateInstitution.getProcHidden(),dataMissing);
        for(Map.Entry<String,Map<String, DataProcedure>> entry : dataHiddenProcedures.entrySet()) {
            this.procHiddenMap.put(entry.getKey(),entry.getValue().entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey,d -> new Procedure(d.getValue(), stepsHiddenList,false),(prev,next)->prev)));
        }
    }

    public static PrivateInstitution createEmpty() {
        return new PrivateInstitution(new HashMap<>(),new Box<>());
    }

    public Map<String,Object> toMap(){
        return dataPrivateInstitution.toMap();
    }

    // --- Getters ---

    public String getEmail() {
        return dataPrivateInstitution.getEmail();
    }


    public String getID() {
        return dataPrivateInstitution.getID();
    }

    public Set<String> getModerators() {
        return dataPrivateInstitution.getModerators();
    }

    public Set<String> getAskingForBeingModerator() {
        return dataPrivateInstitution.getAskingForBeingModerator();
    }

    public Map<String, Set<String>> getProceduresExperts() {
        return dataPrivateInstitution.getProceduresExperts();
    }

    public Map<String, Set<String>> getAskingForBeingExpert() {
        return dataPrivateInstitution.getAskingForBeingExpert();
    }

    public Procedure getProcHidden(String id, String version) {
        Map<String,Procedure> aux = procHiddenMap.get(id);
        if(aux!=null) {
            return aux.get(version);
        }
        return null;
    }

    public List<Procedure> getCopyVersionsProcHidden(String id) {
        Map<String,Procedure> aux = procHiddenMap.get(id);
        if(aux!=null) {
            return new ArrayList<>(aux.values());
        }
        return new ArrayList<>();
    }

    public Set<Procedure> getCopyAllProcHidden() {
        Set<Procedure> result = new HashSet<>();
        for(Map.Entry<String,Map<String,Procedure>> entry : procHiddenMap.entrySet()) {
            result.addAll(entry.getValue().values());
        }
        return result;
    }

    public Map<String, Step> getStepsHidden() {
        return stepsHidden;
    }

    // --- Setters ---

    public List<String> addNewProcDraft(Procedure draft) {
        //Se supone que los Data de Procedure, Step están al día
        //Question, Answer ?

        //Update this class
        procHiddenMap.putIfAbsent(draft.getIdProcedure(), new HashMap<>());
        procHiddenMap.get(draft.getIdProcedure()).put(draft.getVersionProcedure(), draft);
        for(Node<Step> node : draft.getAllSteps()) {
            stepsHidden.put(node.getNode().getIdStep(), node.getNode());
        }

        //Update DataPrivateInstitution
        dataPrivateInstitution.getProcHidden().putIfAbsent(draft.getIdProcedure(), new HashMap<>());
        dataPrivateInstitution.getProcHidden().get(draft.getIdProcedure()).put(draft.getVersionProcedure(), draft.dataToMap());
        for(Node<Step> step : draft.getAllSteps()) {
            dataPrivateInstitution.getStepsHidden().put(step.getNode().getIdStep(), step.getNode().toMap());
        }

        //Which fields have to be updated on DB
        return new ArrayList<>(Arrays.asList(DataPrivateInstitution.KEY_PROC_HIDDEN,DataPrivateInstitution.KEY_STEPS_HIDDEN));
    }

    public List<String> deleteDraft(Procedure draft) {
        //Se supone que los Data de Procedure, Step están al día
        //Question, Answer ?

        //Update this class
        procHiddenMap.get(draft.getIdProcedure()).remove(draft.getVersionProcedure());
        if(procHiddenMap.get(draft.getIdProcedure()).isEmpty()) {
            procHiddenMap.remove(draft.getIdProcedure());
        }
        for(Node<Step> node : draft.getAllSteps()) {
            stepsHidden.remove(node.getNode().getIdStep());
        }

        //Update DataPrivateInstitution
        dataPrivateInstitution.getProcHidden().get(draft.getIdProcedure()).remove(draft.getVersionProcedure());
        if(dataPrivateInstitution.getProcHidden().get(draft.getIdProcedure()).isEmpty()) {
            dataPrivateInstitution.getProcHidden().remove(draft.getIdProcedure());
        }
        for(Node<Step> step : draft.getAllSteps()) {
            dataPrivateInstitution.getStepsHidden().remove(step.getNode().getIdStep());
        }

        //Which fields have to be updated on DB
        return new ArrayList<>(Arrays.asList(DataPrivateInstitution.KEY_PROC_HIDDEN,DataPrivateInstitution.KEY_STEPS_HIDDEN));
    }

}
