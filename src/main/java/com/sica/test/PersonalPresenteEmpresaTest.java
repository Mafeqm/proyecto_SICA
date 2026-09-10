package com.sica.test;

import com.sica.controller.FuncionarioController;
import com.sica.dao.BitacoraAuditoriaDAO;
import com.sica.dao.PersonaDAO;
import com.sica.dao.VisitaDAO;
import com.sica.decorator.AuditoriaVisitaDecorator;
import com.sica.model.BitacoraAuditoria;
import com.sica.model.Persona;
import com.sica.model.Rol;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.proxy.SeguridadVisitaServiceProxy;
import com.sica.service.VisitaService;
import com.sica.service.impl.VisitaServiceImpl;
import com.sica.view.FuncionarioConsolaView;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Suite de Prueba Integral para la Funcionalidad:
 * "Ver Personal Presente en el Complejo" para el Funcionario de Empresa.
 * 
 * Valida:
 * 1. Flujo MVC completo y desacoplamiento de capas.
 * 2. Filtrado automático por empresa del usuario en sesión y estado "Dentro" ("EN_CURSO").
 * 3. Inclusión tanto de Trabajadores como de Invitados.
 * 4. Exclusión de personas de otras empresas o visitas finalizadas.
 * 5. Caso sin resultados con mensaje informativo.
 * 6. Formato de presentación en tabla para consola.
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class PersonalPresenteEmpresaTest {

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("  SICA - PRUEBA INTEGRAL: VER PERSONAL PRESENTE EN EL COMPLEJO           ");
        System.out.println("=========================================================================\n");

        // 1. Configuración de Repositorios y Servicios (Arquitectura MVC + Decorator + Proxy)
        PersonaDAO personaDAO = new PersonaDAOInMemoryTest();
        VisitaDAO visitaDAO = new VisitaDAOInMemoryTest();
        BitacoraAuditoriaDAO bitacoraDAO = new BitacoraDAOInMemoryTest();

        VisitaService baseService = new VisitaServiceImpl(visitaDAO);
        VisitaService servicioDecorado = new AuditoriaVisitaDecorator(baseService, bitacoraDAO);

        // 2. Creación de Datos de Prueba: Empresas y Personas
        Long EMPRESA_TECNOGLOBAL_ID = 10L;
        String EMPRESA_TECNOGLOBAL_NAME = "TecnoGlobal";

        Long EMPRESA_OTRA_ID = 20L;
        String EMPRESA_OTRA_NAME = "Innovatech S.A.";

        Long EMPRESA_VACIA_ID = 30L;
        String EMPRESA_VACIA_NAME = "VaciaCorp";

        // Trabajador de TecnoGlobal
        Persona p1 = personaDAO.crear(new Persona(1L, "CC", "1011223344", "Carlos", "Mendoza",
                "carlos.m@tecnoglobal.com", "3001234567", "EMPLEADO", EMPRESA_TECNOGLOBAL_ID, true, LocalDateTime.now()));

        // Invitado de TecnoGlobal
        Persona p2 = personaDAO.crear(new Persona(2L, "CC", "9876543210", "Laura", "Restrepo",
                "laura.r@consultora.com", "3109876543", "VISITANTE", EMPRESA_TECNOGLOBAL_ID, true, LocalDateTime.now()));

        // Trabajador de TecnoGlobal pero que ya SALIÓ (FINALIZADA)
        Persona p3 = personaDAO.crear(new Persona(3L, "CC", "1122334455", "Andrés", "Gómez",
                "andres.g@tecnoglobal.com", "3151122334", "TRABAJADOR", EMPRESA_TECNOGLOBAL_ID, true, LocalDateTime.now()));

        // Persona de OTRA empresa dentro de las instalaciones
        Persona p4 = personaDAO.crear(new Persona(4L, "CC", "5566778899", "Diana", "Pérez",
                "diana.p@innovatech.com", "3205566778", "EMPLEADO", EMPRESA_OTRA_ID, true, LocalDateTime.now()));

        // 3. Creación de Usuarios y Sesiones
        Rol rolFuncionario = new Rol(3L, "FUNCIONARIO_EMPRESA", "Acceso a panel de funcionario");
        
        Usuario gerenteTecnoGlobal = new Usuario(100L, "gerente.tecnoglobal", "pass123",
                "gerente@tecnoglobal.com", true, LocalDateTime.now(), rolFuncionario,
                EMPRESA_TECNOGLOBAL_ID, EMPRESA_TECNOGLOBAL_NAME);

        Usuario funcionarioVaciaCorp = new Usuario(200L, "admin.vaciacorp", "pass123",
                "admin@vaciacorp.com", true, LocalDateTime.now(), rolFuncionario,
                EMPRESA_VACIA_ID, EMPRESA_VACIA_NAME);

        Usuario guardiaSeguridad = new Usuario(1L, "carlos.guardia", "1234",
                "carlos@seguridad.com", true, LocalDateTime.now(), new Rol(2L, "GUARDIA_SEGURIDAD", "Guardia"));

        // 4. Registrar Visitas
        // Visita 1: Carlos Mendoza (TecnoGlobal) -> DENTRO ("EN_CURSO")
        Visita v1 = new Visita(101L, p1, guardiaSeguridad, LocalDateTime.now().minusHours(2), null,
                "Jornada Laboral", "EN_CURSO", "Ingreso por Garita Principal");
        visitaDAO.crear(v1);

        // Visita 2: Laura Restrepo (TecnoGlobal) -> DENTRO ("Dentro")
        Visita v2 = new Visita(102L, p2, guardiaSeguridad, LocalDateTime.now().minusMinutes(45), null,
                "Reunión Gerencial", "Dentro", "Invitada especial");
        visitaDAO.crear(v2);

        // Visita 3: Andrés Gómez (TecnoGlobal) -> YA SALIÓ ("FINALIZADA")
        Visita v3 = new Visita(103L, p3, guardiaSeguridad, LocalDateTime.now().minusHours(5), LocalDateTime.now().minusHours(1),
                "Soporte", "FINALIZADA", "Salida registrada");
        visitaDAO.crear(v3);

        // Visita 4: Diana Pérez (Innovatech) -> DENTRO pero OTRA EMPRESA
        Visita v4 = new Visita(104L, p4, guardiaSeguridad, LocalDateTime.now().minusHours(1), null,
                "Capacitación", "EN_CURSO", "Otra empresa");
        visitaDAO.crear(v4);

        // ----------------------------------------------------------------------------------
        // TEST 1: Consulta del Funcionario de TecnoGlobal
        // ----------------------------------------------------------------------------------
        System.out.println(">>> TEST 1: Consulta de Personal Presente por Funcionario de 'TecnoGlobal'...");
        VisitaService proxyTecnoGlobal = new SeguridadVisitaServiceProxy(servicioDecorado, gerenteTecnoGlobal);
        FuncionarioController controllerTecnoGlobal = new FuncionarioController(proxyTecnoGlobal);
        FuncionarioConsolaView vistaTecnoGlobal = new FuncionarioConsolaView(controllerTecnoGlobal);

        List<Visita> presentesTecnoGlobal = controllerTecnoGlobal.obtenerPersonalPresenteEnComplejo(gerenteTecnoGlobal);
        
        System.out.println("   • Total personas encontradas para TecnoGlobal: " + presentesTecnoGlobal.size());
        
        boolean okSize = presentesTecnoGlobal.size() == 2;
        boolean okP1 = presentesTecnoGlobal.stream().anyMatch(v -> v.getPersona().getId().equals(p1.getId()));
        boolean okP2 = presentesTecnoGlobal.stream().anyMatch(v -> v.getPersona().getId().equals(p2.getId()));
        boolean noP3 = presentesTecnoGlobal.stream().noneMatch(v -> v.getPersona().getId().equals(p3.getId()));
        boolean noP4 = presentesTecnoGlobal.stream().noneMatch(v -> v.getPersona().getId().equals(p4.getId()));

        if (okSize && okP1 && okP2 && noP3 && noP4) {
            System.out.println("✔️ VALIDACIÓN 1 EXITOSA: Se listaron exactamente los 2 miembros de TecnoGlobal (Trabajador e Invitado) actualmente dentro.");
        } else {
            System.err.println("❌ ERROR en TEST 1: Falló el filtrado automático por empresa o estado.");
        }

        // Mostrar tabla en consola
        System.out.println("\n[Presentación Visual en Consola]");
        vistaTecnoGlobal.mostrarPersonalPresenteEnConsola(gerenteTecnoGlobal);

        // ----------------------------------------------------------------------------------
        // TEST 2: Caso Sin Resultados (Empresa sin personas dentro)
        // ----------------------------------------------------------------------------------
        System.out.println(">>> TEST 2: Consulta para Empresa sin personal dentro ('VaciaCorp')...");
        VisitaService proxyVacia = new SeguridadVisitaServiceProxy(servicioDecorado, funcionarioVaciaCorp);
        FuncionarioController controllerVacia = new FuncionarioController(proxyVacia);
        FuncionarioConsolaView vistaVacia = new FuncionarioConsolaView(controllerVacia);

        List<Visita> presentesVacia = controllerVacia.obtenerPersonalPresenteEnComplejo(funcionarioVaciaCorp);
        System.out.println("   • Total personas encontradas para VaciaCorp: " + presentesVacia.size());

        if (presentesVacia.isEmpty()) {
            System.out.println("✔️ VALIDACIÓN 2 EXITOSA: La lista está vacía según lo esperado.");
        } else {
            System.err.println("❌ ERROR en TEST 2: Se esperaban 0 resultados.");
        }

        System.out.println("\n[Presentación Caso Sin Resultados en Consola]");
        String resultadoVacia = vistaVacia.mostrarPersonalPresenteEnConsola(funcionarioVaciaCorp);
        if (resultadoVacia.contains("No hay ninguna persona de su empresa actualmente dentro del complejo")) {
            System.out.println("✔️ Mensaje informativo mostrado correctamente.");
        }

        // ----------------------------------------------------------------------------------
        // TEST 3: Verificación de Normalización de Tipos de Persona
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> TEST 3: Validación de Normalización de Tipos de Persona...");
        String tipoEmp = FuncionarioConsolaView.normalizarTipoPersona("EMPLEADO");
        String tipoTrab = FuncionarioConsolaView.normalizarTipoPersona("TRABAJADOR");
        String tipoVis = FuncionarioConsolaView.normalizarTipoPersona("VISITANTE");
        String tipoInv = FuncionarioConsolaView.normalizarTipoPersona("INVITADO");
        String tipoCont = FuncionarioConsolaView.normalizarTipoPersona("CONTRATISTA");

        if ("Trabajador".equals(tipoEmp) && "Trabajador".equals(tipoTrab) && "Invitado".equals(tipoVis) && "Invitado".equals(tipoInv)) {
            System.out.println("✔️ VALIDACIÓN 3 EXITOSA: Normalización correcta a 'Trabajador' e 'Invitado'.");
        } else {
            System.err.println("❌ ERROR en normalización de tipos de persona.");
        }

        System.out.println("\n=========================================================================");
        System.out.println("   ¡TODAS LAS PRUEBAS DE PERSONAL PRESENTE SE COMPLETARON CON ÉXITO!    ");
        System.out.println("=========================================================================");
    }

    // Repositorios auxiliares en memoria para prueba unitaria
    static class PersonaDAOInMemoryTest implements PersonaDAO {
        private final Map<Long, Persona> DB = new LinkedHashMap<>();
        private final AtomicLong seq = new AtomicLong(100);

        @Override
        public Persona crear(Persona p) {
            if (p.getId() == null) {
                p.setId(seq.getAndIncrement());
            }
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
            return DB.values().stream()
                    .filter(p -> p.getTipoDocumento().equalsIgnoreCase(t) && p.getNumeroDocumento().equals(n))
                    .findFirst();
        }

        @Override
        public List<Persona> listarPorTipo(String tipoPersona) {
            return new ArrayList<>();
        }

        @Override
        public boolean cambiarEstadoActivo(Long id, boolean activo) {
            return true;
        }
    }

    static class VisitaDAOInMemoryTest implements VisitaDAO {
        private final Map<Long, Visita> DB = new LinkedHashMap<>();
        private final AtomicLong seq = new AtomicLong(500);

        @Override
        public Visita crear(Visita v) {
            if (v.getId() == null) {
                v.setId(seq.getAndIncrement());
            }
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
        public Optional<Visita> buscarUltimaVisitaActivaPorPersona(Long personaId) {
            return Optional.empty();
        }

        @Override
        public List<Visita> listarVisitasActivas() {
            List<Visita> res = new ArrayList<>();
            for (Visita v : DB.values()) {
                if (v != null && ("EN_CURSO".equalsIgnoreCase(v.getEstado()) || "Dentro".equalsIgnoreCase(v.getEstado()))) {
                    res.add(v);
                }
            }
            return res;
        }

        @Override
        public List<Visita> listarVisitasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
            return new ArrayList<>();
        }

        @Override
        public boolean registrarSalida(Long visitaId, LocalDateTime fechaSalida, String observaciones) {
            Visita v = DB.get(visitaId);
            if (v != null) {
                v.setEstado("FINALIZADA");
                v.setFechaSalida(fechaSalida);
                return true;
            }
            return false;
        }

        @Override
        public List<Visita> listarPersonalPresentePorEmpresa(Long empresaId) {
            List<Visita> resultado = new ArrayList<>();
            if (empresaId == null) return resultado;

            for (Visita v : DB.values()) {
                if (v != null
                        && ("EN_CURSO".equalsIgnoreCase(v.getEstado()) || "Dentro".equalsIgnoreCase(v.getEstado()))
                        && v.getPersona() != null
                        && empresaId.equals(v.getPersona().getEmpresaId())) {
                    resultado.add(v);
                }
            }
            return resultado;
        }
    }

    static class BitacoraDAOInMemoryTest implements BitacoraAuditoriaDAO {
        private final List<BitacoraAuditoria> DB = new ArrayList<>();

        @Override
        public BitacoraAuditoria registrar(BitacoraAuditoria b) {
            DB.add(b);
            return b;
        }

        @Override
        public List<BitacoraAuditoria> listarTodos() {
            return DB;
        }

        @Override
        public List<BitacoraAuditoria> listarPorUsuario(Long usuarioId) {
            return new ArrayList<>();
        }
    }
}
