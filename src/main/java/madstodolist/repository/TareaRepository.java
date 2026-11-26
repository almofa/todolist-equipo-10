package madstodolist.repository;

import madstodolist.model.Tarea;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TareaRepository extends CrudRepository<Tarea, Long> {
    List<Tarea> findByUsuarioIdAndCompletadaOrderByIdAsc(Long usuarioId, boolean completada);

    List<Tarea> findByUsuarioIdAndPrioridadOrderByIdAsc(Long usuarioId, String prioridad);

    List<Tarea> findByUsuarioIdAndCompletadaAndPrioridadOrderByIdAsc(Long usuarioId, boolean completada, String prioridad);

    // Spring Data genera un SELECT COUNT(*) a partir del nombre del método
    long countByUsuarioIdAndCompletada(Long usuarioId, boolean completada);
}