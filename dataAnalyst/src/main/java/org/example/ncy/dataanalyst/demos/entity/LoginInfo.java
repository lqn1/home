package org.example.ncy.dataanalyst.demos.entity;

import javax.persistence.*;

/**
 * 但用于记录每次登录的时间，后续考虑记录登记人ip等信息
 */
@Entity
@Table(name = "logininfo")
public class LoginInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String logintime;

    public LoginInfo(){}

    public LoginInfo(Long id, String logintime) {
        this.id = id;
        this.logintime = logintime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogintime() {
        return logintime;
    }

    public void setLogintime(String logintime) {
        this.logintime = logintime;
    }
}

