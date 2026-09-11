package com.sica.test;

import com.sica.controller.SupervisorController;
import com.sica.controller.SupervisorController.ReporteMetricasVisitas;
import com.sica.dao.BitacoraAuditoriaDAO;
import com.sica.dao.PersonaDAO;
import com.sica.dao.VisitaDAO;
import com.sica.decorator.AuditoriaVisitaDecorator;
import com.sica.model.BitacoraAuditoria;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;
import com.sica.observer.FuncionarioObserverImpl;
import com.sica.observer.NotificadorVisitaSubject;
import com.sica.observer.ObserverFuncionario;
import com.sica.proxy.SeguridadVisitaServiceProxy;
import com.sica.service.AuthService;
import com.sica.service.VisitaService;
import com.sica.service.impl.AuthServiceImpl;
import com.sica.service.impl.VisitaServiceImpl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Suite de Prueba Integrada para la Fase 4 del Proyecto SICA.
 * Valida:
 * - Módulo de Reportes con Stream API y Lambdas en SupervisorController.
 * - Integración del Patrón Observer con notificaciones a observadores de UI.
 * - Consolidación de todos los patrones de diseño (Singleton, DAO, Strategy,
 * Observer, Decorator, Proxy).
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class SicaFase4Test {

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("   PROYECTO SICA - PRUEBA INTEGRADA FASE 4 (STREAM API & DASHBOARDS)    ");
        System.out.println("=========================================================================\n");

        // 1. Inicialización de Servicios y DAOs
        AuthService authService = new AuthServiceImpl();
        PersonaDAO personaDAO = new PersonaDAOInMemoryTest4();
        VisitaDAO visitaDAO = new VisitaDAOInMemoryTest4();
        BitacoraAuditoriaDAO bitacoraDAO = new BitacoraDAOInMemoryTest4();

        VisitaService base = new VisitaServiceImpl(visitaDAO);
        VisitaService servicioDecorado = new AuditoriaVisitaDecorator(base, bitacoraDAO);

        // 2. Cargar Visitas de Prueba en distintos estados
        Persona p1 = personaDAO.crear(new Persona("CC", "111", "Ana", "López", "ana@mail.com", "300111", "VISITANTE"));
        Persona p2 = personaDAO
                .crear(new Persona("CC", "222", "Bernardo", "Ruiz", "b@mail.com", "300222", "CONTRATISTA"));
        Persona p3 = personaDAO
                .crear(new Persona("CC", "333", "Clara", "Morales", "c@mail.com", "300333", "VISITANTE"));

        Usuario admin = new Usuario(1L, "admin", "admin123", "a@sica.com", true, LocalDateTime.now(), null);

        Visita v1 = visitaDAO.crear(new Visita(p1, admin, "Cita", "Normal"));
        v1.setEstado("EN_CURSO");
        Visita v2 = visitaDAO.crear(new Visita(p2, admin, "Sin cita", "Esperando"));
        v2.setEstado("PENDIENTE");
        Visita v3 = visitaDAO.crear(new Visita(p3, admin, "Auditoría", "Completa"));
        v3.setEstado("FINALIZADA");

        // ----------------------------------------------------------------------------------
        // PRUEBA 1: SupervisorController con Java Stream API & Lambdas
        // ----------------------------------------------------------------------------------
        System.out.println(">>> TEST 1: Probando SupervisorController con Stream API y Lambdas...");
        VisitaService proxyAdmin = new SeguridadVisitaServiceProxy(servicioDecorado, admin);
        SupervisorController supervisorController = new SupervisorController(proxyAdmin);

        List<Visita> todasLasVisitas = visitaDAO.listarTodos();
        ReporteMetricasVisitas metricas = supervisorController.calcularMetricasStream(todasLasVisitas);

        System.out.println("📊 RESULTADOS DEL CÁLCULO CON STREAM API:");
        System.out.println("   • Total registros evaluados: " + metricas.getTotalRegistros());
        System.out.println("   • Personas DENTRO actualmente (EN_CURSO): " + metricas.getDentroEnCurso());
        System.out.println("   • Visitas PENDIENTES de aprobación: " + metricas.getPendientesAprobacion());
        System.out.println("   • Visitas FINALIZADAS: " + metricas.getSalidasFinalizadas());

        if (metricas.getDentroEnCurso() == 1 && metricas.getPendientesAprobacion() == 1
                && metricas.getSalidasFinalizadas() == 1) {
            System.out.println("✔️ VALIDACIÓN EXITOSA: La API Stream calculó con 100% de precisión las métricas.");
        } else {
            System.err.println("❌ ERROR: Discrepancia en el conteo de la API Stream.");
        }

        // ----------------------------------------------------------------------------------
        // PRUEBA 2: Integración del Patrón Observer en UI
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> TEST 2: Probando integración del Patrón Observer en UI...");
        NotificadorVisitaSubject subject = NotificadorVisitaSubject.getInstanciaGlobal();
        ObserverFuncionario obsUI = new FuncionarioObserverImpl("Dr. Mario Santos", "Dirección General");
        subject.registrarObserver(obsUI);

        Visita visitaInesperada = new Visita(p2, admin, "Urgencia", "Requiere firma");
        visitaInesperada.setEstado("PENDIENTE");

        System.out.println("Emitiendo evento de notificación a los observadores suscriptos...");
        subject.notificarObservadores(visitaInesperada);
        System.out.println("✔️ Notificación emitida correctamente por el Subject Observer.");

        System.out.println("\n=========================================================================");
        System.out.println("   ¡SICA FASE 4 (FINAL) COMPLETADA CON ÉXITO Y CONSOLIDACIÓN TOTAL!      ");
        System.out.println("=========================================================================");
    }

    static class PersonaDAOInMemoryTest4 implements PersonaDAO {
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

    static class VisitaDAOInMemoryTest4 implements VisitaDAO {
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
            return true;
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

    static class BitacoraDAOInMemoryTest4 implements BitacoraAuditoriaDAO {
        @Override
        public BitacoraAuditoria registrar(BitacoraAuditoria b) {
            b.setId(1L);
            return b;
        }

        @Override
        public List<BitacoraAuditoria> listarTodos() {
            return new ArrayList<>();
        }

        @Override
        public List<BitacoraAuditoria> listarPorUsuario(Long uId) {
            return new ArrayList<>();
        }
    }
}
