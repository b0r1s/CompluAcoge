package gal.boris.compluacoge.data.objects;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gal.boris.compluacoge.data.IDProcedure;
import gal.boris.compluacoge.data.Node;
import gal.boris.compluacoge.data.StatusProcedure;
import gal.boris.compluacoge.data.pojos.DataAProcedure;

public class AProcedure implements IDProcedure {

    @NonNull
    private final DataAProcedure dataAProcedure;
    @NonNull
    private final Procedure procedure;
    @NonNull
    private final PublicInstitution publicInstitution;
    @NonNull
    private final List<Node<Step>> stepsDone; //Marca uno de los varios caminos hasta un nodo
    @NonNull
    private final StatusProcedure status;

    public AProcedure(@NonNull DataAProcedure dataAProcedure, @NonNull Procedure procedure, @NonNull PublicInstitution publicInstitution) {
        this.dataAProcedure = dataAProcedure;
        this.procedure = procedure;
        this.publicInstitution = publicInstitution;

        this.stepsDone = new ArrayList<>();
        stepsDone.add(procedure.getRootStep());
        for (int i = 1; i < dataAProcedure.getIdStepsDone().size(); i++) {
            String id = dataAProcedure.getIdStepsDone().get(i);
            Node<Step> next = null;
            for(Node<Step> child : stepsDone.get(stepsDone.size()-1).getChildren()) {
                if(child.getNode().getIdStep().equals(id)) {
                    next = child;
                    break;
                }
            }
            if(next==null) throw new IllegalArgumentException();
            stepsDone.add(next);
        }

        if(procedure.getEndStep().getNode().getIdStep().equals(stepsDone.get(stepsDone.size()-1).getNode().getIdStep())) {
            status = StatusProcedure.FINISHED;
        } else {
            status = procedure.getClosed() ? StatusProcedure.OLD_WITHOUT_FINISH : StatusProcedure.OPEN;
        }
    }

    public static AProcedure createProcBased(PublicInstitution publicInstitution, String idProc, String versionProc) {
        Procedure proc = publicInstitution.getProcVisible(idProc, versionProc);
        DataAProcedure dataAProcedure = new DataAProcedure(idProc, versionProc, publicInstitution.getID(),
                new ArrayList<>(),proc.getCreated(),proc.getLastModified());
        return new AProcedure(dataAProcedure,proc,publicInstitution);
    }

    public Map<String,Object> toMap(){
        return dataAProcedure.toMap();
    }

    // --- Getters ---

    public StatusProcedure getStatus() {
        return status;
    }

    public Procedure getProcedure() {
        return procedure;
    }

    public List<Node<Step>> getStepsDone() {
        return stepsDone;
    }

    public Node<Step> getStepNow() {
        return stepsDone.get(stepsDone.size()-1);
    }

    public List<Node<Step>> getPreviousStepsDone() {
        return new ArrayList<>(stepsDone.subList(0,stepsDone.size()-1));
    }

    public PublicInstitution getPublicInstitution() {
        return publicInstitution;
    }

    public String getIdProcedure() {
        return dataAProcedure.getIdProcedure();
    }

    public String getVersionProcedure() {
        return dataAProcedure.getVersionProcedure();
    }

    public Long getLastModified() {
        return dataAProcedure.getLastModified();
    }

    public double getProgress() {
        double done = stepsDone.size() - 1; //Mejor todo
        double missing = stepsDone.get(stepsDone.size()-1).getShortestWay(procedure.getEndStep());
        return done / (done+missing);
    }

    // --- Setter ---

    public void setStepsDone(List<Node<Step>> stepsDone) {
        this.stepsDone.clear();
        this.stepsDone.addAll(stepsDone);
        this.dataAProcedure.setIdStepsDone(stepsDone.stream()
                .map(n -> n.getNode().getIdStep())
                .collect(Collectors.toList()));
    }

    // --- Equals ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AProcedure that = (AProcedure) o;
        return dataAProcedure.equals(that.dataAProcedure) &&
                procedure.equals(that.procedure) &&
                publicInstitution.equals(that.publicInstitution) &&
                stepsDone.equals(that.stepsDone) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataAProcedure, procedure, publicInstitution, stepsDone, status);
    }
}
