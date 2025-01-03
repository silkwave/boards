package com.nyc.boards.service;

import org.jasypt.util.text.AES256TextEncryptor;

public class PasswordEncryptor {
    public static void main(String[] args) {
        AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        textEncryptor.setPassword("mySecretKey"); // 암호화 키
        String encryptedPassword = textEncryptor.encrypt("docker123");
        System.out.println("Encrypted Password: " + encryptedPassword);
    }
}
