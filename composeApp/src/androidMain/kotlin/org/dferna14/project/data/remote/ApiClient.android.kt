package org.dferna14.project.data.remote

/**
 * En el emulador Android, 10.0.2.2 apunta al localhost del PC.
 * Si usas dispositivo físico, cambia esta IP por la de tu PC en la red WiFi.
 * Ejemplo: "http://192.168.1.100:8080"
 *
 * IMPORTANTE: La IP 172.29.208.1 es de WSL (virtual) y NO funciona para la tablet.
 * Usa la IP de tu adaptador WiFi físico (busca en ipconfig "Wireless LAN adapter Wi-Fi").
 *
 * PASO 1: Obtén tu IP real (debe ser 192.168.x.x o 10.x.x.x)
 * PASO 2: Reemplaza "TU_IP_FISICA" abajo con esa IP.ç
 *192.168.1.138 ethernet casa
 * 172.20.10.2 datos movil
 * emulador 10.0.2.2
 *
 */
actual val BASE_URL: String = "http://192.168.1.138:8080"
