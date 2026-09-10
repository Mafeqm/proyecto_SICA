package com.sica.proxy;

import com.sica.exception.AccesoDenegadoException;
import com.sica.model.Permiso;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.service.VisitaService;
import com.sica.strategy.EstrategiaAcceso;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Proxy de Seguridad RBAC (Patrón de Diseño Proxy / Protection Proxy).
 * Implementa la interfaz VisitaService interceptando cada método para verificar
 * si el usuario
 * autenticado posee los permisos adecuados (definidos en su Rol) antes de
 * delegar la llamada al servicio real.
 * 
 * Principios SOLID:
 * - Single Responsibility Principle (SRP): Control exclusivo de autorización y
 * seguridad RBAC.
 * - Open/Closed Principle (OCP): Añade validaciones de seguridad sin tocar la
 * implementación de negocio original.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class SeguridadVisitaServiceProxy implements VisitaService {

    private final VisitaService servicioReal;
    private final Usuario usuarioAutenticado;

    /**
     * Codigos de permisos estandar del sistema
     */
    public static final String PERMISO_REGISTRAR_ACCESO = "VISITA_REGISTRAR";
    public static final String PERMISO_REGISTRAR_SALIDA = "VISITA_SALIDA";
    public static final String PERMISO_CONSULTAR_VISITAS = "VISITA_CONSULTAR";

    /**
     * Constructor del Proxy de Seguridad.
     * 
     * @param servicioReal       Instancia concreta de VisitaService a proteger.
     * @param usuarioAutenticado Usuario activo en la sesión del sistema.
     */
    public SeguridadVisitaServiceProxy(VisitaService servicioReal, Usuario usuarioAutenticado) {
        this.servicioReal = servicioReal;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    /**
     * Valida si el usuario actual posee el permiso especificado.
     * 
     * @param codigoPermiso Código del permiso requerido.
     * @throws AccesoDenegadoException Si no está autenticado o no posee el permiso.
     */
    private void validarPermiso(String codigoPermiso) throws AccesoDenegadoException {
        if (usuarioAutenticado == null) {
            throw new AccesoDenegadoException("ANÓNIMO", codigoPermiso,
                    "ACCESO DENEGADO: No existe una sesión de usuario autenticada activa.");
        }

        if (!usuarioAutenticado.isActivo()) {
            throw new AccesoDenegadoException(usuarioAutenticado.getUsername(), codigoPermiso,
                    "ACCESO DENEGADO: La cuenta del usuario '" + usuarioAutenticado.getUsername()
                            + "' está inactiva o bloqueada.");
        }

        if (usuarioAutenticado.getRol() == null) {
            throw new AccesoDenegadoException(usuarioAutenticado.getUsername(), codigoPermiso,
                    "ACCESO DENEGADO: El usuario '" + usuarioAutenticado.getUsername() + "' no tiene un Rol asignado.");
        }

        // El rol ADMINISTRADOR tiene acceso maestro a todas las funciones
        if ("ADMINISTRADOR".equalsIgnoreCase(usuarioAutenticado.getRol().getNombre())) {
            return;
        }

        boolean tienePermiso = false;
        if (usuarioAutenticado.getRol().getPermisos() != null) {
            for (Permiso p : usuarioAutenticado.getRol().getPermisos()) {
                if (p != null && (codigoPermiso.equalsIgnoreCase(p.getCodigo())
                        || "ACCESO_TOTAL".equalsIgnoreCase(p.getCodigo()))) {
                    tienePermiso = true;
                    break;
                }
            }
        }

        if (!tienePermiso) {
            throw new AccesoDenegadoException(
                    usuarioAutenticado.getUsername(),
                    codigoPermiso,
                    "ACCESO DENEGADO [RBAC]: El usuario '" + usuarioAutenticado.getUsername()
                            + "' con rol '" + usuarioAutenticado.getRol().getNombre()
                            + "' NO posee el permiso requerido ['" + codigoPermiso + "'].");
        }
    }

    @Override
    public Visita registrarAcceso(Persona persona, EstrategiaAcceso estrategia, Usuario usuarioRegistro, String motivo,
            String observaciones) {
        try {
            // Validar permiso RBAC antes de ejecutar
            validarPermiso(PERMISO_REGISTRAR_ACCESO);
            System.out.println("[PROXY SEGURIDAD] ✔️ Permiso '" + PERMISO_REGISTRAR_ACCESO + "' validado para usuario: "
                    + usuarioAutenticado.getUsername());

            // Delegar la ejecución al servicio real protegido
            return servicioReal.registrarAcceso(persona, estrategia,
                    usuarioRegistro != null ? usuarioRegistro : usuarioAutenticado, motivo, observaciones);
        } catch (AccesoDenegadoException e) {
            System.err.println("[PROXY SEGURIDAD] 🚫 " + e.getMessage());
            throw new RuntimeException(e); // Encapsular en RuntimeException para cumplir la firma de interfaz o lanzar
                                           // la excepcion
        }
    }

    /**
     * Sobrecarga conveniente que lanza directamente AccesoDenegadoException
     * chequeada.
     */
    public Visita registrarAccesoConExcepcion(Persona persona, EstrategiaAcceso estrategia, Usuario usuarioRegistro,
            String motivo, String observaciones) throws AccesoDenegadoException {
        validarPermiso(PERMISO_REGISTRAR_ACCESO);
        System.out.println("[PROXY SEGURIDAD] ✔️ Permiso '" + PERMISO_REGISTRAR_ACCESO + "' validado para usuario: "
                + usuarioAutenticado.getUsername());
        return servicioReal.registrarAcceso(persona, estrategia,
                usuarioRegistro != null ? usuarioRegistro : usuarioAutenticado, motivo, observaciones);
    }

    @Override
    public boolean registrarSalida(Long visitaId, LocalDateTime fechaSalida, String observaciones) {
        try {
            validarPermiso(PERMISO_REGISTRAR_SALIDA);
            System.out.println("[PROXY SEGURIDAD] ✔️ Permiso '" + PERMISO_REGISTRAR_SALIDA + "' validado para usuario: "
                    + usuarioAutenticado.getUsername());
            return servicioReal.registrarSalida(visitaId, fechaSalida, observaciones);
        } catch (AccesoDenegadoException e) {
            System.err.println("[PROXY SEGURIDAD] 🚫 " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean registrarSalidaConExcepcion(Long visitaId, LocalDateTime fechaSalida, String observaciones)
            throws AccesoDenegadoException {
        validarPermiso(PERMISO_REGISTRAR_SALIDA);
        System.out.println("[PROXY SEGURIDAD] ✔️ Permiso '" + PERMISO_REGISTRAR_SALIDA + "' validado para usuario: "
                + usuarioAutenticado.getUsername());
        return servicioReal.registrarSalida(visitaId, fechaSalida, observaciones);
    }

    @Override
    public List<Visita> listarTodas() {
        try {
            validarPermiso(PERMISO_CONSULTAR_VISITAS);
            return servicioReal.listarTodas();
        } catch (AccesoDenegadoException e) {
            System.err.println("[PROXY SEGURIDAD] 🚫 " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Visita> filtrarVisitasPorEstado(List<Visita> visitas, String estado) {
        return servicioReal.filtrarVisitasPorEstado(visitas, estado);
    }

    @Override
    public List<Visita> obtenerVisitasRecientesPorPersona(List<Visita> visitas, Long personaId) {
        return servicioReal.obtenerVisitasRecientesPorPersona(visitas, personaId);
    }

    @Override
    public long contarVisitasActivas(List<Visita> visitas) {
        return servicioReal.contarVisitasActivas(visitas);
    }

    @Override
    public List<Visita> consultarPersonalPresenteEmpresa(Usuario funcionario) {
        // Usa el usuario autenticado del proxy si el parámetro no se proporciona o valida coherencia
        Usuario usuarioAUsar = funcionario != null ? funcionario : usuarioAutenticado;
        return servicioReal.consultarPersonalPresenteEmpresa(usuarioAUsar);
    }

    @Override
    public List<Visita> listarPersonalPresentePorEmpresa(Long empresaId) {
        return servicioReal.listarPersonalPresentePorEmpresa(empresaId);
    }

    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }
}
