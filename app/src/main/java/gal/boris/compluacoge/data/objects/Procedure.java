package gal.boris.compluacoge.data.objects;

import androidx.core.util.Pair;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import gal.boris.compluacoge.R;
import gal.boris.compluacoge.data.IDProcedure;
import gal.boris.compluacoge.data.Node;
import gal.boris.compluacoge.data.pojos.DataProcedure;
import gal.boris.compluacoge.logic.CloudDB;

public class Procedure implements IDProcedure {

    private DataProcedure dataProcedure;
    private Map<String,Node<Step>> steps;
    private Node<Step> rootTree;
    private Node<Step> endTree;

    public Procedure(DataProcedure dataProcedure, List<Step> allSteps, boolean checkCorrectness) {
        this.dataProcedure = dataProcedure;
        this.steps = new HashMap<>();
        createStepsGraph(allSteps,checkCorrectness);
    }

    public static Procedure createEmpty(String publicIdInstitution, String initialStepName, String initialDescription) {
        Procedure proc = new Procedure();
        proc.dataProcedure = DataProcedure.createEmpty(CloudDB.autoId(), "-1", publicIdInstitution);
        proc.rootTree = new Node<>(Step.createEmpty(initialStepName,initialDescription));
        proc.steps = new HashMap<>(Map.of(proc.rootTree.getNode().getIdStep(),proc.rootTree));
        proc.dataProcedure.getMapTreeSteps().put(proc.rootTree.getNode().getIdStep(), new ArrayList<>());
        return proc;
    }

    public Procedure(Procedure that) {
        this.dataProcedure = new DataProcedure(that.dataProcedure);
        Map<String,Node<Step>> mapNewNodes = new HashMap<>();
        for(Map.Entry<String,Node<Step>> entry : that.steps.entrySet()) {
            mapNewNodes.put(entry.getKey(),new Node<>(new Step(entry.getValue().getNode())));
        }
        for(Map.Entry<String,Node<Step>> oldEntry : that.steps.entrySet()) {
            Node<Step> newNode = mapNewNodes.get(oldEntry.getKey());
            newNode.addParents(oldEntry.getValue().getParents().stream()
                    .map(n -> mapNewNodes.get(n.getNode().getIdStep()))
                    .collect(Collectors.toList()));
            newNode.addChildren(oldEntry.getValue().getChildren().stream()
                    .map(n -> mapNewNodes.get(n.getNode().getIdStep()))
                    .collect(Collectors.toList()));
        }
        this.steps = mapNewNodes;
        this.rootTree = that.rootTree!=null ? mapNewNodes.get(that.rootTree.getNode().getIdStep()) : null;
    }

    private Procedure() {}

    public static Procedure createBased(Procedure that) { //respecto al const. de copia, este cambia el id de los steps
        Procedure proc = new Procedure();
        proc.dataProcedure = new DataProcedure(that.dataProcedure);
        Map<String,Node<Step>> mapNewNodes = new HashMap<>();
        for(Map.Entry<String,Node<Step>> entry : that.steps.entrySet()) {
            mapNewNodes.put(entry.getKey(),new Node<>(Step.createBased(entry.getValue().getNode())));
        }
        Map<String,List<String>> newMapTreeSteps = new HashMap<>();
        for(Map.Entry<String,List<String>> entry : proc.dataProcedure.getMapTreeSteps().entrySet()) {
            List<String> newList = entry.getValue().stream()
                    .map(id -> mapNewNodes.get(id).getNode().getIdStep())
                    .collect(Collectors.toList());
            newMapTreeSteps.put(mapNewNodes.get(entry.getKey()).getNode().getIdStep(),newList);
        }
        proc.dataProcedure.setMapTreeSteps(newMapTreeSteps);
        for(Map.Entry<String,Node<Step>> oldEntry : that.steps.entrySet()) {
            Node<Step> newNode = mapNewNodes.get(oldEntry.getKey());
            newNode.addParents(oldEntry.getValue().getParents().stream()
                    .map(n -> mapNewNodes.get(n.getNode().getIdStep()))
                    .collect(Collectors.toList()));
            newNode.addChildren(oldEntry.getValue().getChildren().stream()
                    .map(n -> mapNewNodes.get(n.getNode().getIdStep()))
                    .collect(Collectors.toList()));
        }
        proc.steps = mapNewNodes;
        proc.rootTree = that.rootTree!=null ? mapNewNodes.get(that.rootTree.getNode().getIdStep()) : null;
        return proc;
    }

