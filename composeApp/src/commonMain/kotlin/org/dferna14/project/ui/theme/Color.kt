package org.dferna14.project.ui.theme

import androidx.compose.ui.graphics.Color

// ════════════════════════════════════════════════════════════════════════
//  Paleta «Cuaderno Vintage» — sistema de diseño Desktop
//  Primario: oliva  ·  Acento: terracota  ·  Base: crema/pergamino
// ════════════════════════════════════════════════════════════════════════

// ── Fondos ───────────────────────────────────────────────────────────────
val CremaPrincipal   = Color(0xFFFFFDF7)   // bg — pergamino
val CremaSecundario  = Color(0xFFF5ECD7)   // surface — sepia claro
val SuperficieSepia  = Color(0xFFF5ECD7)   // alias semántico para cards
val SuperficieDk     = Color(0xFFEAD9B8)   // cabecera de tablas
val BlancoPuro       = Color(0xFFFFFFFF)

// ── Oliva (color primario) ───────────────────────────────────────────────
val OlivaPrimario    = Color(0xFF5C6B2E)
val OlivaOscuro      = Color(0xFF4A5625)
val OlivaClaro       = Color(0xFF8AA055)
val OlivaTint        = Color(0xFFE8EDD8)   // fondos suaves primario

// ── Ocre (secundario) ───────────────────────────────────────────────────
val OcreSecundario   = Color(0xFFC8922A)

// ── Terracota (acento / alertas) ────────────────────────────────────────
// Nota: este tono era el primario naranja en la versión anterior
val NaranjaPrimario  = Color(0xFFD85A30)   // conservado por compatibilidad Android
val TerracotaAccent  = NaranjaPrimario
val NaranjaClaro     = Color(0xFFFBE5DA)   // conservado por compatibilidad Android
val TerracotaTint    = NaranjaClaro
val NaranjaOscuro    = Color(0xFF993C1D)   // conservado por compatibilidad Android

// ── Texto ────────────────────────────────────────────────────────────────
val TextoPrimario    = Color(0xFF2C1A0E)   // marrón muy oscuro
val TextoSecundario  = Color(0xFF5C4A38)   // marrón medio
val TextoTerciario   = Color(0xFF8B7355)   // marrón claro
val TextoPlaceholder = Color(0xFFB4A890)

// ── Bordes ───────────────────────────────────────────────────────────────
val BordeNormal      = Color(0xFFD4C5A8)
val BordeClaro       = Color(0xFFE8DDC4)
// Aliases para no romper código Android existente
val BordeSuave       = BordeClaro
val BordeMedio       = BordeNormal
val BordeNaranjaSuave = Color(0xFFE8C4B0)

// ── Sidebar (paleta oscura) ──────────────────────────────────────────────
val SidebarFondo     = Color(0xFF2C1A0E)
val SidebarBorde     = Color(0x14FFFDF7)   // rgba(255,253,247,0.08)
val SidebarLabel     = Color(0x4DFFFDF7)   // rgba(255,253,247,0.30)
val SidebarTexto     = Color(0xA6FFFDF7)   // rgba(255,253,247,0.65)
val SidebarHi        = Color(0xFFFFFDF7)   // = CremaPrincipal
val SidebarActivo    = Color(0x525C6B2E)   // rgba(92,107,46,0.32)

// ── Semánticos — sin cambios (usados por badges y estados) ──────────────
val VerdeValidada       = Color(0xFF3B6D11)
val VerdeFondoValidada  = Color(0xFFEAF3DE)
val AzulPendiente       = Color(0xFF185FA5)
val AzulFondoPendiente  = Color(0xFFE6F1FB)
val GrisBorrador        = Color(0xFF5F5E5A)
val GrisFondoBorrador   = Color(0xFFF1EFE8)
val RojoEliminar        = Color(0xFFA32D2D)
val RojoFondoEliminar   = Color(0xFFFCEBEB)
val AmbarProducto       = Color(0xFF854F0B)
val AmbarFondoProducto  = Color(0xFFFAEEDA)
val VerdeInfo           = Color(0xFF3B6D11)
val VerdeFondoInfo      = Color(0xFFEAF3DE)
