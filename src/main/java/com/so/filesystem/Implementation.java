package com.so.filesystem;

import com.so.filesystem.model.Arquivo;
import com.so.filesystem.model.Disco;
import com.so.filesystem.model.FileSystem;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Implementation implements FileSystem {

    private final Disco disco;
    @Getter
    private final List<Arquivo> arquivos;
    private List<Integer> FAT = new ArrayList<>(Disco.NUMERO_BLOCOS);
    public Implementation() {
        this.disco = new Disco();
        this.arquivos = new ArrayList<>();
        byte[] bytes = this.disco.ler(0);
        for(int i = 0; i < bytes.length; i += 19){
            try{
                byte[] archiveBytes = new byte[19];
                System.arraycopy(bytes, i, archiveBytes, 0, 19);
                if(archiveBytes[0] == 0){
                    break;
                }
                Arquivo arquivo = Arquivo.fromByteArray(archiveBytes);
                this.arquivos.add(arquivo);
            } catch (Exception e){
                break;
            }
        }

        System.out.println(this.arquivos);

        byte[] bytesFat = this.disco.ler(1);

        this.FAT = bytesToFat(bytesFat);

    }

    @Override
    public void criar(String nomeArquivo, byte[] data) {
        if(data.length > espacoDisponivel()){
            throw new IllegalArgumentException("Tamanho do arquivo > espaco livre!");
        }
        String extensao = formatarExtensao(nomeArquivo);
        nomeArquivo = formataNomeArquivo(nomeArquivo);
        for(Arquivo arquivo : this.arquivos){
            if(arquivo.getNome().equals(nomeArquivo)){
                throw new IllegalArgumentException("Arquivo ja existe!");
            }
        }

        int proximoBloco = proximoBlocoLivre();
        int length = data.length;

        arquivos.add(new Arquivo(nomeArquivo, extensao,  length, proximoBloco));

        System.out.println(arquivos);

        int dataPosicao = 0;

        do{
            FAT.set(proximoBloco, 69);

            byte[] parteData = new byte[Disco.TAMANHO_BLOCO];

            System.arraycopy(data, dataPosicao, parteData, 0,
                    Math.min(length, Disco.TAMANHO_BLOCO));

            disco.escrever(proximoBloco, parteData);

            length -= Disco.TAMANHO_BLOCO;
            dataPosicao += Disco.TAMANHO_BLOCO;

            int atual = proximoBloco;

            if(length <= 0){
                proximoBloco = 1;
            }
            else {
                proximoBloco = proximoBlocoLivre();
            }
            FAT.set(atual, proximoBloco);

        } while(length > 0);
        updateFat();
        updateIndex();

    }

    @Override
    public void append(String fileName, byte[] data) {

        if(data.length > espacoDisponivel()){
            throw new IllegalArgumentException("Tamanho do arquivo > espaco restante");
        }
        fileName = formataNomeArquivo(fileName);

        for(Arquivo arquivo : this.arquivos){
            if(arquivo.getNome().equals(fileName)){
                int blocoAtual = arquivo.getPosicao();
                int proximoBloco = blocoAtual;

                do {
                    blocoAtual = proximoBloco;
                    proximoBloco = FAT.get(proximoBloco);
                    if (proximoBloco == 1){
                        proximoBloco = blocoAtual;
                    }
                } while(FAT.get(proximoBloco) > 1);

                System.out.println("Bloco atual: " + blocoAtual + " Proximo bloco: " + proximoBloco);

                int lastLength = arquivo.getTamanho() % Disco.TAMANHO_BLOCO;

                blocoAtual = proximoBloco;

                byte[] ultimoBloco = disco.ler(blocoAtual);
                byte[] novoBloco = new byte[Disco.TAMANHO_BLOCO];
                System.arraycopy(ultimoBloco, 0, novoBloco, 0, lastLength);
                System.arraycopy(data, 0, novoBloco, lastLength,
                        Math.min((Disco.TAMANHO_BLOCO - lastLength), data.length));
                disco.escrever(blocoAtual, novoBloco);

                int length = data.length - (Disco.TAMANHO_BLOCO - lastLength);

                System.out.println("Ultimo bloco: " + proximoBloco);
                proximoBloco = proximoBlocoLivre();
                System.out.println("Proximo bloco livre: " + proximoBloco);

                int dataPos = Disco.TAMANHO_BLOCO - lastLength;
                if(length > 0){
                    FAT.set(blocoAtual, proximoBloco);
                    do{
                        FAT.set(proximoBloco, 69);

                        byte[] parteData = new byte[Disco.TAMANHO_BLOCO];
                        System.arraycopy(data, dataPos, parteData, 0,
                                Math.min(length, Disco.TAMANHO_BLOCO));

                        disco.escrever(proximoBloco, parteData);

                        dataPos += Disco.TAMANHO_BLOCO;

                        length -= Disco.TAMANHO_BLOCO;

                        int current = proximoBloco;

                        if(length < 0){
                            proximoBloco = 1;
                        } else {
                            proximoBloco = proximoBlocoLivre();
                        }

                        FAT.set(current, proximoBloco);
                    } while(length >= 0);
                }
                arquivo.setTamanho(arquivo.getTamanho() + data.length);
                updateFat();
                updateIndex();
            }
        }
    }

    @Override
    public void excluir(String nomeArquivo) {
        nomeArquivo = formataNomeArquivo(nomeArquivo);
        for(Arquivo arquivo : this.arquivos){
            if(arquivo.getNome().equals(nomeArquivo)) {
                int nextBlock = arquivo.getPosicao();
                do {
                    System.out.println("Bloco removido: " + nextBlock);
                    int current = nextBlock;
                    nextBlock = FAT.get(current);
                    FAT.set(current, 0);
                    if(nextBlock == 1){
                        FAT.set(nextBlock, 0);
                    }
                } while(nextBlock > 1);
                arquivos.remove(arquivo);
                updateIndex();
                updateFat();
                return;
            }
        }
    }

    @Override
    public int espacoDisponivel() {
        int free = 0;
        for(Integer block: FAT){
            if(block == null || block == 0){
                free ++;
            }
        }
        return free * Disco.TAMANHO_BLOCO;
    }

    private int proximoBlocoLivre(){
        for(int i = 2; i < Disco.NUMERO_BLOCOS; i++){
            if(FAT.get(i) == 0){
                System.out.println("Bloco livre: " + i);
                return i;
            }
        }
        return -1;
    }

    private void updateIndex(){
        byte[] bytes = new byte[Disco.TAMANHO_BLOCO];
        int pos = 0;
        for(Arquivo arquivo : this.arquivos){
            byte[] archiveBytes = arquivo.toByteArray();
            System.arraycopy(archiveBytes, 0, bytes, pos, archiveBytes.length);
            pos += archiveBytes.length;
        }
        disco.escrever(0, bytes);
    }

    private void updateFat(){
        byte[] fatBytes = fatToBytes();
        disco.escrever(1, fatBytes);
    }

    private byte[] fatToBytes() {
        byte[] byteArray = new byte[FAT.size() * Integer.BYTES];
        for (int i = 0; i < FAT.size(); i++) {
            int value = FAT.get(i);
            byteArray[i * Integer.BYTES] = (byte) ((value >> 24) & 0xFF);
            byteArray[i * Integer.BYTES + 1] = (byte) ((value >> 16) & 0xFF);
            byteArray[i * Integer.BYTES + 2] = (byte) ((value >> 8) & 0xFF);
            byteArray[i * Integer.BYTES + 3] = (byte) (value & 0xFF);
        }
        return byteArray;
    }
    private List<Integer> bytesToFat(byte[] byteArray) {
        List<Integer> integerList = new ArrayList<>();
        for (int i = 0; i < byteArray.length; i += Integer.BYTES) {
            int value = 0;
            value |= (byteArray[i] & 0xFF) << 24;
            value |= (byteArray[i + 1] & 0xFF) << 16;
            value |= (byteArray[i + 2] & 0xFF) << 8;
            value |= (byteArray[i + 3] & 0xFF);
            integerList.add(value);
        }
        return integerList;
    }

    private String formataNomeArquivo(String nomeArquivo) {
        nomeArquivo = nomeArquivo.split("\\.")[0];

        if(nomeArquivo.length() > 8){
            nomeArquivo = nomeArquivo.substring(0, 8);
        }
        else if (nomeArquivo.length() < 8){
            StringBuilder fileNameBuilder = new StringBuilder(nomeArquivo);
            do{
                fileNameBuilder.append(" ");
            } while(fileNameBuilder.length() < 8);
            nomeArquivo = fileNameBuilder.toString();
        }
        return nomeArquivo;
    }

    private String formatarExtensao(String nomeArquivo){

        nomeArquivo = nomeArquivo.split("\\.")[1];

        if(nomeArquivo.length() > 3){
            nomeArquivo = nomeArquivo.substring(0, 3);
        }

        else if(nomeArquivo.length() < 3){
            StringBuilder fileNameBuilder = new StringBuilder(nomeArquivo);
            do{
                fileNameBuilder.append(" ");
            } while(fileNameBuilder.length() < 3);
            nomeArquivo = fileNameBuilder.toString();
        }

        return nomeArquivo;
    }

    @Override
    public byte[] ler(String fileName, int offset, int limit) {

        fileName = formataNomeArquivo(fileName);
        System.out.println("Lendo arquivo: " + fileName);

        for(Arquivo archive : this.arquivos){

            if(archive.getNome().equals(fileName)){
                System.out.println("Arquivo encontrado: " + archive);

                if(offset > archive.getTamanho() || limit > archive.getTamanho()){
                    return null;
                }

                if(limit == -1){
                    limit = archive.getTamanho();
                }

                List<Integer> blocos = new ArrayList<>();
                int current = archive.getPosicao();

                do{
                    blocos.add(current);
                    System.out.println("Lendo bloco: " + current);
                    current = FAT.get(current);
                    System.out.println("Proximo bloco: " + current);
                } while(current != 1);

                System.out.println("\nBlocos: " + blocos);

                int initialBlock = blocos.get((int) (double) (offset / Disco.TAMANHO_BLOCO));
                int initialOffset = offset % Disco.TAMANHO_BLOCO;

                int p1 = (int) (double) (limit / Disco.TAMANHO_BLOCO);
                System.out.println(p1);

                int finalBlock = blocos.get(p1);
                int finalOffset = limit % Disco.TAMANHO_BLOCO;

                byte[] arquivo = new byte[limit - offset];

                int pos = 0;

                if(initialBlock == finalBlock){
                    byte[] parteArchive = disco.ler(initialBlock);
                    System.arraycopy(parteArchive, initialOffset, arquivo, 0, finalOffset - initialOffset);
                    return arquivo;
                }

                int bloco = initialBlock;

                do{
                    byte[] parteArchive = disco.ler(bloco);

                    if(bloco == initialBlock){
                        System.arraycopy(parteArchive, initialOffset, arquivo, pos, Disco.TAMANHO_BLOCO - initialOffset);
                        pos += Disco.TAMANHO_BLOCO - initialOffset;
                    }
                    else if(bloco == finalBlock){
                        System.arraycopy(parteArchive, 0, arquivo, pos, finalOffset);
                        break;
                    }
                    else {
                        System.arraycopy(parteArchive, 0, arquivo, pos, Disco.TAMANHO_BLOCO);
                        pos += Disco.TAMANHO_BLOCO;
                    }
                    bloco = FAT.get(bloco);
                } while (bloco > 1);

                return arquivo;
            }
        }
        return null;
    }

    @Deprecated
    public byte[] ler2(String nomeArquivo) {

        nomeArquivo = formataNomeArquivo(nomeArquivo);

        for(Arquivo arquivo : this.arquivos){

            if(arquivo.getNome().equals(nomeArquivo)){
                System.out.println("Arquivo encontrado: " + arquivo.getNome());

                int nextBlock = arquivo.getPosicao();
                int currentBlock = nextBlock;
                int blocks = 0;

                while(nextBlock > 1) {
                    blocks ++;
                    nextBlock = FAT.get(nextBlock);
                }

                byte[] data = new byte[blocks * Disco.TAMANHO_BLOCO];
                int destPos = 0;

                do{
                    byte[] parteArchive = disco.ler(currentBlock);
                    System.arraycopy(parteArchive, 0, data, destPos, parteArchive.length);
                    destPos += Disco.TAMANHO_BLOCO;
                    currentBlock = FAT.get(currentBlock);

                }while(currentBlock > 1);
                System.out.println(data.length);

                return data;
            }
        }
        return null;
    }
}
