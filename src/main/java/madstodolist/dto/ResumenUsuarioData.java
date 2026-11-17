package madstodolist.dto;

import java.util.List;

public class ResumenUsuarioData {

    private int totalTareas;
    private int tareasCompletadas;
    private int tareasPendientes;
    private int porcentajeCompletado;
    private List<EquipoData> equipos;

    public ResumenUsuarioData(int totalTareas, int tareasCompletadas, int tareasPendientes,
                              int porcentajeCompletado, List<EquipoData> equipos) {
        this.totalTareas = totalTareas;
        this.tareasCompletadas = tareasCompletadas;
        this.tareasPendientes = tareasPendientes;
        this.porcentajeCompletado = porcentajeCompletado;
        this.equipos = equipos;
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

    public int getPorcentajeCompletado() {
        return porcentajeCompletado;
    }

    public List<EquipoData> getEquipos() {
        return equipos;
    }
}