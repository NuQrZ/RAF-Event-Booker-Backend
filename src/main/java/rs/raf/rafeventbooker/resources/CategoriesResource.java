package rs.raf.rafeventbooker.resources;

import rs.raf.rafeventbooker.model.Category;
import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.requests.categories.CreateCategoryRequest;
import rs.raf.rafeventbooker.requests.categories.UpdateCategoryRequest;
import rs.raf.rafeventbooker.services.CategoryService;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Optional;

@Path("/ems/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategoriesResource {

    @Inject private CategoryService service;

    @PermitAll
    @GET
    public Page<Category> listCategories(@QueryParam("page") @DefaultValue("1") int page,
                                         @QueryParam("size") @DefaultValue("10") int size) {
        return service.getCategories(page, size);
    }

    @RolesAllowed({ "ADMIN", "CREATOR" })
    @GET @Path("/{categoryID}")
    public Response getCategory(@PathParam("categoryID") int categoryID) {
        Category c = service.getCategory(categoryID)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        return Response.ok(c).build();
    }

    @RolesAllowed({ "ADMIN", "CREATOR" })
    @GET @Path("/by-name/{name}")
    public Response getByName(@PathParam("name") String name) {
        Optional<Category> opt = service.getCategoryByName(name);
        return opt.map(Response::ok)
                .orElseThrow(() -> new NotFoundException("Category not found"))
                .build();
    }

    @RolesAllowed({ "ADMIN", "CREATOR" })
    @GET @Path("/exists")
    public Response exists(@QueryParam("name") String name) {
        boolean exists = service.categoryExists(name);
        return Response.ok(exists).build();
    }

    @RolesAllowed({ "ADMIN", "CREATOR" })
    @GET @Path("/{categoryID}/has-events")
    public Response hasEvents(@PathParam("categoryID") int categoryID) {
        boolean has = service.hasEvents(categoryID);
        return Response.ok(has).build();
    }

    @RolesAllowed({ "ADMIN", "CREATOR" })
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCategory(@Valid CreateCategoryRequest body, @Context UriInfo uri) {
        Category c = new Category();
        c.setCategoryName(body.categoryName());
        c.setCategoryDescription(body.categoryDescription());

        int id = service.createCategory(c);

        Category created = service.getCategory(id)
                .orElseThrow(() -> new InternalServerErrorException("Created category not found"));

        URI location = uri.getAbsolutePathBuilder().path(String.valueOf(id)).build();
        return Response.created(location).entity(created).build();
    }

    @RolesAllowed({ "ADMIN", "CREATOR" })
    @PUT @Path("/{categoryID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCategory(@PathParam("categoryID") int categoryID,
                                   @Valid UpdateCategoryRequest body) {
        Category c = new Category();
        c.setCategoryID(categoryID);
        c.setCategoryName(body.categoryName());
        c.setCategoryDescription(body.categoryDescription());

        service.updateCategory(c);
        return Response.noContent().build();
    }

    @RolesAllowed({ "ADMIN", "CREATOR" })
    @DELETE @Path("/{categoryID}")
    public Response deleteCategory(@PathParam("categoryID") int categoryID) {
        service.deleteCategory(categoryID);
        return Response.noContent().build();
    }
}
