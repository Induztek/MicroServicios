# Induztek Ingeniería · Microservicios

Este es el backend de **Induztek_Ingeniera_app**, la app Android que hicimos para el ramo de Desarrollo de Aplicaciones Móviles (DSY1105) en Duoc UC.

La idea del proyecto nace de algo bien concreto: en terreno, los técnicos de Induztek miden la aislación de transformadores y registran los resultados en un protocolo. Antes eso quedaba en papel o en planillas sueltas. Con la app, el técnico registra la medición en el lugar (con foto y ubicación), el supervisor asigna los equipos a cada técnico y el cliente puede revisar los protocolos de su planta cuando quiera.

Este repositorio contiene todos los servicios que hay detrás de eso.

---

## Cómo está armado

Partimos con un solo backend, pero lo separamos en microservicios para que cada parte del negocio viva por su cuenta y tenga su propia base de datos:

- **empresas** (8081): las empresas cliente, como Industrias Aurora o Envases del Sur.
- **usuarios** (8082): los datos de cada persona: nombre, RUT, teléfono, rol y empresa.
- **auth** (8083): correos, contraseñas, inicio de sesión con JWT y recuperación de contraseña. Lo dejamos aparte a propósito, para que las credenciales no se mezclen con los datos personales.
- **equipos** (8084): los transformadores, dónde están instalados y qué técnico los tiene asignados.
- **protocolos** (8085): las mediciones que registra el técnico y su estado (registrado en terreno, formalizado, aprobado o con observación).

Alrededor de ellos hay dos piezas que los mantienen ordenados:

- **Eureka** (8761): es como una guía telefónica. Cada servicio se anota ahí al arrancar, así los demás lo encuentran por su nombre y no por una IP fija.
- **API Gateway** (8090): es la única puerta de entrada. La app nunca habla directo con un microservicio, siempre pasa por aquí, y el gateway sabe a quién derivar cada petición.

```
            App Android
                 │
          API Gateway :8090  ◄──►  Eureka :8761
                 │
  ┌────────┬─────┴────┬─────────┬────────────┐
empresas usuarios   auth     equipos    protocolos
```

Cada servicio tiene su propia base (`induztek_empresas`, `induztek_usuarios`, etc.) y no hay claves foráneas entre ellas. Cuando uno necesita información de otro, se la pide por HTTP. Por ejemplo, `usuarios` le pregunta a `empresas` el nombre de la empresa, y `auth` le pregunta a `usuarios` quién es la persona que acaba de iniciar sesión.

---

## Levantarlo con Docker

Es la forma más rápida, porque deja todo corriendo con un solo comando, incluida la base de datos. Solo necesitas tener Docker Desktop abierto.

```bash
cp .env.example .env
docker compose up --build
```

> En Windows, el primer paso es `copy .env.example .env`.

La primera vez se demora unos minutos, porque descarga Java, Maven, MySQL y todas las dependencias. Las siguientes veces es mucho más rápido.

Cuando termine, puedes revisar:

- **http://localhost:8761**: el panel de Eureka. Deberías ver los seis servicios registrados.
- **http://localhost:8090/swagger-ui.html**: la documentación de toda la API en un solo lugar. Arriba a la derecha eliges el microservicio que quieres revisar, y desde ahí mismo puedes probar los endpoints.

Para apagar todo, usa `docker compose down`. Si además quieres partir con la base de datos limpia, usa `docker compose down -v`.

**Si la descarga falla por internet** (nos pasó: al compilar los siete servicios a la vez, Maven a veces se queda sin conexión), compílalos de a uno:

```powershell
$env:COMPOSE_PARALLEL_LIMIT = 1
docker compose build
docker compose up
```

---

## Levantarlo sin Docker

Mientras desarrollábamos lo corríamos así, con el MySQL de Laragon (puerto 3306, usuario `root` sin contraseña). No hace falta crear las bases: cada servicio crea la suya al arrancar.

