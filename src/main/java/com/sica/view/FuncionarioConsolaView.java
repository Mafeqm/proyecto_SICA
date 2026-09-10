package com.sica.view;

import com.sica.controller.FuncionarioController;
import com.sica.model.Persona;
import com.sica.model.Usuario;
import com.sica.model.Visita;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Vista de Consola para el Rol de Funcionario de Empresa (Patrón MVC).
 * Provee el menú de opciones en consola, destacando la funcionalidad
 * "Ver Personal Presente en el Complejo" que filtra automáticamente por
 * la empresa del funcionario en sesión y estado de visita ("Dentro" / "EN_CURSO").
 * 
 * @author Arquitectura SICA
 * @version 1.0
 */
public class FuncionarioConsolaView {

    private final FuncionarioController funcionarioController;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FuncionarioConsolaView(FuncionarioController funcionarioController) {
        if (funcionarioController == null) {
            throw new IllegalArgumentException("El controlador de funcionario no puede ser nulo.");
        }
        this.funcionarioController = funcionarioController;
    }

    /**
     * Despliega el menú de consola interactivo para el Funcionario de Empresa.
     * 
     * @param funcionario Usuario autenticado en el sistema.
     * @param scanner     Scanner para lectura de opciones (opcional).
     */
    public void mostrarMenuFuncionario(Usuario funcionario, Scanner scanner) {
        if (funcionario == null) {
            System.err.println("❌ Error: No hay un funcionario autenticado para desplegar el menú.");
            return;
        }

        // Verificar si el rol corresponde
        String rolNombre = funcionario.getRol() != null ? funcionario.getRol().getNombre() : "";
        if (!"FUNCIONARIO_EMPRESA".equalsIgnoreCase(rolNombre) && !"FUNCIONARIO".equalsIgnoreCase(rolNombre)
                && !"ADMINISTRADOR".equalsIgnoreCase(rolNombre)) {
            System.err.println("⛔ ACCESO DENEGADO: El menú es accesible únicamente para el rol Funcionario de Empresa.");
            return;
        }

        boolean salir = false;
        while (!salir) {
            System.out.println("\n" + "=".repeat(65));
            System.out.println("          SICA - MENÚ FUNCIONARIO DE EMPRESA");
            System.out.println("=".repeat(65));
            System.out.println("  Funcionario : " + funcionario.getUsername() + " (" + funcionario.getEmail() + ")");
            System.out.println("  Empresa     : " + (funcionario.getEmpresaNombre() != null ? funcionario.getEmpresaNombre() : "Empresa ID #" + funcionario.getEmpresaId()));
            System.out.println("-".repeat(65));
            System.out.println("  1. Ver Personal Presente en el Complejo");
            System.out.println("  2. Ver Visitas Pendientes de Aprobación");
            System.out.println("  0. Salir al Menú Principal");
            System.out.println("=".repeat(65));
            System.out.print("Seleccione una opción: ");

            String opcion = scanner != null ? scanner.nextLine().trim() : "1";
            switch (opcion) {
                case "1":
                    mostrarPersonalPresenteEnConsola(funcionario);
                    break;
                case "2":
                    mostrarVisitasPendientesEnConsola();
                    break;
                case "0":
                    salir = true;
                    System.out.println("Saliendo del menú de funcionario...");
                    break;
                default:
                    System.out.println("⚠️ Opción no válida. Intente nuevamente.");
            }

            if (scanner == null) {
                break; // Ejecución no interactiva
            }
        }
    }

