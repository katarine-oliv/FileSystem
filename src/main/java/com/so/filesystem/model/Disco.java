package com.so.filesystem.model;

import java.io.File;
import java.io.RandomAccessFile;

import lombok.SneakyThrows;

public class Disco {
    private static final String NOME_ARQUIVO = "fat32.data";
    public static final int NUMERO_BLOCOS = 16 * 1024;
    public static final int TAMANHO_BLOCO = 64 * 1024;
    private final RandomAccessFile disco;

    @SneakyThrows
    public Disco(){
        File f = new File(NOME_ARQUIVO);
        this.disco = new RandomAccessFile(f, "rws");
        this.disco.setLength(NUMERO_BLOCOS * TAMANHO_BLOCO);
    }

    public byte[] ler(int block){
        if(block < 0 || block >= NUMERO_BLOCOS){
            throw new IllegalArgumentException();
        }
        byte[] data = new byte[TAMANHO_BLOCO];
        try {
            this.disco.seek(block * TAMANHO_BLOCO);
            this.disco.read(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public void escrever(int block, byte[] data){
        if(block < 0 || block >= NUMERO_BLOCOS){
            throw new IllegalArgumentException();
        }
        if(data.length > TAMANHO_BLOCO){
            throw new IllegalArgumentException();
        }
        try {
            this.disco.seek(block * TAMANHO_BLOCO);
            this.disco.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
