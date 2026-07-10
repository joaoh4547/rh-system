alter table rh_parameter drop column parameter_description;
alter table rh_parameter alter parameter_validator DROP NOT NULL;