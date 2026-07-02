package com.example.Scoreminton.controllers;

import com.example.Scoreminton.models.Match;
import com.example.Scoreminton.models.Player;
import com.example.Scoreminton.models.Team;
import com.example.Scoreminton.models.User;
import com.example.Scoreminton.repositories.MatchRepository;
import com.example.Scoreminton.repositories.PlayerRepository;
import com.example.Scoreminton.repositories.TeamRepository;
import com.example.Scoreminton.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    // Clean up teams if a player was deleted
    private void cleanupBrokenTeams(List<Team> teams) {
        teams.removeIf(team -> {
            try {
                team.getPlayerOne().getName();
                team.getPlayerTwo().getName();
                return false;
            } catch (Exception e) {
                teamRepository.delete(team);
                return true;
            }
        });
    }

    // ==========================================
    // AUTHENTICATION ROUTES
    // ==========================================
    @GetMapping("/")
    public String redirectToLogin() { return "redirect:/login"; }

    @GetMapping("/login")
    public String showLoginPage() { return "login"; }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            session.setAttribute("loggedInUser", userOpt.get());
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    @GetMapping("/signup")
    public String showSignUpPage(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignUp(@ModelAttribute User user, Model model) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Email address is already registered!");
            return "signup";
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Username is already taken!");
            return "signup";
        }
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ==========================================
    // MAIN DASHBOARD & HISTORY ROUTES
    // ==========================================
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        List<Match> myMatches = matchRepository.findByUserId(loggedInUser.getId());
        model.addAttribute("user", loggedInUser);
        model.addAttribute("matches", myMatches);
        return "dashboard";
    }

    @GetMapping("/matches/history")
    public String showMatchHistory(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        List<Match> myMatches = matchRepository.findByUserId(loggedInUser.getId());
        model.addAttribute("matches", myMatches);
        return "history";
    }

    // ==========================================
    // PLAYER & TEAM MANAGEMENT
    // ==========================================
    @GetMapping("/players/manage")
    public String showPlayerManagement(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        List<Player> myPlayers = playerRepository.findByUserId(loggedInUser.getId());
        List<Team> myTeams = teamRepository.findByUserId(loggedInUser.getId());
        cleanupBrokenTeams(myTeams);

        model.addAttribute("players", myPlayers);
        model.addAttribute("teams", myTeams);
        return "manage-players";
    }

    @PostMapping("/players/add")
    public String addPlayer(@RequestParam String playerName, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Player newPlayer = new Player();
        newPlayer.setName(playerName);
        newPlayer.setUser(loggedInUser);
        playerRepository.save(newPlayer);
        return "redirect:/players/manage";
    }

    @PostMapping("/teams/add")
    public String addTeam(@RequestParam String teamName, @RequestParam Long playerOneId, @RequestParam Long playerTwoId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Player p1 = playerRepository.findById(playerOneId).orElse(null);
        Player p2 = playerRepository.findById(playerTwoId).orElse(null);

        if (p1 != null && p2 != null) {
            Team newTeam = new Team();
            newTeam.setTeamName(teamName);
            newTeam.setPlayerOne(p1);
            newTeam.setPlayerTwo(p2);
            newTeam.setUser(loggedInUser);
            teamRepository.save(newTeam);
        }
        return "redirect:/players/manage";
    }

    // ==========================================
    // DELETION & EDITING ROUTES (NO MATCH DELETIONS REQUIRED ANYMORE!)
    // ==========================================
    @PostMapping("/players/delete")
    public String deletePlayer(@RequestParam Long playerId) {
        // Only delete teams that require this player. History is safe!
        List<Team> allTeams = teamRepository.findAll();
        for (Team t : allTeams) {
            if (t.getPlayerOne().getId().equals(playerId) || t.getPlayerTwo().getId().equals(playerId)) {
                teamRepository.delete(t);
            }
        }
        playerRepository.deleteById(playerId);
        return "redirect:/players/manage";
    }

    @PostMapping("/players/edit")
    public String editPlayer(@RequestParam Long playerId, @RequestParam String newName) {
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player != null && !newName.trim().isEmpty()) {
            player.setName(newName.trim());
            playerRepository.save(player);
        }
        return "redirect:/players/manage";
    }

    @PostMapping("/teams/delete")
    public String deleteTeam(@RequestParam Long teamId) {
        teamRepository.deleteById(teamId); // History remains safe!
        return "redirect:/players/manage";
    }

    @PostMapping("/teams/edit")
    public String editTeam(@RequestParam Long teamId, @RequestParam String newTeamName) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team != null && !newTeamName.trim().isEmpty()) {
            team.setTeamName(newTeamName.trim());
            teamRepository.save(team);
        }
        return "redirect:/players/manage";
    }

    @PostMapping("/teams/deleteAll")
    public String deleteAllTeams() {
        teamRepository.deleteAll(); // History remains safe!
        return "redirect:/players/manage";
    }

    // ==========================================
    // MATCH CREATION (SNAPSHOT SYSTEM)
    // ==========================================
    @GetMapping("/matches/create")
    public String showCreateMatchPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        List<Team> myTeams = teamRepository.findByUserId(loggedInUser.getId());
        cleanupBrokenTeams(myTeams);
        List<Player> myPlayers = playerRepository.findByUserId(loggedInUser.getId());

        model.addAttribute("teams", myTeams);
        model.addAttribute("players", myPlayers);
        return "create-match";
    }

    @PostMapping("/matches/create")
    public String processCreateMatch(@RequestParam(required = false) Long teamOneId,
                                     @RequestParam(required = false) Long teamTwoId,
                                     @RequestParam(required = false) Long playerOneId,
                                     @RequestParam(required = false) Long playerTwoId,
                                     @RequestParam String matchType,
                                     HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Match newMatch = new Match();
        newMatch.setUser(loggedInUser);

        if ("double".equals(matchType)) {
            if (teamOneId != null && teamOneId.equals(teamTwoId)) return "redirect:/matches/create?error=sameTeam";

            Team t1 = teamRepository.findById(teamOneId).orElse(null);
            Team t2 = teamRepository.findById(teamTwoId).orElse(null);

            if (t1 != null && t2 != null) {
                // Snapshot the names!
                newMatch.setTeamOneName(t1.getTeamName());
                newMatch.setT1P1Name(t1.getPlayerOne().getName());
                newMatch.setT1P2Name(t1.getPlayerTwo().getName());

                newMatch.setTeamTwoName(t2.getTeamName());
                newMatch.setT2P1Name(t2.getPlayerOne().getName());
                newMatch.setT2P2Name(t2.getPlayerTwo().getName());
            }
        } else if ("single".equals(matchType)) {
            if (playerOneId != null && playerOneId.equals(playerTwoId)) return "redirect:/matches/create?error=samePlayer";

            Player p1 = playerRepository.findById(playerOneId).orElse(null);
            Player p2 = playerRepository.findById(playerTwoId).orElse(null);

            if (p1 != null && p2 != null) {
                // Snapshot the individual players as the team!
                newMatch.setTeamOneName(p1.getName());
                newMatch.setT1P1Name(p1.getName());
                newMatch.setT1P2Name(p1.getName());

                newMatch.setTeamTwoName(p2.getName());
                newMatch.setT2P1Name(p2.getName());
                newMatch.setT2P2Name(p2.getName());
            }
        }

        Match savedMatch = matchRepository.save(newMatch);
        return "redirect:/matches/live/" + savedMatch.getId();
    }

    @GetMapping("/matches/live/{id}")
    public String showLiveMatch(@PathVariable Long id, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Match match = matchRepository.findById(id).orElse(null);
        if (match == null || !match.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("match", match);
        return "live-match";
    }

    @PostMapping("/matches/live/{id}/score")
    @ResponseBody
    public String updateScore(@PathVariable Long id, @RequestParam int team, @RequestParam int change) {
        Match match = matchRepository.findById(id).orElse(null);
        if (match != null) {
            if (team == 1) match.setTeamOneScore(Math.max(0, match.getTeamOneScore() + change));
            else if (team == 2) match.setTeamTwoScore(Math.max(0, match.getTeamTwoScore() + change));

            match.setTeamOneHistory(match.getTeamOneHistory() + "," + match.getTeamOneScore());
            match.setTeamTwoHistory(match.getTeamTwoHistory() + "," + match.getTeamTwoScore());
            matchRepository.save(match);
        }
        return "Success";
    }

    @PostMapping("/matches/live/{id}/end")
    public String endMatch(@PathVariable Long id) {
        Match match = matchRepository.findById(id).orElse(null);
        if (match != null) {
            match.setStatus("COMPLETED");
            matchRepository.save(match);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/matches/history/delete")
    public String deleteMatchHistory(@RequestParam Long matchId) {
        matchRepository.deleteById(matchId);
        return "redirect:/matches/history";
    }

    @GetMapping("/matches/history/{id}")
    public String showMatchDetails(@PathVariable Long id, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Match match = matchRepository.findById(id).orElse(null);
        if (match == null || !match.getUser().getId().equals(loggedInUser.getId())) {
            return "redirect:/matches/history";
        }

        model.addAttribute("match", match);
        return "match-details";
    }
}