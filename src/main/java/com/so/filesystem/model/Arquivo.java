package com.so.filesystem.model;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import lombok.Data;

@Data
public class Arquivo {

    private String nome;
    private String extensao;
    private Integer tamanho;
    private Integer posicao;

    public Arquivo(String nome, String extensao, Integer tamanho, Integer posicao) {
        this.nome = nome;

        if(this.nome.length() > 8){
            this.nome = nome.substring(0, 8);
        }

        else if (this.nome.length() < 8){
            do{
                this.nome += " ";
            } while(this.nome.length() < 8);
        }

        this.extensao = extensao;

        if(this.extensao.length() > 3){
            this.extensao = extensao.substring(0, 3);
        }

        else if(this.extensao.length() < 3){

            do{
                this.extensao += " ";
            } while(this.extensao.length() < 3);
        }

        this.tamanho = tamanho;
        this.posicao = posicao;
    }
    public byte[] toByteArray(){
        byte[] data = new byte[19];
        System.arraycopy(nome.getBytes(StandardCharsets.ISO_8859_1), 0, data, 0, 8);
        System.arraycopy(extensao.getBytes(StandardCharsets.ISO_8859_1), 0, data, 8, 3);
        System.arraycopy(intToBytes(tamanho), 0, data, 11, 4);
        System.arraycopy(intToBytes(posicao), 0, data, 15, 4);
        return data;
    }
    private static byte[] intToBytes(int i){
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    public static Arquivo fromByteArray(byte[] data){
        String name = new String(data, 0, 8, StandardCharsets.ISO_8859_1);
        String ext = new String(data, 8, 3, StandardCharsets.ISO_8859_1);
        int size = ByteBuffer.wrap(data, 11, 4).getInt();
        int pos = ByteBuffer.wrap(data, 15, 4).getInt();
        return new Arquivo(name, ext, size, pos);
    }

}
