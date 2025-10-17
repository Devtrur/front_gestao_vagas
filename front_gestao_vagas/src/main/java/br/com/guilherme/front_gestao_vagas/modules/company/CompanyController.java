package br.com.guilherme.front_gestao_vagas.modules.company;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.guilherme.front_gestao_vagas.modules.candidate.dto.CreateJobsDTO;
import br.com.guilherme.front_gestao_vagas.modules.company.dto.CreateCompanyDTO;
import br.com.guilherme.front_gestao_vagas.modules.company.services.CreateCompanyService;
import br.com.guilherme.front_gestao_vagas.modules.company.services.CreateJobsService;
import br.com.guilherme.front_gestao_vagas.modules.company.services.LoginCompanyService;
import br.com.guilherme.front_gestao_vagas.utils.FormatErrorMessage;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    private LoginCompanyService loginCompanyService;

    @Autowired
    private CreateCompanyService createCompanyService;

    @Autowired
    private CreateJobsService createJobsService;

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("company", new CreateCompanyDTO());
        return "company/create";
    }

    @PostMapping("/create")
    public String save(CreateCompanyDTO companyDTO, Model model) {
        try {
            this.createCompanyService.execute(companyDTO);
        } catch (HttpClientErrorException ex) {
            model.addAttribute("error_message", FormatErrorMessage.formatErrorMessage(ex.getResponseBodyAsString()));
            model.addAttribute("company", companyDTO);
        }
        model.addAttribute("company", companyDTO);
        return "company/create";
    }

    @GetMapping("/login")
    public String login() {
        return "company/login";
    }

    @PostMapping("/signIn")
    public String signIn(RedirectAttributes redirectAttributes, HttpSession session, String username, String password) {

        try {
            var token = this.loginCompanyService.execute(username, password);
            var grants = token.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" +
                            role.toString().toUpperCase()))
                    .toList();

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(null, null, grants);
            auth.setDetails(token.getAccess_token());

            SecurityContextHolder.getContext().setAuthentication(auth);
            SecurityContext securityContext = SecurityContextHolder.getContext();
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            session.setAttribute("token", token);

            return "redirect:/company/jobs";

        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("error_message", "Usuário ou senha incorretos");
            return "redirect:/company/jobs";
        }
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('COMPANY')")
    public String jobs(Model model) {
        model.addAttribute("jobs", new CreateJobsDTO());
        return "company/jobs";
    }

    @PostMapping("/jobs")
    public String createJobs(CreateJobsDTO jobs) {
        this.createJobsService.execute(jobs, getToken());
        return "redirect:/company/jobs";
    }

    private String getToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getDetails().toString();
    }
}