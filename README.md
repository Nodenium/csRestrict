# csRestrict

Server-side Fabric mod for Minecraft 26.2, built for Project Nodenium.

## What it does

Stops non-operator players from using spectator mode (entered via Carpet's `/cs`
command, or any other means) to instantly jump to and view real players' bases.
Carpet fake players (bots) are unaffected.

`/cs` itself still works, and a restricted spectator can still fly around normally.
Only the vanilla "click a player's name to teleport your camera to them" feature is
blocked, and only against real players.

### How it works

1. **Teleport block** - the spectator "jump to player" feature sends a
   `ServerboundTeleportToEntityPacket` which the server resolves and uses to physically
   teleport the requester. A mixin on `ServerGamePacketListenerImpl#handleTeleportToEntityPacket`
   cancels this specific packet when the requester is a non-op player currently in
   spectator mode and the resolved target is a real player (not a Carpet fake player).
2. **Tab list hiding** - as a belt-and-suspenders measure, a mixin filters outgoing
   `ClientboundPlayerInfoUpdatePacket`s so restricted spectators never see real players
   listed in their tab list at all (Carpet bots and their own entry are never hidden).
3. **Tab list sync** - entering or leaving restricted mode pushes a corrective packet
   (removing already-known real players, or resending the full list on exit) so nothing
   is left showing a stale gamemode or tab-list entry from before the transition.
4. **Command detection** - a mixin on `handleChatCommand` logs (debug level) whenever a
   player runs a "watched" command (`cs` by default, configurable in
   `config/csrestrict.json`). This is diagnostic only; the actual restriction is based on
   live gamemode + op state, not the literal command name, so it can't be bypassed by
   renaming or aliasing the Carpet script.

Whether a player is currently restricted is evaluated live, every time, from their actual
gamemode and op status (`ServerPlayer#isSpectator()` and `PlayerList#isOp`) - not cached.
The only stored state is a small per-player flag used solely to detect the enter/exit
transition for step 3.

Carpet fake players are detected via a soft dependency (no compile/runtime dependency on
fabric-carpet): by exact class name (`carpet.patches.EntityPlayerMPFake`) and, as a
fallback for forks that relocate the package, by the presence of Carpet's `isAShadow`
marker field.

## Building

Requires JDK 25.

```
./gradlew build
```

The built jar is in `build/libs/csrestrict-1.0.0.jar`.

## Installing

Drop the jar into the server's `mods/` folder alongside Fabric API 0.160.0+26.2 (or
newer for 26.2) and Fabric Loader 0.19.5+. It's server-side only (`"environment": "server"`
in `fabric.mod.json`) - no client installation needed.

## Configuration

`config/csrestrict.json` is created on first run:

```json
{
  "watchedCommands": ["cs"]
}
```

Add extra command names/aliases here if the server renames or adds alternate ways to
trigger the same script; this only changes what gets logged, not what gets blocked.