    /**
     * Ejecuta la consulta de filtrado automático y presenta los resultados
     * de personal presente en formato de tabla en la consola.
     * 
     * @param funcionario Usuario en sesión.
     * @return Formato de tabla generado en String.
     */
    public String mostrarPersonalPresenteEnConsola(Usuario funcionario) {
        System.out.println("\n" + "=".repeat(95));
        System.out.println("          LISTADO DE PERSONAL PRESENTE EN EL COMPLEJO (ZONA ACME)");
        String empresaInfo = funcionario != null && funcionario.getEmpresaNombre() != null
                ? funcionario.getEmpresaNombre()
                : (funcionario != null && funcionario.getEmpresaId() != null ? "ID #" + funcionario.getEmpresaId() : "N/A");
        System.out.println("          Empresa: " + empresaInfo);
        System.out.println("=".repeat(95));

        List<Visita> personalPresente = funcionarioController.obtenerPersonalPresenteEnComplejo(funcionario);

        // Caso Sin Resultados
        if (personalPresente == null || personalPresente.isEmpty()) {
            String msgSinResultados = "ℹ️ [INFORMACIÓN] No hay ninguna persona de su empresa actualmente dentro del complejo.";
            System.out.println("\n" + msgSinResultados + "\n");
            System.out.println("=".repeat(95));
            return msgSinResultados;
        }

        // Presentación de Resultados en Formato de Tabla Claro y Legible
        StringBuilder sb = new StringBuilder();
        String separador = "+--------------------------------+----------------------+-------------------+---------------------+";
        String cabecera  = "| Nombre Completo                | Documento Identidad  | Tipo de Persona   | Fecha/Hora Entrada  |";

        System.out.println(separador);
        System.out.println(cabecera);
        System.out.println(separador);

        sb.append(separador).append("\n").append(cabecera).append("\n").append(separador).append("\n");

        for (Visita v : personalPresente) {
            Persona p = v.getPersona();
            String nombreCompleto = p != null ? p.getNombreCompleto() : "N/A";
            String docIdentidad = (p != null ? (p.getTipoDocumento() != null ? p.getTipoDocumento() + " " : "") + (p.getNumeroDocumento() != null ? p.getNumeroDocumento() : "-") : "N/A");
            
            // Normalizar Tipo de Persona a "Trabajador" o "Invitado"
            String tipoPersona = normalizarTipoPersona(p != null ? p.getTipoPersona() : "");
            String fechaEntradaStr = v.getFechaEntrada() != null ? v.getFechaEntrada().format(formatter) : "N/A";

            String fila = String.format("| %-30s | %-20s | %-17s | %-19s |",
                    truncar(nombreCompleto, 30),
                    truncar(docIdentidad, 20),
                    truncar(tipoPersona, 17),
                    truncar(fechaEntradaStr, 19));

            System.out.println(fila);
            sb.append(fila).append("\n");
        }

        System.out.println(separador);
        System.out.println("Total de personas presentes: " + personalPresente.size());
        System.out.println("=".repeat(95) + "\n");

        sb.append(separador).append("\n");
        return sb.toString();
    }

    private void mostrarVisitasPendientesEnConsola() {
        List<Visita> pendientes = funcionarioController.listarVisitasPendientes();
        if (pendientes.isEmpty()) {
            System.out.println("ℹ️ No hay visitas pendientes de aprobación.");
        } else {
            System.out.println("Visitas pendientes de autorización: " + pendientes.size());
            for (Visita v : pendientes) {
                System.out.println("  • ID: " + v.getId() + " | Visitante: "
                        + (v.getPersona() != null ? v.getPersona().getNombreCompleto() : "N/A")
                        + " | Motivo: " + v.getMotivo());
            }
        }
    }

    /**
     * Mapea y normaliza el tipo de persona al formato requerido ("Trabajador" o "Invitado").
     */
    public static String normalizarTipoPersona(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return "Invitado";
        }
        String t = tipo.trim().toUpperCase();
        if (t.contains("EMPLEADO") || t.contains("TRABAJADOR") || t.contains("FUNCIONARIO") || t.contains("PERSONAL")) {
            return "Trabajador";
        }
        return "Invitado";
    }

    private static String truncar(String texto, int maxLen) {
        if (texto == null) return "";
        if (texto.length() <= maxLen) return texto;
        return texto.substring(0, maxLen - 3) + "...";
    }
}