En cada carpeta se ejecuta `.\mvnw spring-boot:run`, en este orden:

1. `eureka-main`
2. `empresas-main`, `usuarios-main`, `auth-main`, `equipos` y `protocolos`
3. `api-gateway-main`, al final, cuando los demás ya estén arriba

No hay que cambiar nada para pasar de un modo al otro. Los `application.properties` usan valores como `${EUREKA_URL:http://localhost:8761/eureka/}`: si la variable no existe, usan `localhost`, y en Docker el `docker-compose.yml` se encarga de definirla.

---

## Cuentas para probar

Al arrancar por primera vez, los servicios cargan datos de ejemplo. Todas las cuentas usan la contraseña **`123456`**:

| Correo | Rol | Empresa |
|---|---|---|
| tecnico@gmail.com | Técnico | Induztek Ingeniería SpA |
| supervisor@gmail.com | Supervisor | Induztek Ingeniería SpA |
| admin@gmail.com | Administrador | Induztek Ingeniería SpA |
| cliente@gmail.com | Cliente | Industrias Aurora S.A. |
| cliente2@gmail.com | Cliente | Envases del Sur S.A. |

También quedan cargados cuatro transformadores: tres en Industrias Aurora (TR-0142, TR-0098 y TR-0215) y uno en Envases del Sur (TR-0330).

---

## Qué ofrece cada servicio

Todo se consume a través del gateway, en `http://localhost:8090`. En Swagger está el detalle de cada endpoint, pero a grandes rasgos:

**auth** · `/api/auth`
Inicio de sesión (`/login`, devuelve el token JWT), creación de credenciales para un usuario nuevo, cambio de contraseña y recuperación con un código de 6 dígitos que dura 15 minutos (`/olvide` y `/restablecer`).

**usuarios** · `/api/usuarios`
Listar (con búsqueda por nombre o RUT), ver, crear, editar y eliminar usuarios. Cada respuesta ya trae el nombre de la empresa y del rol.

**empresas** · `/api/empresas`
Listar, ver, crear y editar empresas. No permite dos empresas con el mismo nombre.

**equipos** · `/api/equipos`
El CRUD de equipos, más filtros por cliente (`/cliente/{id}`) y por técnico (`/tecnico/{id}`). Tiene dos acciones aparte: actualizar la ubicación en el mapa (`PATCH /{id}/ubicacion`) y que el supervisor asigne un técnico (`PATCH /{id}/tecnico`).

**protocolos** · `/api/protocolos`
Registrar un protocolo con sus mediciones y foto, consultarlos por equipo o por cliente, y formalizarlos (`PATCH /{id}/formalizar`).

---

## Pruebas

Cada microservicio tiene sus pruebas unitarias, que corren sobre una base H2 en memoria, así que no necesitas MySQL para ejecutarlas:

```bash
cd equipos
.\mvnw test
```

Después de correrlas, el reporte de cobertura de JaCoCo queda en `target/site/jacoco/index.html`.

---

## Algunas decisiones que tomamos

- **auth separado de usuarios.** Así las contraseñas (guardadas con BCrypt) quedan aisladas del resto de los datos personales.
- **La app funciona aunque no haya conexión.** Si el servidor no responde, la app sigue trabajando con lo que tiene guardado en Room, y los protocolos que no alcanzaron a subirse quedan marcados como no sincronizados. En terreno, la señal no siempre acompaña.
- **Las fotos viajan en Base64.** La app las guarda como archivo en el teléfono, y al enviarlas al servidor se convierten a texto para que vayan dentro del JSON.
- **Cosas que dejamos pendientes:** el código de recuperación hoy se imprime en la consola de `auth` en vez de enviarse por correo, y los servicios todavía no exigen el token JWT en cada petición. Son los siguientes pasos naturales del proyecto.

---

Hecho por Ignacio González y Mario, Duoc UC, 2026.
