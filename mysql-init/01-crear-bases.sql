-- Se ejecuta solo la primera vez que se crea el volumen de MySQL.
-- Una base de datos por microservicio, todas en el mismo servidor.
CREATE DATABASE IF NOT EXISTS induztek_empresas   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS induztek_usuarios   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS induztek_auth       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS induztek_equipos    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS induztek_protocolos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;