package madstodolist.dto;

import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

public class RegistroData {

    private String email;
    private String nombre;
    private String password;

    // ANOTACIÓN CLAVE: Mapea la fecha "YYYY-MM-DD" que envía el input type="date"
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fechaNacimiento;

    private Boolean admin = false;

    // Getters y Setters...
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public Boolean getAdmin() { return admin; }
    public void setAdmin(Boolean admin) { this.admin = admin; }
}