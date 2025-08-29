package com.artheus.cidadaoalerta.service;

import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CSVService {

    private final ReclamacaoRepository repository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Gera CSV filtrando direto pelo repository.
     */
    public ResponseEntity<Resource> gerarResponseCsv(
            StatusReclamacao status,
            Long usuarioId,
            CategoriaReclamacao categoria,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        List<Reclamacao> reclamacoes = repository.buscarReclamacoesPorFiltrosCompletos(
                status, usuarioId, categoria, dataInicio, dataFim
        );

        ByteArrayInputStream csvStream = gerarCsv(reclamacoes);
        InputStreamResource file = new InputStreamResource(csvStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reclamacoes.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(file);
    }

    private ByteArrayInputStream gerarCsv(List<Reclamacao> reclamacoes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter osWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osWriter)) {

            // BOM UTF-8 para compatibilidade com Excel
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // Cabeçalho
            writer.println("id;titulo;descricao;categoria;status;latitude;longitude;dataCriacao;usuario");

            for (Reclamacao r : reclamacoes) {
                String lat = r.getLocalizacao() != null && r.getLocalizacao().getLatitude() != null
                        ? r.getLocalizacao().getLatitude().toString() : "";
                String lon = r.getLocalizacao() != null && r.getLocalizacao().getLongitude() != null
                        ? r.getLocalizacao().getLongitude().toString() : "";

                String[] campos = new String[]{
                        r.getId() != null ? r.getId().toString() : "",
                        escapeCsv(r.getTitulo()),
                        escapeCsv(r.getDescricao()),
                        r.getCategoriaReclamacao() != null ? r.getCategoriaReclamacao().name() : "",
                        r.getStatus() != null ? r.getStatus().name() : "",
                        lat,
                        lon,
                        r.getDataCriacao() != null ? r.getDataCriacao().format(DATE_FORMATTER) : "",
                        r.getUsuario() != null ? escapeCsv(r.getUsuario().getNome()) : ""
                };

                writer.println(String.join(";", campos));
            }

            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar CSV", e);
        }
    }

    private String escapeCsv(String valor) {
        if (valor == null) return "";
        String escaped = valor.replace("\"", "\"\"");
        if (escaped.contains(";") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }
}
