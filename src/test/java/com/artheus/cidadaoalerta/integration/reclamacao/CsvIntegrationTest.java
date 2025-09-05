package com.artheus.cidadaoalerta.integration.reclamacao;

import com.artheus.cidadaoalerta.CidadaoAlertaApplication;
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

    // ================== TESTES ==================

    @Test
    void deveGerarCsvComUmaReclamacao() throws Exception {
        Reclamacao reclamacao = criarReclamacao(
                usuario1,
                "Título válido para teste CSV",
                "Descrição válida para teste CSV com mais de 20 caracteres",
                CategoriaReclamacao.SANEAMENTO,
                StatusReclamacao.ABERTA,
                LocalDateTime.now()
        );

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(
                StatusReclamacao.ABERTA,
                reclamacao.getUsuario().getId(),
                CategoriaReclamacao.SANEAMENTO,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        validarCsvComUmaLinha(response, reclamacao);
    }

    @Test
    void deveRetornarVazioSeDataForaDoIntervalo() throws Exception {
        criarReclamacao(usuario1,
                "Título válido teste vazio",
                "Descrição válida teste vazio com mais de 20 caracteres",
                CategoriaReclamacao.SANEAMENTO,
                StatusReclamacao.ABERTA,
                LocalDateTime.now()
        );

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(
                null,
                null,
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody().getInputStream(), StandardCharsets.UTF_8))) {

            String header = removerBom(reader.readLine());
            assertEquals("id;titulo;descricao;categoria;status;latitude;longitude;dataCriacao;usuario", header);

            String registro = lerRegistroCsv(reader);
            assertNull(registro, "Não deve haver registros no CSV");
        }
    }

    @Test
    void deveGerarCsvComDatasComoLocalDateConvertidasParaLocalDateTime() throws Exception {
        Reclamacao reclamacao = criarReclamacao(
                usuario1,
                "Título datas válido",
                "Descrição datas válida com mais de 20 caracteres",
                CategoriaReclamacao.SANEAMENTO,
                StatusReclamacao.ABERTA,
                LocalDateTime.now()
        );

        LocalDate dataInicio = LocalDate.now().minusDays(1);
        LocalDate dataFim = LocalDate.now().plusDays(1);

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(
                StatusReclamacao.ABERTA,
                reclamacao.getUsuario().getId(),
                CategoriaReclamacao.SANEAMENTO,
                dataInicio.atStartOfDay(),
                dataFim.atTime(23, 59, 59)
        );

        validarCsvComUmaLinha(response, reclamacao);
    }

    @Test
    void deveGerarCsvComMultiplasReclamacoes() throws Exception {
        Reclamacao r1 = criarReclamacao(usuario1,
                "Título 1 válido",
                "Descrição 1 válida com mais de 20 caracteres",
                CategoriaReclamacao.SANEAMENTO,
                StatusReclamacao.ABERTA,
                LocalDateTime.now());

        Reclamacao r2 = criarReclamacao(usuario1,
                "Título 2 válido",
                "Descrição 2 válida com mais de 20 caracteres",
                CategoriaReclamacao.ILUMINACAO,
                StatusReclamacao.RESOLVIDA,
                LocalDateTime.now());

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(
                null,
                usuario1.getId(),
                null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody().getInputStream(), StandardCharsets.UTF_8))) {

            String header = removerBom(reader.readLine());
            assertEquals("id;titulo;descricao;categoria;status;latitude;longitude;dataCriacao;usuario", header);

            List<String> registros = new ArrayList<>();
            String linha;
            while ((linha = lerRegistroCsv(reader)) != null) {
                registros.add(linha);
            }

            assertEquals(2, registros.size());
        }
    }

    @Test
    void deveFiltrarPorStatus() throws Exception {
        criarReclamacao(usuario1,
                "Título ABERTA válido",
                "Descrição ABERTA válida com mais de 20 caracteres",
                CategoriaReclamacao.SANEAMENTO,
                StatusReclamacao.ABERTA,
                LocalDateTime.now());

        criarReclamacao(usuario1,
                "Título RESOLVIDA válido",
                "Descrição RESOLVIDA válida com mais de 20 caracteres",
                CategoriaReclamacao.SANEAMENTO,
                StatusReclamacao.RESOLVIDA,
                LocalDateTime.now());

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(
                StatusReclamacao.ABERTA,
                null,
                null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody().getInputStream(), StandardCharsets.UTF_8))) {

            reader.readLine(); // cabeçalho
            String registro = lerRegistroCsv(reader);
            assertNotNull(registro);
            assertTrue(registro.contains("ABERTA"));
        }
    }

    @Test
    void deveFiltrarPorUsuario() throws Exception {
        Reclamacao r1 = criarReclamacao(usuario1,
                "Título João válido",
                "Descrição João válida com mais de 20 caracteres",
                CategoriaReclamacao.SANEAMENTO,
                StatusReclamacao.ABERTA,
                LocalDateTime.now());

        Reclamacao r2 = criarReclamacao(usuario2,
                "Título Maria válido",
                "Descrição Maria válida com mais de 20 caracteres",
                CategoriaReclamacao.SANEAMENTO,
                StatusReclamacao.ABERTA,
                LocalDateTime.now());

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(
                null,
                usuario1.getId(),
                null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody().getInputStream(), StandardCharsets.UTF_8))) {

            reader.readLine(); // cabeçalho
            String registro = lerRegistroCsv(reader);
            assertNotNull(registro);
            assertTrue(registro.contains("João"));
            assertFalse(registro.contains("Maria"));
        }
    }

    @Test
    void deveFiltrarPorCategoria() throws Exception {
        criarReclamacao(usuario1,
                "Título SANEAMENTO válido",
                "Descrição SANEAMENTO válida com mais de 20 caracteres",
                CategoriaReclamacao.SANEAMENTO,
                StatusReclamacao.ABERTA,
                LocalDateTime.now());

        criarReclamacao(usuario1,
                "Título LIXO válido",
                "Descrição LIXO válida com mais de 20 caracteres",
                CategoriaReclamacao.SEGURANCA,
                StatusReclamacao.ABERTA,
                LocalDateTime.now());

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(
                null,
                null,
                CategoriaReclamacao.SEGURANCA,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody().getInputStream(), StandardCharsets.UTF_8))) {

            reader.readLine(); // cabeçalho
            String registro = lerRegistroCsv(reader);
            assertNotNull(registro);
            assertTrue(registro.contains("LIXO"));
            assertFalse(registro.contains("SANEAMENTO"));
        }
    }

    // ================== HELPERS ==================

    private void validarCsvComUmaLinha(ResponseEntity<Resource> response, Reclamacao reclamacao) throws Exception {
        assertEquals(MediaType.parseMediaType("text/csv; charset=UTF-8"), response.getHeaders().getContentType());

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody().getInputStream(), StandardCharsets.UTF_8))) {

            String header = removerBom(reader.readLine());
            assertEquals("id;titulo;descricao;categoria;status;latitude;longitude;dataCriacao;usuario", header);

            String registro = lerRegistroCsv(reader);
            assertNotNull(registro);

            String[] colunas = parseCsvSemicolonLine(registro);
            assertEquals(9, colunas.length);

            assertAll("validação colunas",
                    () -> assertEquals(String.valueOf(reclamacao.getId()), colunas[0]),
                    () -> assertEquals(normalizar(reclamacao.getTitulo()), normalizar(colunas[1])),
                    () -> assertEquals(normalizar(reclamacao.getDescricao()), normalizar(colunas[2])),
                    () -> assertEquals(reclamacao.getCategoriaReclamacao().name(), colunas[3]),
                    () -> assertEquals(reclamacao.getStatus().name(), colunas[4]),
                    () -> assertEquals(String.valueOf(reclamacao.getLocalizacao().getLatitude()), colunas[5]),
                    () -> assertEquals(String.valueOf(reclamacao.getLocalizacao().getLongitude()), colunas[6]),
                    () -> assertEquals(reclamacao.getDataCriacao().toLocalDate().toString(), colunas[7]),
                    () -> assertEquals(normalizar(reclamacao.getUsuario().getNome()), normalizar(colunas[8]))
            );
        }
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
}
