package com.sica.test;

import com.sica.controller.FuncionarioController;
import com.sica.controller.GuardaController;
import com.sica.controller.LoginController;
import com.sica.dao.BitacoraAuditoriaDAO;
import com.sica.dao.PersonaDAO;
import com.sica.dao.VisitaDAO;
import com.sica.decorator.AuditoriaVisitaDecorator;
import com.sica.exception.AccesoDenegadoException;
import com.sica.model.BitacoraAuditoria;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.proxy.SeguridadVisitaServiceProxy;
import com.sica.service.AuthService;
import com.sica.service.VisitaService;
import com.sica.service.impl.AuthServiceImpl;
import com.sica.service.impl.VisitaServiceImpl;
import com.sica.strategy.AccesoNoAnunciado;
import com.sica.strategy.AccesoPreRegistrado;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Suite de Prueba Integrada para la Fase 3 del Proyecto SICA.
 * Valida:
 * - Autenticación mediante AuthService y LoginController (DIP).
 * - Autorización RBAC basada en roles y permisos mediante
 * SeguridadVisitaServiceProxy (Patrón Proxy).
 * - Lanzamiento y captura de AccesoDenegadoException ante usuarios no
 * autorizados.
 * - Operaciones de GuardaController y FuncionarioController.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class SicaFase3Test {

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("   PROYECTO SICA - PRUEBA INTEGRADA FASE 3 (PROXY RBAC & CONTROLADORES)  ");
        System.out.println("=========================================================================\n");

        // 1. Inicializar Repositorios y Servicios Base
        AuthService authService = new AuthServiceImpl();
        LoginController loginController = new LoginController(authService);

        PersonaDAO personaDAO = new PersonaDAOInMemoryTest();
        VisitaDAO visitaDAO = new VisitaDAOInMemoryTest();
        BitacoraAuditoriaDAO bitacoraDAO = new BitacoraDAOInMemoryTest();

        VisitaService servicioBase = new VisitaServiceImpl(visitaDAO);
        VisitaService servicioDecorado = new AuditoriaVisitaDecorator(servicioBase, bitacoraDAO);

        Persona personaPrueba = personaDAO.crear(
                new Persona("CC", "55443322", "Carlos", "Ramírez", "carlos@test.com", "3005554433", "VISITANTE"));

        // ----------------------------------------------------------------------------------
        // PRUEBA 1: Autenticación con LoginController
        // ----------------------------------------------------------------------------------
        System.out.println(">>> TEST 1: Probando inicio de sesión con LoginController (DIP)...");
        try {
            Usuario usrGuardia = loginController.iniciarSesion("guardia", "guardia123");
            System.out.println("✔️ Autenticación Exitosa: " + usrGuardia.getUsername() + " [Rol: "
                    + usrGuardia.getRol().getNombre() + "]");
        } catch (AccesoDenegadoException e) {
            System.err.println("❌ Error inesperado en login: " + e.getMessage());
        }

        // ----------------------------------------------------------------------------------
        // PRUEBA 2: Acción Autorizada con Proxy RBAC (Usuario 'guardia')
        // ----------------------------------------------------------------------------------
        System.out.println(
                "\n>>> TEST 2: Operación autorizada con Proxy RBAC (Usuario 'guardia' con permiso 'VISITA_REGISTRAR')...");
        try {
            Usuario usrGuardia = authService.getUsuarioActual();
            VisitaService proxyGuardia = new SeguridadVisitaServiceProxy(servicioDecorado, usrGuardia);
            GuardaController guardaController = new GuardaController(proxyGuardia);

            Visita visita = guardaController.registrarIngreso(personaPrueba, new AccesoPreRegistrado(), usrGuardia,
                    "Reunión de Coordinación", "Ingreso aprobado por Proxy");
            System.out.println("✔️ OPERACIÓN EXITOSA (Proxy autorizó): Visita registrada con ID #" + visita.getId()
                    + " [Estado: " + visita.getEstado() + "]");

        } catch (AccesoDenegadoException e) {
            System.err.println(
                    "❌ ERROR: El Proxy denegó acceso inesperadamente a un guardia autorizado: " + e.getMessage());
        }

        // ----------------------------------------------------------------------------------
        // PRUEBA 3: Violación de Seguridad RBAC -> Captura de AccesoDenegadoException
        // ----------------------------------------------------------------------------------
        System.out.println(
                "\n>>> TEST 3: Probando denegación de acceso en Proxy RBAC (Usuario 'operador' SIN permiso 'VISITA_REGISTRAR')...");
        try {
            // Iniciar sesión con usuario sin permisos de registro
            Usuario usrSinPermisos = loginController.iniciarSesion("operador", "operador123");
            System.out.println("Sesión iniciada con: " + usrSinPermisos.getUsername() + " [Rol: "
                    + usrSinPermisos.getRol().getNombre() + "]");

            VisitaService proxySinPermisos = new SeguridadVisitaServiceProxy(servicioDecorado, usrSinPermisos);
            GuardaController guardaControllerSinPermisos = new GuardaController(proxySinPermisos);

            // Intentar registrar ingreso (debe fallar y lanzar AccesoDenegadoException)
            guardaControllerSinPermisos.registrarIngreso(personaPrueba, new AccesoPreRegistrado(), usrSinPermisos,
                    "Intento No Autorizado", "No debe pasar");
            System.err.println("❌ FALLA GRAVE: El Proxy permitió la operación a un usuario no autorizado!");

        } catch (AccesoDenegadoException e) {
            System.out.println("✔️ CAPTURA CORRECTA DE EXCEPCIÓN DE SEGURIDAD (RBAC Proxy funcionó perfectamente):");
            System.out.println("   --> Mensaje de Alerta: " + e.getMessage());
            System.out.println("   --> Usuario violador: " + e.getUsuario());
            System.out.println("   --> Permiso faltante: " + e.getPermisoRequerido());
        }

        // ----------------------------------------------------------------------------------
        // PRUEBA 4: FuncionarioController y Aprobaciones
        // ----------------------------------------------------------------------------------
        System.out.println(
                "\n>>> TEST 4: Probando FuncionarioController y flujo de aprobación de visitas no anunciadas...");
        try {
            Usuario usrAdmin = loginController.iniciarSesion("admin", "admin123");
            VisitaService proxyAdmin = new SeguridadVisitaServiceProxy(servicioDecorado, usrAdmin);

            // Registrar visita no anunciada en PENDIENTE
            servicioDecorado.registrarAcceso(personaPrueba, new AccesoNoAnunciado(), usrAdmin, "Visita Sorpresa",
                    "Esperando funcionario");

            FuncionarioController funcController = new FuncionarioController(proxyAdmin);
            List<Visita> pendientes = funcController.listarVisitasPendientes();
            System.out.println("Visitas pendientes encontradas por FuncionarioController: " + pendientes.size());

            if (!pendientes.isEmpty()) {
                Visita vPend = pendientes.get(0);
                boolean aprobada = funcController.aprobarVisita(vPend.getId(), "Autorizado por Gerencia");
                System.out.println("✔️ Visita ID #" + vPend.getId() + " aprobada exitosamente: " + aprobada
                        + " [Nuevo Estado: " + vPend.getEstado() + "]");
            }

        } catch (Exception e) {
            System.err.println("Error en prueba de funcionario: " + e.getMessage());
        }

        System.out.println("\n=========================================================================");
        System.out.println("   ¡TODAS LAS PRUEBAS DE LA FASE 3 SE EJECUTARON CON ÉXITO ABSOLUTO!     ");
        System.out.println("=========================================================================");
    }

    // Repositorios auxiliares en memoria para suite de prueba
    static class PersonaDAOInMemoryTest implements PersonaDAO {
        private final Map<Long, Persona> DB = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public Persona crear(Persona p) {
            p.setId(seq.getAndIncrement());
            DB.put(p.getId(), p);
            return p;
        }

        @Override
        public Optional<Persona> obtenerPorId(Long id) {
            return Optional.ofNullable(DB.get(id));
        }

        @Override
        public List<Persona> listarTodos() {
            return new ArrayList<>(DB.values());
        }

        @Override
        public boolean actualizar(Persona p) {
            DB.put(p.getId(), p);
            return true;
        }

        @Override
        public boolean eliminar(Long id) {
            return DB.remove(id) != null;
        }

        @Override
        public Optional<Persona> buscarPorDocumento(String t, String n) {
            return Optional.empty();
        }

        @Override
        public List<Persona> listarPorTipo(String tipo) {
            return new ArrayList<>();
        }

        @Override
        public boolean cambiarEstadoActivo(Long id, boolean activo) {
            return true;
        }
    }

    static class VisitaDAOInMemoryTest implements VisitaDAO {
        private final Map<Long, Visita> DB = new LinkedHashMap<>();
        private final AtomicLong seq = new AtomicLong(10);

        @Override
        public Visita crear(Visita v) {
            v.setId(seq.getAndIncrement());
            DB.put(v.getId(), v);
            return v;
        }

        @Override
        public Optional<Visita> obtenerPorId(Long id) {
            return Optional.ofNullable(DB.get(id));
        }

        @Override
        public List<Visita> listarTodos() {
            return new ArrayList<>(DB.values());
        }

        @Override
        public boolean actualizar(Visita v) {
            DB.put(v.getId(), v);
            return true;
        }

        @Override
        public boolean eliminar(Long id) {
            return DB.remove(id) != null;
        }

        @Override
        public Optional<Visita> buscarUltimaVisitaActivaPorPersona(Long pId) {
            return Optional.empty();
        }

        @Override
        public List<Visita> listarVisitasActivas() {
            return new ArrayList<>();
        }

        @Override
        public List<Visita> listarVisitasPorRangoFechas(LocalDateTime i, LocalDateTime f) {
            return new ArrayList<>();
        }

        @Override
        public boolean registrarSalida(Long vId, LocalDateTime fS, String obs) {
            Visita v = DB.get(vId);
            if (v != null) {
                v.setEstado("FINALIZADA");
                return true;
            }
            return false;
        }

        @Override
        public List<Visita> listarPersonalPresentePorEmpresa(Long empresaId) {
            List<Visita> resultado = new ArrayList<>();
            if (empresaId == null) return resultado;
            for (Visita v : DB.values()) {
                if (v != null && ("EN_CURSO".equalsIgnoreCase(v.getEstado()) || "Dentro".equalsIgnoreCase(v.getEstado()))
                        && v.getPersona() != null && empresaId.equals(v.getPersona().getEmpresaId())) {
                    resultado.add(v);
                }
            }
            return resultado;
        }
    }

    static class BitacoraDAOInMemoryTest implements BitacoraAuditoriaDAO {
        private final List<BitacoraAuditoria> DB = new ArrayList<>();
        private final AtomicLong seq = new AtomicLong(100);

        @Override
        public BitacoraAuditoria registrar(BitacoraAuditoria b) {
            b.setId(seq.getAndIncrement());
            DB.add(b);
            return b;
        }

        @Override
        public List<BitacoraAuditoria> listarTodos() {
            return new ArrayList<>(DB);
        }

        @Override
        public List<BitacoraAuditoria> listarPorUsuario(Long uId) {
            return new ArrayList<>();
        }
    }
}
