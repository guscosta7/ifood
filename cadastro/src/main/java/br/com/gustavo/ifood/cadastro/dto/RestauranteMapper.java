package br.com.gustavo.ifood.cadastro.dto;

import javax.enterprise.inject.Default;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import br.com.gustavo.ifood.cadastro.Restaurante;

@Default
@Mapper(componentModel = "cdi")
public interface RestauranteMapper {
	
    @Mapping(target = "nome", source = "nomeFantasia")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "localizacao.id", ignore = true)
    public Restaurante toRestaurante(AdicionarRestauranteDto dto);

    @Mapping(target = "nome", source = "nomeFantasia")
    public void toRestaurante(AtualizarRestauranteDto dto, @MappingTarget Restaurante restaurante);

    @Mapping(target = "nomeFantasia", source = "nome")
    //Exemplo de formatação.
    @Mapping(target = "dataCriacao", dateFormat = "dd/MM/yyyy HH:mm:ss")
    public RestauranteDto toRestauranteDTO(Restaurante r);
}
