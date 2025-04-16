# Sistema de RH

##  Requisitos Funcionais

- [ ] Deve ser possível manter um funcionário
- [ ] Deve ser possível manter um setor
- [ ] Deve ser possível transferir um funcionário de setor
- [ ] Deve ser possível manter cargos
- [ ] Deve ser possível se autenticar
- [ ] Deve ser possível obter o perfil da pessoa autenticada
- [ ] Deve ser possível registar os pontos (entradas e saídas de expediente)
- [ ] Deve ser possível justificar faltas
- [ ] Deve ser possível obter os indicadores de determinado funcionário
- [ ] Deve ser possível registar atividades exercidas em um périodo de tempo
- [ ] Deve ser possível manter benefícios 
- [ ] Deve ser possível realizar avaliações

## Requisitos de Negocio

- ####  Funcionário

  - Deve possuir a seguinte informações
    - Nome 
        - até 150 caracteres 
        - obrigatório
    - Cargo
    - Data de nascimento
      - Deve ser validado pela data de nascimento se a pessoa possui ao menos 18 anos idade caso o cargo da pessoa não permita pessoas com menos de 18 anos
      - obrigatório
    - RG
      - obrigatório
      - mascara 99.999.999-9
    - CPF
      - obrigatório
      - ser um cpf válido
      - mascara 999.999.999-99
    - Sexo
      - Masculino 
      - Feminino
    - Documentação incluir documentos necessários como comprovante de alistamento militar, exame de admissão entre outros.
    - Dependentes do funcionário
      - Nome
        - até 15 caracteres
        - obrigatório
      - RG
      - CPF (caso possua)
      - Data de nascimento
        - obrigatório
    - Status
      - Ativo
      - Inativo
      - Afastado
    