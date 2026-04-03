package com.example.E_Learning_Platform.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class InvalidatedToken {
    @Id
  private   String id;
  private   Date expiryTime;
}
