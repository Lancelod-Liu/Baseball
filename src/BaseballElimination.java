import java.util.ArrayList;


public class BaseballElimination {
    private int numberOfTeams, remainAll;
    private int[] win, l, r;
    private int[][] g;
    private ArrayList<String> teams;
    //private FlowNetwork fn;
    private int gameV;
    private int teamV;
    private int V;
    private int t;
    private int s;
    
    public BaseballElimination(String filename) // create a baseball division from given filename in format specified below
    {
        numberOfTeams = 0;
        teams = new ArrayList<String>();
        In in = new In(filename);
        //fn = null;
        
        if (!in.isEmpty()) {
            numberOfTeams = in.readInt();
            win = new int[numberOfTeams];
            l = new int[numberOfTeams];
            r = new int[numberOfTeams];
            g = new int[numberOfTeams][numberOfTeams];
        }
        else 
            throw new java.lang.IllegalArgumentException();
        
        int i = 0;
        remainAll = 0;
        while (i < numberOfTeams && in.hasNextLine()) {
            // read team name
            teams.add(in.readString());
            // read win lose remain
            win[i] = in.readInt();
            l[i] = in.readInt();
            r[i] = in.readInt();
            remainAll += r[i];
            // read against
            for (int j = 0; j < numberOfTeams; j++) {
                g[i][j] = in.readInt();
            }
            i++;
        }
        remainAll /= 2;
    }
    
    public int numberOfTeams() // number of teams
    {
        return numberOfTeams;
    }
    
    public Iterable<String> teams() // all teams
    {
        return teams;
    }
    
    public int wins(String team)                      // number of wins for given team
    {
        if (!teams.contains(team))
            throw new java.lang.IllegalArgumentException();
        int i = teams.indexOf(team);
        return win[i];
    }
    
    public int losses(String team)                    // number of losses for given team
    {
        if (!teams.contains(team))
            throw new java.lang.IllegalArgumentException();
        int i = teams.indexOf(team);
        return l[i];
    }
    
    public int remaining(String team)                 // number of remaining games for given team
    {
        if (!teams.contains(team))
            throw new java.lang.IllegalArgumentException();
        int i = teams.indexOf(team);
        return r[i];
    }
    
    public int against(String team1, String team2)    // number of remaining games between team1 and team2
    {
        if (!teams.contains(team1) || !teams.contains(team2))
            throw new java.lang.IllegalArgumentException();
        int i = teams.indexOf(team1);
        int j = teams.indexOf(team2);
        return g[i][j];
    }
    
    private int convertGV(int i, int j) //convert (i-j) vertex into flownetwork vertex
    {
        if (j == i) return 0;
        if (j < i) 
            return (numberOfTeams * 2 - j - 1) * j / 2 + i - j;
        else
            return (numberOfTeams * 2 - i - 1) * i / 2 + j - i;
    }
    
    
    private FlowNetwork buildFNG(int x) 
    {
        gameV = numberOfTeams * (numberOfTeams - 1) / 2;
        teamV = numberOfTeams;
        V = 2 + teamV + gameV;
        t = V - 1;
        s = 0;
        FlowNetwork fn = new FlowNetwork(V);
        // create Flow edge
        for (int k = 0; k < numberOfTeams; k++) {
            for (int j = k + 1; j < numberOfTeams; j++) {
                if (k == x || j == x) continue;
                fn.addEdge(new FlowEdge(s, convertGV(k, j), g[k][j]));
                fn.addEdge(new FlowEdge(convertGV(k, j), k + gameV + 1, Double.POSITIVE_INFINITY));
                fn.addEdge(new FlowEdge(convertGV(k, j), j + gameV + 1, Double.POSITIVE_INFINITY));
            }
        }
        for (int j = 0; j < teamV; j++) {
            if (j != x) {
                fn.addEdge(new FlowEdge(j + gameV + 1, t, win[x] + r[x] - win[j]));
            }
        }
        
        return fn;
    }
       
    public boolean isEliminated(String team)              // is given team eliminated?
    {
        if (!teams.contains(team))
            throw new java.lang.IllegalArgumentException();
        
        int x = teams.indexOf(team);
        
        // build graph and check
        for (int i = 0; i < numberOfTeams; i++) {
            if (i != x) {
                if (win[x] + r[x] - win[i] < 0) {
                    return true;
                }
            }
        }
        FlowNetwork fn = buildFNG(x);                
        new FordFulkerson(fn, s, t);
        for (FlowEdge fe: fn.adj(s)) {
            if (fe.flow() < fe.capacity())
                return true;
        }
        return false;
    }
    
    
    
    public Iterable<String> certificateOfElimination(String team)  // subset R of teams that eliminates given team; null if not eliminated
    {
        if (!teams.contains(team))
            throw new java.lang.IllegalArgumentException();
        int x = teams.indexOf(team);
        // build graph and check
        FordFulkerson ff = null;
        for (int i = 0; i < numberOfTeams; i++) {
            if (i != x) {
                if (win[x] + r[x] - win[i] < 0) {
                    ArrayList<String> alt = new ArrayList<String>();
                    alt.add(teams.get(i));
                    return alt;
                }                
            }
        }
        FlowNetwork fn = buildFNG(x);
        ff = new FordFulkerson(fn, s, t);
        for (FlowEdge fe: fn.adj(s)) {
            if (fe.flow() < fe.capacity()) { // eliminated !
                ArrayList<String> alt = new ArrayList<String>();
                // find mincut
                for (int a = gameV + 1; a < V - 1; a++) {
                    if (ff.inCut(a)) {
                        alt.add(teams.get(a - gameV - 1));
                    }
                }
                return alt;
            }
            else 
                continue;
        }
        
        return null;
    }
    /*The last six methods should throw a java.lang.IllegalArgumentException 
     * if one (or both) of the input arguments are invalid teams. */
    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        //StdOut.println(division.fn.toString());
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team))
                    StdOut.print(t + " ");
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }

}
