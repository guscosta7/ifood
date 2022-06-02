package br.com.gustavo.ifood.cadastro.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import br.com.gustavo.ifood.cadastro.Prato;

@Mapper(componentModel = "cdi")
public interface PratoMapper {
	
    PratoDto toDTO(Prato p);

    Prato toPrato(AdicionarPratoDTO dto);

    void toPrato(AtualizarPratoDto dto, @MappingTarget Prato prato);
}
