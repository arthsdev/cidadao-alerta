package com.artheus.cidadaoalerta.integration.reclamacao;

import com.artheus.cidadaoalerta.CidadaoAlertaApplication;
import com.artheus.cidadaoalerta.dto.FiltroReclamacaoDTO;
import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import com.artheus.cidadaoalerta.service.CsvService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = CidadaoAlertaApplication.class,
        properties = "spring.config.location=classpath:application-test.properties"
)
public class CsvIntegrationTest {

    @Autowired
    private ReclamacaoRepository reclamacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CsvService csvService;

    private Usuario usuario1;
    private Usuario usuario2;

    @BeforeEach
    void setup() {
        reclamacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario1 = criarUsuario("João da Silva Teste", "joao.teste@example.com");
        usuario2 = criarUsuario("Maria Teste", "maria.teste@example.com");
    }

    private Usuario criarUsuario(String nome, String email) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha("senhaSegura123");
        u.setPapel(Role.ROLE_USER);
        return usuarioRepository.save(u);
    }

    private Reclamacao criarReclamacao(Usuario usuario, String titulo, String descricao,
                                       CategoriaReclamacao categoria, StatusReclamacao status,
                                       LocalDateTime dataCriacao) {
        Localizacao loc = new Localizacao();
        loc.setLatitude(12.34);
        loc.setLongitude(56.78);

        Reclamacao r = new Reclamacao();
        r.setTitulo(titulo);
        r.setDescricao(descricao);
        r.setCategoriaReclamacao(categoria);
        r.setStatus(status);
        r.setLocalizacao(loc);
        r.setUsuario(usuario);
        r.setDataCriacao(dataCriacao);

        return reclamacaoRepository.save(r);
    }

    // ================== BUILDER ==================
    private ReclamacaoBuilder reclamacaoBuilder() {
        return new ReclamacaoBuilder();
    }

    private class ReclamacaoBuilder {
        private Usuario usuario = usuario1;
        private String titulo = "Título padrão";
        private String descricao = "Descrição padrão com mais de 20 caracteres";
        private CategoriaReclamacao categoria = CategoriaReclamacao.SANEAMENTO;
        private StatusReclamacao status = StatusReclamacao.ABERTA;
        private LocalDateTime dataCriacao = LocalDateTime.now();

        ReclamacaoBuilder usuario(Usuario u) { this.usuario = u; return this; }
        ReclamacaoBuilder titulo(String t) { this.titulo = t; return this; }
        ReclamacaoBuilder descricao(String d) { this.descricao = d; return this; }
        ReclamacaoBuilder categoria(CategoriaReclamacao c) { this.categoria = c; return this; }
        ReclamacaoBuilder status(StatusReclamacao s) { this.status = s; return this; }
        ReclamacaoBuilder dataCriacao(LocalDateTime dt) { this.dataCriacao = dt; return this; }

        Reclamacao build() {
            return criarReclamacao(usuario, titulo, descricao, categoria, status, dataCriacao);
        }
    }

    // ================== HELPERS ==================
    private BufferedReader gerarCsv(StatusReclamacao status, Long usuarioId, CategoriaReclamacao categoria,
                                    LocalDateTime dataInicio, LocalDateTime dataFim) throws Exception {
        LocalDate inicio = dataInicio != null ? dataInicio.toLocalDate() : null;
        LocalDate fim = dataFim != null ? dataFim.toLocalDate() : null;

        FiltroReclamacaoDTO filtro = new FiltroReclamacaoDTO(status, usuarioId, categoria, inicio, fim);
        ResponseEntity<Resource> response = csvService.gerarResponseCsv(filtro);
        assertEquals(MediaType.parseMediaType("text/csv; charset=UTF-8"), response.getHeaders().getContentType());
        return new BufferedReader(new InputStreamReader(response.getBody().getInputStream(), StandardCharsets.UTF_8));
    }


    private void validarHeaderCsv(BufferedReader reader) throws Exception {
        String header = removerBom(reader.readLine());
        assertEquals("id;titulo;descricao;categoria;status;latitude;longitude;dataCriacao;usuario", header);
    }

    private List<String> lerRegistrosCsv(BufferedReader reader) throws Exception {
        List<String> registros = new ArrayList<>();
        String linha;
        while ((linha = lerRegistroCsv(reader)) != null) {
            registros.add(linha);
        }
        return registros;
    }

    private String removerBom(String linha) {
        if (linha != null && linha.startsWith("\uFEFF")) {
            return linha.substring(1);
        }
        return linha;
    }

    private String lerRegistroCsv(BufferedReader reader) throws Exception {
        String primeira = reader.readLine();
        if (primeira == null) return null;

        StringBuilder sb = new StringBuilder(primeira);
        while ((contarAspas(sb) % 2) != 0) {
            String prox = reader.readLine();
            if (prox == null) break;
            sb.append("\n").append(prox);
        }
        return sb.toString();
    }

    private int contarAspas(CharSequence s) {
        int c = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '"') c++;
        return c;
    }

    private String[] parseCsvSemicolonLine(String line) {
        List<String> out = new ArrayList<>(9);
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ';' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private String normalizar(String v) {
        if (v == null) return null;
        return v.replace("\r\n", " ").replace("\n", " ").replace("\r", " ").trim();
    }

    private void validarCsvComUmaLinha(BufferedReader reader, Reclamacao reclamacao) throws Exception {
        validarHeaderCsv(reader);

        String registro = lerRegistroCsv(reader);
        assertNotNull(registro);

        String[] colunas = parseCsvSemicolonLine(registro);
        assertEquals(9, colunas.length);

        // Ajuste: permite tanto data completa quanto apenas LocalDate
        String esperadoData = reclamacao.getDataCriacao().toLocalDate().toString();
        String atualData = colunas[7];
        boolean dataOk = atualData.equals(esperadoData) || atualData.startsWith(esperadoData);

        assertAll("validação colunas",
                () -> assertEquals(String.valueOf(reclamacao.getId()), colunas[0]),
                () -> assertEquals(normalizar(reclamacao.getTitulo()), normalizar(colunas[1])),
                () -> assertEquals(normalizar(reclamacao.getDescricao()), normalizar(colunas[2])),
                () -> assertEquals(reclamacao.getCategoriaReclamacao().name(), colunas[3]),
                () -> assertEquals(reclamacao.getStatus().name(), colunas[4]),
                () -> assertEquals(String.valueOf(reclamacao.getLocalizacao().getLatitude()), colunas[5]),
                () -> assertEquals(String.valueOf(reclamacao.getLocalizacao().getLongitude()), colunas[6]),
                () -> assertTrue(dataOk,
                        () -> "Data esperada: " + esperadoData + " ou começando com ela, mas veio: " + atualData),
                () -> assertEquals(normalizar(reclamacao.getUsuario().getNome()), normalizar(colunas[8]))
        );
    }

    // ================== TESTES ==================

    @Test
    void deveGerarCsvComUmaReclamacao() throws Exception {
        Reclamacao reclamacao = reclamacaoBuilder()
                .titulo("Título válido para teste CSV")
                .descricao("Descrição válida para teste CSV com mais de 20 caracteres")
                .categoria(CategoriaReclamacao.SANEAMENTO)
                .status(StatusReclamacao.ABERTA)
                .build();

        try (BufferedReader reader = gerarCsv(StatusReclamacao.ABERTA, reclamacao.getUsuario().getId(),
                CategoriaReclamacao.SANEAMENTO, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))) {
            validarCsvComUmaLinha(reader, reclamacao);
        }
    }

    @Test
    void deveRetornarVazioSeDataForaDoIntervalo() throws Exception {
        reclamacaoBuilder()
                .titulo("Título válido teste vazio")
                .descricao("Descrição válida teste vazio com mais de 20 caracteres")
                .build();

        try (BufferedReader reader = gerarCsv(null, null, null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))) {

            validarHeaderCsv(reader);
            List<String> registros = lerRegistrosCsv(reader);
            assertTrue(registros.isEmpty(), "Não deve haver registros no CSV");
        }
    }

    @Test
    void deveGerarCsvComMultiplasReclamacoes() throws Exception {
        reclamacaoBuilder().titulo("Título 1 válido").categoria(CategoriaReclamacao.SANEAMENTO).build();
        reclamacaoBuilder().titulo("Título 2 válido").categoria(CategoriaReclamacao.ILUMINACAO)
                .status(StatusReclamacao.RESOLVIDA).build();

        try (BufferedReader reader = gerarCsv(null, usuario1.getId(), null,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))) {

            validarHeaderCsv(reader);
            List<String> registros = lerRegistrosCsv(reader);
            assertEquals(2, registros.size());
        }
    }

    @Test
    void deveFiltrarPorStatus() throws Exception {
        reclamacaoBuilder().titulo("Título ABERTA válido").status(StatusReclamacao.ABERTA).build();
        reclamacaoBuilder().titulo("Título RESOLVIDA válido").status(StatusReclamacao.RESOLVIDA).build();

        try (BufferedReader reader = gerarCsv(StatusReclamacao.ABERTA, null, null,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))) {

            validarHeaderCsv(reader);
            List<String> registros = lerRegistrosCsv(reader);
            assertEquals(1, registros.size());
            assertTrue(registros.get(0).contains("ABERTA"));
        }
    }

    @Test
    void deveFiltrarPorUsuario() throws Exception {
        reclamacaoBuilder().titulo("Título João válido").usuario(usuario1).build();
        reclamacaoBuilder().titulo("Título Maria válido").usuario(usuario2).build();

        try (BufferedReader reader = gerarCsv(null, usuario1.getId(), null,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))) {

            validarHeaderCsv(reader);
            List<String> registros = lerRegistrosCsv(reader);
            assertEquals(1, registros.size());
            assertTrue(registros.get(0).contains("João"));
            assertFalse(registros.get(0).contains("Maria"));
        }
    }

    @Test
    void deveFiltrarPorCategoria() throws Exception {
        reclamacaoBuilder().titulo("Título SANEAMENTO válido").categoria(CategoriaReclamacao.SANEAMENTO).build();
        reclamacaoBuilder().titulo("Título SEGURANCA válido").categoria(CategoriaReclamacao.SEGURANCA).build();

        try (BufferedReader reader = gerarCsv(null, null, CategoriaReclamacao.SEGURANCA,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))) {

            validarHeaderCsv(reader);
            List<String> registros = lerRegistrosCsv(reader);
            assertEquals(1, registros.size());
            assertTrue(registros.get(0).contains("SEGURANCA"));
            assertFalse(registros.get(0).contains("SANEAMENTO"));
        }
    }
}
