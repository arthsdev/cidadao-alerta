package com.artheus.cidadaoalerta.model;

import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reclamacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Titulo não deve estar vazio")
    @Size(min = 5, max = 100)
    private String titulo;

    @NotBlank(message = "A descricao nao pode estar vazia")
    @Size(min = 20, max = 400)
    private String descricao;

    @NotNull(message = "Categoria da reclamação não pode estar vazia")
    @Enumerated(EnumType.STRING)
    private CategoriaReclamacao categoriaReclamacao;

    @NotNull(message = "Localização não pode estar vazia")
    @Embedded
    private Localizacao localizacao;

    @NotNull(message = "Status da Reclamação não pode estar vazio")
    @Enumerated(EnumType.STRING)
    private StatusReclamacao status = StatusReclamacao.ABERTA;

    @CreationTimestamp
    private LocalDateTime dataCriacao;

     @ManyToOne(optional = false)
     @JoinColumn(name = "usuario_id")
     private Usuario usuario;

    @Column(nullable = false)
     private boolean ativo = true;

    @Version
    @Column(nullable = false)
    private Long version;
}
