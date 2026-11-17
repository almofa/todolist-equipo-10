package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.ResumenUsuarioData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResumenService {

    @Autowired
    private TareaService tareaService;

    @Autowired
    private EquipoService equipoService;

    @Transactional(readOnly = true)
    public ResumenUsuarioData resumenUsuario(Long idUsuario) {
        int completadas = tareaService.allTareasUsuarioPorEstado(idUsuario, "completadas").size();
        int pendientes = tareaService.allTareasUsuarioPorEstado(idUsuario, "pendientes").size();
        int total = completadas + pendientes;
        int porcentaje = total == 0 ? 0 : (int) Math.round(completadas * 100.0 / total);
        List<EquipoData> equipos = equipoService.equiposUsuario(idUsuario);
        return new ResumenUsuarioData(total, completadas, pendientes, porcentaje, equipos);
    }
}