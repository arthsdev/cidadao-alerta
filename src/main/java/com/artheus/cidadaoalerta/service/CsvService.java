package com.artheus.cidadaoalerta.service;

import com.artheus.cidadaoalerta.dto.FiltroReclamacaoDTO;
import com.artheus.cidadaoalerta.exception.csv.CsvGenerationException;
import com.artheus.cidadaoalerta.model.Reclamacao;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável por gerar arquivos CSV de reclamações.
 * Permite filtrar por status, usuário, categoria e período de datas.
 * Garante CSV UTF-8 com BOM e cabeçalho descritivo.
 */
@Service
@RequiredArgsConstructor
public class CsvService {

    private final ReclamacaoRepository repository;

    // Formato de data/hora utilizado nas linhas do CSV
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ===================== API PÚBLICA =====================

    /**
     * Gera um arquivo CSV contendo as reclamações filtradas, retornando um ResponseEntity pronto para download.
     *
     * @param filtro DTO contendo os filtros opcionais: status, usuário, categoria e intervalo de datas
     * @return ResponseEntity com o arquivo CSV gerado
     */


    public ResponseEntity<Resource> gerarResponseCsv(FiltroReclamacaoDTO filtro) {
        List<Reclamacao> reclamacoes = repository.buscarReclamacoesPorFiltrosCompletos(
                filtro.status(),
                filtro.usuarioId(),
                filtro.categoria(),
                filtro.getDataInicioLdt().orElse(null), // passa null se não houver data
                filtro.getDataFimLdt().orElse(null)
        );

        return montarResponseCsv(reclamacoes);
    }

    // ===================== MÉTODOS PRIVADOS =====================

    /**
     * Cria um ResponseEntity para download do CSV.
     *
     * @param reclamacoes Lista de reclamações que serão exportadas
     * @return ResponseEntity contendo o arquivo CSV
     */
    private ResponseEntity<Resource> montarResponseCsv(List<Reclamacao> reclamacoes) {
        ByteArrayInputStream csvStream = gerarCsv(reclamacoes);
        InputStreamResource file = new InputStreamResource(csvStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reclamacoes.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(file);
    }

    /**
     * Converte a lista de reclamações em um CSV dentro de um ByteArrayInputStream.
     *
     * @param reclamacoes Lista de reclamações
     * @return CSV como ByteArrayInputStream
     */
    private ByteArrayInputStream gerarCsv(List<Reclamacao> reclamacoes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter osWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osWriter)) {

            // Escreve BOM UTF-8 para compatibilidade com Excel
            escreverBomUtf8(out);

            // Escreve a linha de cabeçalho
            escreverCabecalho(writer);

            // Escreve cada reclamação como uma linha no CSV
            escreverLinhas(writer, reclamacoes);

            writer.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new CsvGenerationException("Erro ao gerar CSV para as reclamações", e);
        }
    }

    /**
     * Adiciona o BOM UTF-8 no início do arquivo para que seja lido corretamente no Excel.
     *
     * @param out ByteArrayOutputStream onde o BOM será escrito
     */
    private void escreverBomUtf8(ByteArrayOutputStream out) {
        out.write(0xEF);
        out.write(0xBB);
        out.write(0xBF);
    }

    /**
     * Escreve a linha de cabeçalho do CSV.
     *
     * @param writer PrintWriter para escrever no CSV
     */
    private void escreverCabecalho(PrintWriter writer) {
        writer.println("id;titulo;descricao;categoria;status;latitude;longitude;dataCriacao;usuario");
    }

    /**
     * Escreve cada reclamação como uma linha no CSV.
     *
     * @param writer      PrintWriter para escrever no CSV
     * @param reclamacoes Lista de reclamações
     */
    private void escreverLinhas(PrintWriter writer, List<Reclamacao> reclamacoes) {
        reclamacoes.forEach(r -> writer.println(converterReclamacaoParaLinhaCsv(r)));
    }

    /**
     * Converte uma reclamação em uma linha CSV, escapando caracteres especiais.
     *
     * @param r Reclamacao a ser convertida
     * @return Linha CSV como String
     */
    private String converterReclamacaoParaLinhaCsv(Reclamacao r) {
        String lat = r.getLocalizacao() != null && r.getLocalizacao().getLatitude() != null
                ? r.getLocalizacao().getLatitude().toString() : "";
        String lon = r.getLocalizacao() != null && r.getLocalizacao().getLongitude() != null
                ? r.getLocalizacao().getLongitude().toString() : "";

        String[] campos = {
                r.getId() != null ? r.getId().toString() : "",
                escaparCsv(r.getTitulo()),
                escaparCsv(r.getDescricao()),
                r.getCategoriaReclamacao() != null ? r.getCategoriaReclamacao().name() : "",
                r.getStatus() != null ? r.getStatus().name() : "",
                lat,
                lon,
                r.getDataCriacao() != null ? r.getDataCriacao().format(DATE_TIME_FORMATTER) : "",
                r.getUsuario() != null ? escaparCsv(r.getUsuario().getNome()) : ""
        };

        return String.join(";", campos);
    }

    /**
     * Escapa caracteres especiais do CSV, como ponto e vírgula, aspas e quebras de linha.
     *
     * @param valor Texto a ser escapado
     * @return Texto escapado pronto para o CSV
     */
    private String escaparCsv(String valor) {
        if (valor == null) return "";
        String escaped = valor.replace("\"", "\"\"");
        if (escaped.contains(";") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }
}
