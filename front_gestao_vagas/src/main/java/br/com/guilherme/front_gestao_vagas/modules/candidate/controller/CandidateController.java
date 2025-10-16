package br.com.guilherme.front_gestao_vagas.modules.candidate.controller;

import org.springframework.security.core.Authentication;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.guilherme.front_gestao_vagas.modules.candidate.service.ApplyJobService;
import br.com.guilherme.front_gestao_vagas.modules.candidate.service.CandidateService;
import br.com.guilherme.front_gestao_vagas.modules.candidate.service.FindJobService;
import br.com.guilherme.front_gestao_vagas.modules.candidate.service.ProfileCandidateService;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/candidate")
public class CandidateController {

    @Autowired
    private FindJobService findJobService;

    @Autowired
    private ProfileCandidateService profileCandidateService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private ApplyJobService applyJobService;

    @GetMapping("/login")
    public String login() {
        return "candidate/login";
    }

    @GetMapping("/create")
    public String create() {
        return "candidate/create";
    }

    @PostMapping("/signIn")
    public String signIn(RedirectAttributes redirectAttributes, HttpSession session, String username, String password) {

        try {
            var token = this.candidateService.login(username, password);
            var grants = token.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase())).toList();

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(null, null, grants);
            auth.setDetails(token.getAccess_token());

            SecurityContextHolder.getContext().setAuthentication(auth);
            SecurityContext securityContext = SecurityContextHolder.getContext();
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            session.setAttribute("token", token);

            return "redirect:/candidate/profile";

        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("error_message", "Usuário ou senha incorretos");
            return "redirect:/candidate/login";
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public String profile(Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            var user = this.profileCandidateService.execute(authentication.getDetails().toString());

            model.addAttribute("user", user);

            return "candidate/profile";
        } catch (HttpClientErrorException e) {
            return "redirect:/candidate/login";
        }
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('CANDIDATE')")
    public String jobs(Model model, String filter) {
        try {
            if (filter != null) {
                var jobs = this.findJobService.execute(getToken(), filter);
                model.addAttribute("jobs", jobs);
            }
        } catch (HttpClientErrorException e) {
            return "redirect:/candidate/login";
        }
        return "candidate/jobs";
    }

    @PostMapping("/job/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    public String applyJob(@RequestParam("jobId") UUID jobId) {
        this.applyJobService.execute(getToken(), jobId);
        return "redirect:/candidate/jobs";
    }

    private String getToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getDetails().toString();
    }
}