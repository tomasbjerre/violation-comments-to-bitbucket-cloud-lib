package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Locale;
import tools.jackson.databind.json.JsonMapper;

/**
 * A minimal, Jakarta-namespace {@code jakarta.ws.rs.ext.Provider} wrapping a Jackson 3 {@link
 * JsonMapper}. Jackson's own {@code tools.jackson.jaxrs:jackson-jaxrs-json-provider} is still
 * annotated with {@code javax.ws.rs.ext.Provider}, so a Jakarta-based JAX-RS client (like RESTEasy
 * here) never registers it; the Jakarta-namespace sibling artifact, {@code
 * tools.jackson.jakarta.rs:jackson-jakarta-rs-json-provider}, only has 3.0.0-rc releases so far.
 * This avoids depending on either.
 */
@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class JacksonJakartaJsonProvider
    implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

  private final JsonMapper mapper;

  public JacksonJakartaJsonProvider(final JsonMapper mapper) {
    this.mapper = mapper;
  }

  private static boolean isJson(final MediaType mediaType) {
    return mediaType != null
        && "application".equalsIgnoreCase(mediaType.getType())
        && (mediaType.getSubtype().equalsIgnoreCase("json")
            || mediaType.getSubtype().toLowerCase(Locale.ROOT).endsWith("+json"));
  }

  @Override
  public boolean isReadable(
      final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType) {
    return isJson(mediaType);
  }

  @Override
  public Object readFrom(
      final Class<Object> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType,
      final MultivaluedMap<String, String> httpHeaders,
      final InputStream entityStream)
      throws IOException {
    final Type resolvedType = genericType != null ? genericType : type;
    return this.mapper.readValue(entityStream, this.mapper.constructType(resolvedType));
  }

  @Override
  public boolean isWriteable(
      final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType) {
    return isJson(mediaType);
  }

  @Override
  public void writeTo(
      final Object entity,
      final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType,
      final MultivaluedMap<String, Object> httpHeaders,
      final OutputStream entityStream)
      throws IOException {
    this.mapper.writeValue(entityStream, entity);
  }
}
