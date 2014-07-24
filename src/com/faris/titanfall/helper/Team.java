package com.faris.titanfall.helper;

/**
 * @author KingFaris10
 */
public enum Team {
    NONE(0, "None", "&c"), IMC(1, "IMC", "&a"), MILITIA(2, "Militia", "&b");

    private String teamName = "";
    private String teamColour = "";
    private int teamID = -1;

    private int teamScore = 0;

    private Team(int teamID, String teamName, String teamColour) {
        this.teamID = teamID;
        this.teamName = teamName;
        this.teamColour = Utils.replaceChatColour(teamColour);
        this.teamScore = 0;
    }

    public int addScore() {
        return this.addScore(1);
    }

    public int addScore(int score) {
        this.teamScore += score;
        return this.teamScore;
    }

    public String getColour() {
        return this.teamColour;
    }

    public int getID() {
        return this.teamID;
    }

    public String getName() {
        return this.teamName;
    }

    public int getScore() {
        return this.teamScore;
    }

    public void resetScore() {
        this.teamScore = 0;
    }

    public void setScore(int score) {
        this.teamScore = score;
    }

    public String toString() {
        return this.teamName;
    }

}
