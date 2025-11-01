package com.tndev.slotsswapperbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User
{
    @Id
    private String id;
    private String name;
    private String email;
    private String passwordHash;

    public User(String name,String email,String passwordHash)
    {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}
