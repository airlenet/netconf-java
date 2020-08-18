package com.airlenet.netconf.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table
@Entity
public class UserModel  implements Serializable {
    @Id
    private Long id;
    private String name;
    private String password;
    private String username;
    private boolean enable;


    public String getName() {
        return name;
    }


    public String getPassword() {
        return password;
    }


    public String getUsername() {
        return username;
    }


    public boolean isAccountNonExpired() {
        return false;
    }

    public boolean isAccountNonLocked() {
        return false;
    }


    public boolean isCredentialsNonExpired() {
        return false;
    }


    public boolean isEnabled() {
        return enable;
    }
}
