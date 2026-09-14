package com.thrive.portal.dto;

/**
 * DTO de cadastro de cliente. No baseline NAO possui nenhuma anotacao de
 * Bean Validation (Jakarta Validation) - qualquer valor e aceito.
 * As anotacoes (@NotBlank, @Email, @Size, @CNPJ...) sao adicionadas no Lab 2.3.
 */
public class CadastroClienteForm {
    private String razaoSocial;
    private String cnpj;
    private String email;
    private String senha;

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
