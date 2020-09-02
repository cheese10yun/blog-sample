package com.example.mariadb;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "member")
public class Member implements Persistable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Transient
    private boolean isNew = true;

    // for jpa proxy
    public Member() {
    }

    public Member(String name) {
        this.name = name;
    }

    public Member(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return isNew ? null : id.toString();
    }

    @Override
    public boolean isNew() {
        return isNew;
//        return false;
    }

    public String getName() {
        return name;
    }



    @PrePersist
    public void changePersistStatus() {
        this.isNew = false;
    }

    @Override
    public String toString() {
        return "Member{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
    }
}
