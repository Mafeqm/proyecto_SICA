import java.time.LocalDateTime;

import java.util.Objects;



/**

 * Entidad que representa a una Persona física dentro del sistema SICA.

 * Puede ser un Visitante, Empleado, Contratista, entre otros.

 *

 * @author Arquitectura SICA

 * @version 1.0

 */

public class Persona {



    private Long id;

    private String tipoDocumento; // ej. CC, CE, PASAPORTE, NIT

    private String numeroDocumento;

    private String nombres;

    private String apellidos;

    private String email;

    private String telefono;

    private String tipoPersona; // ej. EMPLEADO, VISITANTE, CONTRATISTA

    private Long empresaId;

    private boolean activo;

    private LocalDateTime fechaRegistro;



    /**

     * Constructor por defecto. Asigna la fecha actual y estado activo.

     */

    public Persona() {

        this.activo = true;

        this.fechaRegistro = LocalDateTime.now();

    }



    /**

     * Constructor parcial sin ID.

     *

     * @param tipoDocumento   Tipo de documento de identidad.

     * @param numeroDocumento Número de documento.

     * @param nombres         Nombres completos.

     * @param apellidos       Apellidos completos.

     * @param email           Correo electrónico de contacto.

     * @param telefono        Teléfono de contacto.

     * @param tipoPersona     Categoría (EMPLEADO, VISITANTE, CONTRATISTA).

     */

    public Persona(String tipoDocumento, String numeroDocumento, String nombres, String apellidos,

            String email, String telefono, String tipoPersona) {

        this();

        this.tipoDocumento = tipoDocumento;

        this.numeroDocumento = numeroDocumento;

        this.nombres = nombres;

        this.apellidos = apellidos;

        this.email = email;

        this.telefono = telefono;

        this.tipoPersona = tipoPersona;

    }



    /**

     * Constructor parcial con empresa.

     *

     * @param tipoDocumento   Tipo de documento de identidad.

     * @param numeroDocumento Número de documento.

     * @param nombres         Nombres completos.

     * @param apellidos       Apellidos completos.

     * @param email           Correo electrónico de contacto.

     * @param telefono        Teléfono de contacto.

     * @param tipoPersona     Categoría (EMPLEADO, VISITANTE, CONTRATISTA).

     * @param empresaId       Identificador de la empresa.

     */

    public Persona(String tipoDocumento, String numeroDocumento, String nombres, String apellidos,

            String email, String telefono, String tipoPersona, Long empresaId) {

        this(tipoDocumento, numeroDocumento, nombres, apellidos, email, telefono, tipoPersona);

        this.empresaId = empresaId;

    }



    /**

     * Constructor completo.

     *

     * @param id              Identificador único.

     * @param tipoDocumento   Tipo de documento.

     * @param numeroDocumento Número de documento.

     * @param nombres         Nombres.

     * @param apellidos       Apellidos.

     * @param email           Correo electrónico.

     * @param telefono        Teléfono.

     * @param tipoPersona     Clasificación.

     * @param activo          Estado activo/inactivo.

     * @param fechaRegistro   Fecha de registro en el sistema.

     */

    public Persona(Long id, String tipoDocumento, String numeroDocumento, String nombres, String apellidos,

            String email, String telefono, String tipoPersona, boolean activo, LocalDateTime fechaRegistro) {

        this.id = id;

        this.tipoDocumento = tipoDocumento;

        this.numeroDocumento = numeroDocumento;

        this.nombres = nombres;

        this.apellidos = apellidos;

        this.email = email;

        this.telefono = telefono;

        this.tipoPersona = tipoPersona;

        this.activo = activo;

        this.fechaRegistro = fechaRegistro;

    }



    /**

     * Constructor completo con Empresa.

     *

     * @param id              Identificador único.

     * @param tipoDocumento   Tipo de documento.

     * @param numeroDocumento Número de documento.

     * @param nombres         Nombres.

     * @param apellidos       Apellidos.

     * @param email           Correo electrónico.

     * @param telefono        Teléfono.

     * @param tipoPersona     Clasificación.

     * @param empresaId       Identificador de la empresa.

     * @param activo          Estado activo/inactivo.

     * @param fechaRegistro   Fecha de registro en el sistema.

     */

    public Persona(Long id, String tipoDocumento, String numeroDocumento, String nombres, String apellidos,

            String email, String telefono, String tipoPersona, Long empresaId, boolean activo, LocalDateTime fechaRegistro) {

        this(id, tipoDocumento, numeroDocumento, nombres, apellidos, email, telefono, tipoPersona, activo, fechaRegistro);

        this.empresaId = empresaId;

    }



    // Getters y Setters



    public Long getId() {

        return id;

    }



    public void setId(Long id) {

        this.id = id;

    }



    public String getTipoDocumento() {

        return tipoDocumento;

    }



    public void setTipoDocumento(String tipoDocumento) {

        this.tipoDocumento = tipoDocumento;

    }



    public String getNumeroDocumento() {

        return numeroDocumento;

    }



    public void setNumeroDocumento(String numeroDocumento) {

        this.numeroDocumento = numeroDocumento;

    }



    public String getNombres() {

        return nombres;

    }



    public void setNombres(String nombres) {

        this.nombres = nombres;

    }



    public String getApellidos() {

        return apellidos;

    }



    public void setApellidos(String apellidos) {

        this.apellidos = apellidos;

    }



    public String getNombreCompleto() {

        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");

    }



    public String getEmail() {

        return email;

    }



    public void setEmail(String email) {

        this.email = email;

    }



    public String getTelefono() {

        return telefono;

    }



    public void setTelefono(String telefono) {

        this.telefono = telefono;

    }



    public String getTipoPersona() {

        return tipoPersona;

    }



    public void setTipoPersona(String tipoPersona) {

        this.tipoPersona = tipoPersona;

    }



    public Long getEmpresaId() {

        return empresaId;

    }



    public void setEmpresaId(Long empresaId) {

        this.empresaId = empresaId;

    }



    public boolean isActivo() {

        return activo;

    }



    public void setActivo(boolean activo) {

        this.activo = activo;

    }



    public LocalDateTime getFechaRegistro() {

        return fechaRegistro;

    }



    public void setFechaRegistro(LocalDateTime fechaRegistro) {

        this.fechaRegistro = fechaRegistro;

    }



    @Override

    public boolean equals(Object o) {

        if (this == o)

            return true;

        if (o == null || getClass() != o.getClass())

            return false;

        Persona persona = (Persona) o;

        return Objects.equals(id, persona.id) ||

                (Objects.equals(tipoDocumento, persona.tipoDocumento)

                        && Objects.equals(numeroDocumento, persona.numeroDocumento));

    }



    @Override

    public int hashCode() {

        return Objects.hash(id, tipoDocumento, numeroDocumento);

    }



    @Override

    public String toString() {

        return "Persona{" +

                "id=" + id +

                ", tipoDocumento='" + tipoDocumento + '\'' +

                ", numeroDocumento='" + numeroDocumento + '\'' +

                ", nombreCompleto='" + getNombreCompleto() + '\'' +

                ", tipoPersona='" + tipoPersona + '\'' +

                ", empresaId=" + empresaId +

                ", activo=" + activo +

                '}';

    }



    // Atributo temporal para evitar el error de compilación

    private String urlFoto;



    public String getUrlFoto() {

        return urlFoto;

    }



    public void setUrlFoto(String urlFoto) {

        this.urlFoto = urlFoto;

    }

} 

