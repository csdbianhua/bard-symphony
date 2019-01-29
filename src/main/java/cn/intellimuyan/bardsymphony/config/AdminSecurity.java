package cn.intellimuyan.bardsymphony.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;

/**
 * @author hason
 * @version 19-1-29
 */
@Configuration
public class AdminSecurity extends WebSecurityConfigurerAdapter {


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**").authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth,
                                @Value("${auth.user:admin}") String user,
                                @Value("${auth.passport:{bcrypt}$2a$10$/PrkTsL0wPYd9L9XlbtAy.47iS48mDz6fB/BY7xZo9qFKuVeVLZF.}") String password) throws Exception {
        auth.inMemoryAuthentication().withUser(User.builder().username(user).password(password).roles("USER"));
    }
}
