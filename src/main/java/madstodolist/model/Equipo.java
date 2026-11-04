package madstodolist.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "equipos")
public class Equipo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "equipo_usuario",
            joinColumns = @JoinColumn(name = "fk_equipo"),
            inverseJoinColumns = @JoinColumn(name = "fk_usuario")
    )
    private Set<Usuario> usuarios = new HashSet<>();

    public Equipo() {}
    // Constructor mínimo que recibe el nombre
    public Equipo(String nombre) {
        this.nombre = nombre;
    }

    // Getters y Setters previos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    //método helper para obtener los usuarios del equipo
    public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    //método helper para añadir un usuario al equipo
    public void addUsuario(Usuario usuario) {
        this.usuarios.add(usuario);
        usuario.getEquipos().add(this);
    }

    //método helper para eliminar un usuario del equipo
    public void removeUsuario(Usuario usuario) {
        this.getUsuarios().remove(usuario);
        usuario.getEquipos().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipo equipo = (Equipo) o;

        // Si ambos tienen id, comparamos por id
        if (id != null && equipo.id != null) {
            return Objects.equals(id, equipo.id);
        }
        // Si no tienen id, comparamos por nombre
        return Objects.equals(nombre, equipo.nombre);
    }

    @Override
    public int hashCode() {
        // Generamos el hashCode basándonos en el id si existe, o en el nombre si es nulo
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(nombre);
    }
}