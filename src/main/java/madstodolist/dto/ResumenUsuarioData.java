package madstodolist.dto;

public class ResumenUsuarioData {

    private int totalTareas;
    private int tareasCompletadas;
    private int tareasPendientes;

    public ResumenUsuarioData(int totalTareas, int tareasCompletadas, int tareasPendientes) {
        this.totalTareas = totalTareas;
        this.tareasCompletadas = tareasCompletadas;
        this.tareasPendientes = tareasPendientes;
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
}