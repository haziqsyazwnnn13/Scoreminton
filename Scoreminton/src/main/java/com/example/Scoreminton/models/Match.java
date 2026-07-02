package com.example.Scoreminton.models;

import jakarta.persistence.*;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    // --- SNAPSHOT FIELDS (Decoupled from Team/Player databases) ---
    private String teamOneName;
    private String teamTwoName;

    private String t1P1Name;
    private String t1P2Name;
    private String t2P1Name;
    private String t2P2Name;

    private int teamOneScore = 0;
    private int teamTwoScore = 0;

    private String status = "IN_PROGRESS";

    @Column(length = 2000)
    private String teamOneHistory = "0";

    @Column(length = 2000)
    private String teamTwoHistory = "0";


    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTeamOneName() { return teamOneName; }
    public void setTeamOneName(String teamOneName) { this.teamOneName = teamOneName; }

    public String getTeamTwoName() { return teamTwoName; }
    public void setTeamTwoName(String teamTwoName) { this.teamTwoName = teamTwoName; }

    public String getT1P1Name() { return t1P1Name; }
    public void setT1P1Name(String t1P1Name) { this.t1P1Name = t1P1Name; }

    public String getT1P2Name() { return t1P2Name; }
    public void setT1P2Name(String t1P2Name) { this.t1P2Name = t1P2Name; }

    public String getT2P1Name() { return t2P1Name; }
    public void setT2P1Name(String t2P1Name) { this.t2P1Name = t2P1Name; }

    public String getT2P2Name() { return t2P2Name; }
    public void setT2P2Name(String t2P2Name) { this.t2P2Name = t2P2Name; }

    public int getTeamOneScore() { return teamOneScore; }
    public void setTeamOneScore(int teamOneScore) { this.teamOneScore = teamOneScore; }

    public int getTeamTwoScore() { return teamTwoScore; }
    public void setTeamTwoScore(int teamTwoScore) { this.teamTwoScore = teamTwoScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTeamOneHistory() { return teamOneHistory; }
    public void setTeamOneHistory(String teamOneHistory) { this.teamOneHistory = teamOneHistory; }

    public String getTeamTwoHistory() { return teamTwoHistory; }
    public void setTeamTwoHistory(String teamTwoHistory) { this.teamTwoHistory = teamTwoHistory; }
}