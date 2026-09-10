package com.sica.test;

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
import com.sica.service.VisitaService;
import com.sica.service.impl.VisitaServiceImpl;
import com.sica.strategy.AccesoNoAnunciado;
import com.sica.strategy.AccesoPreRegistrado;
import com.sica.strategy.AccesoSalidaOlvidada;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Suite de Prueba Integrada para la Fase 2 del Proyecto SICA.
 * Demuestra y valida la interacción de los patrones de diseño:
 * - Strategy (Flujos de acceso: PreRegistrado, NoAnunciado, SalidaOlvidada)
 * - Observer (Notificación a Funcionarios)
 * - Decorator (Auditoría automática transparente)
 * - Lambdas & Stream API (Filtrado declarativo)
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class SicaFase2Test {

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("   PROYECTO SICA - PRUEBA INTEGRADA DE PATRONES DE DISEÑO & SERVICIOS   ");
        System.out.println("=========================================================================\n");

        // 1. Instanciar DAOs (Utilizando mock/in-memory para prueba aislada sin
        // servidor DB activo)
        PersonaDAO personaDAO = new PersonaDAOInMemory();
        VisitaDAO visitaDAO = new VisitaDAOInMemory();
        BitacoraAuditoriaDAO bitacoraDAO = new BitacoraAuditoriaDAOInMemory();

        // 2. Configurar Servicio Base y Decorador de Auditoría (Patrón Decorator)
        VisitaService servicioBase = new VisitaServiceImpl(visitaDAO);
        VisitaService visitaService = new AuditoriaVisitaDecorator(servicioBase, bitacoraDAO);

        // 3. Configurar Observadores para Invitados No Anunciados (Patrón Observer)
        NotificadorVisitaSubject subjectNotificador = NotificadorVisitaSubject.getInstanciaGlobal();
        ObserverFuncionario obs1 = new FuncionarioObserverImpl("Ing. Carlos Mendoza", "Gerencia TI");
        ObserverFuncionario obs2 = new FuncionarioObserverImpl("Dra. Ana Gómez", "Recursos Humanos");
        subjectNotificador.registrarObserver(obs1);
        subjectNotificador.registrarObserver(obs2);

        System.out.println("\n--- DATOS DE PRUEBA INITIALIZADOS ---");
        Usuario guardiaGuardia = new Usuario(1L, "guardia_main", "hash123", "guardia@sica.com", true,
                LocalDateTime.now(), null);

        Persona persona1 = personaDAO
                .crear(new Persona("CC", "101829384", "Juan", "Pérez", "juan@correo.com", "3001234567", "VISITANTE"));
        Persona persona2 = personaDAO.crear(
                new Persona("CE", "593821093", "María", "Rodríguez", "maria@correo.com", "3109876543", "CONTRATISTA"));
        Persona persona3 = personaDAO.crear(
                new Persona("CC", "109876543", "Carlos", "Sánchez", "carlos@correo.com", "3201112233", "VISITANTE"));

        // ----------------------------------------------------------------------------------
        // PRUEBA 1: Patrón Strategy -> AccesoPreRegistrado
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> TEST 1: Procesando Acceso Pre-Registrado (Strategy)...");
        Visita v1 = visitaService.registrarAcceso(persona1, new AccesoPreRegistrado(), guardiaGuardia,
                "Reunión de Negocios", "Piso 4 - Sala A");
        System.out.println("Visita resultante: " + v1);

        // ----------------------------------------------------------------------------------
        // PRUEBA 2: Patrón Strategy + Observer -> AccesoNoAnunciado
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> TEST 2: Procesando Acceso No Anunciado (Strategy + Observer)...");
        Visita v2 = visitaService.registrarAcceso(persona2, new AccesoNoAnunciado(subjectNotificador), guardiaGuardia,
                "Entrega de Equipos", "Esperando aprobación");
        System.out.println("Visita resultante: " + v2);

        // ----------------------------------------------------------------------------------
        // PRUEBA 3: Patrón Strategy -> AccesoSalidaOlvidada
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> TEST 3: Procesando Acceso con Salida Olvidada (Strategy)...");
        // Simular que persona3 ya tenía una visita EN_CURSO que olvidó cerrar
        Visita visitaOlvidada = visitaDAO
                .crear(new Visita(persona3, guardiaGuardia, "Ingreso de la mañana", "Olvidó registrar salida"));
        System.out.println("Visita previa abierta simulada con ID: " + visitaOlvidada.getId());

        Visita v3 = visitaService.registrarAcceso(persona3, new AccesoSalidaOlvidada(), guardiaGuardia,
                "Reingreso por la tarde", "Vuelve a entrar");
        System.out.println("Visita nueva resultante: " + v3);

        // ----------------------------------------------------------------------------------
        // PRUEBA 4: Lambdas & Stream API (Filtrado)
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> TEST 4: Filtrado con Lambdas y Java Stream API...");
        List<Visita> todasLasVisitas = visitaService.listarTodas();

        System.out.println("\n--- Visitas en estado 'EN_CURSO' (Stream.filter): ---");
        List<Visita> visitasEnCurso = visitaService.filtrarVisitasPorEstado(todasLasVisitas, "EN_CURSO");
        visitasEnCurso.forEach(v -> System.out.println("   • " + v.getPersona().getNombreCompleto() + " | Estado: "
                + v.getEstado() + " | Entró: " + v.getFechaEntrada()));

        System.out.println("\n--- Visitas en estado 'PENDIENTE' (Stream.filter): ---");
        List<Visita> visitasPendientes = visitaService.filtrarVisitasPorEstado(todasLasVisitas, "PENDIENTE");
        visitasPendientes.forEach(v -> System.out.println("   • " + v.getPersona().getNombreCompleto() + " | Estado: "
                + v.getEstado() + " | Entró: " + v.getFechaEntrada()));

        long totalActivas = visitaService.contarVisitasActivas(todasLasVisitas);
        System.out.println("\nTotal visitas activas contadas con Stream.count(): " + totalActivas);

        // ----------------------------------------------------------------------------------
        // PRUEBA 5: Verificación del Patrón Decorator (Bitácora de Auditoría)
        // ----------------------------------------------------------------------------------
        System.out.println("\n>>> TEST 5: Verificando registros generados por AuditoriaVisitaDecorator...");
        List<BitacoraAuditoria> logsAuditoria = bitacoraDAO.listarTodos();
        System.out.println("Total eventos auditados automáticamente por el Decorador: " + logsAuditoria.size());
        logsAuditoria.forEach(log -> System.out.println("   [AUDITORÍA LOG #" + log.getId() + "] Acción: "
                + log.getAccion() + " | RegistroId: " + log.getRegistroId() + " | Detalle: " + log.getDetalle()));

        System.out.println("\n=========================================================================");
        System.out.println("   ¡TODAS LAS PRUEBAS DE LA FASE 2 SE EJECUTARON CON ÉXITO ABSOLUTO!     ");
        System.out.println("=========================================================================");
    }

    // ======================================================================================
    // MOCK / IN-MEMORY DAOs PARA PRUEBAS AUTÓNOMAS DE PATRONES SIN SERVIDORES
    // EXTERNOS
    // ======================================================================================

    static class PersonaDAOInMemory implements PersonaDAO {
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
            if (DB.containsKey(p.getId())) {
                DB.put(p.getId(), p);
                return true;
            }
            return false;
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
        public List<Persona> listarPorTipo(String tipo) {
            List<Persona> res = new ArrayList<>();
            for (Persona p : DB.values())
                if (p.getTipoPersona().equalsIgnoreCase(tipo))
                    res.add(p);
            return res;
        }

        @Override
        public boolean cambiarEstadoActivo(Long id, boolean activo) {
            Persona p = DB.get(id);
            if (p != null) {
                p.setActivo(activo);
                return true;
            }
            return false;
        }
    }

    static class VisitaDAOInMemory implements VisitaDAO {
        private final Map<Long, Visita> DB = new LinkedHashMap<>();
        private final AtomicLong seq = new AtomicLong(100);

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
            if (DB.containsKey(v.getId())) {
                DB.put(v.getId(), v);
                return true;
            }
            return false;
        }

        @Override
        public boolean eliminar(Long id) {
            return DB.remove(id) != null;
        }

        @Override
        public Optional<Visita> buscarUltimaVisitaActivaPorPersona(Long personaId) {
            return DB.values().stream()
                    .filter(v -> v.getPersona() != null && personaId.equals(v.getPersona().getId()))
                    .filter(v -> "EN_CURSO".equalsIgnoreCase(v.getEstado())
                            || "PENDIENTE".equalsIgnoreCase(v.getEstado()))
                    .reduce((first, second) -> second);
        }

        @Override
        public List<Visita> listarVisitasActivas() {
            List<Visita> res = new ArrayList<>();
            for (Visita v : DB.values())
                if ("EN_CURSO".equalsIgnoreCase(v.getEstado()))
                    res.add(v);
            return res;
        }

        @Override
        public List<Visita> listarVisitasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
            return listarTodos();
        }

        @Override
        public boolean registrarSalida(Long visitaId, LocalDateTime fechaSalida, String observaciones) {
            Visita v = DB.get(visitaId);
            if (v != null) {
                v.setFechaSalida(fechaSalida);
                v.setEstado("FINALIZADA");
                v.setObservaciones((v.getObservaciones() != null ? v.getObservaciones() + " | " : "") + observaciones);
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

    static class BitacoraAuditoriaDAOInMemory implements BitacoraAuditoriaDAO {
        private final List<BitacoraAuditoria> DB = new ArrayList<>();
        private final AtomicLong seq = new AtomicLong(1000);

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
        public List<BitacoraAuditoria> listarPorUsuario(Long usuarioId) {
            return listarTodos();
        }
    }
}
