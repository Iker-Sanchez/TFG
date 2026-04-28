<<<<<<< HEAD
package com.example.tfg;

public class Standing {
    private int rank;
    private Team team;
    private int points;
    private Stats all;

    public int getRank() { return rank; }
    public Team getTeam() { return team; }
    public int getPoints() { return points; }
    public Stats getAll() { return all; }

    public class Team {
        private String name;
        private String logo;
        public String getName() { return name; }
        public String getLogo() { return logo; }
    }

    public class Stats {
        private int played, win, draw, lose;
        public int getPlayed() { return played; }
        public int getWin() { return win; }
        public int getDraw() { return draw; }
        public int getLose() { return lose; }
    }
=======
package com.example.tfg;

public class Standing {
    private int rank;
    private Team team;
    private int points;
    private Stats all;

    public int getRank() { return rank; }
    public Team getTeam() { return team; }
    public int getPoints() { return points; }
    public Stats getAll() { return all; }

    public class Team {
        private String name;
        private String logo;
        public String getName() { return name; }
        public String getLogo() { return logo; }
    }

    public class Stats {
        private int played, win, draw, lose;
        public int getPlayed() { return played; }
        public int getWin() { return win; }
        public int getDraw() { return draw; }
        public int getLose() { return lose; }
    }
>>>>>>> 9fa90aba5b4448f5c142f03d970a87a0aed0c36f
}