    private void createStepsGraph(List<Step> allSteps, boolean checkCorrectness) {
        Map<String,Node<Step>> map = allSteps.stream()
                .collect(Collectors.toMap(Step::getIdStep,Node<Step>::new,(prev,next)->prev));

        for(Map.Entry<String, List<String>> mapTree : dataProcedure.getMapTreeSteps().entrySet()) {
            Node<Step> nodeChild = map.get(mapTree.getKey());
            steps.put(nodeChild.getNode().getIdStep(),nodeChild);
            List<String> parents = mapTree.getValue();
            for(String parent : parents) {
                Node<Step> nodeParent = map.get(parent);
                steps.put(nodeParent.getNode().getIdStep(),nodeParent);
                nodeChild.addParent(nodeParent);
                nodeParent.addChild(nodeChild);
            }
        }
        int numBeginning = 0;
        Node<Step> beginning = null;
        for(Node<Step> node : steps.values()) {
            if(node.isBeginning()) {
                beginning = node;
                numBeginning++;
            }
        }
        if(numBeginning != 1) throw new IllegalArgumentException();
        rootTree = beginning;
        endTree = null;

        if(checkCorrectness) {
            if(!checkGraphCorrectness().first) throw new IllegalArgumentException();
        }
    }

    public Pair<Boolean,Integer> checkGraphCorrectness() {
        int numEnd = 0;
        endTree = null;
        for(Node<Step> node : steps.values()) {
            if(node.isEnd()) {
                endTree = node;
                numEnd++;
            }
        }
        Integer error = null;
        if(rootTree==null) {
            error = R.string.error_publishing_start_null;
            FirebaseCrashlytics.getInstance().recordException(new IllegalStateException("no habia root step"));
        } else if(endTree==null) {
            error = R.string.error_publishing_end_null;
        } else if(numEnd!=1) {
            error = R.string.error_publishing_end_multiple;
        }
        if(error==null) {
            Pair<Boolean,Set<Node<Step>>> pair = rootTree.allRoadsLeadToRome(endTree);
            if(!steps.equals(pair.second)) {
                FirebaseCrashlytics.getInstance().recordException(new IllegalStateException("steps no estaba correctamente"));
                steps.clear();
                for(Node<Step> node : pair.second) {
                    steps.put(node.getNode().getIdStep(),node);
                }
            }
            if(!pair.first) {
                error = R.string.error_publishing_end_multiple;
            }
        }
        return new Pair<>(error==null,error);
    }

    // --- Getters ---

    public Node<Step> getRootStep() {
        return rootTree;
    }

    public Node<Step> getEndStep() {
        return endTree;
    }

    public String getIdProcedure() {
        return dataProcedure.getIdProcedure();
    }

    public String getVersionProcedure() {
        return dataProcedure.getVersionProcedure();
    }

    public String getPublicIdInstitution() {
        return dataProcedure.getPublicIdInstitution();
    }

    public String getName() {
        return dataProcedure.getName();
    }

    public String getShortDescription() {
        return dataProcedure.getShortDescription();
    }

    public String getLongDescription() {
        return dataProcedure.getLongDescription();
    }

    public List<String> getTags() {
        return dataProcedure.getTags();
    }

    public Boolean getClosed() {return dataProcedure.getClosed(); }

    public Long getCreated() {
        return dataProcedure.getCreated();
    }

    public Long getLastModified() {
        return dataProcedure.getLastModified();
    }

