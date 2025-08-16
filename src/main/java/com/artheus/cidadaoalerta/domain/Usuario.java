package com.artheus.cidadaoalerta.domain;

import com.artheus.cidadaoalerta.domain.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "Nome não pode estar vazio")
    @Size(min = 8, max = 20)
    String nome;

    @Email(message = "Formato de email inválido")
    String email;

    @Size(min = 10, message = "Senha deve ter no mínimo 10 caracteres")
    @NotBlank(message = "Senha não pode estar vazia")
    String senha;

    @Column(nullable = false)
    boolean ativo = true;

    private Role papel = Role.ROLE_USER;

}
