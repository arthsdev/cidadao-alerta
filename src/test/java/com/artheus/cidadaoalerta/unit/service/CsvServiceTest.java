package com.artheus.cidadaoalerta.unit.service;

import com.artheus.cidadaoalerta.dto.FiltroReclamacaoDTO;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class CsvServiceTest {

    @Mock
    private ReclamacaoRepository reclamacaoRepository;

    @InjectMocks
    private CsvService csvService;

    private Reclamacao reclamacao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reclamacao = criarReclamacaoPadrao();
    }

    @Test
    void deveGerarCsvComUmaReclamacao() throws Exception {
        when(reclamacaoRepository.buscarReclamacoesPorFiltrosCompletos(any(), any(), any(), any(), any()))
                .thenReturn(List.of(reclamacao));

        FiltroReclamacaoDTO filtro = new FiltroReclamacaoDTO(
                StatusReclamacao.ABERTA,
                1L,
                CategoriaReclamacao.SEGURANCA,
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 5, 30)
        );

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(filtro);

        assertNotNull(response);
        assertEquals(MediaType.parseMediaType("text/csv; charset=UTF-8"), response.getHeaders().getContentType());

        try (BufferedReader reader = getBufferedReader(response.getBody())) {
            String header = removerBom(reader.readLine());
            assertEquals("id;titulo;descricao;categoria;status;latitude;longitude;dataCriacao;usuario", header);

            String linha = lerRegistroCsv(reader);
            assertNotNull(linha);
            String[] colunas = parseCsvSemicolonLine(linha);

            assertAll("validação colunas",
                    () -> assertEquals("1", colunas[0]),
                    () -> assertEquals(normalizar(reclamacao.getTitulo()), normalizar(colunas[1])),
                    () -> assertEquals(normalizar(reclamacao.getDescricao()), normalizar(colunas[2])),
                    () -> assertEquals(reclamacao.getCategoriaReclamacao().name(), colunas[3]),
                    () -> assertEquals(reclamacao.getStatus().name(), colunas[4]),
                    () -> assertEquals("12.34", colunas[5]),
                    () -> assertEquals("56.78", colunas[6]),
                    () -> assertEquals(reclamacao.getDataCriacao().toLocalDate().toString(), colunas[7].split(" ")[0]),
                    () -> assertEquals(normalizar(reclamacao.getUsuario().getNome()), normalizar(colunas[8]))
            );
        }
    }

    @Test
    void deveGerarCsvVazioSemReclamacoes() throws Exception {
        when(reclamacaoRepository.buscarReclamacoesPorFiltrosCompletos(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        FiltroReclamacaoDTO filtro = new FiltroReclamacaoDTO(null, null, null, null, null);

        ResponseEntity<Resource> response = csvService.gerarResponseCsv(filtro);

        try (BufferedReader reader = getBufferedReader(response.getBody())) {
            String header = removerBom(reader.readLine());
            assertEquals("id;titulo;descricao;categoria;status;latitude;longitude;dataCriacao;usuario", header);
            assertNull(lerRegistroCsv(reader));
        }
    }

    @Test
    void deveGerarCsvComCamposNulos() throws Exception {
        Reclamacao parcial = new Reclamacao();
        parcial.setId(1L);

        when(reclamacaoRepository.buscarReclamacoesPorFiltrosCompletos(any(), any(), any(), any(), any()))
                .thenReturn(List.of(parcial));

        FiltroReclamacaoDTO filtro = new FiltroReclamacaoDTO(null, null, null, null, null);
        ResponseEntity<Resource> response = csvService.gerarResponseCsv(filtro);

        try (BufferedReader reader = getBufferedReader(response.getBody())) {
            reader.readLine(); // pular header
            String linha = lerRegistroCsv(reader);
            assertNotNull(linha);
            String[] colunas = parseCsvSemicolonLine(linha);

            assertEquals("1", colunas[0]);
            assertEquals("ABERTA", colunas[4]);

            for (int i = 1; i < colunas.length; i++) {
                if (i != 4) {
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

        FiltroReclamacaoDTO filtro = new FiltroReclamacaoDTO(null, null, null, null, null);
        ResponseEntity<Resource> response = csvService.gerarResponseCsv(filtro);

        try (BufferedReader reader = getBufferedReader(response.getBody())) {
            reader.readLine(); // skip header
            String linha = lerRegistroCsv(reader);
            assertNotNull(linha);
            String[] colunas = parseCsvSemicolonLine(linha);

            assertEquals(normalizar(r.getTitulo()), normalizar(colunas[1]));
            assertEquals(normalizar(r.getDescricao()), normalizar(colunas[2]));
        }
    }

    // ================== HELPERS ==================

    private BufferedReader getBufferedReader(Resource resource) throws Exception {
        assertNotNull(resource, "O corpo do CSV não deve ser nulo");
        InputStream is = resource.getInputStream();
        assertNotNull(is, "O InputStream do Resource não pode ser nulo");
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    private String removerBom(String linha) {
        return (linha != null && linha.startsWith("\uFEFF")) ? linha.substring(1) : linha;
    }

    private String lerRegistroCsv(BufferedReader reader) throws Exception {
        String primeira = reader.readLine();
        if (primeira == null) return null;

        StringBuilder sb = new StringBuilder(primeira);
        while ((countQuotes(sb) % 2) != 0) {
            String next = reader.readLine();
            if (next == null) break;
            sb.append("\n").append(next);
        }
        return sb.toString();
    }

    private int countQuotes(CharSequence s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '"') count++;
        return count;
    }

    private String[] parseCsvSemicolonLine(String line) {
        List<String> fields = new java.util.ArrayList<>();
        StringBuilder curr = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    curr.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ';' && !inQuotes) {
                fields.add(curr.toString());
                curr.setLength(0);
            } else {
                curr.append(ch);
            }
        }
        fields.add(curr.toString());
        return fields.toArray(new String[0]);
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.replaceAll("\\r?\\n", " ").trim();
    }

    private Reclamacao criarReclamacaoPadrao() {
        Localizacao loc = new Localizacao();
        loc.setLatitude(12.34);
        loc.setLongitude(56.78);

        Usuario usuario = new Usuario();
        usuario.setNome("João \"da Silva\"; Teste");

        Reclamacao r = new Reclamacao();
        r.setId(1L);
        r.setTitulo("Título com ; e quebra\nlinha");
        r.setDescricao("Descrição com \"aspas\"");
        r.setCategoriaReclamacao(CategoriaReclamacao.SANEAMENTO);
        r.setStatus(StatusReclamacao.ABERTA);
        r.setLocalizacao(loc);
        r.setDataCriacao(LocalDateTime.of(2025, 9, 5, 20, 36, 10));
        r.setUsuario(usuario);

        return r;
    }
}
