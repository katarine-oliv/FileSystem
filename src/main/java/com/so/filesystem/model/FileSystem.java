package com.so.filesystem.model;

public interface FileSystem {
    /**
     * Cria um novo arquivo.
     * @param nomeArquivo nome do arquivo para criar
     * @param data dados a serem salvos
     */
    void criar(String nomeArquivo, byte[] data);
    /**
     * Adiciona dados ao final do arquivo.
     * @param nomeArquivo nome do arquivo
     * @param data dados a serem adicionados
     */
    void append(String nomeArquivo, byte[] data);
    /**
     * Lê arquivo.
     * @param nomeArquivo nome do arquivo
     * @param offset a partir de qual posição a leitura deve ser feita.
     * @param limite até aonde a leitura será feita, -1 para ler até o final do arquivo.
     * @return dados lidos
     */
    byte[] ler(String nomeArquivo, int offset, int limite);
    /**
     * Remove o arquivo.
     * @param nomeArquivo
     */
    void excluir(String nomeArquivo);
    /**
     * Calcula o espaço disponível no sistema de arquivos.
     * @return bytes disponíveis
     */
    int espacoDisponivel();

}