    public List<Node<Step>> getAllSteps() {
        return Collections.unmodifiableList(new ArrayList<>(steps.values()));
    }

    public Node<Step> getNode(String id) {
        return steps.getOrDefault(id,null);
    }

    public Map<String,Object> dataToMap() {
        return dataProcedure.toMap();
    }

    // --- Setter ---

    public void addStep(Node<Step> parent, Node<Step> newChild) {
        this.steps.put(newChild.getNode().getIdStep(),newChild);
        linkStep(parent,newChild);
    }

    public void linkStep(Node<Step> parent, Node<Step> newChild) {
        //Update Procedure
        parent.addChild(newChild);
        newChild.addParent(parent);

        //Update DataProcedure
        if(!dataProcedure.getMapTreeSteps().containsKey(newChild.getNode().getIdStep())) {
            dataProcedure.getMapTreeSteps().put(newChild.getNode().getIdStep(), new ArrayList<>());
        }
        List<String> mapAux = dataProcedure.getMapTreeSteps().get(newChild.getNode().getIdStep());
        mapAux.add(parent.getNode().getIdStep());
    }

    public boolean removeStep(Node<Step> step) {
        if(step.canDelete()) {
            this.steps.remove(step.getNode().getIdStep());

            //Update DataProcedure
            dataProcedure.getMapTreeSteps().remove(step.getNode().getIdStep());
            for(Node<Step> child : step.getChildren()) {
                dataProcedure.getMapTreeSteps().get(child.getNode().getIdStep()).remove(step.getNode().getIdStep());
            }

            //Update Procedure
            for(Node<Step> child : step.getChildren()) {
                child.getParents().remove(step);
            }
            for(Node<Step> parent : step.getParents()) {
                parent.getChildren().remove(step);
            }

            return true;
        }
        return false;
    }

    public void generateWords() {
        long maxFrequency = 0L;
        dataProcedure.setSearchIndexName(CloudDB.autoId());
        Map<String,Long> words = new HashMap<>();
        List<String> texts = new ArrayList<>();
        texts.add(getName());
        texts.add(getShortDescription());
        texts.add(getLongDescription());
        texts.add(getTags().stream().reduce("",(a,b)->a+"\n"+b));
        texts.addAll(steps.values().stream()
                .map(n -> n.getNode().getName()).collect(Collectors.toList()));
        texts.addAll(steps.values().stream()
                .map(n -> n.getNode().getDescription()).collect(Collectors.toList()));
        for(String text : texts) {
            String[] wordsStep = text.split("[ \n]+");
            for(String word : wordsStep) {
                if("".equals(word) || " ".equals(word)) {
                    continue;
                }
                long freq = words.getOrDefault(word,0L)+1;
                maxFrequency = Math.max(maxFrequency,freq);
                words.put(word,freq);
            }
        }
        words.put(" ",maxFrequency);
        dataProcedure.setSearchWords(words);
    }

    public void setIdProcedure(String idProcedure) {
        dataProcedure.setIdProcedure(idProcedure);
    }

    public void setVersionProcedure(String versionProcedure) {
        dataProcedure.setVersionProcedure(versionProcedure);
    }

    public void setName(String name) {
        dataProcedure.setName(name);
    }

    public void setShortDescription(String shortDescription) {
        dataProcedure.setShortDescription(shortDescription);
    }

    public void setLongDescription(String longDescription) {
        dataProcedure.setLongDescription(longDescription);
    }

    public void setTags(List<String> tags) {
        dataProcedure.setTags(tags);
    }

    public void setClosed(boolean closed) {
        dataProcedure.setClosed(closed);
    }

    public void setCreated(long created) {
        dataProcedure.setCreated(created);
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Procedure procedure = (Procedure) o;
        return Objects.equals(dataProcedure, procedure.dataProcedure) &&
                Objects.equals(steps, procedure.steps) &&
                Objects.equals(rootTree, procedure.rootTree);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataProcedure, steps, rootTree);
    }

}
