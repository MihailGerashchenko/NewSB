package com.example.newsb.controller;

import com.example.newsb.entity.Customer;
import com.example.newsb.entity.Test;
import com.example.newsb.entity.UserRole;
import com.example.newsb.service.TestService;
import com.example.newsb.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
public class MyController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TestService testService;

    public MyController(UserService userService, PasswordEncoder passwordEncoder, TestService testService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.testService = testService;
    }


    @GetMapping("/")
    public String index(Model model, @PageableDefault(sort ={"id"}, direction = Sort.Direction.DESC)
                        Pageable pageable) {
        User user = getCurrentUser();
        String login = user.getUsername();
        Customer dbUser = userService.findByLogin(login);

        model.addAttribute("login", login);
        model.addAttribute("roles", user.getAuthorities());
        model.addAttribute("admin", isAdmin(user));
        model.addAttribute("email", dbUser.getEmail());
        model.addAttribute("phone", dbUser.getPhone());
        model.addAttribute("address", dbUser.getAddress());

//        long pageCount = 6;
//        Page<Test> tests = testService.getAllTestsPageable(pageable);
//        model.addAttribute("tests", tests);
//
//        ArrayList<Test> list = testService.findTestByID(id);
//        model.addAttribute("tests", list);


        List<Test> list = testService.findAllTests();
        model.addAttribute("tests", list);
//        model.addAttribute("pages", pageCount);

//        return findPagenated(1, model);
      return "index";
    }

    @GetMapping("/page/{pageNo}")
    public String findPagenated(@PathVariable(value = "pageNo") int pageNo,
                                Model model){
        int pageSize = 5;
        Page<Test> page = testService.findPagenated(pageNo, pageSize);
        List<Test> listTests = page.getContent();
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("listTests", listTests);
        return "index";
    }

    @PostMapping(value = "/update")
    public String update(@RequestParam(required = false) String email,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String address) {
        User user = getCurrentUser();
        String login = user.getUsername();
        userService.updateUser(login, email, phone, address);
        return "redirect:/";
    }


    @PostMapping(value = "/newuser")
    public String update(@Valid
                         @RequestParam String login,
                         @RequestParam String password,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String address,
                         Model model) {
        String passHash = passwordEncoder.encode(password);
        if (!userService.addUser(login, passHash, UserRole.STUDENT, email, phone, address)) {
            model.addAttribute("exists", true);
            model.addAttribute("login", login);
            return "register";
        }
        return "redirect:/";
    }


    @PostMapping(value = "/delete")
    public String delete(@RequestParam(name = "toDelete[]", required = false) List<Long> ids, Model model) {
        userService.deleteUsers(ids);
        return "admin";
    }


    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }


    @GetMapping("/register")
    public String register() {
        return "register";
    }


    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // !!!
    public String admin(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }


    @GetMapping("/unauthorized")
    public String unauthorized(Model model) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        model.addAttribute("login", user.getUsername());
        return "unauthorized";
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private boolean isAdmin(User user) {
        Collection<GrantedAuthority> roles = user.getAuthorities();
        return roles.stream().anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

    }
}
