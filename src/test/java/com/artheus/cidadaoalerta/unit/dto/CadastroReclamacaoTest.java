package com.artheus.cidadaoalerta.unit.dto;

import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CadastroReclamacaoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void tituloNaoPodeSerVazio() {
        CadastroReclamacao dto = new CadastroReclamacao(
                "", // título inválido
                "Descrição válida com mais de 20 caracteres",
                CategoriaReclamacao.ASFALTO,
                new Localizacao(12.34, 56.78),
                1L
        );

        Set<ConstraintViolation<CadastroReclamacao>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("titulo"));
    }

    @Test
    void descricaoNaoPodeSerVazia() {
        CadastroReclamacao dto = new CadastroReclamacao(
                "Título válido",
                "Curta", // descrição inválida (menos de 20 caracteres)
                CategoriaReclamacao.ILUMINACAO,
                new Localizacao(12.34, 56.78),
                1L
        );

        Set<ConstraintViolation<CadastroReclamacao>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("descricao"));
    }

    @Test
    void categoriaNaoPodeSerNula() {
        CadastroReclamacao dto = new CadastroReclamacao(
                "Título válido",
                "Descrição válida com mais de 20 caracteres",
                null, // categoria inválida
                new Localizacao(12.34, 56.78),
                1L
        );

        Set<ConstraintViolation<CadastroReclamacao>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("categoriaReclamacao"));
    }

    @Test
    void localizacaoNaoPodeSerNula() {
        CadastroReclamacao dto = new CadastroReclamacao(
                "Título válido",
                "Descrição válida com mais de 20 caracteres",
                CategoriaReclamacao.SANEAMENTO,
                null, // localizacao inválida
                1L
        );

        Set<ConstraintViolation<CadastroReclamacao>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("localizacao"));
    }

    @Test
    void usuarioIdNaoPodeSerNulo() {
        CadastroReclamacao dto = new CadastroReclamacao(
                "Título válido",
                "Descrição válida com mais de 20 caracteres",
                CategoriaReclamacao.SEGURANCA,
                new Localizacao(12.34, 56.78),
                null // usuarioId inválido
        );

        Set<ConstraintViolation<CadastroReclamacao>> violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("usuarioId"));
    }

    @Test
    void dtoValidoNaoDeveGerarViolacoes() {
        CadastroReclamacao dto = new CadastroReclamacao(
                "Título válido",
                "Descrição válida com mais de 20 caracteres",
                CategoriaReclamacao.ASFALTO,
                new Localizacao(12.34, 56.78),
                1L
        );

        Set<ConstraintViolation<CadastroReclamacao>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }
}
