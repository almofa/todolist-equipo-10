package madstodolist.service;

import madstodolist.dto.ResumenUsuarioData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumenService {

    @Autowired
    private TareaService tareaService;

    @Transactional(readOnly = true)
    public ResumenUsuarioData resumenUsuario(Long idUsuario) {
        int completadas = tareaService.allTareasUsuarioPorEstado(idUsuario, "completadas").size();
        int pendientes = tareaService.allTareasUsuarioPorEstado(idUsuario, "pendientes").size();
        return new ResumenUsuarioData(completadas + pendientes, completadas, pendientes);
    }
}