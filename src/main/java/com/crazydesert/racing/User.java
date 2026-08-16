package com.crazydesert.racing;

import com.crazydesert.racing.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int age;
    @Column(nullable = false, unique = true)
    private String email;

    private String licenseCategory;
    private boolean licenseVerified;
    @JsonIgnore
    private String password;

    @JsonIgnore
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "avatar_data", columnDefinition = "bytea")
    private byte[] avatarData;

    @JsonIgnore
    @Column(name = "avatar_content_type", length = 50)
    private String avatarContentType;

    @JsonIgnore
    private Long avatarVersion;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "owner")
    @JsonManagedReference
    private List<RaceCar> raceCars;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getLicenseCategory() {
        return licenseCategory;
    }

    public boolean isLicenseVerified() {
        return licenseVerified;
    }

    public String getPassword() {
        return password;
    }

    public byte[] getAvatarData() {
        return avatarData;
    }

    public String getAvatarContentType() {
        return avatarContentType;
    }

    public long getAvatarVersion() {
        return avatarVersion == null ? 0L : avatarVersion;
    }

    public Role getRole() {
        return role;
    }

    public List<RaceCar> getRaceCars() {
        return raceCars;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLicenseCategory(String licenseCategory) {
        this.licenseCategory = licenseCategory;
    }

    public void setLicenseVerified(boolean licenseVerified) {
        this.licenseVerified = licenseVerified;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAvatarData(byte[] avatarData) {
        this.avatarData = avatarData;
    }

    public void setAvatarContentType(String avatarContentType) {
        this.avatarContentType = avatarContentType;
    }

    public void setAvatarVersion(long avatarVersion) {
        this.avatarVersion = avatarVersion;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setRaceCars(List<RaceCar> raceCars) {
        this.raceCars = raceCars;
    }
}
