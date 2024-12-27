package ru.ricardocraft.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.VerifyHelper;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.socket.handlers.ContentType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Controller
public class FilesController {

    private final int ARBITARY_SIZE = 512;

    public static final DateTimeFormatter dateFormatter;
    public static final String READ = "r";
    public static final int HTTP_CACHE_SECONDS = VerifyHelper.verifyInt(Integer.parseInt(System.getProperty("launcher.fileserver.cachesec", "60")), VerifyHelper.NOT_NEGATIVE, "HttpCache seconds should be positive");
    private static final boolean OLD_ALGO = Boolean.parseBoolean(System.getProperty("launcher.fileserver.oldalgo", "true"));
    private static final ContentType TYPE_PROBE = Arrays.stream(ContentType.values()).filter(e -> e.name().toLowerCase(Locale.US).equals(System.getProperty("launcher.fileserver.typeprobe", "nio"))).findFirst().orElse(ContentType.UNIVERSAL);
    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[^-\\._]?[^<>&\\\"]*");

    static {
        dateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).withZone(ZoneId.of("UTC"));
    }

    private final Path base;
    private final boolean fullOut;
    private final boolean showHiddenFiles;

    public FilesController(DirectoriesManager directoriesManager, HttpServerProperties httpServerProperties) {
        this.base = directoriesManager.getUpdatesDir();
        this.fullOut = true;
        showHiddenFiles = httpServerProperties.getShowHiddenFiles();
    }

    @GetMapping("/files/**")
    public void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // URLDecoder tries to decode string as application/x-www-form-urlencoded which allows + character to be used as space escape therefore breaking file names with +
        final String baseUrl = IOHelper.urlDecodeStrict(request.getRequestURI());

        if (baseUrl == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final String uri = baseUrl.replace("/files", "");

        final String path;
        try {
            path = Paths.get(IOHelper.getPathFromUrlFragment(uri)).normalize().toString().substring(1);
        } catch (InvalidPathException e) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = base.resolve(path).toFile();
        if ((file.isHidden() && !showHiddenFiles) || !file.exists()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (file.isDirectory()) {
            if (fullOut) {
                if (uri.endsWith("/")) {
                    sendListing(response, file, uri, showHiddenFiles);
                    return;
                } else {
                    sendRedirect(response, baseUrl + '/');
                    return;
                }
            } else sendError(response, HttpServletResponse.SC_NOT_FOUND); // can not handle dirs
            return;
        }

        if (!file.isFile()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Cache Validation
        String ifModifiedSince = request.getHeader("if-modified-since");
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            TemporalAccessor ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            try {
                long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getLong(ChronoField.INSTANT_SECONDS);
                long fileLastModifiedSeconds = file.lastModified() / 1000;
                if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                    sendNotModified(response);
                    return;
                }
            } catch (UnsupportedTemporalTypeException ignored) {
            }
        }

        long fileLength = Files.size(file.toPath());

        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentLengthLong(fileLength);
        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file);

        if (isKeepAlive(request)) {
            response.setHeader("connection", "keep-alive");
        }

        try (OutputStream os = response.getOutputStream()) {
            Files.copy(file.toPath(), os);
            os.flush();
        }

        response.flushBuffer();
    }

    private static void sendError(HttpServletResponse response, int status) throws IOException {
        response.resetBuffer();
        response.setStatus(status);
        response.setHeader("Content-Type", "text/plain; charset=UTF-8");
        response.getOutputStream().print("Failure: " + status + "\r\n");
        response.flushBuffer();
    }

    private static void sendRedirect(HttpServletResponse response, String newUri) throws IOException {
        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("location", newUri);
        response.flushBuffer();
    }

    private static void sendNotModified(HttpServletResponse response) throws IOException {
        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        setDateHeader(response);
        response.flushBuffer();
    }

    private static void sendListing(HttpServletResponse response, File dir, String dirPath, boolean showHidden) throws IOException {
        StringBuilder buf = new StringBuilder()
                .append("<!DOCTYPE html>\r\n")
                .append("<html><head><meta charset='utf-8' /><title>")
                .append("Listing of: ")
                .append(dirPath)
                .append("</title></head><body>\r\n")

                .append("<h3>Listing of: ")
                .append(dirPath)
                .append("</h3>\r\n")

                .append("<ul>")
                .append("<li><a href=\"../\">..</a></li>\r\n");

        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if ((f.isHidden() && !showHidden) || !f.canRead()) {
                continue;
            }

            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }

            buf.append("<li><a href=\"")
                    .append(name)
                    .append("\">")
                    .append(name)
                    .append("</a></li>\r\n");
        }

        buf.append("</ul></body></html>\r\n");

        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Type", "text/html; charset=UTF-8");
        response.getOutputStream().print(buf.toString());
        response.flushBuffer();
    }

    private static void setDateHeader(HttpServletResponse response) {
        response.setDateHeader("date", System.currentTimeMillis());
    }

    private static void setDateAndCacheHeaders(HttpServletResponse response, File fileToCache) {
        // Date header
        LocalDateTime time = LocalDateTime.now(Clock.systemUTC());
        response.setHeader("date", dateFormatter.format(time));

        // Add cache headers
        response.setHeader("expires", dateFormatter.format(time.plusSeconds(HTTP_CACHE_SECONDS)));
        response.setHeader("cache-control", "private, max-age=" + HTTP_CACHE_SECONDS);
        response.setHeader("last-modified", dateFormatter.format(Instant.ofEpochMilli(fileToCache.lastModified())));
    }

    private static void setContentTypeHeader(HttpServletResponse response, File file) {
        String contentType = TYPE_PROBE.forPath(file);
        if (contentType != null)
            response.setContentType(contentType);
    }

    public static boolean isKeepAlive(HttpServletRequest request) {
        String connection = request.getHeader("connection");
        return !(connection != null && connection.equalsIgnoreCase("close")) &&
                (request.getProtocol().equalsIgnoreCase("HTTP/1.1") ||
                        (connection != null && connection.equalsIgnoreCase("keep-alive")));
    }
}
