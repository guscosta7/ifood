package br.com.gustavo.ifood.cadastro;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import br.com.gustavo.ifood.cadastro.dto.AdicionarPratoDTO;
import br.com.gustavo.ifood.cadastro.dto.AdicionarRestauranteDto;
import br.com.gustavo.ifood.cadastro.dto.AtualizarPratoDto;
import br.com.gustavo.ifood.cadastro.dto.AtualizarRestauranteDto;
import br.com.gustavo.ifood.cadastro.dto.ConstraintViolationResponse;
import br.com.gustavo.ifood.cadastro.dto.PratoDto;
import br.com.gustavo.ifood.cadastro.dto.PratoMapper;
import br.com.gustavo.ifood.cadastro.dto.RestauranteDto;
import br.com.gustavo.ifood.cadastro.dto.RestauranteMapper;

@Path("/restaurantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "restaurante")
@RolesAllowed("proprietario")
@SecurityScheme(securitySchemeName = "ifood-oauth", type = SecuritySchemeType.OAUTH2, flows = @OAuthFlows(password = @OAuthFlow(tokenUrl = "http://localhost:8180/auth/realms/ifood/protocol/openid-connect/token")))
@SecurityRequirement(name = "ifood-oauth", scopes = {})
public class RestauranteResource {
	
    @Inject
    RestauranteMapper restauranteMapper;

    @Inject
    PratoMapper pratoMapper;

    @Inject
    JsonWebToken jwt;

    @Inject
    @Claim(standard = Claims.sub)
    String sub;
    
	@GET
	public List<RestauranteDto> getRestaurantes() {
		Stream<Restaurante> restaurantes = Restaurante.streamAll();
		
		return restaurantes.map(r -> restauranteMapper.toRestauranteDTO(r)).collect(Collectors.toList());
	}

	@POST
	@Transactional
	@APIResponse(responseCode = "201", content = @Content(schema = @Schema(allOf = ConstraintViolationResponse.class)))
	@APIResponse(responseCode = "201", description = "Quando o cadastro é feito com sucesso")
	public Response adicionar(@Valid  AdicionarRestauranteDto dto) {
		Restaurante restaurante = restauranteMapper.toRestaurante(dto);
		restaurante.persist();
		return Response.status(Status.CREATED).build();
	}

	@PUT
	@Path("{id}")
	@Transactional
	public void atualizar(@PathParam("id") Long id, AtualizarRestauranteDto dto) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);
		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}

		Restaurante restaurante = restauranteOp.get();
		
		restauranteMapper.toRestaurante(dto, restaurante);		
		restaurante.persist();
	}

	@DELETE
	@Path("{id}")
	@Transactional
	public void delete(@PathParam("id") Long id) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(id);
		restauranteOp.ifPresentOrElse(Restaurante::delete, () -> {
			throw new NotFoundException("Restaurante não existe");
		});
	}

	@GET
	@Tag(name = "prato")
	@Path("{idRestaurante}/pratos")
	public List<PratoDto> getPratos(@PathParam("idRestaurante") Long idRestaurante) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);

		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}

		Stream<Prato> pratos = Prato.stream("restaurante", restauranteOp.get());
		
		return pratos.map(p -> pratoMapper.toDTO(p)).collect(Collectors.toList());		
	}

	@POST
	@Tag(name = "prato")
	@Path("{idRestaurante}/pratos")
	@Transactional
	public Response adicionarPrato(@PathParam("idRestaurante") Long idRestaurante, AdicionarPratoDTO dto) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);

		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}
		
		Prato prato = pratoMapper.toPrato(dto);
		prato.restaurante = restauranteOp.get();
		prato.persist();
		return Response.status(Status.CREATED).build();
	}

	@PUT
	@Tag(name = "prato")
	@Path("{idRestaurante}/pratos/{id}")
	@Transactional
	public void atualizar(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id, AtualizarPratoDto dto) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);

		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}

		Optional<Prato> pratoOp = Prato.findByIdOptional(id);
		if (pratoOp.isEmpty()) {
			 throw new NotFoundException("Prato não existe");
		}

		Prato prato = pratoOp.get();
		pratoMapper.toPrato(dto, prato);		
		prato.persist();
	}

	@DELETE
	@Tag(name = "prato")
	@Path("{idRestaurante}/pratos/{id}")
	@Transactional
	public void deletarPratos(@PathParam("idRestaurante") Long idRestaurante, @PathParam("id") Long id) {
		Optional<Restaurante> restauranteOp = Restaurante.findByIdOptional(idRestaurante);

		if (restauranteOp.isEmpty()) {
			throw new NotFoundException("Restaurante não existe");
		}

		Optional<Prato> pratoOp = Prato.findByIdOptional(id);
		pratoOp.ifPresentOrElse(Prato::delete, () -> {
			throw new NotFoundException("Prato não existe");	
		});
	}
}
