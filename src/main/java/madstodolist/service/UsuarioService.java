package madstodolist.service;

import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public enum LoginStatus {LOGIN_OK, USER_NOT_FOUND, ERROR_PASSWORD, USER_BLOCKED}

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public LoginStatus login(String eMail, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(eMail);
        if (!usuario.isPresent()) {
            return LoginStatus.USER_NOT_FOUND;
        } else if (!usuario.get().getPassword().equals(password)) {
            return LoginStatus.ERROR_PASSWORD;
        } else if (usuario.get().getBloqueado()) {
            return LoginStatus.USER_BLOCKED;
        } else {
            return LoginStatus.LOGIN_OK;
        }
    }

    // Se añade un usuario en la aplicación.
    // El email y password del usuario deben ser distinto de null
    // El email no debe estar registrado en la base de datos
    @Transactional
    public UsuarioData registrar(UsuarioData usuario) {
        Optional<Usuario> usuarioBD = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioBD.isPresent())
            throw new UsuarioServiceException("El usuario " + usuario.getEmail() + " ya está registrado");
        if (usuario.getEmail() == null || usuario.getPassword() == null)
            throw new UsuarioServiceException("El usuario no tiene email o password");

        // Si intenta registrarse como admin pero YA existe uno, no se le permite
        if (Boolean.TRUE.equals(usuario.getAdmin()) && existeAdmin()) {
            usuario.setAdmin(false);
        }

        Usuario usuarioNuevo = modelMapper.map(usuario, Usuario.class);

        usuarioRepository.save(usuarioNuevo);
        return modelMapper.map(usuarioNuevo, UsuarioData.class);
    }

    @Transactional(readOnly = true)
    public UsuarioData findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findById(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> findAll() {
        Iterable<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioData> usuariosData = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            usuariosData.add(modelMapper.map(usuario, UsuarioData.class));
        }
        return usuariosData;
    }

    @Transactional(readOnly = true)
    public boolean existeAdmin(){
        List<Usuario> admins = usuarioRepository.findByAdminTrue();
        return !admins.isEmpty();
    }

    @Transactional
    public void setBloqueoUsuario(Long id, boolean bloqueado) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null) {
            usuario.setBloqueado(bloqueado);
            usuarioRepository.save(usuario);
        }
    }
}
