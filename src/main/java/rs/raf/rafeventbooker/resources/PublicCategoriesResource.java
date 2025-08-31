package rs.raf.rafeventbooker.resources;

import rs.raf.rafeventbooker.model.Category;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.services.CategoryService;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/public/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicCategoriesResource {
    @Inject
    private CategoryService service;

    @PermitAll
    @GET
    public Page<Category> listCategories(@QueryParam("page") @DefaultValue("1") int page,
                                         @QueryParam("size") @DefaultValue("10") int size) {
        return service.getCategories(page, size);
    }
}
