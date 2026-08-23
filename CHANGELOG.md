# Changelog

## [2.3] - 2026-08-23

### Full plugin refactor

- **Improved performance** - All systems optimized for lower CPU and memory usage.
- **Tab-completion** - All commands now show suggestions while typing.
- **Cached messages** - Texts are loaded once and kept in memory, reducing lag.
- **Optimized bypass** - Exception lists are searched in O(1) instead of O(n).
- **Fixed memory leaks** - Plugin now properly releases all resources on disable.
- **Fixed thread safety** - Fixed concurrency issues in the hide players system.
- **Removed duplicate code** - Command registration logic unified and simplified.
- **Javadoc documentation** - All public classes documented.

### Affected commands

- `/hideplayers` - Now shows suggestions: `on`, `off`, `toggle`, `status`.
- `/hidebypass` - Now shows suggestions: `on`, `off`, `toggle`, `status`.
- `/weatherlock` - Now shows suggestions for subcommands `weather` and `time`.
- `/pvp` - Now shows suggestions: `on`, `off`, `toggle`, `status`.
- `/pblcore` - Now shows suggestions for all subcommands.

### Compatibility

- Maintained support for Paper 1.18.2+ and Spigot.
- No changes to existing configuration.
- No changes to permissions.

---

## [2.3] - 2026-08-23

### Refactorización completa del plugin

- **Rendimiento mejorado** - Todos los sistemas optimizados para menor uso de CPU y memoria.
- **Tab-completion** - Todos los comandos ahora muestran sugerencias al escribir.
- **Mensajes cacheados** - Los textos se cargan una vez y se mantienen en memoria, reduciendo lag.
- **Bypass optimizado** - Las listas de excepciones se buscan en O(1) en lugar de O(n).
- **Fix memory leaks** - El plugin ahora libera correctamente todos los recursos al desactivarse.
- **Fix thread safety** - Corregidos problemas de concurrencia en el sistema de ocultar jugadores.
- **Código duplicado eliminado** - Lógica de registro de comandos unificada y simplificada.
- **Documentación Javadoc** - Todas las clases públicas documentadas.

### Comandos afectados

- `/hideplayers` - Ahora muestra sugerencias: `on`, `off`, `toggle`, `status`.
- `/hidebypass` - Ahora muestra sugerencias: `on`, `off`, `toggle`, `status`.
- `/weatherlock` - Ahora muestra sugerencias para subcomandos `weather` y `time`.
- `/pvp` - Ahora muestra sugerencias: `on`, `off`, `toggle`, `status`.
- `/pblcore` - Ahora muestra sugerencias para todos los subcomandos.

### Compatibilidad

- Mantenido soporte para Paper 1.18.2+ y Spigot.
- Sin cambios en la configuración existente.
- Sin cambios en los permisos.

---