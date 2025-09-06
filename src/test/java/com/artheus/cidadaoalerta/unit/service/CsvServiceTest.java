package com.artheus.cidadaoalerta.unit.service;

import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import com.artheus.cidadaoalerta.service.CsvService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class CsvServiceTest {

    @Mock
    private ReclamacaoRepository reclamacaoRepository;

    @InjectMocks
    private CsvService csvService;

    private Reclamacao reclamacao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Localizacao loc = new Localizacao();
        loc.setLatitude(12.34);
        loc.setLongitude(56.78);

        Usuario usuario = new Usuario();
        usuario.setNome("João \"da Silva\"; Teste");

        reclamacao = new Reclamacao();
        reclamacao.setId(1L);
        reclamacao.setTitulo("Título com ; e quebra\nlinha");
        reclamacao.setDescricao("Descrição com \"aspas\"");
        reclamacao.setCategoriaReclamacao(CategoriaReclamacao.SANEAMENTO);
        reclamacao.setStatus(StatusReclamacao.ABERTA);
        reclamacao.setLocalizacao(loc);
        reclamacao.setDataCriacao(LocalDateTime.of(2025, 9, 5, 20, 36, 10));
        reclamacao.setUsuario(usuario);
    }

    // ===== Testes =====

    @Test
    void deveGerarCsvComUmaReclamacao() throws Exception {
        when(reclamacaoRepository.buscarReclamacoesPorFiltrosCompletos(any(), any(), any(), any(), any()))
                .thenReturn(List.of(reclamacao));

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(
                StatusReclamacao.ABERTA,
                1L,
                CategoriaReclamacao.SEGURANCA,
                LocalDateTime.of(2024, 5, 1, 0, 0),
                LocalDateTime.of(2024, 5, 30, 23, 59)
        );

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
                    () -> assertEquals("1", colunas[0]),
                    () -> assertEquals(normalizar(reclamacao.getTitulo()), normalizar(colunas[1])),
                    () -> assertEquals(normalizar(reclamacao.getDescricao()), normalizar(colunas[2])),
                    () -> assertEquals(reclamacao.getCategoriaReclamacao().name(), colunas[3]),
                    () -> assertEquals(reclamacao.getStatus().name(), colunas[4]),
                    () -> assertEquals(String.valueOf(reclamacao.getLocalizacao().getLatitude()), colunas[5]),
                    () -> assertEquals(String.valueOf(reclamacao.getLocalizacao().getLongitude()), colunas[6]),
                    // compara apenas a data, ignora a hora
                    () -> assertEquals(reclamacao.getDataCriacao().toLocalDate().toString(), colunas[7].split(" ")[0]),
                    () -> assertEquals(normalizar(reclamacao.getUsuario().getNome()), normalizar(colunas[8]))
            );
        }
    }

    @Test
    void deveGerarCsvVazioSemReclamacoes() throws Exception {
        when(reclamacaoRepository.buscarReclamacoesPorFiltrosCompletos(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(null, null, null, null, null);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody().getInputStream(), StandardCharsets.UTF_8))) {

            String header = removerBom(reader.readLine());
            assertEquals("id;titulo;descricao;categoria;status;latitude;longitude;dataCriacao;usuario", header);

            String registro = lerRegistroCsv(reader);
            assertNull(registro);
        }
    }

    @Test
    void deveGerarCsvComCamposNulos() throws Exception {
        Reclamacao r = new Reclamacao();
        r.setId(1L);

        when(reclamacaoRepository.buscarReclamacoesPorFiltrosCompletos(any(), any(), any(), any(), any()))
                .thenReturn(List.of(r));

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(null, null, null, null, null);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody().getInputStream(), StandardCharsets.UTF_8))) {

            reader.readLine(); // cabeçalho
            String linha = reader.readLine();
            assertNotNull(linha);

            String[] colunas = parseCsvSemicolonLine(linha);

            assertEquals("1", colunas[0]);
            assertEquals("ABERTA", colunas[4]); // status default da entidade

            for (int i = 1; i < colunas.length; i++) {
                if (i != 4) { // todas as colunas exceto status devem estar vazias
                    assertEquals("", colunas[i]);
                }
            }
        }
    }


    @Test
    void deveGerarCsvComCaracteresEspeciais() throws Exception {
        Reclamacao r = new Reclamacao();
        r.setId(1L);
        r.setTitulo("Título; com \"aspas\" e \nquebra");
        r.setDescricao("Descrição; teste");

        when(reclamacaoRepository.buscarReclamacoesPorFiltrosCompletos(any(), any(), any(), any(), any()))
                .thenReturn(List.of(r));

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(null, null, null, null, null);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody().getInputStream(), StandardCharsets.UTF_8))) {

            reader.readLine(); // cabeçalho
            String linha = lerRegistroCsv(reader);
            assertNotNull(linha);

            String[] colunas = parseCsvSemicolonLine(linha);

            assertEquals(normalizar(r.getTitulo()), normalizar(colunas[1]));
            assertEquals(normalizar(r.getDescricao()), normalizar(colunas[2]));
        }
    }

    // ===== Métodos Auxiliares =====

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
        if (v == null) return "";
        return v.replace("\r\n", " ").replace("\n", " ").replace("\r", " ").trim();
    }
}
