package com.so.filesystem;

import javax.swing.SwingUtilities;
import com.so.filesystem.painel.PainelPrincipal;

public class Main {

    private static final Implementation IMPLEMENTACAO = new Implementation();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PainelPrincipal painelPrincipal = new PainelPrincipal(IMPLEMENTACAO);
            painelPrincipal.setVisible(true);
        });

        testarCriacaoArquivo();
    }

    private static void testarCriacaoArquivo() {
        String nomeArquivo = "katarine.txt";
        IMPLEMENTACAO.excluir(nomeArquivo);

        String conteudo = "Sistemas de arquivos sao estruturas que permitem aos sistemas operacionais " +
                "organizar e gerenciar dados em dispositivos de armazenamento. Eles determinam como " +
                "os dados sao armazenados, acessados e recuperados. Exemplos incluem FAT32, NTFS, " +
                "ext4 e APFS, cada um com caracteristicas proprias em termos de compatibilidade, " +
                "seguranca e capacidade. A escolha do sistema de arquivos influencia o desempenho, " +
                "a seguranca e a eficiencia do armazenamento de dados.";

        IMPLEMENTACAO.criar(nomeArquivo, conteudo.getBytes());

        byte[] dadosCompletos = IMPLEMENTACAO.ler(nomeArquivo, 0, -1);
        System.out.println("Texto completo [append]: " + new String(dadosCompletos));
    }
}
