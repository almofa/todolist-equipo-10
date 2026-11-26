package madstodolist.service;

import madstodolist.model.Tarea;
import madstodolist.repository.TareaRepository;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import madstodolist.dto.TareaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class TareaService {

    Logger logger = LoggerFactory.getLogger(TareaService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    private ModelMapper modelMapper;

    private static final Set<String> PRIORIDADES_VALIDAS =
            new HashSet<>(Arrays.asList("ALTA", "MEDIA", "BAJA"));

    private String normalizarYValidarPrioridad(String prioridad) {
        if (prioridad == null || prioridad.trim().isEmpty()) {
            return null;
        }
        String normalizada = prioridad.trim().toUpperCase();
        if (!PRIORIDADES_VALIDAS.contains(normalizada)) {
            throw new TareaServiceException("Prioridad no válida: " + prioridad);
        }
        return normalizada;
    }


    @Transactional
    public TareaData nuevaTareaUsuario(Long idUsuario, String tituloTarea) {
        return nuevaTareaUsuario(idUsuario, tituloTarea, null);
    }

    @Transactional
    public TareaData nuevaTareaUsuario(Long idUsuario, String tituloTarea, String prioridad) {
        logger.debug("Añadiendo tarea " + tituloTarea + " con prioridad " + prioridad + " al usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al crear tarea " + tituloTarea);
        }
        String prioridadValida = normalizarYValidarPrioridad(prioridad);
        Tarea tarea = new Tarea(usuario, tituloTarea);
        if (prioridadValida != null) {
            tarea.setPrioridad(prioridadValida);
        }
        tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional(readOnly = true)
    public List<TareaData> allTareasUsuario(Long idUsuario) {
        logger.debug("Devolviendo todas las tareas del usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al listar tareas ");
        }
        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        List<TareaData> tareas = usuario.getTareas().stream()
                .map(tarea -> modelMapper.map(tarea, TareaData.class))
                .collect(Collectors.toList());
        // Ordenamos la lista por id de tarea
        Collections.sort(tareas, (a, b) -> a.getId() < b.getId() ? -1 : a.getId() == b.getId() ? 0 : 1);
        return tareas;
    }

    @Transactional(readOnly = true)
    public List<TareaData> allTareasUsuarioPorEstadoYPrioridad(Long idUsuario, String estado, String prioridad) {
        logger.debug("Devolviendo tareas del usuario " + idUsuario + " con estado=" + estado + " y prioridad=" + prioridad);
        String prioridadNorm = (prioridad == null) ? "" : prioridad.trim();
        if (prioridadNorm.isEmpty() || prioridadNorm.equalsIgnoreCase("todas")) {
            return allTareasUsuarioPorEstado(idUsuario, estado);
        }

        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al listar tareas ");
        }

        String prioridadValida = normalizarYValidarPrioridad(prioridadNorm);
        String estadoNorm = (estado == null) ? "" : estado.trim();

        List<Tarea> tareas;
        if (estadoNorm.equalsIgnoreCase("completadas")) {
            tareas = tareaRepository.findByUsuarioIdAndCompletadaAndPrioridadOrderByIdAsc(idUsuario, true, prioridadValida);
        } else if (estadoNorm.equalsIgnoreCase("pendientes")) {
            tareas = tareaRepository.findByUsuarioIdAndCompletadaAndPrioridadOrderByIdAsc(idUsuario, false, prioridadValida);
        } else {
            tareas = tareaRepository.findByUsuarioIdAndPrioridadOrderByIdAsc(idUsuario, prioridadValida);
        }

        return tareas.stream()
                .map(tarea -> modelMapper.map(tarea, TareaData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TareaData findById(Long tareaId) {
        logger.debug("Buscando tarea " + tareaId);
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        if (tarea == null) return null;
        else return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData modificaTarea(Long idTarea, String nuevoTitulo) {
        return modificaTarea(idTarea, nuevoTitulo, null);
    }

    @Transactional
    public TareaData modificaTarea(Long idTarea, String nuevoTitulo, String nuevaPrioridad) {
        logger.debug("Modificando tarea " + idTarea + " - " + nuevoTitulo + " - prioridad " + nuevaPrioridad);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        String prioridadValida = normalizarYValidarPrioridad(nuevaPrioridad);
        tarea.setTitulo(nuevoTitulo);
        if (prioridadValida != null) {
            tarea.setPrioridad(prioridadValida);
        }
        tarea = tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public void borraTarea(Long idTarea) {
        logger.debug("Borrando tarea " + idTarea);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tareaRepository.delete(tarea);
    }

    @Transactional
    public boolean usuarioContieneTarea(Long usuarioId, Long tareaId) {
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (tarea == null || usuario == null) {
            throw new TareaServiceException("No existe tarea o usuario id");
        }
        return usuario.getTareas().contains(tarea);
    }

    @Transactional
    public TareaData cambiarEstadoCompletada(Long idTarea) {
        logger.debug("Cambiando estado completada de tarea " + idTarea);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tarea.setCompletada(!tarea.isCompletada());
        tarea = tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional(readOnly = true)
    public List<TareaData> allTareasUsuarioPorEstado(Long idUsuario, String estado) {
        logger.debug("Devolviendo tareas del usuario " + idUsuario + " con estado " + estado);
        String estadoNorm = (estado == null) ? "" : estado.trim();
        if (estadoNorm.isEmpty() || estadoNorm.equalsIgnoreCase("todas")) {
            return allTareasUsuario(idUsuario);
        }
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al listar tareas ");
        }
        boolean completada;
        if (estadoNorm.equalsIgnoreCase("completadas")) {
            completada = true;
        } else if (estadoNorm.equalsIgnoreCase("pendientes")) {
            completada = false;
        } else {
            return allTareasUsuario(idUsuario);
        }
        return tareaRepository.findByUsuarioIdAndCompletadaOrderByIdAsc(idUsuario, completada)
                .stream()
                .map(tarea -> modelMapper.map(tarea, TareaData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long contarTareasPorEstado(Long idUsuario, boolean completada) {
        logger.debug("Contando tareas del usuario " + idUsuario + " con completada=" + completada);
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al contar tareas");
        }
        return tareaRepository.countByUsuarioIdAndCompletada(idUsuario, completada);
    }
}
