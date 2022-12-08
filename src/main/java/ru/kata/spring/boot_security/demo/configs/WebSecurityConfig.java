package ru.kata.spring.boot_security.demo.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.entities.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserServiceImpl;

import javax.annotation.PostConstruct;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final SuccessUserHandler successUserHandler;
    private UserServiceImpl userService;

    private UserRepository userRepo;
    private RoleRepository roleRepo;

    private RoleService roleService;

    @Autowired
    public void setUserService(UserServiceImpl userService) {
        this.userService = userService;
    }

    public WebSecurityConfig(SuccessUserHandler successUserHandler, UserRepository userRepo, RoleService roleService, RoleRepository roleRepo) {
        this.successUserHandler = successUserHandler;
        this.userRepo = userRepo;
        this.roleService = roleService;
        this.roleRepo = roleRepo;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/admin/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
                .and()
                .formLogin().successHandler(successUserHandler)
                .permitAll()
                .and()
                .logout()
                .permitAll();
        http.csrf().disable();
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userService);
        return authenticationProvider;
    }

    @PostConstruct
    public void addRolesInDB() {

        if (userService.findByEmail("admin@mail.ru") == null) {
            if (roleService.getRoleByName("ROLE_ADMIN") == null) {
                Role role = new Role("ROLE_ADMIN");
                roleService.saveRole(role);
            }
            Role role = roleService.getRoleByName("ROLE_ADMIN");
            User userAdmin = new User();
            userAdmin.setFirstName("admin");
            userAdmin.setLastName("adm");
            userAdmin.setEmail("admin@mail.ru");
            userAdmin.setPassword("111");
            userAdmin.setAge(33);
            userAdmin.setRoles(Set.of(role));

            userService.saveUser(userAdmin);
        }
        if (userService.findByEmail("user@mail.ru") == null) {
            if (roleService.getRoleByName("ROLE_USER") == null) {
                Role role = new Role("ROLE_USER");
                roleService.saveRole(role);
            }
            Role role = roleService.getRoleByName("ROLE_USER");
            User user = new User();
            user.setFirstName("user");
            user.setLastName("user");
            user.setEmail("user@mail.ru");
            user.setPassword("111");
            user.setAge(33);
            user.setRoles(Set.of(role));

            userService.saveUser(user);
        }
        if (userService.findByEmail("admin2@mail.ru") == null) {
            if (roleService.getRoleByName("ROLE_ADMIN") == null) {
                Role role = new Role("ROLE_ADMIN");
                roleService.saveRole(role);
            }
            Role role = roleService.getRoleByName("ROLE_ADMIN");
            Role role1 = roleService.getRoleByName("ROLE_USER");
            User userAdmin = new User();
            userAdmin.setFirstName("user2");
            userAdmin.setLastName("usr2");
            userAdmin.setEmail("user2@mail.ru");
            userAdmin.setPassword("111");
            userAdmin.setAge(33);
            userAdmin.setRoles(Set.of(role, role1));

            userService.saveUser(userAdmin);
        }
    }

}