# Extremo

Siempre que hagas update del mod, borra el .jar anterior y pega el nuevo en estas 2 rutas:

- `C:\Users\alana\AppData\Roaming\.dawn\hosted-servers\servers\f01939df-c018-4eef-91f6-d0c691d35753\mods`
- `C:\Users\alana\AppData\Roaming\.dawn\profiles\dawn-performance\private-game-content\mods`

## Ejemplos de misiones

**Misión básica:**
```
/extremo misiones create cazar_zombis kill minecraft:zombie 20
/extremo misiones setxp cazar_zombis 100
/extremo misiones settime cazar_zombis 120
```
Caza 20 zombis, recompensa 100 XP, expira en 120 minutos (2h).

**Misión con límite de jugadores:**
```
/extremo misiones create recolectar_diamantes collect minecraft:diamond 5
/extremo misiones setxp recolectar_diamantes 200
/extremo misiones setmaxclaims recolectar_diamantes 5
```
Consigue 5 diamantes, recompensa 200 XP, máximo 5 jugadores pueden reclamarla.
