package madstodolist.dto;

public class ResumenUsuarioData {

    private int totalTareas;
    private int tareasCompletadas;
    private int tareasPendientes;
    private int porcentajeCompletado;

    public ResumenUsuarioData(int totalTareas, int tareasCompletadas, int tareasPendientes, int porcentajeCompletado) {
        this.totalTareas = totalTareas;
        this.tareasCompletadas = tareasCompletadas;
        this.tareasPendientes = tareasPendientes;
        this.porcentajeCompletado = porcentajeCompletado;
    }

    public int getTotalTareas() {
        return totalTareas;
    }

    public int getTareasCompletadas() {
        return tareasCompletadas;
    }

    public int getTareasPendientes() {
        return tareasPendientes;
    }

    public int getPorcentajeCompletado(){
        return porcentajeCompletado;
    }
}