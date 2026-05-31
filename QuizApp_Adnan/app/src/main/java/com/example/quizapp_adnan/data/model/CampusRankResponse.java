package com.example.quizapp_adnan.data.model;

public class CampusRankResponse {
    private String campusNom;
    private int rang;
    private int totalUsers;

    public CampusRankResponse() {}

    public String getCampusNom() { return campusNom; }
    public void setCampusNom(String campusNom) { this.campusNom = campusNom; }

    public int getRang() { return rang; }
    public void setRang(int rang) { this.rang = rang; }

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
}
