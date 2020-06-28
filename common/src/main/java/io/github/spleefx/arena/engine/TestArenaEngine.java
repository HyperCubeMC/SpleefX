package io.github.spleefx.arena.engine;

import io.github.spleefx.arena.ArenaPlayer;
import io.github.spleefx.arena.ArenaStage;
import io.github.spleefx.extension.GameExtension.ScoreboardType;
import io.github.spleefx.team.GameTeam;

public class TestArenaEngine {

    private int countdown;
    private int gameTime;

    public void join() {
    }

    protected void placeInTeam(ArenaPlayer player) {
    }

    public boolean isTeamFull(GameTeam team) {
        return false;
    }

    public void leave(ArenaPlayer player) {
    }

    public void countdown() {
    }

    public ArenaStage getArenaStage() {
        return ArenaStage.WAITING;
    }

    public void start() {
    }

    public void executeLoop() {
    }

    public void end() {
    }

    public void forceEnd() {
    }

    public void reset() {
    }

    public void onDisconnect(ArenaPlayer player) {
    }

    public void onReconnect(ArenaPlayer player) {
    }

    public void onLose(ArenaPlayer player) {
    }

    public void displayScoreboard(ArenaPlayer player) {
    }

    public void broadcast(String message) {
    }

    public void loadPreGameData(ArenaPlayer player) {
    }

    public ScoreboardType getCurrentScoreboard() {
        return ScoreboardType.WAITING_IN_LOBBY;
    }

}