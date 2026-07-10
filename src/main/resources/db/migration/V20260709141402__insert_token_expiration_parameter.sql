insert into rh_parameter(parameter_code, parameter_name, parameter_type, parameter_value)
values ('USER_ACTIVATION_TOKEN_EXPIRATION_TIME_HOURS',
        'Tempo de expiração do token de ativação do usuário em horas',
        'number',
        '24');
commit;